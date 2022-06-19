package actions;

import java.awt.*;
import java.awt.event.InputEvent;

public class WithdrawAction extends Action{

    int base_x = 662;
    int base_y = 137;
    int base_size = 12;
    int amount = 1;
    MouseController mouse;
    Point center;

    public WithdrawAction(MouseController mouse, int row, int col, int amount, int wait_time, String name) {
        this.mouse = mouse;
        this.wait_time = wait_time;
        this.amount = amount;
        this.name = name;

        center = new Point(base_x + (col - 1) * 48, base_y + (row - 1) * 36);
    }

    public WithdrawAction(MouseController mouse, int row, int col, int wait_time, String name) {
        this.mouse = mouse;
        this.wait_time = wait_time;
        this.name = name;

        center = new Point(base_x + (col - 1) * 48, base_y + (row - 1) * 36);
    }

    public void execute() throws InterruptedException, AWTException {
        // MouseController mouse, Point center, int size, int wait_time, String name
        Point[] bounds = {
                center.add(new Point(-12, -12)),
                center.add(new Point(12, -12)),
                center.add(new Point(12, 12)),
                center.add(new Point(-12, 12))
        };
        Point item_coords = Point.get_random_point(bounds);
        Action move_action = new MouseMoveAction(mouse, item_coords, 0, 500);
        move_action.execute();

        int rand_sleep = 200 + (int) (Math.random() * 20);
        Thread.sleep(rand_sleep);

        Robot robot = new Robot();
        for (int i = 0; i < amount; i++){
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep((long) (100 + Math.random() * 10));
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep((long) (100 + Math.random() * 10));
        }
    }
}
