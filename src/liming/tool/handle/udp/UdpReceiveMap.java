package liming.tool.handle.udp;

import liming.tool.handle.receive.RootReceiveMap;

import java.net.InetAddress;

public class UdpReceiveMap extends RootReceiveMap {
    private static final long serialVersionUID = "liming.tool.handle.udp.UdpReceiveMap".hashCode();

    private transient int length=0;

    public int getLength() {
        return length;
    }

    public UdpReceiveMap setLength(int length) {
        this.length = length;
        return this;
    }



    private transient InetAddress inetAddress;
    private transient int port;

    public UdpReceiveMap setIP(InetAddress inetAddress, int port){
        this.inetAddress = inetAddress;
        this.port = port;
        return this;
    }
    public UdpReceiveMap(RootReceiveMap rootReceiveMap) {
        super(rootReceiveMap);
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public int getPort() {
        return port;
    }

    public UdpReceiveMap() {
        super();
    }



    public UdpReceiveMap copy() {
        return new UdpReceiveMap(this);
    }

    @Override
    public UdpReceiveMap setByte(int x, byte value) {
        if (x == 0) bytes[0] = (byte) ((bytes[0] & 0xC0) | (value & 0x3f));
        else super.setByte(x, value);
        return this;
    }

    @Override
    public UdpReceiveMap setBit(int x, int y, boolean value) {
        if (x == 0 && (y >= 0 && y <= 1))
            throw new RuntimeException("Illegal assignment at (0, 0) or (0, 1)");
        super.setBit(x, y, value);
        return this;
    }

    /**
     * 设置数据包状态，
     * @return
     */
    public boolean isState() {
        return getBit(0, 0);
    }

    public UdpReceiveMap setState(boolean value) {
        bytes[0] = (byte) ((int) bytes[0] & 0x7f | (value ? 1 : 0) << 7);
        return this;
    }

    public boolean isResAll() {
        return getBit(0, 1);
    }

    public UdpReceiveMap setResAll(boolean value) {
        bytes[0] = (byte) ((int) bytes[0] & 0xbf | (value ? 1 : 0) << 6);
        return this;
    }
    public boolean isRm() {
        return getBit(0, 2);
    }

    public UdpReceiveMap setRm(boolean value) {
        bytes[0] = (byte) ((int) bytes[0] & 0x7f | (value ? 1 : 0) << 5);
        return this;
    }
}
