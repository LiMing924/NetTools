package liming.tool.handle.tcp;

import liming.tool.handle.root.RootReceiveMap;

import java.net.Socket;

public class TcpReceiveMap extends RootReceiveMap {
    private transient Socket socket;
    private boolean state = true;
    private String E_String;

    public TcpReceiveMap() {
        super();
    }

    public TcpReceiveMap(RootReceiveMap rootReceiveMap) {
        super(rootReceiveMap);
    }

    protected void setE_String() {
        if(state){
            E_String= "无错误";
        }else {
            E_String ="Exception: "+ optString("Exception","无\n");
            E_String +="Error: "+ optString("Error","无\n");
        }
    }

    public String getE_String() {
        return E_String;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public boolean isState() {
        return state;
    }

    private long sendTime, receiveTime;

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public long getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void removeSocket() {
        socket = null;
    }
}
