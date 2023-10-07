package liming.tool.test;

import liming.tool.handle.tcp.TcpClientSocket;
import liming.tool.handle.tcp.TcpServetSocket;
import liming.tool.handle.tcp.TcpReceiveMap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPTest {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        ServerSocket serverSocket=new ServerSocket(64650);
        TcpServetSocket tcpServetSocket=new TcpServetSocket(serverSocket,20,5000,0) {
            @Override
            public boolean doWork(TcpReceiveMap request, TcpReceiveMap respond) throws Exception {
                System.out.println(request);
                System.out.println(respond);
                respond.putAll(request);
                respond.put("readTime",System.currentTimeMillis());
                return false;
            }
        };
        try {
            TcpReceiveMap tcpReceiveMap=new TcpReceiveMap();
            tcpReceiveMap.put("sendTime",System.currentTimeMillis());
//            tcpReceiveMap.put("bytes",new byte[1024*1024*128]);
            tcpReceiveMap= TcpClientSocket.send(InetAddress.getByName("127.0.0.1"),64650,tcpReceiveMap);
            System.out.println(tcpReceiveMap.isState());
            System.out.println(tcpReceiveMap.getE_String());
            Socket socket = tcpReceiveMap.getSocket();
            System.out.println("client: "+(tcpReceiveMap.optLong("readTime",0L)-tcpReceiveMap.optLong("sendTime",0L)+" "+socket.getLocalPort())+" "+socket.getInetAddress());
            tcpServetSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
