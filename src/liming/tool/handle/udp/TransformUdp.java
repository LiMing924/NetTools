package liming.tool.handle.udp;

import liming.tool.handle.receive.RootReceiveMap;
import liming.tool.handle.receive.Transform;

public interface TransformUdp extends Transform {
    @Override
    default RootReceiveMap transform(RootReceiveMap rootReceiveMap){
        return transformUdp(rootReceiveMap);
    }

    default UdpReceiveMap transformUdp(RootReceiveMap rootReceiveMap){
        if(rootReceiveMap instanceof UdpReceiveMap) return (UdpReceiveMap) rootReceiveMap;
        else return new UdpReceiveMap(rootReceiveMap);
    }
}
