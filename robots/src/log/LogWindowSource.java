package log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class LogWindowSource {
    private int QueueLength;
    
    private LinkedList<LogEntry> messages;
    private final ArrayList<LogChangeListener> listeners;
    private volatile LogChangeListener[] activeListeners;

    public LogWindowSource(int QueueLength) {
        this.QueueLength = QueueLength;
        messages = new LinkedList<>();
        listeners = new ArrayList<>();
    }
    
    public void registerListener(LogChangeListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
            activeListeners = null;
        }
    }
    
    public void unregisterListener(LogChangeListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
            activeListeners = null;
        }
    }
    
    public void append(LogLevel logLevel, String strMessage) {
        LogEntry entry = new LogEntry(logLevel, strMessage);
        addMessage(entry);
        onUpdateListeners();
    }

    public void onUpdateListeners() {
        if (activeListeners == null)
            synchronized (listeners) {
                if (activeListeners == null)
                    activeListeners = listeners.toArray(new LogChangeListener[0]);
            }
        for (LogChangeListener listener : activeListeners)
            listener.onLogChanged();
    }

    public void addMessage(LogEntry entry) {
        if (size() >= QueueLength)
            messages.remove();
        messages.add(entry);
    }
    
    public int size() {
        return messages.size();
    }

    public Iterable<LogEntry> all() {
        return messages;
    }

    public void releaseListeners() {
        listeners.clear();
    }

    public void clear() {
        messages.clear();
    }
}
