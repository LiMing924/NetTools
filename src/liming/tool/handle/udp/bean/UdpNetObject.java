package liming.tool.handle.udp.bean;

import liming.tool.handle.ByteTool;
import org.json.JSONArray;

import java.io.Serializable;
import java.net.InetAddress;

public class UdpNetObject implements Serializable {

    private static final long serialVersionUID="liming.tool.handle.udp.bean.UdpNetObject".hashCode();
    private String id;
    private int length,num;
    private byte[] data;

    private transient UdpNetObject[] udpNetObjects;
    private transient byte[] values;
    private transient int coast =1;
    private transient InetAddress inetAddress;
    private transient int port;

    public UdpNetObject(String id, int length, int num, byte[] data) {
        this.id = id;
        this.length = length;
        this.num = num;
        this.data = data;
        udpNetObjects=new UdpNetObject[length];
        values=new byte[(int)Math.ceil(length/8.0)];
        add(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public UdpNetObject setIP(InetAddress inetAddress,int port){
        this.inetAddress=inetAddress;
        this.port=port;
        return this;
    }

    public InetAddress getInetAddress(){
        return inetAddress;
    }
    public int getPort(){
        return port;
    }

    @Override
    public String toString() {
        return "UdpNetObject{" +
                "id='" + id + '\'' +
                ", length=" + length +
                ", num=" + num +
                ", data=" + data.length +
                ", coast=" + coast +
                '}';
    }

    public synchronized void add(UdpNetObject udpNetObject){
        if(udpNetObjects==null||values==null){
            udpNetObjects=new UdpNetObject[length];
            values=new byte[(int)Math.ceil(length/8.0)];
            add(this);
        }
        udpNetObjects[udpNetObject.num] = udpNetObject;
        values[udpNetObject.num / 8] |= 1 << (udpNetObject.num % 8);
        coast++;
    }

    public synchronized boolean isValid() {
        if (length == 1) {
            return true;
        }
        if (coast < length)
            return false;
        for (int i = values.length - 1; i >= 0; i--) {
            if (i == values.length - 1) {
                for (int j = 0; j < length % 8; j++) {
                    if ((values[i] >>> j & 0x01) == 0) {
                        return false;
                    }
                }
            } else {
                if (values[i] != (byte) 255) {
                    return false;
                }
            }
        }
        return true;
    }

    public synchronized UdpNetObject[] getUdpNetObjects(){
        if(!isValid()){
            return new UdpNetObject[0];
        }
        if(udpNetObjects == null){
            return new UdpNetObject[]{this};
        }else return udpNetObjects;
    }

    public synchronized byte[] getValues() {
        if(udpNetObjects == null){
            return new byte[length];
        }else return values;
    }

    public static JSONArray getDeletion(UdpNetObject udpNetObject){
        return getDeletion(udpNetObject.values,udpNetObject.length);
    }

    public static JSONArray getDeletion(byte[] values,int length) {
        JSONArray deletion = new JSONArray();
        for (int i = values.length - 1; i >= 0; i--) {
            if (i == values.length - 1) {
                deletion.putAll(getDeletionBytes(values[i],i,length%8));
            } else {
                if (values[i] != 0xff) {
                    for (int j = 0; j < 8; j++) {
                        if (((values[i] >>> j) & 0x01) == 0)
                            deletion.put(i * 8 + j);
                    }
                }
            }
        }
        return deletion;
    }

    /**
     * 将
     * @param b 需解析的byte
     * @param ratio  系数
     * @param length 有效bit位数
     * @return
     */
    public static JSONArray getDeletionBytes(byte b,int ratio,int length){
        JSONArray deletion = new JSONArray();
        byte difference= ByteTool.ExclusiveOR(b,ByteTool.getStandard(length));
        for (int i=0;i<length;i++){
            if (ByteTool.SameOR (difference , (byte) (1 << i)) != 0){
                deletion.put(ratio*8+i);
            }
        }
        return deletion;
    }
}
