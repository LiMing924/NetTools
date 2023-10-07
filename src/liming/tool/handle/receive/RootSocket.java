package liming.tool.handle.receive;


import java.io.Closeable;
import java.net.InetAddress;

/**
 * 网络通信基类，初始化接口方法
 */
public abstract class RootSocket implements GetDataAndPacket, Closeable {
    protected InetAddress inetAddress;
    protected int port;
    /**
     * 默认构造函数，在网络通信中，套接字信息都是一个必要的参数
     * @param port
     */
    public RootSocket(InetAddress inetAddress, int port){
        this.inetAddress=inetAddress;
        this.port=port;
    }

    public void setIP(InetAddress inetAddress, int port){
        this.inetAddress=inetAddress;
        this.port=port;
    }
}
