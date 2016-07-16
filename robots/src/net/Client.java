package net;

import logic.Model;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Observable;

public class Client extends Observable implements Closeable {
    private Socket socket;
    private Model model;

    public Client(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        model = null;
    }

    public void getObjectFromServer() {
        ObjectInputStream in;
        try {
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            setChanged();
            notifyObservers();
            return;
        }
        Object obj;
        try {
            obj = in.readObject();
        } catch (Exception e) {
            setChanged();
            notifyObservers();
            return;
        }
        if (obj instanceof Model)
            model = (Model)obj;
        setChanged();
        notifyObservers();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    public Model getModel() {
        return model;
    }
}
