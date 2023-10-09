package liming.tool.handle.tcp;

import liming.tool.handle.root.RootReceiveMap;
import liming.tool.handle.root.base.Server;

public interface TcpServer extends Server,TransformTcp {
    @Override
    default boolean doWork(RootReceiveMap request, RootReceiveMap respond) throws Exception{
        return doWork((TcpReceiveMap)transform(request),(TcpReceiveMap)transform(respond));
    }
    /**
     * 执行服务端的处理逻辑
     * @param request 客户端的请求
     * @param respond 服务端的响应
     * @return 是否有转发，如果在调用过程中，需要跳转的其他的节点则将该返回值为true
     */
    boolean doWork(TcpReceiveMap request, TcpReceiveMap respond) throws Exception;

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
