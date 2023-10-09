package liming.tool.handle.tcp;

import liming.tool.handle.root.RootReceiveMap;
import liming.tool.handle.root.base.Client;

public interface TcpClient extends Client,TransformTcp {
    @Override
    default boolean doWork(RootReceiveMap respond) throws Exception{
        return doWork((TcpReceiveMap)transform(respond));
    };
    /**
     * 执行客户端的处理逻辑
     * @param respond 来自服务器的响应
     * @return 是否在结束后保留连接
     */
    boolean doWork(TcpReceiveMap respond) throws Exception;

    @Override
    default RootReceiveMap write(RootReceiveMap rootReceiveMap) throws Exception{
        return write((TcpReceiveMap)transform(rootReceiveMap));
    };

    /**
     * 实现发送与接收功能，将数据发送，并返回对方接收处理后回发的结果
     * @param tcpReceiveMap 携带的数据
     * @return 对方处理的结果
     * @throws Exception 允许所有异常
     */
    TcpReceiveMap write(TcpReceiveMap tcpReceiveMap) throws Exception;

    @Override
    default RootReceiveMap addReceiveMap(RootReceiveMap rootReceiveMap) throws Exception{
        return addReceiveMap((TcpReceiveMap) transform(rootReceiveMap));
    };
    /**
     * 向Socket中通过本地提交数据
     * @param tcpReceiveMap 需要提交的数据
     * @return 处理后的结果
     */
    TcpReceiveMap addReceiveMap(TcpReceiveMap tcpReceiveMap)throws Exception;
}
