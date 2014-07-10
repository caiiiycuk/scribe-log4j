package name.caiiiycuk.scribe;

import org.apache.log4j.Appender;
import scribe.thrift.LogEntry;

/**
 * Simple provider interface where clients can provide local store forwarding capabilities. This may be needed when
 * the scribe server is not installed locally on the app server running as an agent.
 *
 * The implementation of this class must have a default no arg constructor.
 * The putLogEntry method will be called when the scribe connection has been lost.
 * The getLogEntry method will be called when the scribe connection is established again. This method will be called
 * until null is returned. This method should get all log entries which have been stored locally
 * while waiting for connection to establish again.
 */
public interface ILocalStoreForward {

    /**
     *
     * @param logEntry
     */
    public void putLogEntry (LogEntry logEntry);

    /**
     *
     * @return
     */
    public LogEntry getLogEntry();

    public void setAppender(Appender appender);
}
