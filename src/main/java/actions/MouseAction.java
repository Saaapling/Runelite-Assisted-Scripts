package actions;

import base.MouseController;

import static actions.Point.generate_rectangle;

public abstract class MouseAction extends Action{

    MouseController mouse;

    public MouseAction(MouseController mouse, Point[] bounds, int wait_time) {
        this.bounds = bounds;
        this.wait_time = wait_time;
        this.mouse = mouse;
    }

    public MouseAction(MouseController mouse, Point center, int size, int wait_time) {
        this.wait_time = wait_time;
        bounds = generate_rectangle(center, size);
        this.mouse = mouse;
    }

    public Point[] get_bounds(){
        return bounds;
    }

    public Point get_random_point_in_bounds(){
        return Point.get_random_point(get_bounds());
    }
}
