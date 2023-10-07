package liming.tool.handle.udp;

import liming.tool.handle.receive.Server;
import liming.tool.handle.receive.RootReceiveMap;

public interface UdpServer extends Server, TransformUdp{
    default boolean doWork(RootReceiveMap request, RootReceiveMap respond) throws Exception{
        return doWork((UdpReceiveMap) transform(request),(UdpReceiveMap)transform(respond));
    };
    boolean doWork(UdpReceiveMap request, UdpReceiveMap respond) throws Exception;
    @Override
    default RootReceiveMap addReceiveMap(RootReceiveMap rootReceiveMap) throws Exception{
        return addReceiveMap((UdpReceiveMap) transform(rootReceiveMap));
    };
    /**
     * 向Socket中通过本地提交数据
     * @param udpReceiveMap 需要提交的数据
     * @return 处理后的结果
     */
    UdpReceiveMap addReceiveMap(UdpReceiveMap udpReceiveMap)throws Exception;
}
