package actions;

import base.InputController;

public class MouseLeftClickAction extends MouseAction{

    public MouseLeftClickAction(InputController mouse, Point[] bounds, int wait_time, String name) {
        super(mouse, bounds, wait_time);
        this.name = name;
    }

    public MouseLeftClickAction(InputController mouse, Point center, int size, int wait_time, String name) {
        super(mouse, center, size, wait_time);
        this.name = name;
    }

    public void execute() throws InterruptedException {
        mouse.move(Point.get_random_point(bounds));
        mouse.left_click();
    }

}
