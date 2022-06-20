package image_parsing;

import actions.Point;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
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

import static image_parsing.Offsets.*;

public class ImageParser {

    static int[][][] image_to_rgb_array(BufferedImage image) {

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

    static int[][] transpose_matrix(int[][] matrix){
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

    static BufferedImage array_to_image(int[][] pixels) {

        BufferedImage image = new BufferedImage(pixels[0].length, pixels.length, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < pixels.length; y++) {
            for (int x = 0; x < pixels[0].length; x++) {
                int rgb;
                if (pixels[y][x] == 1)
                    rgb = 0;
                else{
                    rgb = 16777215; //(255<<16) + (255<<8) + 255
                }
                image.setRGB(x, y, rgb);
            }
        }

        return image;
    }

    static boolean matrix_comparison(int[][] a, int[][] b){
        for (int i = 0; i < a.length; i++){
            if (!Arrays.equals(a[i], b[i]))
                return false;
        }
        return true;
    }

    static int[][] flatten_image(int[][][] pixels){
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

    static ArrayList<int[][]> separate_blackened_imaged(int[][] binary_representation){
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

    static int parse_digit_image(int[][] binary_representation) throws IOException {
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

    // Returns true only when the images are exact matches (Useful for inventory checking) - Potentially wasteful checking
    public static boolean compare_images(BufferedImage image_a, BufferedImage image_b) {
        DataBuffer data_a = image_a.getData().getDataBuffer();
        DataBuffer data_b = image_b.getData().getDataBuffer();

        if(data_a.getSize() != data_b.getSize())
            return false;

        for(int i = 0; i < data_a.getSize(); i++)
            if(data_a.getElem(i) != data_b.getElem(i))
                return false;

        return true;
    }

    public static BufferedImage get_screenshot_roi(Point start, int width, int height) throws AWTException, IOException {
        Robot robot = new Robot();
        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        BufferedImage sub_image = screenShot.getSubimage((int) start.getX(), (int) start.getY(), width, height);

        ImageIO.write(sub_image, "PNG", new File(".\\src\\main\\sample_images\\current.png"));
        return ImageIO.read(new File(".\\src\\main\\sample_images\\current.png"));
    }

    public static BufferedImage get_inventory_image(int row, int col) throws AWTException, IOException {
        return ImageParser.get_screenshot_roi(
            Offsets.get_inventory_coordinate(row, col).subtract(Offsets.inventory_item_size),
            Offsets.inventory_item_size * 2,
            Offsets.inventory_item_size * 2
        );
    }
}
