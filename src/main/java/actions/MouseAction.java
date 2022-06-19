package actions;

import java.util.Arrays;

public abstract class MouseAction extends Action{

    MouseController mouse;

    public MouseAction(MouseController mouse, Point[] bounds, int wait_time) {
        super(wait_time);
        this.bounds = bounds;
        this.wait_time = wait_time;
        this.mouse = mouse;
    }

    public MouseAction(MouseController mouse, Point center, int size, int wait_time) {
        super(wait_time);
        bounds = new Point[]{
                new Point(center.x - size, center.y - size),
                new Point(center.x + size, center.y - size),
                new Point(center.x + size, center.y + size),
                new Point(center.x - size, center.y - size)
        };
        this.mouse = mouse;
    }

    //Return a random point within the bounds, scaled and offset to the window size
    public Point get_random_point(Point[] bounds){
        Point[] triangle_a = Arrays.copyOfRange(bounds, 0, 3);
        Point[] triangle_b = Arrays.copyOfRange(bounds, 1, 4);

        double area_a = 0.5 * Math.abs(
                triangle_a[0].x*(triangle_a[1].y - triangle_a[2].y) +
                        triangle_a[1].x*(triangle_a[2].y - triangle_a[1].y) +
                        triangle_a[2].x*(triangle_a[0].y - triangle_a[1].y)
        );
        double area_b = 0.5 * Math.abs(
                triangle_b[0].x*(triangle_b[1].y - triangle_b[2].y) +
                        triangle_b[1].x*(triangle_b[2].y - triangle_b[1].y) +
                        triangle_b[2].x*(triangle_b[0].y - triangle_b[1].y)
        );
        double area = area_a + area_b;

        Point[] target;
        if (Math.random() > area_a / area)
            target = triangle_b;
        else
            target = triangle_a;

        Point vector_a = target[1].subtract(target[0]);
        Point vector_b = target[2].subtract(target[1]);

        double[] rand_scales = {Math.random(), Math.random()};
        if (rand_scales[0] + rand_scales[1] > 1) {
            rand_scales = new double[]{1 - rand_scales[1], 1- rand_scales[0]};
        }

        Point rand_point = target[0].add(vector_a.scale(rand_scales[0]).add(vector_b.scale(rand_scales[1])));
        System.out.println(rand_point);

        return rand_point;
    }
}
