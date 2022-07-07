package actions;

import base.InputController;

import static actions.Point.generate_rectangle;

public abstract class MouseAction extends Action {

    Point[] bounds; //Expected to be a quadrilateral
    InputController mouse;

    public MouseAction(InputController mouse, Point[] bounds, int wait_time) {
        this.bounds = bounds;
        this.wait_time = wait_time;
        this.mouse = mouse;
    }

    public MouseAction(InputController mouse, Point center, int size, int wait_time) {
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
