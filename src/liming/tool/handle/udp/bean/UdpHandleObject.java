package liming.tool.handle.udp.bean;

import java.net.DatagramSocket;

class UdpHandleObject {
    private DatagramSocket datagramSocket;

    public UdpHandleObject(DatagramSocket datagramSocket){
        this.datagramSocket=datagramSocket;
    }

    public void setDatagramSocket(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

}
