package actions;

import base.MouseController;

public class MouseRightClickAction extends MouseAction{

    public MouseRightClickAction(MouseController mouse, Point[] bounds, int wait_time, String name) {
        super(mouse, bounds, wait_time);
        this.name = name;
    }

    public MouseRightClickAction(MouseController mouse, Point center, int size, int wait_time, String name) {
        super(mouse, center, size, wait_time);
        this.name = name;
    }

    public void execute() throws InterruptedException {
        mouse.move(Point.get_random_point(bounds));
        mouse.right_click();
    }

}
