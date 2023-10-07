package liming.tool.handle.receive;

/**
 * 定义自己的异常信息
 * 数据通信异常 Data Communication Exception
 */
public class DataCommunicationException extends Exception{
    public DataCommunicationException() {
    }

    public DataCommunicationException(String message) {
        super(message);
    }

    public DataCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataCommunicationException(Throwable cause) {
        super(cause);
    }

    public DataCommunicationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
