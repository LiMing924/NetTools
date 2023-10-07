package liming.tool.test;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class TUTest {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        new TUTest();
    }
    public TUTest() {
//        ServerSocket serverSocket=new ServerSocket(6465);
//        DatagramSocket datagramSocket=new DatagramSocket(6465);
    }

}
class Test01 extends Object {
    private String str = "";

    public Test01(String str) {
        this.str =str;
    }

    private String getStr(){
        return "调用getStr方法："+str;
    }

    @Override
    public String toString() {
        return "Test01{" +
                "str='" + str + '\'' +
                '}';
    }
}
