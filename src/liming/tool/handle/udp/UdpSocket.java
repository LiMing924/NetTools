package liming.tool.handle.udp;

import liming.tool.handle.receive.RootSocket;
import liming.tool.handle.udp.bean.*;
import liming.tool.pool.ListenPool;
import liming.tool.pool.TimePool;
import liming.tool.timeout.TimeOut;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 作为UDP方向总类，规定基础方法与工具类，方便分化后使用
 *
 * 在udp中，需要考虑数据有效性，与丢包情况，在客户端中，尽可能使用一次连接模式，及向服务器端发送数据后，在收到服务器处理的数据后就关闭连接
 * 在服务器端，采用异步发送与接收，在客户端中，采用同步
 * 核心：发送与接收，数据合并功能，重传机制
 * 发送：将ReceiveMap转为多个NetTemp包并加入到发送队列中（记录ReceiveMap的ID），也可直接将NetTemp包加入到队列中（不记录ID），由发送队列异步发送（不阻塞主线程），并将其放入发送超时缓存队列中等待重发或取消
 * 接收：将收到的DatagramPacket放入receive缓存队列中（不阻塞其他线程）
 */
public abstract class UdpSocket extends RootSocket {
    public static final long OVER_TIME=4_000L;

    /**
     * 默认构造函数，在网络通信中，套接字信息都是一个必要的参数
     * @param datagramSocket Udp监听端口
     */
    public UdpSocket(DatagramSocket datagramSocket) {
        super(datagramSocket.getLocalAddress(), datagramSocket.getLocalPort());
        overTime=OVER_TIME;
    }
    public UdpSocket(InetAddress inetAddress, int port){
        super(inetAddress,port);
        overTime=OVER_TIME;
    }
    private ListenPool<UdpHandleObject1> listenPool;//收到的数据,作为接收与初处理的第一处异步
    private ListenPool<UdpHandleObject3> udpHandleObject3ListenPool;//完整数据处理池，作为数据合并后与上层交付的异步
    private TimeOut<UdpHandleObject2> receive;//判定超时与未超时的接收数据
    private TimePool<UdpHandleObject4<UdpNetObject>> send;//发送数据缓存池
    private long overTime;

    public void setDataReceiveListenPool(ListenPool<UdpHandleObject1> listenPool) {
        this.listenPool = listenPool;
    }

    public void setDataSubmissionPool(ListenPool<UdpHandleObject3> udpHandleObject3ListenPool) {
        this.udpHandleObject3ListenPool = udpHandleObject3ListenPool;
    }

    public void setReceivePool(TimeOut<UdpHandleObject2> receive) {
        this.receive = receive;
    }

    public void setSendPool(TimePool<UdpHandleObject4<UdpNetObject>> send) {
        this.send = send;
    }
    public void put(DatagramPacket datagramPacket,DatagramSocket datagramSocket){
        listenPool.put(new UdpHandleObject1(datagramPacket,datagramSocket));
    }
    public void put(UdpReceiveMap udpReceiveMap,DatagramSocket datagramSocket){
        udpHandleObject3ListenPool.put(new UdpHandleObject3(udpReceiveMap,datagramSocket));
    }
    public void put(DatagramSocket datagramSocket,List<UdpNetObject> lists,InetAddress inetAddress, int port,String ID){
        send.put(ID, new UdpHandleObject4(lists,inetAddress,port,datagramSocket),lists.size()* 10L);
    }
    public void put(String id,UdpNetObject udpNetObject,DatagramSocket datagramSocket){
        receive.put(id, new UdpHandleObject2(udpNetObject,datagramSocket), udpNetObject.getLength());
    }

    /**
     * 将收到的数据包转换为UDP的中间序列化对象
     */
    public static UdpNetObject read(DatagramPacket datagramPacket) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(datagramPacket.getData());
        ObjectInputStream objectStream = new ObjectInputStream(byteStream);
        return ((UdpNetObject) objectStream.readObject()).setIP(datagramPacket.getAddress(),datagramPacket.getPort());
    }

    /**
     * 将中间对象转为UDPReceiveMap对象
     * （需要完整的中间对象）
     */
    public static UdpReceiveMap read(UdpNetObject udpNetObject) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(readUdpReceiveMapBytes(udpNetObject.getUdpNetObjects()));
        ObjectInputStream objectStream = new ObjectInputStream(byteStream);
        return ((UdpReceiveMap) objectStream.readObject()).setIP(udpNetObject.getInetAddress(),udpNetObject.getPort());
    }

    /**
     *
     * @param udpNetObjects
     * @return
     */
    private static byte[] readUdpReceiveMapBytes(UdpNetObject[] udpNetObjects) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (UdpNetObject udpNetObject : udpNetObjects) {
                byte[] data = udpNetObject.getData();
                outputStream.write(data);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            // 处理异常
            e.printStackTrace();
            return new byte[0]; // 或者抛出异常，根据您的需求
        }
    }

    protected static int SIZE=9*1024;
    private static final int Reservation=1024;

    /**
     * 从指定端口发送udp数据集
     * @param datagramSocket 发送端
     * @param inetAddress 接收端套接字
     * @param port 接收端端口
     * @param udpReceiveMap 数据
     */
    public static List<UdpNetObject> write(DatagramSocket datagramSocket, InetAddress inetAddress, int port, UdpReceiveMap udpReceiveMap) throws IOException, ClassNotFoundException {
        UdpReceiveMap UdpReceiveMap_copy =udpReceiveMap.copy();
        List<UdpNetObject> udpNetObjects = write(UdpReceiveMap_copy.getID(), UdpReceiveMap_copy);
        for (UdpNetObject udpNetObject : udpNetObjects) {
            write(datagramSocket,inetAddress,port,udpNetObject);
        }
        return udpNetObjects;
    }

    /**
     * 从指定端口发送udp中间对象包
     */
    protected static void write(DatagramSocket datagramSocket,InetAddress inetAddress, int port, UdpNetObject udpNetObject) throws IOException {
        DatagramPacket datagramPacket=write(udpNetObject,inetAddress,port);
        datagramSocket.send(datagramPacket);
    }

    /**
     * 给定id，给定对象，将其转为UDP中间对象数组
     */
    private static List<UdpNetObject> write(String id,Object object) throws IOException {
        return write(id,write(object));
    }

    /**
     * 将待发送的中间对象天然UDP数据包中
     */
    private static DatagramPacket write(UdpNetObject udpNetObject,InetAddress inetAddress,int port) throws IOException {
        byte[] bytes= write(udpNetObject);
        return new DatagramPacket(bytes,bytes.length,inetAddress,port);
    }
    /**
     * 给定id，给定序列化后的对象，将其转为UDP中间对象数组
     */
    private static List<UdpNetObject> write(String id,byte[] bytes){
        List<UdpNetObject> udpNetObjects=new ArrayList<>();
        List<UdpNetTempObject> udpNetTempObjects=new ArrayList<>();
        try {
            int length= bytes.length;
            int startIdx = 0,endIdx , x=0;
            while (startIdx<length){
                endIdx=Math.min(startIdx+SIZE-Reservation,length);
                udpNetTempObjects.add(new UdpNetTempObject(id,Arrays.copyOfRange(bytes, startIdx, endIdx)));
                startIdx=endIdx;
            }
            for (int i=0;i<udpNetTempObjects.size();i++) {
                udpNetObjects.add(udpNetTempObjects.get(i).getUdpNetObject(i, udpNetTempObjects.size()));
            }
            return udpNetObjects;
        }finally {
            udpNetTempObjects.clear();
        }
    }

    /**
     * 将对象序列化
     */
    private static byte[] write(Object object) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
        try {
            objectStream.writeObject(object);
            objectStream.flush();
            return byteStream.toByteArray();
        }finally {
            objectStream.close();
        }
    }
}
