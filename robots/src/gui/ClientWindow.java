package gui;

import logic.Model;
import net.Client;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class ClientWindow extends JInternalFrame implements Observer {
    private final TextArea ip = new TextArea("IP", 1, 1, TextArea.SCROLLBARS_NONE);
    private Client client;
    private final GameWindow view;

    public ClientWindow(GameWindow view) {
        super("Get model from remote server", true, true, true, true);
        JPanel jp = new JPanel(new GridLayout(3, 1));
        jp.add(ip);
        this.view = view;
        Button but = new Button("Get model");
        but.setSize(200, 50);
        but.addActionListener((event) -> {
            getObjectFromServer();
        });
        jp.add(but);
        getContentPane().add(jp);
        setSize(200, 100);
        setPreferredSize(new Dimension(200, 100));
        pack();
    }

    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof Client)
            view.changeVisualizer(client.getModel());
        try {
            setClosed(true);
        } catch (Exception e) {}
    }

    private void getObjectFromServer() {
        try {
            client = new Client(ip.getText(), 12345);
            client.addObserver(this);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Wrong input");
        }
        client.getObjectFromServer();
    }
}
