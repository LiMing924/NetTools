package liming.tool.handle.udp.bean;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * 未处理的UDP数据，含 UDP数据包 和 接收端Socket
 */
public class UdpHandleObject1 extends UdpHandleObject {
    private DatagramPacket datagramPacket;

    public UdpHandleObject1(DatagramPacket datagramPacket, DatagramSocket datagramSocket) {
        super(datagramSocket);
        this.datagramPacket = datagramPacket;
    }

    public void setDatagramPacket(DatagramPacket datagramPacket) {
        this.datagramPacket = datagramPacket;
    }

    public DatagramPacket getDatagramPacket() {
        return datagramPacket;
    }
}
