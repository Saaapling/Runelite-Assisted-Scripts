package actions;

import java.awt.*;
import java.util.Arrays;

public class Point {

    double x;
    double y;

    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point point)) return false;

        if (Double.compare(point.x, x) != 0) return false;
        return Double.compare(point.y, y) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public Point add(Point a){
        return new Point(x + a.x, y + a.y);
    }

    public Point add(double a){
        return new Point(x + a, y + a);
    }

    public Point subtract(Point a){
        return new Point(x - a.x, y - a.y);
    }

    public Point subtract(double a){
        return new Point(x - a, y - a);
    }

    public Point scale(Point scale){
        return new Point(scale.x * x, scale.y * y);
    }

    public Point scale(double scale){
        return new Point((int) (scale * x + 0.5), (int) (scale * y + 0.5));
    }

    public static Point[] generate_rectangle(Point center, int size){
        return new Point[]{
            new Point(center.x - size, center.y - size),
            new Point(center.x + size, center.y - size),
            new Point(center.x + size, center.y + size),
            new Point(center.x - size, center.y + size)
        };
    }

    public static Point[] generate_rectangle(Point center, int width, int height){
        return new Point[]{
                new Point(center.x - width, center.y - height),
                new Point(center.x + width, center.y - height),
                new Point(center.x + width, center.y + height),
                new Point(center.x - width, center.y + height)
        };
    }

    public static boolean check_bounds(Point[] bounds, Point point){
        double max_x = Integer.MIN_VALUE;
        double max_y = Integer.MIN_VALUE;
        double min_x = Integer.MAX_VALUE;
        double min_y = Integer.MAX_VALUE;

        for (Point x : bounds){
            if (x.getX() > max_x)
                max_x = x.getX();
            if (x.getX() < min_x)
                min_x = x.getX();
            if (x.getY() > max_y)
                max_y = x.getY();
            if (x.getY() < min_y)
                min_y = x.getY();
        }

        if (point.getX() < min_x || point.getX() > max_x)
            return false;

        return !(point.getY() < min_y) && !(point.getY() > max_y);
    }

    //Return a random point within the bounds
    public static Point get_random_point(Point[] bounds) {
        Point[] triangle_a = {bounds[1], bounds[0], bounds[2]};
        Point[] triangle_b = {bounds[3], bounds[0], bounds[2]};

        double area_a = 0.5 * Math.abs(
                triangle_a[0].x*(triangle_a[1].y - triangle_a[2].y) +
                        triangle_a[1].x*(triangle_a[2].y - triangle_a[1].y) +
                        triangle_a[2].x*(triangle_a[0].y - triangle_a[1].y)
        );
        double area_b = 0.5 * Math.abs(
                triangle_b[0].x*(triangle_b[1].y - triangle_b[2].y) +
                        triangle_b[1].x*(triangle_b[2].y - triangle_b[0].y) +
                        triangle_b[2].x*(triangle_b[0].y - triangle_b[1].y)
        );

        double area = area_a + area_b;
        Point[] target;
        if (Math.random() > area_a / area)
            target = triangle_b;
        else
            target = triangle_a;

        Point vector_a = target[1].subtract(target[0]);
        Point vector_b = target[2].subtract(target[0]);

        double[] rand_scales = {Math.random(), Math.random()};
        if (rand_scales[0] + rand_scales[1] > 1) {
            rand_scales = new double[]{1 - rand_scales[1], 1- rand_scales[0]};
        }

        Point rand_point = target[0].add(vector_a.scale(rand_scales[0]).add(vector_b.scale(rand_scales[1])));
//        System.out.println("Random point generated: " + rand_point);

        if (!check_bounds(bounds, rand_point)){
            System.out.println("Something went wrong...");
            return get_random_point(bounds);
        }

        return rand_point;
    }
}
