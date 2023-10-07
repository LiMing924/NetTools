package liming.tool.handle.udp;

import liming.tool.handle.FileRW;
import liming.tool.handle.tcp.TcpReceiveMap;
import liming.tool.handle.udp.bean.*;
import liming.tool.pool.ListPool;
import liming.tool.pool.ListenPool;
import liming.tool.pool.Pools;
import liming.tool.pool.TimePool;
import liming.tool.timeout.TimeOut;
import org.json.JSONArray;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class UdpServerSocket extends UdpSocket implements UdpServer {

    private final int BACK_LOG,HAND_SIZE;//
    private final long TIME_OUT;
    private List<DatagramSocket> sockets;//所监听的端口
    private ListPool<DatagramPacket> datagramPacketListPool;//对象缓冲池
    private ListenPool<UdpHandleObject1> listenPool;//收到的数据
    private ListenPool<UdpHandleObject2>[] udpNetObjectListenPools;//分处理池
    private ListenPool<UdpHandleObject3> udpHandleObject3ListenPool;//完整数据处理池
    private ScheduledExecutorService executorService;//监听业务处理池
    private TimeOut<UdpHandleObject2> receive;//判定超时与未超时的接收数据
    private TimePool<UdpHandleObject4<UdpNetObject>> send;//发送数据缓存池
    private final Lock lock;
    private final Map<String, Integer> valuesRun;// 关键字所在的处理线程池序号
    private final int[] valuesWait; // 线程池待处理的数据包数
    /**
     * 默认构造函数，在网络通信中，套接字信息都是一个必要的参数
     *
     * @param datagramSocket Udp监听端口
     */
    public UdpServerSocket(DatagramSocket datagramSocket, int backlog, int handSize, long timeOut ) {
        super(datagramSocket);
        lock=new ReentrantLock();
        valuesRun=new HashMap<>();
        BACK_LOG=backlog;
        HAND_SIZE=handSize;
        TIME_OUT= timeOut;
        valuesWait = new int[handSize];
        udpNetObjectListenPools=new ListenPool[handSize];
        sockets=new ArrayList<>(backlog);
        executorService= Executors.newScheduledThreadPool(backlog);
        initPools();
        add(datagramSocket);
    }

    public UdpServerSocket(InetAddress inetAddress, int port) throws SocketException {
        this(new DatagramSocket(port,inetAddress),20,5,0);
    }

    private void initPools(){
        //初始化 datagramPacket 对象缓冲池
        datagramPacketListPool=Pools.getListPool("System.udp.temp.datagramPacket@"+hashCode());
        while (!datagramPacketListPool.isFull()){
            try {
                datagramPacketListPool.put(new DatagramPacket(new byte[SIZE],SIZE));
            }catch (Exception e){
                break;
            }
        }
        //初始化完整对象状态处理池 根据UdpReceiveMap的状态判断重发还是提交业务
        udpHandleObject3ListenPool=new ListenPool<UdpHandleObject3>(Pools.getListPool("System.udp.temp.UdpHandleObject3@"+hashCode()),0,0) {
            @Override
            public void handle(UdpHandleObject3 udpHandleObject3) throws Exception {
                UdpReceiveMap udpReceiveMap=udpHandleObject3.getUdpReceiveMap();
                writeLog("收到信息 "+udpReceiveMap.getID()+" State:"+udpReceiveMap.isState()+" Rm:"+udpReceiveMap.isRm()+" All:"+udpReceiveMap.isResAll()+" id:"+udpReceiveMap.optString("ID",null));
                if(udpReceiveMap.isRm()){
                    send.remove(udpReceiveMap.getString("ID"));
                    writeLog("正在删除 "+udpReceiveMap.getID()+" State:"+udpReceiveMap.isState()+" Rm:"+udpReceiveMap.isRm()+" All:"+udpReceiveMap.isResAll()+" id:"+udpReceiveMap.optString("ID",null));
                }else {
                    {
                        UdpReceiveMap rm = new UdpReceiveMap();
                        rm.put("ID", udpReceiveMap.getID());
                        InetAddress inetAddress = udpReceiveMap.getInetAddress();//获取套接字
                        int port = udpReceiveMap.getPort();//获取端口号
                        rm.setRm(true);
                        write(udpHandleObject3.getDatagramSocket(), inetAddress, port, rm);
                        writeLog("请求清除 "+rm.getID()+" State:"+rm.isState()+" Rm:"+rm.isRm()+" All:"+rm.isResAll()+" id:"+rm.optString("ID"));
                    }
                    if(udpReceiveMap.isState()){
                        try {
                            String id = udpReceiveMap.optString("ID");
                            byte[] deletion = udpReceiveMap.optBytes("deletion");
                            UdpHandleObject4<UdpNetObject> value = send.getValue(id);//获取存储的值
                            List<UdpNetObject> list = value.getList();//获取缓存数据
                            InetAddress inetAddress = value.getInetAddress();//获取套接字
                            int port = value.getPort();//获取端口号
                            DatagramSocket datagramSocket = value.getDatagramSocket();
                            JSONArray deletion1 = UdpNetObject.getDeletion(deletion, list.size());//解析缺失的包
                            send.flush(id);
                            for(Object object:deletion1){
                                if(object instanceof Integer){
                                    write(datagramSocket,inetAddress,port, list.get((int) object));
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else {
                        UdpReceiveMap request = udpHandleObject3.getUdpReceiveMap() ,response=new UdpReceiveMap();
                        if(doWork(request,response)){
                            send(udpHandleObject3.getDatagramSocket(),response,request.getInetAddress(),request.getPort());
                        }
                    }
                }
            }
        };
        //初始化作为发送端的数据超时缓存池
        send = new TimePool<UdpHandleObject4<UdpNetObject>>("System.TimePool.receive",4_000,2000) {
            @Override
            public void handle(String key, DataObject<UdpHandleObject4<UdpNetObject>> value) {
                List<UdpNetObject> list= value.getValue().getList();
                put(key,value.getValue(),0);
                DatagramSocket datagramSocket = value.getValue().getDatagramSocket();
                for (UdpNetObject udpNetObject : list) {
                    try {
                        write(datagramSocket,inetAddress,port, udpNetObject);
                    } catch (IOException e) {
                        writeStrongLog("发送失败:"+e.getMessage()+" \t\t"+udpNetObject);
                    };
                }
                writeLog("发送缓存全重发： "+key+" "+list.size()+" "+System.currentTimeMillis());
            }
        };
        //初始化作为接收端的数据超时缓存池
        receive =new TimeOut<UdpHandleObject2>("System.TimePool.receive",4_000, 0.5F) {
            @Override
            public void onTimeout(String key, UdpHandleObject2 udpHandleObject2) {
                writeLog(
                        "接收已超时 " + udpHandleObject2.getUdpNetObject().getId() + " 缺失：" + UdpNetObject.getDeletion(udpHandleObject2.getUdpNetObject()));
            }

            @Override
            public void onRestrict(String key, UdpHandleObject2 udpHandleObject2) {
                UdpNetObject udpNetObject=udpHandleObject2.getUdpNetObject();
                UdpReceiveMap receiveMap = new UdpReceiveMap();
                receiveMap.setState(true);
                receiveMap.put("deletion", udpNetObject.getValues());
                receiveMap.put("ID", key);
                try {
                    send(udpHandleObject2.getDatagramSocket(),receiveMap,udpNetObject.getInetAddress(),udpNetObject.getPort());
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        //初始化接收端数据接收初处理池
        listenPool=new ListenPool<UdpHandleObject1>(Pools.getListPool("System.udp.server@"+hashCode()),0,TIME_OUT) {
            /**
             */
            @Override
            public void handle(UdpHandleObject1 udpHandleObject1) throws Exception {
                DatagramPacket datagramPacket=udpHandleObject1.getDatagramPacket();
                String ID;
                try {
                    UdpNetObject udpNetObject=read(datagramPacket);
                    ID = udpNetObject.getId();
                    if (udpNetObject.getLength()==1){
                        UdpReceiveMap udpReceiveMap=read(udpNetObject);
                        udpReceiveMap.setLength(1);
                        udpHandleObject3ListenPool.put(new UdpHandleObject3(udpReceiveMap,udpHandleObject1.getDatagramSocket()));
                    }
                    else {
                        int i = -1;
                        /* 获取当前key在缓冲列表位置，若存在则加入，若不存在则计算最小的那个缓冲位置 */
                        lock.lock();
                        try{
                            if (valuesRun.containsKey(ID)) {
                                i = valuesRun.get(ID);
                            } else {
                                i = 0;
                                for (int n = 0; n < HAND_SIZE; n++) {
                                    if (valuesWait[i] > valuesWait[n])
                                        i = n;
                                }
                            }
                            valuesRun.put(ID,i);
                            udpNetObjectListenPools[i].put(new UdpHandleObject2(udpNetObject,udpHandleObject1.getDatagramSocket()));
                            add(i);
                        }finally {
                            lock.unlock();
                        }
                    }
                }finally {
                    datagramPacketListPool.put(udpHandleObject1.getDatagramPacket());
                }
            }
        };
        //初始化接收端数据接收分处理池
        for (int i=0;i<HAND_SIZE;i++){
            udpNetObjectListenPools[i]=new ListenPool<UdpHandleObject2>(Pools.getListPool("System.udp.server@"+hashCode()+"_"+i),0,0) {
                @Override
                public void handle(UdpHandleObject2 udpHandleObject2) throws Exception {
                    UdpNetObject addUdpNetObject=udpHandleObject2.getUdpNetObject();
                    String id=addUdpNetObject.getId();
                    try {
                        UdpHandleObject2 dateTemp = null;
                        UdpNetObject tempUdpNetObject = null;
                        if (receive.containsKey(id)) {
                            dateTemp = receive.get(id).getValue();
                            tempUdpNetObject = dateTemp.getUdpNetObject();
                            tempUdpNetObject.add(addUdpNetObject);
                            if (tempUdpNetObject.isValid()) {
                                receive.remove(id);
                                udpHandleObject3ListenPool.put(new UdpHandleObject3(read(tempUdpNetObject).setLength(tempUdpNetObject.getLength()),udpHandleObject2.getDatagramSocket()));
                            }else receive.flush(id);
                        } else {
                            dateTemp = new UdpHandleObject2(tempUdpNetObject=addUdpNetObject, udpHandleObject2.getDatagramSocket());
                            receive.put(id,dateTemp,0);
                        }
                    }finally{
                        minus(id);
                    }
                }
            };
        }
    }


    private void minus(String key){
        minus(valuesRun.get(key));
    }

    /**
     * 用于计数减
     * @param i 池子编号
     */
    private void minus(int i) {
        lock.lock();
        try {
            valuesWait[i]--;
        }finally {
            lock.unlock();
        }
    }

    /**
     * 用于计数加
     * @param i 池子编号
     */
    private void add(int i) {
        lock.lock();
        try {
            valuesWait[i]++;
        }finally {
            lock.unlock();
        }
    }

    public void send(UdpReceiveMap udpReceiveMap, InetAddress inetAddress, int port) throws IOException, ClassNotFoundException {
        if(sockets.size()==0){
            send(new DatagramSocket(),udpReceiveMap,inetAddress,port);
        }else
            send(sockets.get(0),udpReceiveMap,inetAddress,port);
    }

    public void send(DatagramSocket datagramSocket, UdpReceiveMap udpReceiveMap, InetAddress inetAddress, int port) throws IOException, ClassNotFoundException {
        String ID=udpReceiveMap.getID();
        List<?> lists=write(datagramSocket,inetAddress,port,udpReceiveMap);
        send.put(ID, new UdpHandleObject4(lists,inetAddress,port,datagramSocket),lists.size()* 10L);
    }

    public void add(DatagramSocket datagramSocket){
        if(sockets.size()<BACK_LOG){
            sockets.add(datagramSocket);
            executorService.scheduleAtFixedRate(()->{while (!datagramSocket.isClosed()){
                try {
                    DatagramPacket datagramPacket=datagramPacketListPool.getWait(1000);
                    datagramSocket.receive(datagramPacket);
                    listenPool.put(new UdpHandleObject1(datagramPacket,datagramSocket));
                }catch (SocketException ignored){}
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            },0,Math.max(TIME_OUT,1), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public UdpReceiveMap addReceiveMap(UdpReceiveMap request) throws Exception {
        UdpReceiveMap respond=new UdpReceiveMap();
        boolean run=false;
        try {
            doWork(request,respond);
            run=true;
        }catch (Exception e){
            respond.put("Exception", FileRW.getError(e));
        }catch (Error e){
            respond.put("Error", FileRW.getError(e));
        }
        respond.setState(run);
        return respond;
    }

    @Override
    public void close() throws IOException {
        listenPool.close();
        executorService.shutdownNow();
        send.stop();
        receive.stop();
        for (ListenPool<?> listenPool:udpNetObjectListenPools){
            listenPool.close();
        }
        for (DatagramSocket socket : sockets) socket.close();
        System.exit(0);
    }

    @Override
    public String toString() {
        return "UDPServerSocket{" +
                "BACK_LOG=" + BACK_LOG +
                ", HAND_SIZE=" + HAND_SIZE +
                ", TIME_OUT=" + TIME_OUT +
                ", sockets=" + sockets +
                ", listenPool=" + listenPool +
                ", datagramPacketListPool=" + datagramPacketListPool +
                ", executorService=" + executorService +
                '}';
    }
}
