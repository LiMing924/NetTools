package liming.tool.handle.udp.bean;

import java.util.List;

public class UdpNetTempObject {
    private String id;
    private byte[] bytes;

    public UdpNetTempObject(String id,byte[] bytes){
        this.id=id;
        this.bytes=bytes;
    }

    public UdpNetObject getUdpNetObject(int num,int length){
        return new UdpNetObject(id,length,num,bytes);
    }
}
