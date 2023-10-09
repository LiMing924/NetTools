package liming.tool.handle.root;

import liming.tool.handle.FileRW;
import liming.tool.rsa.RSA_Encryption;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RootReceiveMap implements Serializable,Cloneable {
    public static final String UUID = java.util.UUID.randomUUID().toString();// 获取当前计算机的唯一id
    private static final int MajorVersionNumber = 4;// 主版本号 协议主要版本
    private static final int MinorVersionNumber = 0;// 副版本号 主要在性能方向的优化版本
    private static final int SerialNumber = 1;// 序列号 测试时的序列号

    private String ID;

    /**
     * 获取核心版本号
     * @return
     */
    public static String VERSION() {
        return "版本号=" + MajorVersionNumber + "." + MinorVersionNumber + "." + SerialNumber;
    }

    private int majorVersionNumber, minorVersionNumber, serialNumber;

    protected void setVersion(int majorVersionNumber, int minorVersionNumber, int serialNumber) {
        this.majorVersionNumber = majorVersionNumber;
        this.minorVersionNumber = minorVersionNumber;
        this.serialNumber = serialNumber;
    }

    public String getVersion() {
        return "版本号=" + majorVersionNumber + "." + minorVersionNumber + "." + serialNumber;
    }

    /**
     * 携带的数据集
     */
    private final Map<String, Object> map;
    /**
     * byte[] 类型的key
     */
    private final Set<String> bytesKey;
    protected byte[] bytes;//补充数据段，共10个字节

    public RootReceiveMap() {
        map = new HashMap<>();
        bytesKey = new HashSet<>();
        bytes = new byte[10];
        setVersion(MajorVersionNumber, MinorVersionNumber, SerialNumber);
    }

    public RootReceiveMap(RootReceiveMap rootReceiveMap){
        this();
        ID = rootReceiveMap.getID();
        setVersion(rootReceiveMap.majorVersionNumber, rootReceiveMap.minorVersionNumber, rootReceiveMap.serialNumber);
        putAll(rootReceiveMap);
        bytes= rootReceiveMap.bytes.clone();
    }

    // ========= put putAll ============

    // byte,char,short,int,long,float,double,boolean
    // String,byte[]
    public synchronized RootReceiveMap put(String key, Byte value) {
        map.put(key, (byte) value);
        return this;
    }

    public synchronized RootReceiveMap put(String key, Character value) {
        map.put(key, value);
        return this;
    }

    public synchronized RootReceiveMap put(String key, Short value) {
        map.put(key, value);
        return this;
    }

    public synchronized RootReceiveMap put(String key, Integer value) {
        map.put(key, value);
        return this;
    }

    public synchronized RootReceiveMap put(String key, Long value) {
        map.put(key, value);
        return this;
    }

    public synchronized RootReceiveMap put(String key, Float value) {
        map.put(key, value);
        return this;
    }

    public synchronized RootReceiveMap put(String key, Double value) {
        map.put(key, value);
        return this;
    }

    public synchronized RootReceiveMap put(String key, Boolean value) {
        map.put(key, value);
        return this;
    }

    public synchronized RootReceiveMap put(String key, String value) {
        map.put(key, value);
        return this;
    }

    public synchronized RootReceiveMap put(String key, byte[] value) {
        bytesKey.add(key);
        map.put(key, value);
        return this;
    }

    public synchronized RootReceiveMap put(String key, Object value) {
        if (value instanceof byte[]) {
            put(key, (byte[]) value);
        } else
            map.put(key, value);
        return this;
    }

    public synchronized RootReceiveMap putAll(JSONObject values) {
        for (String key : values.keySet()) {
            put(key, values.get(key));
        }
        return this;
    }

    public synchronized RootReceiveMap putAll(Map<?, ?> values) {
        for (Object key : values.keySet()) {
            put(key.toString(), values.get(key));
        }
        return this;
    }

    public synchronized RootReceiveMap putAll(RootReceiveMap rootReceiveMap) {
        map.putAll(rootReceiveMap.map);
        bytesKey.addAll(rootReceiveMap.bytesKey);
        return this;
    }

    public RootReceiveMap putFile(String key, File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getPath() + " 文件未找到");
        }
        return put(key, FileRW.readFileByte(file));
    }
    // ============= receive 方法区 ======================

    public synchronized Object remove(String key) {
        bytesKey.remove(key);
        return map.remove(key);
    }

    public synchronized void remove(String key, String... keys) {
        remove(key);
        for (String k : keys) {
            remove(k);
        }
    }

    public synchronized void removeBytesAll() {
        for (String key : bytesKey) {
            remove(key);
        }
    }

    public synchronized void removeNoBytesAll() {
        for (String key : getNoBytesKey()) {
            remove(key);
        }
    }

    public synchronized void removeAll() {
        map.clear();
        bytesKey.clear();
    }

    // ============== get pot 方法区 ======================

    public Object get(String key) {
        if (map.containsKey(key))
            return map.get(key);
        throw new RuntimeException("key= " + key + " 未在在Receive中不存在");
    }

    public Object opt(String key) {
        return opt(key, null);
    }

    public Object opt(String key, Object value) {
        Object object = map.get(key);
        return object == null ? value : object;
    }

    public byte getByte(String key) {
        return (byte) get(key);
    }

    public byte optByte(String key) {
        return optByte(key, (byte) 0);
    }

    public byte optByte(String key, Byte value) {
        Object object = opt(key);
        return object instanceof Byte ? (byte) object : value;
    }

    public char getChar(String key) {
        return (char) get(key);
    }

    public char optChar(String key) {
        return optChar(key, (char) 0);
    }

    public char optChar(String key, Character value) {
        Object object = opt(key);
        return object instanceof Character ? (char) object : value;
    }

    public short getShort(String key) {
        return (short) get(key);
    }

    public short optShort(String key) {
        return optShort(key, (short) 0);
    }

    public short optShort(String key, Short value) {
        Object object = opt(key);
        return object instanceof Short ? (short) object : value;
    }

    public int getInt(String key) {
        return (int) get(key);
    }

    public int optInt(String key) {
        return optInt(key, 0);
    }

    public int optInt(String key, Integer value) {
        Object object = opt(key);
        return object instanceof Integer ? (int) object : value;
    }

    public long getLong(String key) {
        return (long) get(key);
    }

    public long optLong(String key) {
        return optLong(key, 0l);
    }

    public long optLong(String key, Long value) {
        Object object = opt(key);
        return object instanceof Long ? (long) object : value;
    }

    public float getFloat(String key) {
        return (float) get(key);
    }

    public float optFloat(String key) {
        return optFloat(key, 0f);
    }

    public float optFloat(String key, Float value) {
        Object object = opt(key);
        return object instanceof Float ? (float) object : value;
    }

    public double getDouble(String key) {
        return (double) get(key);
    }

    public double optDouble(String key) {
        return optDouble(key, 0d);
    }

    public double optDouble(String key, Double value) {
        Object object = opt(key);
        return object instanceof Double ? (double) object : value;
    }

    public boolean getBoolean(String key) {
        return (boolean) get(key);
    }

    public boolean optBoolean(String key) {
        return optBoolean(key, Boolean.FALSE);
    }

    public boolean optBoolean(String key, Boolean value) {
        Object object = opt(key);
        return object instanceof Boolean ? (boolean) object : value;
    }

    public String getString(String key) {
        return (String) get(key);
    }

    public String optString(String key) {
        return optString(key, null);
    }

    public String optString(String key, String value) {
        Object object = opt(key);
        return object == null ? value : object.toString();
    }

    public byte[] getBytes(String key) {
        return (byte[]) get(key);
    }

    public byte[] optBytes(String key) {
        return optBytes(key, null);
    }

    public byte[] optBytes(String key, byte[] value) {
        Object object = opt(key);
        return object instanceof byte[] ? (byte[]) object : value;
    }

    public JSONObject getJsonObject(String key) {
        return (JSONObject) get(key);
    }

    public JSONObject optJsonObject(String key) {
        return optJsonObject(key, null);
    }

    public JSONObject optJsonObject(String key, JSONObject value) {
        Object object = opt(key);
        return object instanceof JSONObject ? (JSONObject) object : value;
    }

    public JSONArray getJsonArray(String key) {
        return (JSONArray) get(key);
    }

    public JSONArray optJsonArray(String key) {
        return optJsonArray(key, null);
    }

    public JSONArray optJsonArray(String key, JSONArray value) {
        Object object = opt(key);
        return object instanceof JSONArray ? (JSONArray) object : value;
    }

    // ================ 设置/获取额外标识符 ==================
    // ============ 采用大端模式，第0字节最高位为0 =============

    public RootReceiveMap setByte(int x, byte value) {
        if (x >= 10 || x < 0)
            throw new RuntimeException(
                    "Subscript out of bounds, x (0-9), current x:" + x);
        bytes[x] = value;
        return this;
    }

    /**
     * 设置额外数据标识区，长度10*8，其中（0,0）（0,1）不可设置
     *
     * @param x     行下标
     * @param y     列下标
     * @param value 设置值
     * @return
     */
    public RootReceiveMap setBit(int x, int y, boolean value) {
        if (x >= 10 || x < 0 || y >= 8)
            throw new RuntimeException(
                    "Subscript out of bounds, x (0-9), y (0-7), current (x, y): (" + x + ":" + y + ")");
        if (value) {
            bytes[x] |= 1 << (7 - y);
        } else {
            bytes[x] &= ~(1 << (7 - y));
        }
        return this;
    }

    public byte getByte(int x) {
        if (x >= 10 || x < 0)
            throw new RuntimeException(
                    "Subscript out of bounds, x (0-9), y (0-7), current x:" + x);
        return bytes[x];
    }

    public boolean getBit(int x, int y) {
        if (x >= 10 || x < 0 || y >= 8)
            throw new RuntimeException(
                    "Subscript out of bounds, x (0-9), y (0-7), current (x, y): (" + x + ":" + y + ")");
        return (bytes[x] >>> (7 - y) & 0x01) != 0;
    }


    public byte[] getOtherBytes() {
        return bytes;
    }

    // ================ 额外区方法 ==================
    public RootReceiveMap copy(){
        return new RootReceiveMap(this);
    }
    /**
     * 获取所有存放的值
     */
    public Map<String, Object> getData() {
        return map;
    }

    /**
     * 获取所有存放的非byte[]类型数据
     */
    public Map<String, Object> getNoBytesData() {
        Map<String, Object> data = new HashMap<>(map);
        for (String key : bytesKey) {
            data.remove(key);
        }
        return data;
    }

    /**
     * 获取所有存放的非byte[]类型数据key
     */
    public Set<String> getNoBytesKey() {
        Set<String> set = new HashSet<>(map.keySet());
        set.removeAll(bytesKey);
        return set;
    }

    /**
     * 获取所有存放的byte[]类型数据
     */
    public Map<String, byte[]> getBytes() {
        Map<String, byte[]> bytes = new HashMap<>();
        for (String key : bytesKey) {
            bytes.put(key, optBytes(key, null));
        }
        return bytes;
    }

    /**
     * 获取所有存放的byte[]类型数据key
     */
    public Set<String> getBytesKey() {
        return bytesKey;
    }

    /**
     * 判断键是否存在
     */
    public boolean contains(String key) {
        return map.containsKey(key);
    }

    /**
     * 判断键是否为byte[]的数据key
     */
    public boolean containsByte(String key) {
        return bytesKey.contains(key);
    }

    public synchronized String getID(){
        if(ID == null){
            ID  =RefreshID();
        }
        return ID;
    }

    public synchronized String RefreshID(){
        return ID = RSA_Encryption.generate(64)+System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "ReceiveMap [map=" + map.keySet() + ",noBytesKey=" + getNoBytesKey() + ", bytesKey=" + bytesKey + "]";
    }
}
