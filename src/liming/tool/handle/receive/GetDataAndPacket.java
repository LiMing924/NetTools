package liming.tool.handle.receive;

public interface GetDataAndPacket{
    String LOG_STRING="LOG: ";
    String STRING_LOG_STRING="STRING_LOG: ";
    /**
     * 写入日志信息
     *
     * @param message 日志信息
     */
    default void writeLog(Object message) {
        System.out.println(LOG_STRING+message);
    };
    /**
     * 写入强调的日志信息
     *
     * @param message 强调的日志信息
     */
    default void writeStrongLog(Object message) {
        System.out.println(STRING_LOG_STRING+message);
    };
}
