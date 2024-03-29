package liming.tool.handle.root.base;

import liming.tool.handle.root.RootReceiveMap;

public interface BaseWork {
    /**
     * 向Socket中通过本地提交数据
     *
     * @param rootReceiveMap 需要提交的数据
     * @return 处理后的结果
     */
    RootReceiveMap addReceiveMap(RootReceiveMap rootReceiveMap) throws Exception;
}
