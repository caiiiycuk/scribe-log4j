package name.caiiiycuk.scribe;

import scribe.thrift.LogEntry;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Created by IntelliJ IDEA.
 * User: mansari
 * Date: 4/12/11
 * Time: 9:24 AM
 * To change this template use File | Settings | File Templates.
 */

public class InMemoryQueueLocalStoreForward implements ILocalStoreForward {

    private static final int MAX_SIZE = 10000;

    private Queue<LogEntry> queue = new LinkedList<LogEntry>();

    @Override
    public synchronized void putLogEntry(LogEntry logEntry) {
        queue.add(logEntry);
        if (queue.size() > MAX_SIZE){
            queue.poll();
        }
    }

    @Override
    public synchronized LogEntry getLogEntry() {
        return queue.poll();
    }
}
