package tasks.AFKCombatHelper;

import actions.*;
import actions.Point;
import base.Client;
import base.InputController;
import image_parsing.ImageParser;
import image_parsing.Offsets;
import tasks.InteractionTask;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import static base.Utils.print;
import static image_parsing.ImageParser.*;

public class AFKCombatHelper extends InteractionTask {

    int failsafe_counter = 0;
    int combat_check_counter = 0;

    public AFKCombatHelper(Client client, InputController mouse, ReentrantLock lock) {
        super(client, mouse, lock);
        task_name = "Combat Helper";
        actions.put("Default Wait", new WaitAction(1000, "Combat Waiting", client.get_name() + ": Still in combat, waiting 1 second"));
        actions.put("Passive Wait", new WaitAction(5000, "Passive Waiting", client.get_name() + ": No target found, waiting 5 seconds"));
    }

    public boolean get_combat_status() {
        combat_check_counter += 1;
        // Too many failed combat checks (Possibly finished slayer task and in a no-combat area)
//        if (combat_check_counter > 25){
//            System.out.println("Too many combat checks failed");
//            failsafe_counter = Integer.MAX_VALUE;
//        }
        Color target = get_color(Offsets.get_enemy_health_coordinate(client.get_dimensions()));

        // Rough Estimates for the RuneLite green health bar
        if (target.getRed() > 10)
            return false;
        if (100 > target.getGreen() || target.getGreen() > 150)
            return false;
        if (35 <= target.getBlue() && target.getBlue() <= 75){
            combat_check_counter = 0;
            return true;
        }
        return false;
    }

    public Point find_target(Color target_rgb, BufferedImage image, double search_size, int offset_size) {
        int[] target_numeric = {target_rgb.getRed(), target_rgb.getGreen(), target_rgb.getBlue()};

        Rectangle dimensions = client.get_dimensions();
        Point center = new Point(dimensions.getX() + dimensions.width / 2d, dimensions.getY() + dimensions.height / 2d);
        for (int offset = 0; offset < Math.min(dimensions.width, dimensions.height) * search_size / 2; offset+=offset_size){
            Point start = center.subtract(offset);
            Point end = center.add(offset);
            for (int i = 0; i <= offset * 2; i+=10){
                Point[] coordinates = {
                        start.add(new Point(i, 0)),
                        start.add(new Point(0, i)),
                        end.subtract(new Point(i, 0)),
                        end.subtract(new Point(0, i))
                };
                for (Point coordinate : coordinates){
                    if (compare_numeric_colors(target_numeric, get_color_numeric(coordinate, image))){
                        return coordinate;
                    }
                }
            }
        }
        return null;
    }

    public Point find_target(Color target_rgb, BufferedImage image) {
        return find_target(target_rgb, image, 1, 30);
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public static Point get_target_center(Point coordinate, Color target_rgb, BufferedImage image) {
        HashSet<Point> points = new HashSet<>();
        Queue<Point> processing_queue = new LinkedList<>();
        processing_queue.add(coordinate);

        long x_sum = 0;
        long y_sum = 0;
        while (!processing_queue.isEmpty()) {
            Point center = processing_queue.poll();
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    Point target = center.add(new Point(x, y));
                    if (!points.contains(target)) {
                        if (target_rgb.equals(ImageParser.get_color(target, image))) {
                            points.add(target);
                            processing_queue.add(target);
                            x_sum += target.getX();
                            y_sum += target.getY();
                        }
                    }
                }
            }
        }

        return new Point(x_sum / points.size(), y_sum / points.size());
    }

    public Action get_next_action(){
        Color target_rgb = new Color(0 , 255, 255);

        // Target has been found, check that it has not moved before attacking
        MouseAction next_action = (MouseAction) action_queue.poll();
        if (next_action != null){
            Point target = next_action.get_random_point_in_bounds();
            if (target_rgb.equals(ImageParser.get_color(target))){
                print(client.get_name() + ": Attacking target");
                return next_action;
            }
        }

        // Check whether the player is already in combat
        if (get_combat_status()){
            return actions.get("Default Wait");
        }

        // Take a screenshot (faster when computing multiple (hundreds) of points)
        BufferedImage image = ImageParser.get_screenshot();

        // Find a new target to attack
        System.out.println(client.get_name() + ": Finding new target");
        Point target = find_target(target_rgb, image);
        if (target == null){
            failsafe_counter += 1;
            return actions.get("Passive Wait");
        } else{
            failsafe_counter = 0;
        }
        print(client.get_name() + ": Target found");
        Point target_center = get_target_center(target, target_rgb, image);
        action_queue.add(new MouseLeftClickAction(mouse, target_center, 0, 5000, "Attack monster"));
        return new MouseMoveAction(mouse, target_center, 30, 0);
    }

    public void run() {
        System.out.println("Starting Task: Combat Helper (" + client.get_name() + ")");

        fetch_lock();
        try {
            focus_client();
            Action next_action = get_next_action();
            while (failsafe_counter < 3){
                if (!next_action.get_name().contains("Wait")){
                    System.out.println(client.get_name() + ": " + next_action.get_name());
                }
                next_action.execute();

                release_lock();
                int wait_time = next_action.get_wait_time();
                int rand_sleep = get_sleep_time(wait_time);
                if (rand_sleep > 0) {
                    System.out.println("Sleeping (" + client.get_name() + "): " + rand_sleep + "ms");
                    Thread.sleep(rand_sleep);
                }

                fetch_lock();
                focus_client();
                next_action = get_next_action();
            }
        }catch (Exception e){
            print(e.fillInStackTrace());
            release_lock();
        }

        System.out.println("Task finished: " + client.get_name());
        System.exit(0);
    }

}
