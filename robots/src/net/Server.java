package net;

import log.LogEntry;
import log.LogLevel;
import log.LogWindowSource;
import logic.Model;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Closeable {
    private ServerSocket socket;
    private Model model;
    private LogWindowSource log;

    public Server(Model model, int port) throws IOException {
        socket = new ServerSocket(port);
        this.model = model;
        log = new LogWindowSource(10);
    }

    public void start() {
        Socket conn = null;
        try {
            conn = socket.accept();
        } catch (IOException exc) {
            log.addMessage(new LogEntry(LogLevel.Error, "Can't establish connection"));
        }
        if (conn != null) {
            log.addMessage(new LogEntry(LogLevel.Info, "Client connected"));
            serve(conn);
        }
    }

    private void serve(Socket sock) {
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(sock.getOutputStream());
        } catch (IOException exc) {
            log.addMessage(new LogEntry(LogLevel.Error, "Can't get out of client"));
            return;
        }
        try {
            out.writeObject(model);
        } catch (IOException exc) {
            log.addMessage(new LogEntry(LogLevel.Error, "Can't send model to client"));
        }
    }

    public LogWindowSource getLog() {
        return log;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
