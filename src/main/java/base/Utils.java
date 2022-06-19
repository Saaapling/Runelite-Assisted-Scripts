package base;

public class Utils {

    public static void print(Object x){
        System.out.println(x);
    }

    public static void print_arr(int[][] arr){
        for (int[] ints : arr) {
            for (int j = 0; j < arr[0].length; j++) {
                System.out.print(ints[j] + ",");
            }
            print("");
        }
    }

}
