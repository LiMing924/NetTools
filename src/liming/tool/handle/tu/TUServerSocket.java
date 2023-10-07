package liming.tool.handle.tu;

import liming.tool.handle.receive.RootSocket;
import liming.tool.handle.tcp.TcpServetSocket;
import liming.tool.handle.tcp.TcpReceiveMap;
import liming.tool.handle.udp.UdpServerSocket;
import liming.tool.handle.udp.UdpReceiveMap;

import java.io.IOException;
import java.net.InetAddress;

public abstract class TUServerSocket extends RootSocket {
    private TcpServetSocket tcpServetSocket;
    private UdpServerSocket udpServerSocket;

    /**
     * 默认构造函数，在网络通信中，套接字信息都是一个必要的参数
     */
    public TUServerSocket(InetAddress inetAddress, int port) throws IOException {
        super(inetAddress, port);
        tcpServetSocket=new TcpServetSocket(port) {

            @Override
            public boolean doWork(TcpReceiveMap request, TcpReceiveMap respond) throws Exception {
                return doTcpWork(request,respond);
            }
        };
        udpServerSocket=new UdpServerSocket(inetAddress,port) {
            @Override
            public boolean doWork(UdpReceiveMap request, UdpReceiveMap respond) throws Exception {
                return doUdpWork(request,respond);
            }
        };
    }

    public void send(UdpReceiveMap udpReceiveMap, InetAddress inetAddress, int port) throws IOException, ClassNotFoundException {
        udpServerSocket.send(udpReceiveMap,inetAddress,port);
    }

    @Override
    public void close() throws IOException {
        udpServerSocket.close();
        tcpServetSocket.close();
    }

    public abstract boolean doTcpWork(TcpReceiveMap request, TcpReceiveMap respond) throws Exception;
    public abstract boolean doUdpWork(UdpReceiveMap request, UdpReceiveMap respond) throws Exception;
}
