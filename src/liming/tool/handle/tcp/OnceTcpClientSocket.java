package liming.tool.handle.tcp;

import liming.tool.handle.root.error.DataCommunicationException;

import java.net.InetAddress;

public class OnceTcpClientSocket extends TcpClientSocket {
    public static final long OVERTIME=16*1000;//超时时间16秒。超过16秒未收到结果则断开连接
    private TcpReceiveMap request,respond;
    private boolean use=false;

    /**
     * TCP客户端连接,需手动调用write方法
     *
     * @param inetAddress 套接字
     * @param port        端口
     */
    public OnceTcpClientSocket(InetAddress inetAddress, int port) throws Exception {
        super(inetAddress, port);
    }
    /**
     * TCP客户端连接，自动调用write方法，手动调用时报错
     *
     * @param inetAddress 套接字
     * @param port        端口
     */
    public OnceTcpClientSocket(InetAddress inetAddress, int port, TcpReceiveMap request) throws Exception {
        super(inetAddress, port);
        write(request);
    }
    @Override
    public synchronized TcpReceiveMap write(TcpReceiveMap tcpReceiveMap) throws Exception {
        if(use)throw new DataCommunicationException("Task repetition");//已有其他链接被使用
        use=true;
        try {
            return this.respond=send(inetAddress,port,request=tcpReceiveMap);
        } finally {
            notifyAll();
        }
    }

    public TcpReceiveMap getRequest() {
        return request;
    }

    public synchronized TcpReceiveMap getRespond(long milliseconds, boolean always) throws DataCommunicationException {
        if(respond==null)
            if(milliseconds>0){
                try {
                    wait(milliseconds);
                } catch (InterruptedException e) {
                    throw new DataCommunicationException(e);
                }
            }else if(always){
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new DataCommunicationException(e);
                }
            }else {
                try {
                    wait(OVERTIME);
                } catch (InterruptedException e) {
                    throw new DataCommunicationException(e);
                }
            }
        if(respond==null) throw new DataCommunicationException("Received data timeout within "+milliseconds+" milliseconds");//接收数据在 milliseconds 毫秒内超时
        return respond;
    }
    public synchronized TcpReceiveMap getRespond(long milliseconds) throws DataCommunicationException {
        return getRespond(milliseconds,false);
    }

    @Override
    public TcpReceiveMap addReceiveMap(TcpReceiveMap tcpReceiveMap) throws Exception {
        throw new DataCommunicationException("The addReceiveMap() method cannot be called in the client");//不能在客户端中调用addReceiveMap()方法
    }

    @Override
    public boolean doWork(TcpReceiveMap respond) throws DataCommunicationException {
        throw new DataCommunicationException("The doWork() method cannot be defined in a customer agreement");//在一次客户协议中不能定义doWork()方法
    }
}
