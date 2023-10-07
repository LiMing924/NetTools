package liming.tool.handle.receive;

/**
 * 定义客户端方法接口
 */
public interface Client extends BaseWork{
    /**
     * 执行客户端的处理逻辑
     * @param respond 来自服务器的响应
     * @return 是否在结束后保留连接
     */
    boolean doWork(RootReceiveMap respond) throws Exception;
    /**
     * 实现发送与接收功能，将数据发送，并返回对方接收处理后回发的结果
     * @param rootReceiveMap 携带的数据
     * @return 对方处理的结果
     * @throws Exception 允许所有异常
     */
    RootReceiveMap write(RootReceiveMap rootReceiveMap) throws Exception;
}
