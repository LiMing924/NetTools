package liming.tool.handle.receive;

/**
 * 定义服务端方法接口
 */
public interface Server extends BaseWork{
    /**
     * 执行服务端的处理逻辑
     * @param request 客户端的请求
     * @param respond 服务端的响应
     * @return 是否有转发，如果在调用过程中，需要跳转的其他的节点则将该返回值为true
     */
    boolean doWork(RootReceiveMap request,RootReceiveMap respond) throws Exception;
}
