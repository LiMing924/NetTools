package liming.tool.handle.udp.bean;

import java.net.DatagramSocket;

/**
 * 初处理的UDP数据，含 UDP转发中间数据包 和 接收端Socket
 */
public class UdpHandleObject2 extends UdpHandleObject {
    private UdpNetObject udpNetObject;

    public UdpHandleObject2(UdpNetObject udpNetObject, DatagramSocket datagramSocket) {
        super(datagramSocket);
        this.udpNetObject = udpNetObject;
    }

    public void setDatagramPacket(UdpNetObject udpNetObject) {
        this.udpNetObject = udpNetObject;
    }

    public UdpNetObject getUdpNetObject() {
        return udpNetObject;
    }
}
