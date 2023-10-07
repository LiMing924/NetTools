package liming.tool.test;

import liming.tool.handle.receive.Client;
import liming.tool.handle.receive.RootReceiveMap;
import liming.tool.handle.udp.UdpServerSocket;
import liming.tool.handle.udp.UdpReceiveMap;
import liming.tool.handle.udp.UdpClientSocket;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpTest {
    public static void main(String[] args) throws Exception {
        System.out.println("开始运行时间： "+System.currentTimeMillis());
        UdpServerSocket udpServerSocket = new UdpServerSocket(new DatagramSocket(6465), 10, 5, 0) {
            @Override
            public boolean doWork(UdpReceiveMap request, UdpReceiveMap respond) throws Exception {
                writeLog(123);
                respond.putAll(request);
                respond.put("serverTime",System.currentTimeMillis());
//                System.out.println(System.currentTimeMillis());
//                System.out.println("server "+request.getInetAddress()+" "+request.getPort());
//                close();
                return true;
            }

            @Override
            public void writeLog(Object message) {
                super.writeLog(message);
            }
        };
        //1695006688690
        //1695006688769
        //1695006696631
        System.out.println("服务器启动，客户端准备时间： "+System.currentTimeMillis());
        Client client=new UdpClientSocket(InetAddress.getByName("127.0.0.1"),6465) {
            @Override
            public boolean doWork(UdpReceiveMap respond) throws Exception {
                System.out.println("doWord:  "+respond);
                return false;
            }

            @Override
            public UdpReceiveMap addReceiveMap(UdpReceiveMap udpReceiveMap) throws Exception {
                return null;
            }
        };
        System.out.println("客户端完成，数据集打包时间： "+System.currentTimeMillis());
        UdpReceiveMap udpReceiveMap=new UdpReceiveMap();
        udpReceiveMap.put("startTime",System.currentTimeMillis());
//        UdpReceiveMap udpReceiveMap1 = udpServerSocket.addReceiveMap(udpReceiveMap);
//        System.out.println(System.currentTimeMillis());
//        System.out.println(udpReceiveMap1.optString("serverTime"));
//        System.out.println(udpReceiveMap1.optString("startTime"));
//        udpServerSocket.close();
        System.out.println("数据集完成，准备发送时间: "+System.currentTimeMillis());
        try {
            UdpReceiveMap udpReceiveMap1 = (UdpReceiveMap) client.write(udpReceiveMap);
            System.out.println("收到数据回发时间： "+System.currentTimeMillis());
            System.out.println(udpReceiveMap1.optString("serverTime"));
            System.out.println(udpReceiveMap1.optString("startTime"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        udpServerSocket.close();
//        udpReceiveMap.put("key","value");
//        udpReceiveMap.put("bytes", FileRW.readFileByte(new File("E:\\mysql-connector-java-8.0.27.jar")));
//        System.out.println("1: "+udpReceiveMap.getID());
//        udpServerSocket.send(udpReceiveMap, InetAddress.getByName("127.0.0.1"),6465);
    }
}
