package liming.tool.pool;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 队列池
 */
public class ListPool<T> {

    private static int X = 0;// 当前创建的实例个数
    private final int x;
    protected Listen<T> listen;
    private final int size;// 最大容量个数
    private long size_put = 0;
    private long size_get = 0;
    List<T> list;
    private Boolean RUN = true;
    private final Lock lock;
    private final Condition putCondition;// 放入
    private final Condition getCondition;// 取出
    private final Condition fullCondition;// 满
    private final Condition emptyCondition;// 空

    ListPool() {
        this(5000);
    }

    /**
     * 构造函数
     *
     * @param size 队列的最大容量
     */
    ListPool(int size) {
        this(size, new LinkedList<>());
    }

    /**
     * 构造函数
     *
     * @param size 队列的最大容量
     * @param list 初始队列
     */
    ListPool(int size, List<T> list) {
        x = X++;
        this.size = size;
        this.list = list;
        lock = new ReentrantLock();
        putCondition = lock.newCondition();
        getCondition = lock.newCondition();
        fullCondition = lock.newCondition();
        emptyCondition = lock.newCondition();
    }

    void add(Listen<T> listen) {
        lock.lock();
        try {
            if (this.listen == null) this.listen = listen;
            else
                throw new RuntimeException("There is already a listen, unable to register for the listen. path: " + this.listen.getPath());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 向队列中添加对象
     *
     * @param object 要添加的对象
     * @return 返回LinkPool对象本身，支持链式调用
     */
    public ListPool<T> put(T object) {
        lock.lock();
        boolean get = false;
        try {
            if (size() >= size)
                throw new RuntimeException("The current queue is full");
            get = true;
            list.add(object);
            size_put++;
        } finally {
            if (list.size() >= size)
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
     * @param object      要添加的对象
     * @param millisecond 等待的时间（毫秒）
     * @return 返回LinkPool对象本身，支持链式调用
     */
    public ListPool<T> putWait(T object, long millisecond) {
        long time = millisecond, endTime = System.currentTimeMillis() + millisecond;
        lock.lock();
        boolean get = false;
        try {
            while (list.size() >= size & RUN) {
                if (millisecond <= 0)
                    putCondition.await();
                else {
                    time = Math.max(endTime - System.currentTimeMillis(), 0);
                    putCondition.await(time, TimeUnit.MILLISECONDS);
                    if (endTime <= System.currentTimeMillis() && list.size() >= size)
                        throw new RuntimeException("Timed out " + millisecond + " milliseconds when inserting data");
                }
            }
            if (RUN) {
                get = true;
                list.add(object);
                size_put++;
                getCondition.signal();
            }else {
                list.add(object);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (list.size() >= size)
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
     * @return 队列中的第一个对象
     */
    public T get() {
        lock.lock();
        boolean put = false;
        try {
            if (list.isEmpty())
                throw new RuntimeException("The current queue is empty");
            put = true;
            size_get++;
            return list.remove(0);
        } finally {
            if (list.isEmpty()) {
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
     * @param millisecond 等待的时间（毫秒）
     * @return 队列中的第一个对象
     */
    public T getWait(long millisecond) {
        long time = millisecond, endTime = System.currentTimeMillis() + millisecond;

        lock.lock();
        if (!RUN&&list.isEmpty()){
            lock.unlock();
            return null;
        }
        boolean put = false;
        try {
            while (list.isEmpty() & RUN) {
                if (millisecond <= 0)
                    getCondition.await();
                else {
                    time = Math.max(endTime - System.currentTimeMillis(), 0);
                    getCondition.await(time, TimeUnit.MILLISECONDS);
                    if (endTime <= System.currentTimeMillis() && list.isEmpty())
                        throw new RuntimeException("Timed out " + millisecond + " milliseconds while retrieving data");
                }
            }
            if (RUN) {
                T object = list.remove(0);
                size_get++;
                put = true;
                putCondition.signal();
                return object;
            } else
                throw new RuntimeException("ListPool has ended");
        } catch (InterruptedException e) {
            return null;
        } finally {
            if (list.isEmpty()) {
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
            return list.isEmpty();
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
            return list.size() >= size;
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
            while (!isEmpty() & RUN) {
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
            while (!isEmpty() & RUN) {
                emptyCondition.await(Math.max(millisecond, 0), TimeUnit.MILLISECONDS);
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
            while (!isFull() & RUN) {
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
     */
    public void waitFull(long millisecond) throws InterruptedException {
        lock.lock();
        try {
            while (!isFull() & RUN) {
                fullCondition.await(Math.max(millisecond, 0), TimeUnit.MILLISECONDS);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取队列的大小
     *
     * @return 队列的大小
     */
    public int size() {
        return list.size();
    }

    public boolean isRun() {
        return RUN;
    }

    public void close() {
        lock.lock();
        if (listen != null){
            listen.close();
            listen.onStop(list);
        }else {
            list.clear();
        }
        RUN = false;
        putCondition.signalAll();
        getCondition.signalAll();
        fullCondition.signalAll();
        emptyCondition.signalAll();
        lock.unlock();
    }

    /**
     * 返回LinkPool对象的字符串表示形式
     *
     * @return LinkPool对象的字符串表示形式
     */
    public String toString() {
        lock.lock();
        try {
            return "ListPool@" + x + " " + size() + " get:" + size_get + " put:" + size_put + " listen:" + (listen != null ? listen.getPath() : "no");
        } finally {
            lock.unlock();
        }
    }
    private String name;
    void setName(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }
}