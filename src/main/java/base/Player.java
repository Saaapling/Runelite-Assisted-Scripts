package base;

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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static image_parsing.ImageParser.image_similarity;

public class Player {

    int health;
    int stamina;
    int prayer;
    String name;

    static HashMap<String, Integer> buff_durations = new HashMap<>();
    static {
        buff_durations.put("SuperCombat", 10);
        buff_durations.put("Ranging", 6);
        buff_durations.put("Antifire", 12);
        buff_durations.put("SuperAntifire", 5);
        buff_durations.put("Antipoison", 6);
    }

    HashMap<String, LocalDateTime> rebuff_timers = new HashMap<>();
    int[][] inventory_stack = new int[7][4];
    String[][] inventory = new String[7][4];

    private boolean check_empty(BufferedImage image){
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

        return counter / (data_a.getSize() / 3.d) > 0.99;
    }

    void update_inventory_slot(BufferedImage base_image, int row, int col) throws IOException {
        String potion_directory = ".\\src\\main\\java\\base\\InventoryImages\\";
        String alchemy_directory = "./src/main/java/tasks/AFKCombatHelper/AlchemyTargets/";

        inventory[row-1][col-1] = "Item";
        inventory_stack[row-1][col-1] = 1;

        if (check_empty(base_image)){
            inventory[row-1][col-1] = "Empty";
            inventory_stack[row-1][col-1] = 0;
            return;
        }

        // Check for potions inventory
        DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(potion_directory));
        for (Path path : stream) {
            BufferedImage compare_image = ImageIO.read(new File(path.toString()));
            double similarity = image_similarity(base_image, compare_image);

            if (similarity > 0.8){
                String regex = "([a-zA-Z]+)_?(\\d)?.png";
                Matcher m = Pattern.compile(regex).matcher(path.getFileName().toString());

                if (!m.find())
                    continue;

                String item = m.group(1);
                inventory[row-1][col-1] = item;
                if (m.group(2) != null){
                    inventory_stack[row-1][col-1] = Integer.parseInt(m.group(2));
                }else {
                    inventory_stack[row-1][col-1] = 1;
                }

                if (buff_durations.containsKey(item) && !rebuff_timers.containsKey(item)){
                    LocalDateTime dateTime = LocalDateTime.now();
                    rebuff_timers.put(item, dateTime);
                }

                return;
            }
        }

        // Check for alchemy targets
        stream = Files.newDirectoryStream(Paths.get(alchemy_directory));
        for (Path path : stream) {
            BufferedImage compare_image = ImageIO.read(new File(path.toString()));
            double similarity = image_similarity(base_image, compare_image);

            if (similarity > 0.8){
                inventory[row-1][col-1] = "Alch";
                inventory_stack[row-1][col-1] = 1;
                return;
            }
        }
    }

    void update_inventory(BufferedImage[][] inventory_images) throws IOException {
        for (int row = 1; row <= 7; row++) {
            for (int col = 1; col <= 4; col++) {
                BufferedImage base_image = inventory_images[row-1][col-1];
                update_inventory_slot(base_image, row, col);
            }
        }
    }

    Point get_item_slot(String item){
        for (int row = 1; row <= 7; row++) {
            for (int col = 1; col <= 4; col++) {
                if (inventory[row-1][col-1].equals(item)){
                    return new Point(row, col);
                }
            }
        }

        return null;
    }

    Point get_food_slot(){
        HashSet<String> food_items = new HashSet<>(List.of("Shark"));

        for (int row = 1; row <= 7; row++) {
            for (int col = 1; col <= 4; col++) {
                if (food_items.contains(inventory[row-1][col-1])){
                    return new Point(row, col);
                }
            }
        }

        return null;
    }

    Point get_prayer_slot(){
        HashSet<String> prayer_items = new HashSet<>(List.of("Prayer"));

        for (int row = 1; row <= 7; row++) {
            for (int col = 1; col <= 4; col++) {
                if (prayer_items.contains(inventory[row-1][col-1])){
                    return new Point(row, col);
                }
            }
        }

        return null;
    }

    void consume_item(int row, int col){
        inventory_stack[row-1][col-1] -= 1;
        String item = inventory[row-1][col-1];
        if (inventory_stack[row-1][col-1] <= 0){
            inventory[row-1][col-1] = "Empty";
        }

        if (rebuff_timers.containsKey(item)){
            LocalDateTime dateTime = LocalDateTime.now().plus(Duration.of(buff_durations.get(item), ChronoUnit.MINUTES));
            rebuff_timers.put(item, dateTime);
        }
    }

    Point check_consumes(){
        for (String item : rebuff_timers.keySet()){
            LocalDateTime dateTime = LocalDateTime.now();
            if (dateTime.isAfter(rebuff_timers.get(item))){
                Point item_slot = get_item_slot(item);
                if (item_slot != null)
                    return item_slot;
            }
        }

        return null;
    }

    boolean check_empty(){
        for (int row = 1; row <= 7; row++) {
            for (int col = 1; col <= 4; col++) {
                if (inventory[row-1][col-1].equals("Empty")){
                    return true;
                }
            }
        }
        return false;
    }

    void fill_empty_slot(){
        for (int row = 1; row <= 7; row++) {
            for (int col = 1; col <= 4; col++) {
                if (inventory[row-1][col-1].equals("Empty")){
                    inventory[row-1][col-1] = "Item";
                    inventory_stack[row-1][col-1] = 1;
                    return;
                }
            }
        }
    }

    Point get_empty_slot(){
        for (int row = 1; row <= 7; row++) {
            for (int col = 1; col <= 4; col++) {
                if (inventory[row-1][col-1].equals("Empty")){
                    return new Point(row, col);
                }
            }
        }

        return null;
    }

    Point get_alch_slot(){
        for (int row = 1; row <= 7; row++) {
            for (int col = 1; col <= 4; col++) {
                if (inventory[row-1][col-1].equals("Alch")){
                    return new Point(row, col);
                }
            }
        }

        return null;
    }
}
