package status_parser;


import actions.Point;
import base.Controller;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class image_parser {

    static int health_x_offset = 240;
    static int health_y_offset = 84;
    static int prayer_x_offset = 240;
    static int prayer_y_offset = 118;
    static int stamina_x_offset = 231;
    static int stamina_y_offset = 151;
    static int fullscreen_offset = 4;

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

    private static int[][][] image_to_rgb_array(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();
        int[][][] result = new int[height][width][3];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int pixel_int = image.getRGB(col, row);
                int r = (pixel_int>>16)&0xFF;
                int g = (pixel_int>>8)&0xFF;
                int b = (pixel_int)&0xFF;
                result[row][col] = new int[]{r, g, b};
            }
        }

        return result;
    }

    private static int[][] transpose_matrix(int[][] matrix){
        int m = matrix.length;
        int n = matrix[0].length;

        int[][] transposedMatrix = new int[n][m];

        for(int x = 0; x < n; x++) {
            for(int y = 0; y < m; y++) {
                transposedMatrix[x][y] = matrix[y][x];
            }
        }

        return transposedMatrix;
    }

    private static BufferedImage array_to_image(int pixels[][]) {

        BufferedImage image = new BufferedImage(pixels[0].length, pixels.length, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[0].length; y++) {
                int rgb;
                if (pixels[x][y] == 1)
                    rgb = 0;
                else{
                    rgb = 16777215; //(255<<16) + (255<<8) + 255
                }
                image.setRGB(y, x, rgb);
            }
        }

        return image;
    }

    private static boolean matrix_comparison(int[][] a, int[][] b){
        for (int i = 0; i < a.length; i++){
            if (!Arrays.equals(a[i], b[i]))
                return false;
        }
        return true;
    }

    private static int[][] flatten_image(int[][][] pixels){
        /*
            Leverages the idea that the number in Runelite have a mono-color background, and cast a mono-color shadow.
            Thus, there should be a total of 3 colors (RGB) values detected, the second of which represents the number.
            The background color and other misc. colors will be turned to white, and then the image will be separated
            into individual digit components
         */

        // Get Hashmap of # of times each color appears
        HashMap<String, Integer> color_map = new HashMap<>();
        for (int[][] pixel : pixels) {
            for (int col = 0; col < pixels[0].length; col++) {
                int[] rgb_vals = pixel[col];
                String rgb_string = rgb_vals[0] + "," + rgb_vals[1] + "," + rgb_vals[2];
                color_map.merge(rgb_string, 1, Integer::sum);
            }
        }

        int max = 0;
        int[] max_rgb = new int[3];
        int second = 0;
        int[] target_rgb = new int[3];

        for (Map.Entry<String, Integer> entry : color_map.entrySet()){
            if (entry.getValue() > max) {
                second = max;
                target_rgb = max_rgb.clone();
                max = entry.getValue();
                String[] str_arr = entry.getKey().split(",");
                for (int i = 0; i < 3; i++){
                    max_rgb[i] = Integer.parseInt(str_arr[i]);
                }
            } else if (entry.getValue() > second){
                String[] str_arr = entry.getKey().split(",");
                for (int i = 0; i < 3; i++){
                    target_rgb[i] = Integer.parseInt(str_arr[i]);
                }
                second = entry.getValue();
            }
        }

        // Shorten the pixel array to a 2D array of 1s and 0s
        int[][] binary_representation = new int[pixels.length][pixels[0].length];
        for (int row = 0; row < pixels.length; row++) {
            for (int col = 0; col < pixels[0].length; col++) {
                if (Arrays.equals(pixels[row][col], target_rgb))
                    binary_representation[row][col] = 1;
                else
                    binary_representation[row][col] = 0;
            }
        }

        return binary_representation;
    }

    private static ArrayList<int[][]> separate_blackened_imaged(int[][] binary_representation){
        // Separate the array into digit components
        binary_representation = transpose_matrix(binary_representation);
        ArrayList<int[][]> digits = new ArrayList<>();
        int start = 0;
        for (int col = 0; col < binary_representation.length; col++) {
            boolean remove = true;
            for (int row = 0; row < binary_representation[0].length; row++) {
                if (binary_representation[col][row] == 1) {
                    remove = false;
                    break;
                }
            }
            if (remove)
                if (start == col){
                    start += 1;
                }else {
                    digits.add(transpose_matrix(Arrays.copyOfRange(binary_representation, start, col)));
                    start = col + 1;
                }
        }

        ArrayList<int[][]> trimmed_digits = new ArrayList<>();
        for (int[][] digit : digits) {
            start = 0;
            for (int row = 0; row < digit.length; row++) {
                boolean remove = true;
                for (int col = 0; col < digit[0].length; col++) {
                    if (digit[row][col] == 1) {
                        remove = false;
                        break;
                    }
                }
                if (remove)
                    if (start == row) {
                        start += 1;
                    } else {
                        trimmed_digits.add(Arrays.copyOfRange(digit, start, row));
                        break;
                    }
            }
        }
        return trimmed_digits;
    }

    private static void check_and_save_image(int pixels[][]) throws IOException {
        String name_canidates = "abcdefghijklmnopqrstuvwxyz";

        DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(".\\src\\main\\digit_images\\"));
        boolean save = true;
        for (Path path : stream) {
            String filename = path.getFileName().toString();
            BufferedImage image = ImageIO.read(new File(".\\src\\main\\digit_images\\" + filename));
            int[][][] image_pixels = image_to_rgb_array(image);
            int[][] image_matrix = flatten_image(image_pixels);

            if (matrix_comparison(pixels, image_matrix)){
                save = false;
            }

            name_canidates = name_canidates.substring(1);
        }

        if (save) {
            BufferedImage image = array_to_image(pixels);
            String filename = name_canidates.substring(0, 1);
            print("Saving image: " + filename + ".png");
            ImageIO.write(image, "PNG", new File(".\\src\\main\\digit_images\\" + filename + ".png"));
        }
    }

    public static void generate_image_bank(Rectangle dimensions) throws AWTException, IOException, InterruptedException {
        Robot robot = new Robot();

        int max_x = dimensions.x + dimensions.width;
        int min_y = dimensions.y;

        int extra_offset = 0;
        if (max_x != 1280 && max_x != 1920 && max_x != 3840) {
            extra_offset = fullscreen_offset;
        }

        print(max_x + ", " + min_y);

        for (int i = 0; i < 60; i++) {
            BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            BufferedImage health_box = screenShot.getSubimage(max_x - health_x_offset - extra_offset, min_y + health_y_offset + extra_offset, 19, 13);
            BufferedImage prayer_box = screenShot.getSubimage(max_x - prayer_x_offset - extra_offset, min_y + prayer_y_offset + extra_offset, 19, 13);
            BufferedImage stamina_box = screenShot.getSubimage(max_x - stamina_x_offset - extra_offset, min_y + stamina_y_offset + extra_offset, 19, 13);

            ImageIO.write(screenShot, "PNG", new File(".\\src\\main\\sample_images\\screenshot.png"));
            ImageIO.write(health_box, "PNG", new File(".\\src\\main\\sample_images\\health.png"));
            ImageIO.write(prayer_box, "PNG", new File(".\\src\\main\\sample_images\\prayer.png"));
            ImageIO.write(stamina_box, "PNG", new File(".\\src\\main\\sample_images\\stamina.png"));

            BufferedImage[] image_list = {health_box, prayer_box, stamina_box};
            for (BufferedImage image : image_list) {
                int[][][] image_pixels = image_to_rgb_array(image);
                int[][] binary_representation = flatten_image(image_pixels);
                ArrayList<int[][]> digits = separate_blackened_imaged(binary_representation);
                for (int[][] digit : digits) {
                    check_and_save_image(digit);
                }
            }

            Thread.sleep(1000);
        }
    }

    public static void bank_screenshot() throws IOException, AWTException {
        Robot robot = new Robot();


        int x_offset = 48;
        int y_offset = 36;
        int slot = 1;
        int size = 12;
        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        for (int i = 0; i < 8; i ++){
            BufferedImage slot_img = screenShot.getSubimage(662  - size, 137 + (y_offset * i) - size, size * 2, size * 2);
            ImageIO.write(slot_img, "PNG", new File(".\\src\\main\\sample_images\\" + slot + ".png"));
            slot += 1;
        }
    }

    public static void bank_x_screenshot() throws IOException, AWTException, InterruptedException {
        Robot robot = new Robot();

        int y_offset = 72;
        int x_size = 230;
        int y_size = 12;

        Controller.mouse.move(new Point(670, 150));
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        Thread.sleep((long) (100 + Math.random() * 10));
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        Thread.sleep((long) (2000 + Math.random() * 10));

        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        for (int i = 0; i < 8; i ++){
            BufferedImage slot_img = screenShot.getSubimage(670 - x_size / 2, 150 + y_offset - y_size / 2,  x_size, y_size);
            ImageIO.write(slot_img, "PNG", new File(".\\src\\main\\sample_images\\withdraw_x.png"));
        }
    }

    private static int parse_digit_image(int[][] binary_representation) throws IOException {
        ArrayList<int[][]> digits = separate_blackened_imaged(binary_representation);
        int[] digit_arr = new int[digits.size()];


        DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(".\\src\\main\\digit_images\\"));
        for (Path path : stream) {
            String filename = path.getFileName().toString();
            BufferedImage image = ImageIO.read(new File(".\\src\\main\\digit_images\\" + filename));
            int[][][] image_pixels = image_to_rgb_array(image);
            int[][] image_matrix = flatten_image(image_pixels);

            for (int i = 0; i < digits.size(); i++) {
                if (matrix_comparison(digits.get(i), image_matrix)) {
                    digit_arr[i] = Integer.parseInt(filename.substring(0, 1));
                }
            }
        }

        int number = 0;
        int j = 0;
        for (int i = digit_arr.length; i > 0; ){
            i -= 1;
            number += digit_arr[j] * Math.pow(10, i);
            j += 1;
        }

        return number;
    }

    public static int[] get_player_status(Rectangle dimensions) throws AWTException, IOException {
        Robot robot = new Robot();

        int max_x = dimensions.x + dimensions.width;
        int min_y = dimensions.y;

        int extra_offset = 0;
        if (max_x != 1280 && max_x != 1920 && max_x != 3840) {
            extra_offset = 4;
        }

        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        BufferedImage health_box = screenShot.getSubimage(max_x - health_x_offset - extra_offset, min_y + health_y_offset + extra_offset, 19, 13);
        BufferedImage prayer_box = screenShot.getSubimage(max_x - prayer_x_offset - extra_offset, min_y + prayer_y_offset + extra_offset, 19, 13);
        BufferedImage stamina_box = screenShot.getSubimage(max_x - stamina_x_offset - extra_offset, min_y + stamina_y_offset + extra_offset, 19, 13);

        ImageIO.write(screenShot, "PNG", new File(".\\src\\main\\sample_images\\screenshot.png"));
        ImageIO.write(health_box, "PNG", new File(".\\src\\main\\sample_images\\health.png"));
        ImageIO.write(prayer_box, "PNG", new File(".\\src\\main\\sample_images\\prayer.png"));
        ImageIO.write(stamina_box, "PNG", new File(".\\src\\main\\sample_images\\stamina.png"));

        BufferedImage[] image_list = {health_box, prayer_box, stamina_box};
        int[] status = new int[3];
        for (int i = 0; i < image_list.length; i++) {
            int[][][] image_pixels = image_to_rgb_array(image_list[i]);
            int[][] binary_representation = flatten_image(image_pixels);
            status[i] = parse_digit_image(binary_representation);
        }

        return status;
    }
}
