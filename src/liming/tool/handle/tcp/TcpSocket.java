package liming.tool.handle.tcp;

import liming.tool.handle.root.RootSocket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Tcp网络通信基类，继承TextSocket
 * 连接无状态，数据流程 客户端->服务端->客户端
 * 结束连接由客户端执行
 */
public abstract class TcpSocket extends RootSocket {
    public TcpSocket(InetAddress inetAddress, int port) {
        super(inetAddress,port);
    }

    /**
     * 获取序列化的TcpReceiveMap对象
     * @param socket Tcp连接对象
     * @return 反序列化的对象
     */
    public static TcpReceiveMap read(Socket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());
        TcpReceiveMap tcpReceiveMap = (TcpReceiveMap) objectInputStream.readObject();
        tcpReceiveMap.setE_String();
        tcpReceiveMap.setSocket(socket);
        tcpReceiveMap.setReceiveTime(System.currentTimeMillis());
        return  tcpReceiveMap;
    }

    /**
     * 发送序列化数据
     * @param socket Tcp连接对象
     * @param tcpReceiveMap 数据对象
     * @throws IOException
     */
    protected static void write(Socket socket, TcpReceiveMap tcpReceiveMap) throws IOException {
        ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
        tcpReceiveMap.setSocket(socket);
        tcpReceiveMap.setSendTime(System.currentTimeMillis());
        objectOutputStream.writeObject(tcpReceiveMap);
    }

}
