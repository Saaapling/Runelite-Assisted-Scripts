package actions;

import java.awt.*;

public class Point {

    double x;
    double y;

    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public Point add(Point a){
        return new Point(x + a.x, y + a.y);
    }

    public Point subtract(Point a){
        return new Point(x - a.x, y - a.y);
    }

    public Point scale(double scale){
        return new Point((int) (scale * x + 0.5), (int) (scale * y + 0.5));
    }

    public Point scale(Point scale){
        return new Point((int) (scale.x * x + 0.5), (int) (scale.y * y + 0.5));
    }
}
