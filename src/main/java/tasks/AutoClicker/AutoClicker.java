package tasks.AutoClicker;

import actions.*;
import base.Client;
import base.InputController;
import tasks.InteractionTask;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static image_parsing.ImageParser.compare_images;
import static image_parsing.ImageParser.get_inventory_image;

public class AutoClicker extends InteractionTask {

    public AutoClicker(Client client, InputController mouse, ReentrantLock lock) {
        super(client, mouse, lock);
        task_name = "Basic Auto Clicker";

        action_queue.add(new MouseLeftClickAction(mouse, new Point(641, 507 ), 20, 300, "Click"));
        action_queue.add(new MouseLeftClickAction(mouse, new Point(641, 507 ), 20, 300, "Click"));
        action_queue.add(new MouseLeftClickAction(mouse, new Point(641, 507 ), 20, 300, "Click"));
        action_queue.add(new MouseLeftClickAction(mouse, new Point(1860, 833 ), 8, 500, "Click"));
        action_queue.add(new InventoryAction(mouse, 7, 1, 1500, "Item"));
    }

    public Action get_next_action(){
        Action next_action = action_queue.poll();
        action_queue.addLast(next_action);
        return next_action;
    }

    public void run() {
        System.out.println("Starting Task: (" + client.get_name() + ")");

        fetch_lock();
        boolean in_focus = client.in_focus();
        if (!in_focus) {
            client.show();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                release_lock();
            }
        }

        Action next_action = get_next_action();

        while (next_action != null) {
            try {
                System.out.println(client.get_name() + ": " + next_action.get_name());
                next_action.execute();

                int wait_time = next_action.get_wait_time();
                if (wait_time > 5000 && !in_focus)
                    client.minimize();

                release_lock();
                int rand_sleep = get_sleep_time(wait_time);
                System.out.println("Sleeping (" + client.get_name() + "): " + rand_sleep + "ms");
                Thread.sleep(rand_sleep);

                fetch_lock();
                in_focus = client.in_focus();
                if (!in_focus) {
                    client.show();
                    Thread.sleep(300);
                }
                next_action = get_next_action();
            } catch (Exception e){
                release_lock();
            }
        }

        System.out.println("Task finished: " + client.get_name());
    }
}
