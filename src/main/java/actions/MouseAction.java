package actions;

import java.util.Arrays;

public abstract class MouseAction extends Action{

    MouseController mouse;

    public MouseAction(MouseController mouse, Point[] bounds, int wait_time) {
        this.bounds = bounds;
        this.wait_time = wait_time;
        this.mouse = mouse;
    }

    public MouseAction(MouseController mouse, Point center, int size, int wait_time) {
        this.wait_time = wait_time;
        bounds = new Point[]{
                new Point(center.x - size, center.y - size),
                new Point(center.x + size, center.y - size),
                new Point(center.x + size, center.y + size),
                new Point(center.x - size, center.y - size)
        };
        this.mouse = mouse;
    }
}
