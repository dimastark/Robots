package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.*;

import log.LogEntry;
import log.LogLevel;
import log.Logger;

public class MainApplicationFrame extends JFrame {
    private JDesktopPane desktopPane = new JDesktopPane();

    public MainApplicationFrame() {
        int inset = 50;        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width  - inset * 2, screenSize.height - inset * 2);
        setContentPane(desktopPane);
        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                super.windowClosing(windowEvent);
                String ObjButtons[] = {"Да", "Нет"};
                int PromptResult = JOptionPane.showOptionDialog(null,
                        "Вы уверены?",
                        null,
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        ObjButtons,
                        ObjButtons[1]);
                if(PromptResult==JOptionPane.YES_OPTION)
                    onClosing();
            }

            @Override
            public void windowOpened(WindowEvent windowEvent) {
                super.windowOpened(windowEvent);
                try {
                    onOpen();
                }
                catch (PropertyVetoException e)
                {
                    System.out.println("Error with privileges");
                }
            }
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    public void setState(FrameState state) throws PropertyVetoException {
        String description = "public class gui.";
        for (FrameState.InternalFrameState istate: state.getAllStates()) {
            if (istate.getTag().equals(description + "GameWindow")) {
                GameWindow game = new GameWindow(0, 0, this);
                addWindow(game);
                game.setState((FrameState.InternalFrameState<GameWindow>)istate);
            }
            else if (istate.getTag().equals(description + "LogWindow")) {
                LogWindow log = createNewLogWindow();
                addWindow(log);
                log.setState((FrameState.InternalFrameState<LogWindow>)istate);
            }
        }
    }

    private void onOpen() throws PropertyVetoException {
        ObjectInputStream ois = getStateFile();
        FrameState state = null;
        if (ois != null)
            try {
                state = (FrameState)ois.readObject();
            } catch (IOException e) {
                System.out.println("Can't load state. Load defaults");
            } catch (ClassNotFoundException e) {
                System.out.println("Can't load state. Load defaults");
            }
        if (state != null)
            setState(state);
    }

    public static ObjectInputStream getStateFile() {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream("save.state"));
        }
        catch (IOException ex) {
            return null;
        }
        return ois;
    }

    private void onClosing() {
        ObjectOutputStream oos = createStateStream();
        if (oos != null) {
            try {
                FrameState state = new FrameState(this);
                oos.writeObject(state);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Can't serialize object, not serializable error");
            }
            try {
                oos.flush();
                oos.close();
            } catch (IOException ex) {
                System.out.println("Can't flush and close");
            }
        }
        System.exit(0);
    }
    // <editor-fold desc="finished">

    private ObjectOutputStream createStateStream() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream("save.state"));
        }
        catch (IOException ex) {
            File stFile = new File("save.state");
            try {
                stFile.createNewFile();
                oos = new ObjectOutputStream(new FileOutputStream("save.state"));
            }
            catch (IOException e) {
                return null;
            }
        }
        return oos;
    }

    protected LogWindow createNewLogWindow() {
        Logger.getDefaultLogSource().releaseListeners();
        Logger.clear();
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10, 10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Protocol started");
        return logWindow;
    }

    public JInternalFrame[] getAllFrames() {
        return desktopPane.getAllFrames();
    }
    
    public void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    private JMenuBar generateMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(documentItem());
        menuBar.add(lookAndFeelItem());
        menuBar.add(testItem());
        return menuBar;
    }

    private JMenu documentItem() {
        JMenu menu = new JMenu("Document");
        menu.setMnemonic(KeyEvent.VK_D);
        menu.add(newDoc());
        menu.add(newGame());
        menu.add(quitApp());
        return menu;
    }

    private JMenuItem newDoc() {
        JMenuItem menuItem = new JMenuItem("New log");
        menuItem.setMnemonic(KeyEvent.VK_L);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
        menuItem.addActionListener(actionEvent -> addWindow(createNewLogWindow()));
        return menuItem;
    }

    private JMenuItem newGame() {
        JMenuItem menuItem = new JMenuItem("New game");
        menuItem.setMnemonic(KeyEvent.VK_G);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.ALT_MASK));
        menuItem.addActionListener(actionEvent -> addWindow(new GameWindow(400, 400, this)));
        return menuItem;
    }

    private JMenuItem quitApp() {
        JMenuItem menuItem = new JMenuItem("Quit");
        menuItem.setMnemonic(KeyEvent.VK_Q);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Q, ActionEvent.ALT_MASK));
        menuItem.setActionCommand("quit");
        menuItem.addActionListener(actionEvent ->
                Toolkit .getDefaultToolkit()
                        .getSystemEventQueue()
                        .postEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        return menuItem;
    }

    private JMenu testItem() {
        JMenu testMenu = new JMenu("For Tests");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription("test commands");
        JMenuItem addLogMessageItem = messageToLogAction();
        testMenu.add(addLogMessageItem);
        return testMenu;
    }

    private JMenuItem messageToLogAction() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        JMenuItem addLogMessageItem = new JMenuItem("Message to log", KeyEvent.VK_S);
        addLogMessageItem.addActionListener((event) -> {
            Logger.debug(String.format("Current time: %s", sdf.format(cal.getTime())));
        });
        return addLogMessageItem;
    }

    private JMenu lookAndFeelItem() {
        JMenu lookAndFeelMenu = new JMenu("Look and Feel");
        String prev = "Change view mode";
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(prev);
        lookAndFeelMenu.add(systemLook());
        lookAndFeelMenu.add(universalLook());
        lookAndFeelMenu.add(nimbusLook());
        lookAndFeelMenu.add(metalLook());
        return lookAndFeelMenu;
    }

    private JMenuItem systemLook() {
        JMenuItem systemLook = new JMenuItem("System", KeyEvent.VK_S);
        systemLook.addActionListener((event) -> {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            this.invalidate(); });
        return systemLook;
    }

    private JMenuItem universalLook() {
        JMenuItem crossplatformLook = new JMenuItem("Universal", KeyEvent.VK_U);
        crossplatformLook.addActionListener((event) -> {
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            this.invalidate(); });
        return crossplatformLook;
    }

    private JMenuItem nimbusLook() {
        JMenuItem crossplatformLook = new JMenuItem("Nimbus", KeyEvent.VK_N);
        crossplatformLook.addActionListener((event) -> {
            setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            this.invalidate(); });
        return crossplatformLook;
    }

    private JMenuItem metalLook() {
        JMenuItem crossplatformLook = new JMenuItem("Metal", KeyEvent.VK_M);
        crossplatformLook.addActionListener((event) -> {
            setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            this.invalidate(); });
        return crossplatformLook;
    }

    private void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
            | IllegalAccessException | UnsupportedLookAndFeelException e) {
            String msg = String.format("<{}> - unsupported look, select other",  className);
            Logger.getDefaultLogSource().addMessage(new LogEntry(LogLevel.Warning, msg));
            throw new IllegalArgumentException("Wrong look");
        }
    }
    // </editor-fold>
}
