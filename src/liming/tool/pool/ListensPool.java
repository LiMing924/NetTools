package liming.tool.pool;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class ListensPool<T> extends Listen<T>{
    private ScheduledExecutorService executorService;
    private String path;

    long outTime;
    public ListensPool(ListPool<T> listPool,long milliseconds, long outTime,int size){
        super(listPool);
        if(size<2){
            throw new RuntimeException("The number of licenses is less than 2");
        }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 3) {
            StackTraceElement caller = stackTrace[2];
            path = caller.getClassName() + " " + caller.getMethodName() + " at " + caller.getLineNumber();
        }

        try {
            listPool.add(this);
        }catch (RuntimeException e){
            throw e;
        }
        this.outTime=outTime;
        executorService=Executors.newScheduledThreadPool(size);
        for(int i=0;i<size;i++){
            executorService.scheduleAtFixedRate(this::listen,0,Math.max(milliseconds,1),TimeUnit.MILLISECONDS);
        }
    }

    public void put(T t){
        try {
            listPool.put(t);
        }catch (RuntimeException e){
            log.put(e);
        }
    }

    public String getPath() {
        return path;
    }

    @Override
    public void close(){
        executorService.shutdownNow();
    }

    @Override
    public void onStop(List<T> list){return;}
}
