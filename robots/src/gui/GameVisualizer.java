package gui;

import logic.*;
import logic.Robot;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;

import javax.swing.JPanel;

public class GameVisualizer extends JPanel {
    private Model model;
    private Timer timer;
    public boolean mouse;

    public GameVisualizer(Model model) {
        this.model = model;
        timer = new Timer("events generator", true);
        timer.schedule(new TimerTask() {
            public void run() {
                onRedrawEvent();
            }
        }, 0, 50);
        timer.schedule(new TimerTask() {
            public void run() {
                model.update();
            }
        }, 0, 1);
        addMouseListener(new MouseAdapter() {
            private Point first = null;
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!mouse) {
                    if (!model.inObstacle(e.getPoint())) {
                        model.setTargetPosition(e.getPoint());
                        repaint();
                    } }
                else
                    if (first != null) {
                        model.addObstacle(first, e.getPoint());
                        mouse = false;
                        first = null;
                    }
                    else
                        first = e.getPoint();
            }
        });
        setDoubleBuffered(true);
    }

    protected void onRedrawEvent() {
        EventQueue.invokeLater(this::repaint);
    }

    public Model getModel() {
        return model;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        for (int i = 0; i < model.getSize(); i++) {
            drawRobot(g2d, model.getRobot(i));
            drawTarget(g2d, model.getTarget(i));
        }
        //drawGraph(g, model.getGraph());
        drawObstacles(g2d, model.getObstacles());
    }

    private void drawGraph(Graphics g, HashMap<Point, ArrayList<Point>> p) {
        for (Point p1 : p.keySet())
            for (Point p2 : p.get(p1))
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
    }
    
    private static void fillOval(Graphics g, double centerX, double centerY, int diam1, int diam2) {
        g.fillOval((int)(centerX - diam1 / 2), (int)(centerY - diam2 / 2), diam1, diam2);
    }
    
    private static void drawOval(Graphics g, double centerX, double centerY, int diam1, int diam2) {
        g.drawOval((int)(centerX - diam1 / 2), (int)(centerY - diam2 / 2), diam1, diam2);
    }

    private static void fillRect(Graphics g, int x, int y, int diam1, int diam2) {
        g.fillRect(x, y, diam1, diam2);
    }

    private static void drawRect(Graphics g, int x, int y, int diam1, int diam2) {
        g.drawRect(x, y, diam1, diam2);
    }
    
    private void drawRobot(Graphics2D g, Robot robot) {
        int width = robot.getWidth();
        int height = robot.getHeight();
        int robotCenterX = (int)(robot.getX() + 0.5);
        int robotCenterY = (int)(robot.getY() + 0.5);
        AffineTransform t = AffineTransform.getRotateInstance(robot.getDirection(), robotCenterX, robotCenterY);
        g.setTransform(t);
        g.setColor(new Color(72, 124, 24));
        fillOval(g, robotCenterX, robotCenterY, width, height);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX, robotCenterY, width, height);
        fillOval(g, robotCenterX  + 10, robotCenterY, 5, 5);
    }
    
    private void drawTarget(Graphics2D g, Target target) {
        int radius = 5;
        AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0); 
        g.setTransform(t);
        g.setColor(Color.GREEN);
        fillOval(g, target.getX(), target.getY(), radius, radius);
        g.setColor(Color.BLACK);
        drawOval(g, target.getX(), target.getY(), radius, radius);
    }

    private void drawObstacles(Graphics2D g, List<Obstacle> obstacles) {
        for (Obstacle obst : obstacles) {
            int x = (int)(obst.getX() + 0.5);
            int y = (int)(obst.getY());
            int width = obst.getWidth();
            int height = obst.getHeight();
            drawRect(g, x, y, width, height);
            g.setColor(new Color(255, 115, 53));
            fillRect(g, x, y, width, height);
        }
    }

    public void stop() {
        timer.cancel();
        timer.purge();
    }
}
