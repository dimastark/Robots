package logic;

import java.awt.*;
import java.io.Serializable;

public class Robot implements Serializable {
    private volatile double x = 100;
    private volatile double y = 100;
    private final int width;
    private final int height;
    private volatile double direction = 0;

    public Robot(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void moveRobot(double vel, double angVel, double dur) {
        x = x + vel * dur * Math.cos(direction);
        y = y + vel * dur * Math.sin(direction);
        direction -= angVel * dur;
    }

    public Point getPosition() {
        return new Point((int)(x + 0.5), (int)(y + 0.5));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getMaxVelocity() {
        return 0.1;
    }

    public double getMaxAngularVelocity() {
        return 0.001;
    }

    public double getDirection() {
        return direction;
    }

    public Rectangle getRectangle() {
        int r_x = (int)(x + 0.5);
        int r_y = (int)(y + 0.5);
        return new Rectangle(r_x, r_y, width, height);
    }
}
