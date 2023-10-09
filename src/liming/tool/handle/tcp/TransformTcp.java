package liming.tool.handle.tcp;

import liming.tool.handle.root.RootReceiveMap;
import liming.tool.handle.root.base.Transform;

public interface TransformTcp extends Transform {
    @Override
    default RootReceiveMap transform(RootReceiveMap rootReceiveMap){
        return transformTcp(rootReceiveMap);
    }

    default TcpReceiveMap transformTcp(RootReceiveMap rootReceiveMap){
        if(rootReceiveMap instanceof TcpReceiveMap) return (TcpReceiveMap) rootReceiveMap;
        else return new TcpReceiveMap(rootReceiveMap);
    }
}
