package liming.tool.handle.udp.bean;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

/**
 * 发送时的UDP数据，含 UDP数据包 和 接收端Socket
 */
public class UdpHandleObject4<T> extends UdpHandleObject {
    private List<T> lists;
    InetAddress inetAddress;
    int port;

    public UdpHandleObject4(List<T> lists, InetAddress inetAddress,int port, DatagramSocket datagramSocket) {
        super(datagramSocket);
        this.lists = lists;
        this.inetAddress=inetAddress;
        this.port=port;
    }

    public List<T> getList() {
        return lists;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public int getPort() {
        return port;
    }
}
