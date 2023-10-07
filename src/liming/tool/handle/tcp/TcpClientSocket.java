package liming.tool.handle.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


public abstract class TcpClientSocket extends TcpSocket implements TcpClient {
    private Socket socket;
    private TcpReceiveMap tcpReceiveMap;
    /**
     * TCP客户端连接
     * @param inetAddress 套接字
     * @param port 端口
//     * @param tcpReceiveMap 数据集
     */
    public TcpClientSocket(InetAddress inetAddress, int port) throws IOException, ClassNotFoundException {
        super(inetAddress, port);
    }

    public static TcpReceiveMap send(InetAddress inetAddress, int port, TcpReceiveMap tcpReceiveMap) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(inetAddress, port)) {
            write(socket, tcpReceiveMap);
            return read(socket);
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
