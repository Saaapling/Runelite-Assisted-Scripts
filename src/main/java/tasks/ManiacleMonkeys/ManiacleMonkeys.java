package tasks.ManiacleMonkeys;

import actions.*;
import actions.Point;
import base.Client;
import base.InputController;
import image_parsing.ImageParser;
import tasks.InteractionTask;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantLock;

import static base.Utils.print;

public class ManiacleMonkeys extends InteractionTask {

    LocalDateTime bone_timer;
    LocalDateTime last_catch;

    public ManiacleMonkeys(Client client, InputController mouse, ReentrantLock lock) {
        super(client, mouse, lock);
        bone_timer = LocalDateTime.now().minus(1, ChronoUnit.MINUTES);
        last_catch = LocalDateTime.now();

        populate_actions();
        action_queue.add(actions.get("Reset Camera"));
    }

    private void populate_actions(){
        // Loot Bones
        actions.put("Pick up Bones 1", new MouseLeftClickAction(mouse, new Point(945, 350), 15, 1500, "Pick up Bones 1"));
        actions.put("Pick up Bones 2", new MouseLeftClickAction(mouse, new Point(765, 355), 15, 1500, "Pick up Bones 2"));
        actions.put("Pick up Bones 3", new MouseLeftClickAction(mouse, new Point(740, 790), 15, 2000, "Pick up Bones 3"));

        // Move to Trap Position
        actions.put("Move to Trap", new MouseLeftClickAction(mouse, new Point(1295, 635), 25, 2000, "Move to Trap"));

        // Set Trap
        actions.put("Set Trap", new MouseLeftClickAction(mouse, new Point(810, 465), 50, 5000, "Set Trap"));

        // Reset Camera (600 zoom position)
        actions.put("Reset Camera", new KeyboardAction(mouse, "ctrl", "Reset Camera"));
    }

    private boolean check_trap(Color target){
//        print(target.getRed());
//        print(target.getGreen());
//        print(target.getBlue());
        if (target.getBlue() < target.getRed() - 75)
            return target.getBlue() < target.getGreen() - 75;
        return false;
    }

    /*
        Steps:
            1. Check inventory
                - If 3 inventory spaces, loot bones (if available)
                    - Determine whether bones can be looted based on the 3rd bone spawn (Use a simple timer)
                    - 4 step rotation, 3 bones, 1 to the original spot
                - If no bananas, cast bones to bananas
                - If there are monkey tails, drop them
            2. Check Trap
                - If trap is available, set up the trap
            3. Default wait (3 seconds)
    */
    public Action get_next_action() {
        Action next_action = action_queue.poll();

        if (next_action != null)
            return next_action;

        try {
            client.update_inventory();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Drop Monkey Tails (MonkeyTail)
        Point item_slot = client.get_item_slot("MonkeyTail");
        if (item_slot != null) {
            last_catch = LocalDateTime.now();
            return new InventoryAction(mouse, (int) item_slot.getX(), (int) item_slot.getY(), 750, true, "Drop Monkey Tail");
        }

        // Check whether bones can be looted (Need 4 spaces, 1 for Monkey Tail, + 1 for security)
        if (client.get_empty_slots() > 4){
            // Check if bones have respawned
            if (LocalDateTime.now().isAfter(bone_timer)){
                bone_timer = LocalDateTime.now().plus(1, ChronoUnit.MINUTES);
                action_queue.addFirst(actions.get("Move to Trap"));
                action_queue.addFirst(actions.get("Pick up Bones 3"));
                action_queue.addFirst(actions.get("Pick up Bones 2"));
                return actions.get("Pick up Bones 1");
            }
        }

        // Cast Bones to Bananas (If no bananas exist)
        item_slot = client.get_item_slot("Banana");
        if (item_slot == null) {
            return new InventoryAction(mouse, 1, 1, 1500, false, "Cast Bones to Bananas");
        }

        // Take a screenshot
        BufferedImage image = ImageParser.get_screenshot();

        // Check Trap (Super ghetto check, R,G values > B value + 75 is approximately yellow
        // Coordinate: 883, 541
        if (!check_trap(ImageParser.get_color(new Point(883, 541)))){
            action_queue.addFirst(actions.get("Set Trap"));
            return new WaitAction(5000, "Trap Wait", "Trap state changed, waiting 5 seconds for animation");
        }

        return new WaitAction(1000, "Default Wait", client.get_name() + ": No available actions, waiting 1 second");
    }

    public void run() {
        System.out.println("Starting Task: Maniacle Monkey Hunter (" + client.get_name() + ")");

        fetch_lock();
        try {
            focus_client();
            Action next_action = get_next_action();
            // Expected to catch at least 1 monkey every 10 minutes
            while (LocalDateTime.now().isBefore(last_catch.plus(10, ChronoUnit.MINUTES))){
                System.out.println("Executing Action (" + client.get_name() + "): " + next_action.get_name());
                next_action.execute();

                release_lock();
                Thread.sleep(get_sleep_time(next_action.get_wait_time()));

                fetch_lock();
                focus_client();
                next_action = get_next_action();
            }
        }catch (Exception e){
            print(e.fillInStackTrace());
            release_lock();
        }

        System.out.println("Task finished: " + client.get_name());
    }

    }
