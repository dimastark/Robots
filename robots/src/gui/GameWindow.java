package gui;

import logic.Model;
import logic.Robot;
import net.Server;

import java.awt.*;
import java.beans.PropertyVetoException;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

class GameWindow extends JInternalFrame {
    private GameVisualizer visualizer;
    private MainApplicationFrame view;
    private DefaultListModel<Robot> robotsList;
    private StateWindow stateView = null;

    GameWindow(int width, int height, MainApplicationFrame view) {
        super("Game field", true, true, true, true);
        this.view = view;
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent internalFrameEvent) {
                if (visualizer != null)
                    visualizer.stop();
                if (stateView != null)
                    try {
                        stateView.setClosed(true);
                    } catch (PropertyVetoException e) {
                        e.printStackTrace();
                    }
            }
        });
        Model model = new Model(new Robot(30, 10));
        visualizer = new GameVisualizer(model);
        restructPane(visualizer);
        setSize(width, height);
    }

    private JMenuBar generateMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(stateItem());
        menuBar.add(randObstItem());
        menuBar.add(robotItem());
        menuBar.add(modelItem());
        return menuBar;
    }

    private void restructPane(GameVisualizer viser) {
        getContentPane().removeAll();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(viser, BorderLayout.CENTER);
        robotsList = new DefaultListModel<>();
        for (Robot rob : getModel().getRobots())
            robotsList.add(0, rob);
        JList<Robot> list = new JList<>(robotsList);
        list.addListSelectionListener(event -> {
            int index = list.getSelectedIndex();
            if (index != -1)
                viser.getModel().selectRobot(index);
        });
        panel.add(list, BorderLayout.EAST);
        getContentPane().add(panel);
        setJMenuBar(generateMenuBar());
        pack();
    }

    private JMenu stateItem() {
        JMenu menu = new JMenu("Debug");
        JMenuItem menuItem = new JMenuItem("New state");
        menuItem.addActionListener(actionEvent -> {
            if (stateView == null) {
                stateView = new StateWindow(getModel());
                view.addWindow(stateView);
            }
        });
        menu.add(menuItem);
        return menu;
    }

    private JMenu randObstItem() {
        JMenu menu = new JMenu("Obstacle");
        JMenuItem menuItem = new JMenuItem("New obstacle");
        menuItem.addActionListener(actionEvent -> visualizer.mouse = true);
        menu.add(menuItem);
        return menu;
    }

    private JMenu robotItem() {
        JMenu menu = new JMenu("Robot");
        JMenuItem menuItemD = new JMenuItem("New robot");
        JMenuItem menuItemR = new JMenuItem("Remove last");
        menuItemD.addActionListener(actionEvent -> {
            Robot rob = new Robot(30, 10);
            robotsList.addElement(rob);
            getModel().addRobot(rob);
        });
        menuItemR.addActionListener(actionEvent -> {
            robotsList.removeElementAt(robotsList.getSize() - 1);
            getModel().removeRobot();
        });
        menu.add(menuItemD);
        menu.add(menuItemR);
        return menu;
    }

    private JMenu modelItem() {
        JMenu menu = new JMenu("Model");
        JMenuItem menuItemS = new JMenuItem("Share");
        JMenuItem menuItemD = new JMenuItem("Download");
        menuItemS.addActionListener(actionEvent -> {
            try {
                Server s = new Server(getModel(), 12345);
                s.start();
                JOptionPane.showMessageDialog(this, "Send");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Something wr1ong");
            }
        });
        menuItemD.addActionListener(actionEvent -> {
            view.addWindow(new ClientWindow(this));
        });
        menu.add(menuItemS);
        menu.add(menuItemD);
        return menu;
    }

    void setState(FrameState.InternalFrameState<GameWindow> state) throws PropertyVetoException {
        Model model = (Model) state.getInformation();
        changeVisualizer(model);
        if (state.params != null) {
            if (state.params.get("isMax") == 1) setMaximum(true);
            else setSize(state.params.get("width"), state.params.get("height"));
            setLocation(state.params.get("x"), state.params.get("y"));
            if (state.params.get("inFocus") == 1) setRequestFocusEnabled(true);
            if (state.params.get("isClosed") == 1) setClosed(true);
        }
    }

    public void changeVisualizer(Model model) {
        Dimension save = getSize();
        visualizer.stop();
        visualizer = new GameVisualizer(model);
        restructPane(visualizer);
        setSize(save);
    }

    Model getModel() {
        return visualizer.getModel();
    }
}
