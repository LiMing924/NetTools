package liming.tool.pool;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 对ListPool，MapPool 进行调度
 */
public class Pools {
    private final static Map<String, Object> pools;
    private final static Map<String, List<LogPool>> log;
    private final static Lock lock;

    static {
        pools = new HashMap<>();
        log = new HashMap<>();
        lock = new ReentrantLock();
    }

    @SuppressWarnings("all")
    public static <T> ListPool<T> getListPool(String name) {
        lock.lock();
        name += "&list";
        ListPool<T> listPool;
        try {
            if (pools.containsKey(name) && pools.get(name) instanceof ListPool) {
                listPool = (ListPool<T>) pools.get(name);
            } else {
                listPool = new ListPool<>();
                listPool.setName(name);
                pools.put(name, listPool);
            }
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace.length >= 3) {
                StackTraceElement caller = stackTrace[2];
                String path = caller.getClassName() + " " + caller.getMethodName() + " at " + caller.getLineNumber();
                String str = name + " " + path;
                if (log.containsKey(name)) {
                    log.get(name).add(new LogPool(name,listPool,path));
                } else {
                    List<LogPool> list = new ArrayList<>();
                    list.add(new LogPool(name,listPool,path));
                    log.put(name, list);
                }
            }
            return listPool;
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("all")
    public static <T> ListPool<T> getListPool(String name, int size) {
        lock.lock();
        name += "&list";
        try {
            ListPool<T> listPool;
            if (pools.containsKey(name) && pools.get(name) instanceof ListPool) {
                listPool = (ListPool<T>) pools.get(name);
            } else {
                listPool = new ListPool<>(size);
                listPool.setName(name);
                pools.put(name, listPool);
            }
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace.length >= 3) {
                StackTraceElement caller = stackTrace[2];
                String path = caller.getClassName() + " " + caller.getMethodName() + " at " + caller.getLineNumber();
                String str = name + " " + path;
                if (log.containsKey(name)) {
                    log.get(name).add(new LogPool(name,listPool,path));
                } else {
                    List<LogPool> list = new ArrayList<>();
                    list.add(new LogPool(name,listPool,path));
                    log.put(name, list);
                }
            }
            return listPool;
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("all")
    public static <K, V> MapPool<K, V> getMapPool(String name) {
        name += "&map";
        lock.lock();
        try {
            if (pools.containsKey(name) && pools.get(name) instanceof MapPool) {
                return (MapPool<K, V>) pools.get(name);
            } else {
                MapPool<K, V> mapPool = new MapPool<>();
                pools.put(name, mapPool);
                return mapPool;
            }
        } finally {
            lock.unlock();
        }
    }

    public static class LogPool{
        String name;
        ListPool<?> listPool;

        String path;

        LogPool(String name,ListPool<?> listPool,String path){
            this.name=name;
            this.listPool=listPool;
            this.path=path;
        }

        @Override
        public String toString() {
            return "LogPool{" +
                    "name='" + name + '\'' +
                    ", listPool=" + listPool +
                    ", path='" + path + '\'' +
                    '}';
        }
    }

    /**
     * 关闭所有功能池
     */
    public static void close() {
        pools.forEach((key, object) -> {
            if (object instanceof ListPool) {
                ((ListPool<?>) object).close();
            } else if (object instanceof MapPool) {
                ((MapPool<?, ?>) object).close();
            }
        });
        pools.clear();
    }

    public synchronized static ListPool<Object> getLogPool(){
        return getListPool("System.liming.tool.log");
    }
    public synchronized static ListPool<Object> getSlogPool(){
        return getListPool("System.liming.tool.slog");
    }

    public static Set<String> getPoos() {
        return pools.keySet();
    }

    public static Map<String, List<LogPool>> getLog() {
        return log;
    }
}
