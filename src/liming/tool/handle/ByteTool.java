package liming.tool.handle;

public class ByteTool {
    /**
     * 异或
     * @param byte1 值1
     * @param byte2 值2
     * @return 异或值
     */
    public static byte ExclusiveOR(byte byte1,byte byte2){
        return (byte) (byte1 ^ byte2);
    }

    /**
     * 同或
     * @param byte1 值1
     * @param byte2 值2
     * @return 同或值
     */
    public static byte SameOR(byte byte1,byte byte2){
        return (byte) ~ExclusiveOR(byte1, byte2);
    }

    /**
     * 生成指定bit长度的全一数组
     */
    public static byte[] getStandards(int length){
        if(length==0){
            return new byte[]{0};
        } else{
            boolean zheng=length%8==0;
            byte[] standards=new byte[(int) Math.ceil(length/8.0)];
            for (int i = 0; i < standards.length-1; i++) {
                standards[i]= -128;
            }
            standards[standards.length-1]=zheng? -128: getStandard(length%8);
            return standards;
        }
    }

    /**
     * @param length 生成长度
     */
    public static byte getStandard(int length){
        if(length==0){
            return 0;
        }else if(length==8){
            return -128;
        }else {
            byte standard=0;
            for (int i = 0; i < length; i++) {
                standard= (byte) ((standard<<1)|1);
            }
            return standard;
        }
    }

    /**
     * 将 byte[] 解释为二进制表达的字符串,不使用间隔符
     */
    public static String ArrayToBytes(byte[] bytes){
        return ArrayToBytes(bytes,"");
    }

    /**
     * 将 byte[] 解释为二进制表达的字符串,使用指定间隔符
     */
    public static String ArrayToBytes(byte[] bytes,String interval){
        StringBuilder binaryString=new StringBuilder();
        for (int i=0;i<bytes.length;i++){
            if(i!=0) binaryString.append(interval);
            binaryString.append(ArrayToByte(bytes[i]));
        }
        return binaryString.toString();
    }

    /**
     * 将 byte 解释为二进制表达的字符串
     */
    public static String ArrayToByte(byte b){
        if(b==-128) return "11111111";
        StringBuilder binaryString = new StringBuilder(8);
        for (int i = 7; i >= 0; i--) {
            binaryString.append((b >> i) & 1);
        }
        return binaryString.toString();
    }
}
