package tasks.AFKCombatHelper;

import actions.*;
import actions.Point;
import base.Client;
import base.MouseController;
import image_parsing.ImageParser;
import image_parsing.Offsets;
import tasks.InteractionTask;
import tasks.Task;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import static base.Utils.print;
import static image_parsing.ImageParser.get_color;

public class AFKCombatHelper extends InteractionTask {

    int failsafe_counter = 0;

    public AFKCombatHelper(Client client, MouseController mouse, ReentrantLock lock) {
        super(client, mouse, lock);
        task_name = "Combat Helper";
        actions.put("Default Wait", new WaitAction(1000, "Combat Waiting", client.get_name() + ": Still in combat, waiting 1 second"));
        actions.put("Passive Wait", new WaitAction(5000, "Passive Waiting", client.get_name() + ": No target found, waiting 5 seconds"));
    }

    public boolean get_combat_status() throws IOException {
        Color target = get_color(Offsets.get_enemy_health_coordinate(client.get_dimensions()));

        // Rough Estimates for the RuneLite green health bar
        if (target.getRed() > 10)
            return false;
        if (100 > target.getGreen() || target.getGreen() > 150)
            return false;
        return 35 <= target.getBlue() && target.getBlue() <= 75;
    }

    public Point find_target(Color target_rgb, BufferedImage image, double search_size, int offset_size) throws IOException {
        Rectangle dimensions = client.get_dimensions();
        Point center = new Point(dimensions.getX() + dimensions.width / 2d, dimensions.getY() + dimensions.height / 2d);
        print(center);
        for (int offset = 0; offset < Math.min(dimensions.width, dimensions.height) * search_size / 2; offset+=offset_size){
            Point start = center.subtract(offset);
            Point end = center.add(offset);
            for (int i = 0; i <= offset * 2; i+=5){
                Point[] coordinates = {
                        start.add(new Point(i, 0)),
                        start.add(new Point(0, i)),
                        end.subtract(new Point(i, 0)),
                        end.subtract(new Point(0, i))
                };
                for (Point coordinate : coordinates){
                    Color color = get_color(coordinate, image);
                    if (target_rgb.equals(color)){
                        return coordinate;
                    }
                }
            }
        }
        return null;
    }

    public Point find_target(Color target_rgb, BufferedImage image) throws IOException {
        return find_target(target_rgb, image, 1, 25);
    }

    public static Point[] get_target_points(Point coordinate, Color target_rgb, BufferedImage image) throws IOException {
        HashSet<Point> points = new HashSet<>();
        Queue<Point> processing_queue = new LinkedList<>();
        processing_queue.add(coordinate);

        while (!processing_queue.isEmpty()) {
            Point center = processing_queue.poll();
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    Point target = center.add(new Point(x, y));
                    if (!points.contains(target)) {
                        if (target_rgb.equals(ImageParser.get_color(target, image))) {
                            points.add(target);
                            processing_queue.add(target);
                        }
                    }
                }
            }
        }

        return points.toArray(new Point[0]);
    }

    public Action get_next_action(){
        Color target_rgb = new Color(0 , 255, 255);

        MouseAction next_action = (MouseAction) action_queue.poll();
        if (next_action != null){
            Point target = next_action.get_random_point_in_bounds();
            try {
                if (target_rgb.equals(ImageParser.get_color(target))){
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
                System.out.println(client.get_name() + ": Finding new target");

                // Take a screenshot (faster when computing multiple (hundreds) of points)
                BufferedImage image = ImageParser.get_screenshot();

                Point target = find_target(target_rgb, image);
                if (target == null){
                    failsafe_counter += 1;
                    return actions.get("Passive Wait");
                } else{
                    failsafe_counter = 0;
                }
                Point[] points = get_target_points(target, target_rgb, image);
                Point random_point = points[(int) (Math.random() * points.length)];
                if (!target_rgb.equals(ImageParser.get_color(random_point))){
                    print(client.get_name() + ": Target already moved");
                    return get_next_action();
                }
                print(client.get_name() + ": Target found");
                action_queue.add(new MouseLeftClickAction(mouse, random_point, 0, 5000, "Attack monster"));
                return new MouseMoveAction(mouse, random_point, 0, 0);
            }
        } catch (IOException ignored) {
            lock.unlock();
        }

        failsafe_counter += 1;
        return actions.get("Passive Wait");
    }

    public void run() {
        System.out.println("Starting Task: Combat Helper (" + client.get_name() + ")");

        fetch_lock();
        boolean in_focus = client.in_focus();
        if (!in_focus) {
            client.show();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                lock.unlock();
            }
        }
        Action next_action = get_next_action();

        while (failsafe_counter < 3){
            try {
                next_action.execute();

                lock.unlock();
                int wait_time = next_action.get_wait_time();
                int rand_sleep = get_sleep_time(wait_time);
                if (rand_sleep > 0) {
                    System.out.println("Sleeping (" + client.get_name() + "): " + rand_sleep + "ms");
                    Thread.sleep(rand_sleep);
                }

                fetch_lock();
                if (!client.in_focus()) {
                    client.show();
                    Thread.sleep(300);
                }
                next_action = get_next_action();
            } catch (Exception e){
                lock.unlock();
            }
        }

        System.out.println("Task finished: " + client.get_name());
    }

}
