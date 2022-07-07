package tasks.AFKCombatHelper;

import actions.Point;
import actions.*;
import base.Client;
import base.InputController;
import image_parsing.ImageParser;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.ReentrantLock;

import static base.Utils.print;
import static image_parsing.ImageParser.get_color;

public class AFKCombatLooter extends AFKCombatHelper {

    Point item_box;
    public Color npc_rgb = new Color(0 , 255, 255);
    public Color item_rgb = new Color(255, 150, 255);
    public int failed_loot_attempts = 0;

    public AFKCombatLooter(Client client, InputController mouse, ReentrantLock lock) {
        super(client, mouse, lock);
        actions.put("Item Search", new WaitAction(1000, "Item Search", client.get_name() + ": Looting ground item"));
    }

    private Point get_text_y(BufferedImage image, Color target_rgb, Point start, int limit, int increment){
        Point curr_y = start;
        curr_y = new Point(curr_y.getX(), curr_y.getY() + increment);
        int counter = 0;
        while (counter < limit){
            if (!target_rgb.equals(get_color(curr_y, image))){
                counter += 1;
            }else {
                counter = 0;
            }
            curr_y = new Point(curr_y.getX(), curr_y.getY() + increment);
        }

        return new Point(curr_y.getX(), curr_y.getY() - increment);
    }

    private Point get_text_x(BufferedImage image, Color target_rgb, Point start, int limit, int increment){
        Point curr_x = start;
        curr_x = new Point(curr_x.getX() + increment, curr_x.getY());
        int counter = 0;
        while (counter < limit){
            if (!target_rgb.equals(get_color(curr_x, image))){
                counter += 1;
            }else {
                counter = 0;
            }
            curr_x = new Point(curr_x.getX() + increment, curr_x.getY());
        }

        return new Point(curr_x.getX() - increment, curr_x.getY());
    }

    public Point find_text_center(Color target_rgb, BufferedImage image, Point coordinate){
        // Find the y-center for the item text
        Point y_avg = get_text_y(image, target_rgb, coordinate, 20, 1).subtract(new Point(0, 23));

        // Find the x-center for the item text
        Point x_min = get_text_x(image, target_rgb, y_avg, 50,-1);
        Point x_max = get_text_x(image, target_rgb, y_avg, 50, 1);

        // In case there are multiple items stacked, find the lowest item text
        Point x_avg = new Point((x_max.getX() + x_min.getX()) / 2, y_avg.getY());
        y_avg = get_text_y(image, target_rgb, x_avg, 20, 1).subtract(new Point(0, 23));

        return new Point(x_avg.getX(), y_avg.getY());
    }

    public Point[] find_item_text_bounds(BufferedImage image, Color target_rgb){
        Point start = item_box.add(new Point(0, 25));
        Point x_max = get_text_x(image, target_rgb, start, 10, 1).subtract(new Point(10, 0));
        Point x_min = get_text_x(image, target_rgb, start, 10, -1).subtract(new Point(-10, 0));

        int fail_counter = 0;
        while (x_max.equals(x_min)){
            fail_counter += 1;
            start = start.add(new Point(0, 15));
            if (fail_counter >= 3){
                return null;
            }
            x_max = get_text_x(image, target_rgb, start, 10, 1).subtract(new Point(10, 0));
            x_min = get_text_x(image, target_rgb, start, 10, -1).subtract(new Point(-10, 0));
        }
        Point y_max = get_text_y(image, target_rgb, x_min, 5, 1).subtract(new Point(0, 5));
        Point y_min = get_text_y(image, target_rgb, x_min, 5, -1).subtract(new Point(0, -5));

        Point center = new Point(x_max.getX() + x_min.getX(), y_max.getY() + y_min.getY()).scale(0.5);
        return Point.generate_rectangle(center, (int) (x_max.getX() - x_min.getX()) / 2, (int) (y_max.getY() - y_min.getY()) / 2);
    }

    public Action get_next_action(){

        // Target has been found, check that it has not moved before attacking
        Action next_action = action_queue.poll();
        if (next_action != null && next_action.get_name().equals("Attack monster")){
            MouseAction action = (MouseAction) next_action;
            Point target = action.get_random_point_in_bounds();
            if (npc_rgb.equals(ImageParser.get_color(target))){
                print(client.get_name() + ": Attacking target");
                failed_loot_attempts = 0;
                return next_action;
            }
        }

        // Take a screenshot (faster when computing multiple (hundreds) of points)
        BufferedImage image = ImageParser.get_screenshot();

        // Item has been found (and right-clicked). Pick up the item
        if (next_action != null && next_action.get_name().equals("Item Search")){
            // Find the bounds to pick up the item
            Point[] item_box_bounds = find_item_text_bounds(image, item_rgb);
            if (item_box_bounds != null) {
                failed_loot_attempts = 0;

                // Scale the waiting time based on the run distance
                Rectangle dimensions = client.get_dimensions();
                Point center = new Point(dimensions.getX() + dimensions.width / 2d, dimensions.getY() + dimensions.height / 2d);
                Point difference = center.subtract(item_box);
                double scale = Math.sqrt(Math.pow(difference.getX(), 2) + Math.pow(difference.getY(), 2)) /
                        Math.sqrt(Math.pow(center.getX(), 2) + Math.pow(center.getY(), 2));
                int wait_time = Math.max(1500, (int) (10000 * scale));

                return new MouseLeftClickAction(mouse, item_box_bounds, wait_time, "Loot Item");
            }
            failed_loot_attempts += 1;
        }

        // Check for items to loot
        Point target = find_target(item_rgb, image, 1, 15);
        if (target != null && failed_loot_attempts < 3){
            System.out.println(client.get_name() + ": Found item to loot");
            Point item_text_center = find_text_center(item_rgb, image, target);
            Point item_center = new Point(item_text_center.getX(), item_text_center.getY() + 10);
            action_queue.add(actions.get("Item Search"));
            item_box = Point.get_random_point(Point.generate_rectangle(item_center, 10));
            return new MouseRightClickAction(mouse, item_box, 0, 100, "Target Item");
        }

        // Check whether the player is already in combat
        if (get_combat_status())
            return actions.get("Default Wait");

        // Find a new target to attack
        System.out.println(client.get_name() + ": Finding new target");
        target = find_target(npc_rgb, image);
        if (target == null){
            failsafe_counter += 1;
            return actions.get("Passive Wait");
        } else{
            failsafe_counter = 0;
        }
        print(client.get_name() + ": Target found");
        Point target_center = get_target_center(target, npc_rgb, image);
        action_queue.add(new MouseLeftClickAction(mouse, target_center, 0, 5000, "Attack monster"));
        return new MouseMoveAction(mouse, target_center, 30, 0);
    }

}
