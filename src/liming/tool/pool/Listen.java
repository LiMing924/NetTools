package liming.tool.pool;

import liming.tool.handle.FileRW;

import java.util.List;

abstract class Listen<T> {
    protected static ListPool<Object> log=Pools.getLogPool();
    protected ListPool<T> listPool;

    protected boolean Run = true;
    protected String path;

    protected String name;


    public Listen(ListPool<T> listPool){
        this.listPool=listPool;
        name=listPool.getName();
    }

    public void listen(){
        do {
            T t = listPool.getWait(0);
            if (!listPool.isRun()){
                Run = false;
                break;
            }
            try {
                if(t!=null)
                    handle(t);
            }catch (Exception e){
                log.put(""+listPool+" 发生异常:\n"+ FileRW.getError(e));
            }
        } while (Run&&!listPool.isEmpty());
    }

    public abstract void handle(T t) throws Exception;

    public abstract void onStop(List<T> list);

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public ListPool<T> getListPool() {
        return listPool;
    }

    public abstract void close();
}
