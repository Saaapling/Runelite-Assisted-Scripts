package image_parsing;

import actions.Point;
import base.Controller;
import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

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
import java.util.List;

import static base.Utils.*;
import static image_parsing.Offsets.*;
import static image_parsing.ImageParser.*;

public class ScreenshotManager {

    static User32 user32 = User32.INSTANCE;

    private static void check_and_save_image(int[][] pixels) throws IOException {
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

        int slot = 1;
        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        for (int i = 0; i < 8; i ++){
            Point item_coord = get_bank_coordinate(0, i);
            BufferedImage slot_img = screenShot.getSubimage(
                    (int) (item_coord.getX() - bank_item_size),
                    (int) (item_coord.getY() - bank_item_size),
                    bank_item_size * 2,
                    bank_item_size * 2
            );
            ImageIO.write(slot_img, "PNG", new File(".\\src\\main\\sample_images\\" + slot + ".png"));
            slot += 1;
        }
    }

    public static void bank_x_screenshot() throws IOException, AWTException, InterruptedException {
        Robot robot = new Robot();

        Point item_center = get_bank_coordinate(0, 0);
        Point item_coord = Point.get_random_point(Point.generate_rectangle(item_center, 12));

        Controller.mouse.move(item_coord);
        Controller.mouse.right_click();

        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        BufferedImage withdraw_x_img = screenShot.getSubimage(
                (int) (item_coord.getX() - bank_withdraw_x_width),
                (int) (item_coord.getY() + bank_withdraw_x_height_offset - bank_withdraw_x_height),
                bank_withdraw_x_width * 2,
                bank_withdraw_x_height * 2
        );
        ImageIO.write(withdraw_x_img, "PNG", new File(".\\src\\main\\sample_images\\withdraw_x.png"));
    }

    public static void take_screenshot(Point start, int width, int height, String path) throws AWTException, IOException {
        Robot robot = new Robot();

        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        BufferedImage sub_image = screenShot.getSubimage((int) start.getX(), (int) start.getY(), width, height);
        ImageIO.write(sub_image, "PNG", new File(path));
    }

    public static void screenshot_inventory_item(String path, int row, int col) throws IOException, AWTException {
        Point item_center = Offsets.get_inventory_coordinate(row, col);
        String file_name = path + row + "_" + col + ".png";
        take_screenshot(item_center.subtract(new Point(inventory_item_size, inventory_item_size)), inventory_item_size * 2, inventory_item_size * 2, file_name);
    }

    public static void screenshot_inventory_items(String path) throws IOException, AWTException {
        for (int row = 1; row <= 7; row++){
            for (int col = 1; col <= 4; col++){
                Point item_center = Offsets.get_inventory_coordinate(row, col);
                String file_name = path + row + "_" + col + ".png";
                take_screenshot(item_center.subtract(new Point(inventory_item_size, inventory_item_size)), inventory_item_size * 2, inventory_item_size * 2, file_name);
            }
        }
    }

    private static double check_empty(BufferedImage image){
        DataBuffer data_a = image.getData().getDataBuffer();
        ArrayList<Color> backgrounds = new ArrayList<>(List.of(
                new Color(62,53,41),
                new Color(64,54,44),
                new Color(59,50,38)
        ));

        int counter = 0;
        outer: for(int i = 0; i < data_a.getSize(); i+=3) {
            int[] rgb = {data_a.getElem(i + 2), data_a.getElem(i + 1), data_a.getElem(i)};
            Color curr = new Color(rgb[0], rgb[1], rgb[2]);

            for (Color background : backgrounds) {
                if (background.equals(curr)) {
                    counter += 1;
                    continue outer;
                }
            }
        }

        return counter / (data_a.getSize() / 3.d);
    }

    public static void main(String[]args) throws Exception {
        List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
        Robot robot = new Robot();
        String path = ".\\src\\main\\sample_images\\inventory\\";

        WinDef.HWND hWnd = null;
        for (DesktopWindow desktopWindow: windows){
            if (desktopWindow.getTitle().contains("RuneLite")) {
                print("Application Found: " + desktopWindow.getTitle());

                // Get HWND and display window
                hWnd = desktopWindow.getHWND();
                user32.ShowWindow(hWnd, User32.SW_SHOWMINIMIZED);
                user32.ShowWindow(hWnd, User32.SW_RESTORE);
            }
        }

        screenshot_inventory_item(path, 1, 1);

        user32.ShowWindow(hWnd, User32.SW_SHOWMINIMIZED);
    }
}
