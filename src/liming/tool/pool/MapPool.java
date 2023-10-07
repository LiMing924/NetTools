package liming.tool.pool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MapPool<K, V> {

    private static int X = 0; // 当前创建的实例个数
    private final int x;
    private int size; // 最大容量个数
    private long size_put = 0;
    private long size_get = 0;

    private Map<K, V> map;
    private Boolean RUN = true;
    private Lock lock;
    private Condition putCondition;
    private Condition getCondition;
    private Condition fullCondition;
    private Condition emptyCondition;

    MapPool() {
        this(5000);
    }

    /**
     * 构造函数
     *
     * @param size 队列的最大容量
     */
    MapPool(int size) {
        this(size, new HashMap<>());
    }

    /**
     * 构造函数
     *
     * @param size 队列的最大容量
     * @param map  初始队列
     */
    MapPool(int size, Map<K, V> map) {
        this.map = map;
        x = X++;
        this.size = size;
        lock = new ReentrantLock();
        putCondition = lock.newCondition();
        getCondition = lock.newCondition();
        fullCondition = lock.newCondition();
        emptyCondition = lock.newCondition();
    }

    /**
     * 向队列中添加对象
     *
     * @param key   要添加的对象的键
     * @param value 要添加的对象的值
     * @return 返回LinkedMapPool对象本身，支持链式调用
     */
    public MapPool<K, V> put(K key, V value) {
        lock.lock();
        boolean get = false;
        try {
            if (getSize() >= size)
                throw new RuntimeException("The current pool is full");
            get = true;
            map.put(key, value);
            size_put++;
        } finally {
            if (map.size() >= size)
                fullCondition.signalAll();
            if (get)
                getCondition.signal();
            lock.unlock();
        }
        return this;
    }

    /**
     * 向队列中添加对象，如果队列已满，则等待指定的时间
     *
     * @param key         要添加的对象的键
     * @param value       要添加的对象的值
     * @param millisecond 等待的时间（毫秒）
     * @return 返回LinkedMapPool对象本身，支持链式调用
     */
    public MapPool<K, V> put(K key, V value, long millisecond) {
        long time = millisecond, endTime = System.currentTimeMillis() + millisecond;
        lock.lock();
        boolean get = false;
        try {
            while (map.size() >= size & RUN) {
                if (millisecond <= 0)
                    putCondition.await();
                else {
                    time = Math.max(endTime - System.currentTimeMillis(), 0);
                    putCondition.await(time, TimeUnit.MILLISECONDS);
                    if (endTime <= System.currentTimeMillis() && map.size() >= size)
                        throw new RuntimeException("Timed out " + millisecond + " milliseconds when inserting data");
                }
            }
            if (RUN) {
                get = true;
                map.put(key, value);
                size_put++;
                getCondition.signal();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (map.size() >= size)
                fullCondition.signalAll();
            if (get)
                getCondition.signal();
            lock.unlock();
        }
        return this;
    }

    /**
     * 从队列中获取对象
     *
     * @param key 要获取的对象的键
     * @return 队列中对应键的对象
     */
    public V get(K key) {
        lock.lock();
        boolean put = false;
        try {
            if (!map.containsKey(key))
                throw new RuntimeException("The current pool does not contain the specified key");
            put = true;
            size_get++;
            return map.remove(key);
        } finally {
            if (map.isEmpty()) {
                emptyCondition.signalAll();
            }
            if (put)
                putCondition.signal();
            lock.unlock();
        }
    }

    /**
     * 从队列中获取对象，如果队列为空，则等待指定的时间
     *
     * @param key         要获取的对象的键
     * @param millisecond 等待的时间（毫秒）
     * @return 队列中对应键的对象
     */
    public V getWait(K key, long millisecond) {
        long time = millisecond, endTime = System.currentTimeMillis() + millisecond;
        lock.lock();
        boolean put = false;
        try {
            while (!map.containsKey(key) & RUN) {
                if (millisecond <= 0)
                    getCondition.await();
                else {
                    time = Math.max(endTime - System.currentTimeMillis(), 0);
                    getCondition.await(time, TimeUnit.MILLISECONDS);
                    if (endTime <= System.currentTimeMillis() && !map.containsKey(key))
                        throw new RuntimeException("Timed out " + millisecond + " milliseconds while retrieving data");
                }
            }
            if (RUN) {
                V value = map.remove(key);
                size_get++;
                put = true;
                putCondition.signal();
                return value;
            } else
                throw new RuntimeException("ListPool has ended");
        } catch (InterruptedException e) {
            return null;
        } finally {
            if (map.isEmpty()) {
                emptyCondition.signalAll();
            }
            if (put)
                putCondition.signal();
            lock.unlock();
        }
    }

    /**
     * 判断队列是否为空
     *
     * @return 如果队列为空，则返回true；否则返回false
     */
    public boolean isEmpty() {
        lock.lock();
        try {
            return map.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 判断队列是否已满
     *
     * @return 如果队列已满，则返回true；否则返回false
     */
    public boolean isFull() {
        lock.lock();
        try {
            return map.size() >= size;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 等待队列为空
     *
     * @throws InterruptedException
     */
    public void waitEmpty() throws InterruptedException {
        lock.lock();
        try {
            while (!isEmpty()&RUN) {
                emptyCondition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 等待队列为空，最多等待指定的时间
     *
     * @param millisecond 等待的时间（毫秒）
     * @throws InterruptedException
     */
    public void waitEmpty(long millisecond) throws InterruptedException {
        lock.lock();
        try {
            while (!isEmpty()&RUN) {
                emptyCondition.await(millisecond, TimeUnit.MILLISECONDS);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 等待队列已满
     *
     * @throws InterruptedException
     */
    public void waitFull() throws InterruptedException {
        lock.lock();
        try {
            while (!isFull()&RUN) {
                fullCondition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 等待队列已满，最多等待指定的时间
     *
     * @param millisecond 等待的时间（毫秒）
     * @throws InterruptedException
     */
    public void waitFull(long millisecond) throws InterruptedException {
        lock.lock();
        try {
            while (!isFull()&RUN) {
                fullCondition.await(millisecond, TimeUnit.MILLISECONDS);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取队列的大小
     *
     * @return
     */
    public int getSize() {
        return map.size();
    }

    public boolean isRun() {
        return RUN;
    }

    /**
     * 从队列中移除对象
     *
     * @param key 要移除的对象的键
     * @return 被移除的对象的值，如果键不存在则返回null
     */
    public V remove(K key) {
        lock.lock();
        try {
            if (!map.containsKey(key))
                return null;
            size_get++;
            return map.remove(key);
        } finally {
            if (map.isEmpty()) {
                emptyCondition.signalAll();
            }
            putCondition.signal();
            lock.unlock();
        }
    }

    /**
     * 判断队列中是否包含指定的键
     *
     * @param key 要判断的键
     * @return 如果队列中包含指定的键，则返回true；否则返回false
     */
    public boolean containsKey(K key) {
        lock.lock();
        try {
            return map.containsKey(key);
        } finally {
            lock.unlock();
        }
    }

    public void close() {
        RUN = false;
        lock.lock();
        putCondition.signalAll();
        getCondition.signalAll();
        fullCondition.signalAll();
        emptyCondition.signalAll();
        lock.unlock();
    }

    public String toString() {
        return "MapPool@" + x + " " + getSize() + " get:" + size_get + " put:" + size_put;
    }
}