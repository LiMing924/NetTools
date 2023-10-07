package liming.tool.test;

import liming.tool.handle.receive.RootSocket;
import liming.tool.handle.tcp.TcpServetSocket;
import liming.tool.handle.tcp.TcpReceiveMap;

import java.io.*;
import java.net.DatagramPacket;
import java.net.ServerSocket;

public class Test {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        byte b;
//        short s;
//        int i;
//        float f;
//        long l;
//        double d;
//        boolean boo;
//        char c;
        new Test();
    }

    public Test() throws IOException, ClassNotFoundException {
        new Test5();
//        new Test3();
        //RootSocket->>TCPSocket->TCPServetSocket
//        RootSocket rootSocket =new TcpServetSocket(new ServerSocket(6465),10,10,0) {
//            @Override
//            public boolean doWork(TcpReceiveMap request, TcpReceiveMap respond) throws Exception {
//                return false;
//            }
//        };
//        //RootSocket->>UDPSocket->UDPServetSocket
//        rootSocket=new UDPServerSocket() {
//            @Override
//            public boolean doWork(UDPReceiveMap request, UDPReceiveMap response) throws Exception {
//                return false;
//            }
//        };
//        TCPSocket tcpSocket = (TCPSocket) rootSocket;
//        byte[] bytes=new byte[]{(byte) 0x00,0x7f};
//        System.out.println(UdpNetObject.ArrayToBytes(bytes));
//        System.out.println(Integer.parseInt(UdpNetObject.ArrayToBytes(bytes)));
//        int x=1_100_200;
//        System.out.println(x);
//        System.out.println(UdpNetObject.getDeletionBytes(bytes[0],1,4));
//        System.out.println(Arrays.toString(UdpNetObject.getStandards(16)));
//        System.out.println(UdpNetObject.ArrayToByte(UdpNetObject.SameOR(bytes[0],bytes[1])));
//        System.out.println(UdpNetObject.ArrayToByte(UdpNetObject.ExclusiveOR(bytes[0],bytes[1])));
//        System.out.println(UdpNetObject.SameOR(bytes[0],bytes[1])+" "+0x37);
//        int length=10;
//        System.out.println(UdpNetObject.getDeletion(bytes,10));

//        Test1 test1=new Test1("liming.test");
//        Test2 test2=new Test2("id",write(test1));
//        byte[] bytes = write(test2);
//
//        DatagramPacket datagramPacket=new DatagramPacket(bytes,bytes.length);
//        Test2 test21 = read(datagramPacket);
//        Test1 test11 = read(test21);
//        System.out.println(test21);
//        System.out.println(test11);
//        System.out.println(test1);
//        System.out.println(test2);

//        JSONObject jsonObject=new JSONObject();
//        jsonObject.put("text1",test1);
//        String str=jsonObject.toString();
//        System.out.println(str);
//        JSONObject jsonObject1=new JSONObject(str);
//        System.out.println(jsonObject1.get("text1").getClass());

    }

    public byte[] write(Object object) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
        try {
            objectStream.writeObject(object);
            objectStream.flush();
            return byteStream.toByteArray();
        }finally {
            objectStream.close();
        }
    }

    public Test2 read(DatagramPacket datagramPacket) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(datagramPacket.getData());
        ObjectInputStream objectStream = new ObjectInputStream(byteStream);
        return (Test2) objectStream.readObject();
    }

    public Test1 read(Test2 test2) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(test2.getData());
        ObjectInputStream objectStream = new ObjectInputStream(byteStream);
        return (Test1) objectStream.readObject();
    }


}
class Test1 implements Serializable{
    String text;
    public Test1(String text){
        this.text=text;
    }

    public void setText(String text) {
        this.text = text;
    }

//    @Override
//    public String toString() {
//        return "Test1{" +
//                "text='" + text + '\'' +
//                '}';
//    }
}

class Test2 implements Serializable{
    private String id;
    private byte[] data;

    public Test2(String id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

//    @Override
//    public String toString() {
//        return "Test2{" +
//                "id='" + id + '\'' +
//                ", data_length=" +data.length +
//                '}';
//    }
}

class Test3 implements Serializable{
    private String id="456";

    public Test3(){
        System.out.println(new T1());
    }

    class T1{
        String id="123";
        T1(){
            this.id=id;
        }

        T1(String id){
            
        }



        @Override
        public String toString() {
            return "T1{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }
}
class Test4{
    private String name;

    public Test4() {
        name="123";
    }

    public String getName() {
        return name;
    }
}
class Test5 extends Test4{
    private String name;

    public Test5() {
        name="456";
        System.out.println(getName());
    }
}
