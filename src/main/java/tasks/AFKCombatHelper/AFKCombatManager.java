package tasks.AFKCombatHelper;

import actions.*;
import actions.Point;
import base.Client;
import base.InputController;
import image_parsing.ImageParser;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import static base.Utils.print;

public class AFKCombatManager extends AFKCombatLooter{

    boolean full = false;
    boolean user_initiated_combat = false;

    public AFKCombatManager(Client client, InputController mouse, ReentrantLock lock) throws IOException {
        super(client, mouse, lock);
        client.update_inventory();
        actions.put("Select Magic Tab", new KeyboardAction(mouse, "F2", "Select Magic Tab"));
        actions.put("Select Inventory Tab",  new KeyboardAction(mouse, "F1", "Select Inventory Tab", 1500));
        actions.put("Select High Alchemy", new MouseLeftClickAction(mouse, new Point(1854,862), 5,500, "Select High Alchemy"));
    }

    public Action get_next_action(){
        Action next_action = action_queue.poll();

        // Cast/Check for Alchemy targets
        if (next_action != null && (next_action.get_name().contains("Alchemy") || next_action.get_name().contains("Tab"))){
            return next_action;
        }

        Point alch_item = client.check_alchemy();
        Point temp;
        while (alch_item != null){
            temp = alch_item;
            try {
                client.update_inventory_slot((int) alch_item.getX(), (int) alch_item.getY());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            alch_item = client.check_alchemy();
            if (temp.equals(alch_item)){
                break;
            }
        }
        if (alch_item != null){
            action_queue.addFirst(actions.get("Select Inventory Tab"));
            action_queue.addFirst(new InventoryAction(mouse, (int) alch_item.getX(), (int) alch_item.getY(), 500, "Cast High Alchemy"));
            action_queue.addFirst(actions.get("Select High Alchemy"));
            return actions.get("Select Magic Tab");
        }

        // Check player status (health, prayer, buffs)
        try {
            client.update_status();
            if (client.get_health() < 50){
                Point food_slot = client.get_food_slot();
                if (food_slot != null){
                    // Consume Food
                    client.consume_item((int) food_slot.getX(), (int) food_slot.getY());
                    return new InventoryAction(mouse, (int) food_slot.getX(), (int) food_slot.getY(), 1000, "Consume Food");
                }else {
                    failsafe_counter = Integer.MAX_VALUE;
                    return null;
                }
            }
            if (client.get_prayer() < 10){
                Point prayer_item = client.get_prayer_slot();
                if (prayer_item != null){
                    // Consume Prayer Potion
                    client.consume_item((int) prayer_item.getX(), (int) prayer_item.getY());
                    return new InventoryAction(mouse, (int) prayer_item.getX(), (int) prayer_item.getY(), 1000, "Consume Prayer Potion");
                }
            }
            Point buff_item = client.check_consumes();
            if (buff_item != null){
                // Consume Buff Item (Super Combat/Antifire, etc.)
                client.consume_item((int) buff_item.getX(), (int) buff_item.getY());
                return new InventoryAction(mouse, (int) buff_item.getX(), (int) buff_item.getY(), 1000, "Refresh Buff");
            }
        } catch (IOException ignored) { }

        // Target has been found, check that it has not moved before attacking
        if (next_action != null && next_action.get_name().equals("Attack monster")){
            MouseAction action = (MouseAction) next_action;
            actions.Point target = action.get_random_point_in_bounds();
            if (npc_rgb.equals(ImageParser.get_color(target))){
                user_initiated_combat = true;
                failed_loot_attempts = 0;
                return next_action;
            }
        }

        // Take a screenshot (faster when computing multiple (hundreds) of points)
        BufferedImage image = ImageParser.get_screenshot();

        // Item has been found (and right-clicked). Pick up the item
        if (next_action != null && next_action.get_name().equals("Item Search")){
            // Find the bounds to pick up the item
            actions.Point[] item_box_bounds = find_item_text_bounds(image, item_rgb);
            if (item_box_bounds != null) {
                failed_loot_attempts = 0;
                // Scale the waiting time based on the run distance
                Rectangle dimensions = client.get_dimensions();
                actions.Point center = new actions.Point(dimensions.getX() + dimensions.width / 2d, dimensions.getY() + dimensions.height / 2d);
                actions.Point difference = center.subtract(item_box);
                double scale = Math.sqrt(Math.pow(difference.getX(), 2) + Math.pow(difference.getY(), 2)) /
                        Math.sqrt(Math.pow(center.getX(), 2) + Math.pow(center.getY(), 2));
                int wait_time = Math.max(1500, (int) (20000 * scale));

                return new MouseLeftClickAction(mouse, item_box_bounds, wait_time, "Loot Item");
            }
            failed_loot_attempts += 1;
        }

        // If the user is in combat, but not by its own volition (auto-retaliate, go loot), otherwise, wait
        boolean in_combat = false;
        if (get_combat_status()) {
            if (user_initiated_combat)
                return actions.get("Default Wait");
            in_combat = true;
        } else if (failsafe_counter > 3) {
            return null;
        } else{
            user_initiated_combat = false;
        }

        // Check for items to loot
        if (client.check_availability() && failed_loot_attempts < 3) {
            full = false;
            Point target = find_target(item_rgb, image, 1, 5);
            if (target != null) {
                System.out.println(client.get_name() + ": Found item to loot");
                actions.Point item_text_center = find_text_center(item_rgb, image, target);
                actions.Point item_center = new actions.Point(item_text_center.getX(), item_text_center.getY() + 10);
                action_queue.add(actions.get("Item Search"));
                item_box = Point.get_random_point(Point.generate_rectangle(item_center, 10));
                return new MouseRightClickAction(mouse, item_box, 0, 100, "Target Item");
            }
        }else{
            try {
                // Prevent repeated inventory updates when inventory is full
                if (!full) {
                    client.update_inventory();
                    full = client.check_availability();
                }
            } catch (IOException ignored) { }
        }

        // No ground items to loot, if auto-retaliate already found a target, go ahead and fight it
        if (in_combat) {
            return actions.get("Default Wait");
        }

        // Find a new target to attack
        System.out.println(client.get_name() + ": Finding new target");
        Point target = find_target(npc_rgb, image);
        if (target == null){
            failsafe_counter += 1;
            return actions.get("Passive Wait");
        } else{
            failsafe_counter = 0;
        }
        print(client.get_name() + ": Target found");
        Point target_center = get_target_center(target, npc_rgb, image);
        action_queue.add(new MouseLeftClickAction(mouse, target_center, 0, 3000, "Attack monster"));
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

                // Post Execution items (Updating Inventory, etc.)
                if (next_action.get_name().contains("Loot")){
                    client.loot_item();
                }

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
