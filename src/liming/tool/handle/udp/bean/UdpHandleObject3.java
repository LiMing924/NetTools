package liming.tool.handle.udp.bean;

import liming.tool.handle.udp.UdpReceiveMap;

import java.net.DatagramSocket;

/**
 * 处理后的UDP数据，含 UdpReceiveMap数据 和 接收端Socket
 */
public class UdpHandleObject3 extends UdpHandleObject {
    private UdpReceiveMap udpReceiveMap;

    public UdpHandleObject3(UdpReceiveMap udpReceiveMap, DatagramSocket datagramSocket) {
        super(datagramSocket);
        this.udpReceiveMap = udpReceiveMap;
    }

    public void setDatagramPacket(UdpReceiveMap udpReceiveMap) {
        this.udpReceiveMap = udpReceiveMap;
    }

    public UdpReceiveMap getUdpReceiveMap() {
        return udpReceiveMap;
    }
}
