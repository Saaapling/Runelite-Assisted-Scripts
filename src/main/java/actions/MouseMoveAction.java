package actions;

public class MouseMoveAction extends MouseAction{


    public MouseMoveAction(MouseController mouse, Point[] bounds, int wait_time) {
        super(mouse, bounds, wait_time);
    }

    public MouseMoveAction(MouseController mouse, Point center, int size, int wait_time) {
        super(mouse, center, size, wait_time);
    }

    public void execute() throws InterruptedException {
        mouse.move(get_random_point(bounds));
    }
}
