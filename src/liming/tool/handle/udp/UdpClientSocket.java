package liming.tool.handle.udp;
/**
 * Udp客户端实现，
 */

import liming.tool.handle.udp.bean.UdpHandleObject4;
import liming.tool.handle.udp.bean.UdpNetObject;
import liming.tool.pool.ListenPool;
import liming.tool.pool.Pools;
import liming.tool.pool.TimePool;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

public abstract class UdpClientSocket extends UdpSocket implements UdpClient{
    private final DatagramSocket datagramSocket;
    private final HandleThread handleThread;

    public UdpClientSocket(DatagramSocket datagramSocket,InetAddress inetAddress, int port) {
        super(inetAddress,port);
        this.datagramSocket=datagramSocket;
        handleThread=new HandleThread();
    }
    public UdpClientSocket(int localPort,InetAddress inetAddress, int port) throws SocketException {
        this(new DatagramSocket(localPort),inetAddress,port);
    }

    public UdpClientSocket(InetAddress inetAddress, int port) throws SocketException {
        this(new DatagramSocket(),inetAddress,port);
    }

    @Override
    public void close() throws IOException {
        handleThread.close();
    }

    @Override
    public UdpReceiveMap write(UdpReceiveMap udpReceiveMap) throws Exception {
        try {
            handleThread.start();
            write(datagramSocket,inetAddress,port,udpReceiveMap);
            return handleThread.getUdpReceiveMap();
        } finally {
            close();
        }
    }

    public UdpReceiveMap getUdpReceiveMap() throws InterruptedException {
        return handleThread.getUdpReceiveMap();
    }
    private DatagramPacket getDatagramPacket(){
        return new DatagramPacket(new byte[SIZE],SIZE);
    }
    private class HandleThread extends Thread{
        private boolean run=false;
        private final ListenPool<DatagramPacket> receiveListenPool;
        private UdpReceiveMap udpReceiveMap;
        private UdpNetObject udpNetObject;

        {
            //初始化作为接收端的数据超时缓存池
            receiveListenPool= new ListenPool<DatagramPacket>(Pools.getListPool("liming.tool.handle.udp.UdpClientSocket.HandleThread&"
                    + Thread.currentThread().getName()),0,0) {
                @Override
                public void handle(DatagramPacket datagramPacket) throws Exception {
                    if(udpNetObject==null) udpNetObject=read(datagramPacket);
                    else udpNetObject.add(read(datagramPacket));
                    if(udpNetObject.isValid()){
                        udpReceiveMap=read(udpNetObject);
                        if(udpReceiveMap.isRm()||udpReceiveMap.isState()||udpReceiveMap.isResAll()){
                            udpReceiveMap=null;
                            udpNetObject=null;
                        }else {
                            run=false;
                            synchronized (this){
                                doWork(udpReceiveMap);
                                UdpClientSocket.this.close();
                            }
                        }
                    }
                }
            };
        }
        public synchronized UdpReceiveMap getUdpReceiveMap() throws InterruptedException {
            if(udpReceiveMap==null) wait();
            return udpReceiveMap;
        }

        @Override
        public void run() {
            run=true;
            while (run){
                try {
                    DatagramPacket datagramPacket = getDatagramPacket();
                    datagramSocket.receive(datagramPacket);
                    receiveListenPool.put(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized void close(){
            notifyAll();
            interrupt();
            receiveListenPool.close();
        }
    }
}
