package liming.tool.timeout;

import liming.tool.pool.ListPool;
import liming.tool.pool.Pools;
import liming.tool.pool.TimePool;
import liming.tool.pool.TimePool.DataObject;

/**
 * 区分超时数据与超限数据，分别处理
 * 自动将超限数据转移，无需手动调配
 */
public abstract class TimeOut<T> {
    private static ListPool<Object> slog= Pools.getListPool("System.slog");
    TimePool<T> pool1, pool2;// 1处理超限，2处理超时
    private long timeOut1, timeOut2;// 4000

    private void keep(String name) {
        pool1 = new TimePool<T>(name+"&1",(int) Math.ceil(timeOut1),5000) {
            @Override
            public void handle(String key, DataObject<T> value) {
                pool2.put(key, value.getValue(), value.getAddTime());
                onRestrict(key, value.getValue());
            }
        };
        pool2 = new TimePool<T>(name+"&2",(int) Math.ceil(timeOut2),5000) {
            @Override
            public void handle(String key, DataObject<T> value) {
                onTimeout(key, value.getValue());
            }
        };
        slog.put("接收池 pool1: " + timeOut1 + ",pool2: " + timeOut2);
    }

    public synchronized DataObject<T> put(String ID, T t, long time) {
        return pool1.put(ID, t, time);
    }

    public synchronized T flush(String ID) {
        if (pool1.containsKey(ID)) {
            pool1.flush(ID);
        } else {
            if (pool2.containsKey(ID)) {
                DataObject<T> object = pool2.remove(ID);
                pool1.put(ID, object.getValue(), object.getAddTime());
            }
        }
        return pool1.get(ID).getValue();
    }

    public void setTime(long time, float ratio) {
        timeOut1 = (long) Math.ceil(time * ratio);
        if (timeOut1 < 200)
            timeOut1 = 200;
        if (timeOut1 > 800 && timeOut1 > time / 4)
            timeOut1 = time / 4;
        timeOut2 = (long) Math.ceil(time / ratio);
        if (timeOut2 > time - timeOut1)
            timeOut2 = time - timeOut1;
        pool1.setTimeOut(timeOut1);
        pool2.setTimeOut(timeOut2);
    }

    public synchronized DataObject<T> get(String ID) {
        if (pool1.containsKey(ID))
            return pool1.get(ID);
        if (pool2.containsKey(ID))
            return pool2.get(ID);
        return null;
    }

    public synchronized DataObject<T> remove(String ID) {
        if (pool1.containsKey(ID))
            return pool1.remove(ID);
        if (pool2.containsKey(ID))
            return pool2.remove(ID);
        return null;
    }

    public boolean containsKey(String ID) {
        return pool1.containsKey(ID) || pool2.containsKey(ID);
    }

    public synchronized void stop() {
        pool1.stop();
        pool2.stop();
    }
    public TimeOut(String name,long timeOut, float timeoutRatio) {
        if(timeOut<800)throw new RuntimeException("TimeOut time is too small, at least 800");
        timeOut1 = (long) Math.ceil(timeOut * timeoutRatio);
        if (timeOut1 < 200)
            timeOut1 = 200;
        if (timeOut1 > 800 && timeOut1 > timeOut / 4)
            timeOut1 = timeOut / 4;
        timeOut2 = (long) Math.ceil(timeOut / timeoutRatio);
        if (timeOut2 > timeOut - timeOut1)
            timeOut2 = timeOut - timeOut1;
        keep(name);
    }

    /**
     * 处理超时数据
     * 
     * @param key
     * @param value
     */
    public abstract void onTimeout(String key, T value);

    /**
     * 处理超限数据
     * 
     * @param key
     * @param value
     */
    public abstract void onRestrict(String key, T value);
}