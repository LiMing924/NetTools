package liming.tool.test;

import liming.tool.handle.tcp.TcpReceiveMap;
import liming.tool.handle.udp.UdpReceiveMap;

public class MapTest {
    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
//        TcpReceiveMap tcpReceiveMap=new TcpReceiveMap();
//        System.out.println(System.currentTimeMillis());
//        TcpReceiveMap tcpReceiveMap2=new TcpReceiveMap();
//        System.out.println(System.currentTimeMillis());
        UdpReceiveMap udpReceiveMap=new UdpReceiveMap();
        System.out.println(System.currentTimeMillis());
        UdpReceiveMap udpReceiveMap2=new UdpReceiveMap();
        System.out.println(System.currentTimeMillis());

    }
}
