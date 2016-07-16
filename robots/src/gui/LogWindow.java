package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.TextArea;
import java.beans.PropertyVetoException;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import log.LogChangeListener;
import log.LogEntry;
import log.LogWindowSource;

public class LogWindow extends JInternalFrame implements LogChangeListener {
    private LogWindowSource logSource;
    private TextArea logContent;

    public LogWindow(LogWindowSource logSource) {
        super("Протокол работы", true, true, true, true);
        this.logSource = logSource;
        this.logSource.registerListener(this);
        logContent = new TextArea();
        logContent.setEnabled(false);
        logContent.setSize(200, 500);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(logContent, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        updateLogContent();
    }

    public LogWindow(LogWindowSource source, String title) {
        super(title, true, true, true, true);
        logSource = source;
        logSource.registerListener(this);
        logContent = new TextArea();
        logContent.setEnabled(false);
        logContent.setSize(200, 500);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(logContent, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        updateLogContent();
    }

    private void updateLogContent() {
        StringBuilder content = new StringBuilder();
        for (LogEntry entry : logSource.all())
            content.append(entry.getMessage()).append("\n");
        logContent.setText(content.toString());
        logContent.invalidate();
    }

    public Iterable<LogEntry> getContent() {
        return logSource.all();
    }

    public void setState(FrameState.InternalFrameState<LogWindow> state) throws PropertyVetoException {
        if (state.getInformation() instanceof Iterable)
            for (LogEntry entry : (Iterable<LogEntry>)state.getInformation())
                if (!entry.getMessage().equals("Protocol started"))
                    logSource.addMessage(entry);
        if (state.params.get("isMax") == 1) setMaximum(true);
        else setSize(state.params.get("width"), state.params.get("height"));
        setLocation(state.params.get("x"), state.params.get("y"));
        if (state.params.get("inFocus") == 1) setRequestFocusEnabled(true);
        if (state.params.get("isClosed") == 1) setClosed(true);
        onLogChanged();
    }
    
    @Override
    public void onLogChanged() {
        EventQueue.invokeLater(this::updateLogContent);
    }
}
