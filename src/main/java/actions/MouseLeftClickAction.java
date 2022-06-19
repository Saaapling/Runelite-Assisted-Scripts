package actions;

import java.awt.*;
import java.awt.event.InputEvent;

public class MouseLeftClickAction extends MouseAction{

    public MouseLeftClickAction(MouseController mouse, Point[] bounds, int wait_time, String name) {
        super(mouse, bounds, wait_time);
        this.name = name;
    }

    public MouseLeftClickAction(MouseController mouse, Point center, int size, int wait_time, String name) {
        super(mouse, center, size, wait_time);
        this.name = name;
    }

    public void execute() throws InterruptedException, AWTException {
        mouse.move(Point.get_random_point(bounds));
        Thread.sleep((long) (300 + Math.random() * 15));

        Robot robot = new Robot();
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep((long) (100 + Math.random() * 10));
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

}
