package base;

import actions.Point;

public class Utils {

    public static void print(Object x){
        System.out.println(x);
    }

    public static <T> void print_arr(T[][] arr){
        for (T[] items : arr) {
            for (int j = 0; j < arr[0].length; j++) {
                System.out.print(items[j] + ",");
            }
            print("");
        }
    }

    public static void print_points(Point[] arr){
        for (Point x : arr){
            print(x);
        }
    }

}
