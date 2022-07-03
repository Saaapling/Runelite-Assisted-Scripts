package tasks.AFKCombatHelper;

import actions.Point;
import actions.*;
import base.Client;
import base.MouseController;
import image_parsing.ImageParser;
import image_parsing.Offsets;
import tasks.InteractionTask;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import static base.Utils.print;
import static image_parsing.ImageParser.get_color;
import static image_parsing.Offsets.get_enemy_health_coordinate;

public class AFKCombatLooter extends AFKCombatHelper {

    int failsafe_counter = 0;

    public AFKCombatLooter(Client client, MouseController mouse, ReentrantLock lock) {
        super(client, mouse, lock);
    }

    public Point find_item_center(Color target_rgb, BufferedImage image, Point coordinate){
        Point y_max = coordinate;
        int counter = 0;
        while (counter < 20){
            if (!target_rgb.equals(get_color(y_max, image))){
                counter += 1;
            }else {
                counter = 0;
            }
            y_max = new Point(y_max.getX(), y_max.getY() + 1);
        }

        Point y_avg = new Point(y_max.getX(), y_max.getY() - 24);
        Point x_max = y_avg;
        Point x_min = y_avg;
        counter = 0;
        while (counter < 50){
            if (!target_rgb.equals(get_color(x_max, image))){
                counter += 1;
            }else {
                counter = 0;
            }
            x_max = new Point(x_max.getX() + 1, x_max.getY());
        }
        counter = 0;
        while (counter < 50){
            if (!target_rgb.equals(get_color(x_min, image))){
                counter += 1;
            }else {
                counter = 0;
            }
            x_min = new Point(x_min.getX() - 1, x_min.getY());
        }

        return new Point((x_max.getX() + x_min.getX()) / 2, y_max.getY() - 24);
    }

    public Action get_next_action(){
        Color npc_rgb = new Color(0 , 255, 255);
        Color item_rgb = new Color(255, 150, 255);

        MouseAction next_action = (MouseAction) action_queue.poll();
        if (next_action != null && next_action.get_name().equals("Attack monster")){
            Point target = next_action.get_random_point_in_bounds();
            try {
                if (npc_rgb.equals(get_color(target))){
                    print(client.get_name() + ": Attacking target");
                    return next_action;
                }
            } catch (IOException e) {
                lock.unlock();
                failsafe_counter += 1;
                return actions.get("Passive Wait");
            }
        }

        try {
            if (get_combat_status()){
                return actions.get("Default Wait");
            }else{
                // Take a screenshot (faster when computing multiple (hundreds) of points)
                BufferedImage image = ImageParser.get_screenshot();

                // Check for items to loot
                Point target = find_target(item_rgb, image, 1, 15);
                if (target != null){
                    System.out.println(client.get_name() + ": Found item to loot");
                    Point item_text_center = find_item_center(item_rgb, image, target);
                    Point item_center = new Point(item_text_center.getX(), item_text_center.getY() + 12);

                    // Scale the waiting time based on the run distance
                    Rectangle dimensions = client.get_dimensions();
                    Point center = new Point(dimensions.getX() + dimensions.width / 2d, dimensions.getY() + dimensions.height / 2d);
                    Point difference = center.subtract(item_center);
                    double scale = Math.sqrt(Math.pow(difference.getX(), 2) + Math.pow(difference.getY(), 2)) /
                            Math.sqrt(Math.pow(center.getX(), 2) + Math.pow(center.getY(), 2));
                    int wait_time = Math.max(1500, (int) (20000 * scale));

                    return new MouseLeftClickAction(mouse, item_center, 10, wait_time, "Loot Item");
                }

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
                Point[] points = get_target_points(target, npc_rgb, image);
                Point random_point = points[(int) (Math.random() * points.length)];
                action_queue.add(new MouseLeftClickAction(mouse, random_point, 0, 5000, "Attack monster"));
                return new MouseMoveAction(mouse, random_point, 0, 0);
            }
        } catch (IOException ignored) {
            lock.unlock();
        }

        failsafe_counter += 1;
        return actions.get("Passive Wait");
    }

}
