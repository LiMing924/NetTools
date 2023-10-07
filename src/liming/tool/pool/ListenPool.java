package liming.tool.pool;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 异步监听池，当池中放入数据时会异步将数据传递给handle方法
 */
public abstract class ListenPool<T> extends Listen<T>{
    private ScheduledExecutorService executorService;

    private long outTime;

    public ListenPool(ListPool<T> listPool, long milliseconds, long outTime) {
        super(listPool);
        if(listPool.listen!=null){
            Pools.close();
            throw new RuntimeException(listPool+" Listening already exists 。"+listPool.listen);
        }
        this.listPool = listPool;
        try {
            listPool.add(this);
        }catch (RuntimeException e){
            throw e;
        }
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::listen, 0, Math.max(1,milliseconds), TimeUnit.MILLISECONDS);
        this.outTime = outTime;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 3) {
            StackTraceElement caller = stackTrace[2];
            path = caller.getClassName() + " " + caller.getMethodName() + " at " + caller.getLineNumber();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ListenPool && listPool.equals(((ListenPool<?>) obj).listPool);
    }

    @Override
    public int hashCode() {
        return listPool.hashCode();
    }

    @Override
    public String toString() {
        return "ListenPool ["+listPool+"] "+path;
    }

    public void put(T t) {
        put(t, outTime);
    }

    public void put(T t, long outTime) {
        listPool.putWait(t, outTime);
    }

    public void close() {
        executorService.shutdownNow();
    }

    @Override
    public void onStop(List<T> list){
        for (T t : list) {
            try {
                handle(t);
            } catch (Exception e) {
                log.put(e.getMessage());
            }
        }
    }
}
