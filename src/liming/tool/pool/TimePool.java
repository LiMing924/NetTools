package liming.tool.pool;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 超时基本类，用于处理超时数据
 * 设置初始超时时间
 * 在放入数据时给追加超时时间
 */
public abstract class TimePool<T> {
    public static final long OVER_TIME = 4_000;
    private Map<String, DataObject<T>> temp = new HashMap<>();
    private PriorityQueue<DataObject<T>> sortedData;
    private ScheduledExecutorService executorService;
    private long timeOut;
    private ListPool<PoolObject<T>> pool;
    private Lock lock;

    public Set<String> getKeySet(){
        return temp.keySet();
    }

    /**
     * 放入数据
     * @param ID 数据ID
     * @param data 数据
     * @param addTime 额外的超时时间
     * @return
     */
    public DataObject<T> put(String ID, T data, long addTime) {
        lock.lock();
        try {
            DataObject<T> outTime = new DataObject<>(ID,data, addTime);
            temp.put(ID, outTime);
            sortedData.offer(outTime);
            return outTime;
        }finally {
            lock.unlock();
        }
    }

    public T flush(String ID) {
        lock.lock();
        try {
            DataObject<T> dataObject = temp.get(ID);
            if(dataObject != null) {
                dataObject.flush();
                sortedData.remove(dataObject);
                sortedData.offer(dataObject);
                return dataObject.flush();
            }
            return null;
        }finally {
            lock.unlock();
        }
    }

    public DataObject<T> get(String ID) {
        return temp.get(ID);
    }

    public T getValue(String ID) {
        DataObject<T> dataObject = temp.get(ID);
        return dataObject == null ? null : dataObject.value;
    }

    public long getTime(String ID) {
        DataObject<T> dataObject = temp.get(ID);
        return dataObject == null ? 0 : (System.currentTimeMillis()- dataObject.time-dataObject.addtime);
    }

    public synchronized DataObject<T> remove(String ID) {
        DataObject<T> removedData = temp.remove(ID);
        if (removedData != null) {
            sortedData.remove(removedData); // 从按结束时间排序的集合中移除数据
        }
        return removedData;
    }

    public boolean containsKey(String ID) {
        return temp.containsKey(ID);
    }

    public TimePool(String name, long timeOut, int size) {
        lock = new ReentrantLock();
        pool = Pools.getListPool(name, size);
        this.timeOut = timeOut;
        sortedData = new PriorityQueue<>(Comparator.comparingLong(d -> (d.time + d.addtime + timeOut)));
        executorService = Executors.newScheduledThreadPool(2);
        executorService.scheduleAtFixedRate(this::examination, 0, 1, TimeUnit.MILLISECONDS);
        executorService.scheduleAtFixedRate(this::push, 0, 1, TimeUnit.MILLISECONDS);
    }


    private void push() {
        do {
            PoolObject<T> poolObject = pool.getWait(0);
            if (!pool.isRun())
                break;
            handle(poolObject.key, poolObject.dataObject);
        } while (!pool.isEmpty());
    }

    public void stop() {
        pool.close();
        if (executorService != null) {
            executorService.shutdown();
        }
        temp.clear();
    }

    public TimePool(String name, int size) {
        this(name, OVER_TIME, size);
    }

    private static class PoolObject<T> {
        String key;
        DataObject<T> dataObject;

        public PoolObject(String key, DataObject<T> object) {
            this.key = key;
            this.dataObject = object;
        }
    }

    public static class DataObject<T> {
        private long time = System.currentTimeMillis(), addtime;

        private String key;
        private T value;

        protected DataObject(String key,T value, long addtime) {
            this.key=key;
            this.value = value;
            this.addtime = addtime;
        }

        public T flush() {
            time = System.currentTimeMillis();
            return value;
        }

        public String getKey(){
            return key;
        }

        public T getValue() {
            return value;
        }

        public long getStartTime() {
            return time;
        }

        public long getAddTime() {
            return addtime;
        }

        @Override
        public String toString() {
            return "DataObject{" +
                    "key='" + key + '\'' +
                    ", time=" + time +
                    ", addtime=" + addtime +
                    ", endtime=" + (time+addtime+4000) +
                    ", valueType=" + value.getClass().getSimpleName() +
                    '}';
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TimePool<Object> timePool = new TimePool<Object>("test1", 4000, 2000) {
            @Override
            public void handle(String key, DataObject<Object> value) {
                System.out.println(new Date() + " " + key + " " + value.getValue());
            }
        };

        timePool.put("1", new Date(), 0);
        Thread.sleep(1000);
        timePool.put("2", new Date(), 0);
        Thread.sleep(1000);
        timePool.put("3", new Date(), 0);
        Thread.sleep(1000);
        timePool.flush("1");
    }

    /**
     * examination 检查 检查首个数据是否超时，若超时则调用提醒机制
     */
    private synchronized void examination() {
//        long currentTime = System.currentTimeMillis();
//        temp.forEach((key, dataObject) -> {
//            if (dataObject.time + timeOut + dataObject.addtime < currentTime) {
//                pool.put(new PoolObject<>(key, temp.remove(key)));
//            }
//        });
        while (!sortedData.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            DataObject<T> dataObject = sortedData.peek();
            long endTime=dataObject.time + timeOut + dataObject.addtime;
            if (endTime < currentTime) {
                pool.put(new PoolObject<>(dataObject.key, temp.remove(dataObject.key)));
                sortedData.poll(); // 从排序集合中移除已处理的数据
            } else {
                long sTime=endTime-currentTime;
                try {
                    synchronized (this) {
                        wait(Math.max(Math.min(sTime, timeOut), 1));
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 处理超时数据
     *
     * @param key
     * @param value
     */
    public abstract void handle(String key, DataObject<T> value);

    public void setTimeOut(long time) {
        timeOut = time;
        synchronized (this) {
            notifyAll();
        }
    }
}