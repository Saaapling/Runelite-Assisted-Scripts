package tasks.AFKCombatHelper;

import actions.*;
import actions.Point;
import base.Client;
import base.MouseController;
import image_parsing.ImageParser;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import static base.Utils.print;

public class AFKKrackenHelper extends AFKCombatLooter{

    boolean full = false;
    boolean user_initiated_combat = false;

    public AFKKrackenHelper(Client client, MouseController mouse, ReentrantLock lock) throws IOException {
        super(client, mouse, lock);
        client.update_inventory();

        actions.put("Attack Kraken", new MouseLeftClickAction(mouse, new Point(940, 405), 50, 10000, "Attack Kraken"));
    }

    private boolean check_water(Color target){
        if (35 < target.getRed() && target.getRed() < 55)
            if (90 < target.getGreen() && target.getGreen() < 110)
                return 70 < target.getBlue() && target.getBlue() < 90;
        return false;
    }

    private boolean check_kraken(Color target){
        return target.getBlue() < 30;
    }

    public Action get_next_action(){

        // Target has been found, check that it has not moved before attacking
        Action next_action = action_queue.poll();

        if (next_action != null && next_action.get_name().equals("Attack Kraken")){
            return next_action;
        }

        // Take a screenshot (faster when computing multiple (hundreds) of points)
        BufferedImage image = ImageParser.get_screenshot();

        // Check player status (health, prayer, buffs)
        try {
            client.update_status();
            if (client.get_health() < 70){
                Point food_slot = client.get_food_slot();
                if (food_slot != null){
                    // Consume Food
                    client.consume_item((int) food_slot.getX(), (int) food_slot.getY());
                    // Continue attacking the kraken
                    if (get_combat_status()){
                        action_queue.add(actions.get("Attack Kraken"));
                    }
                    return new InventoryAction(mouse, (int) food_slot.getX(), (int) food_slot.getY(), 500, "Consume Food");
                }
            }
        } catch (IOException ignored) { }

        // Item has been found (and right-clicked). Pick up the item
        if (next_action != null && next_action.get_name().equals("Item Search")){
            // Find the bounds to pick up the item
            actions.Point[] item_box_bounds = find_item_text_bounds(image, item_rgb);
            if (item_box_bounds != null) {
                failed_loot_attempts = 0;
                client.loot_item();
                return new MouseLeftClickAction(mouse, item_box_bounds, 1500, "Loot Item");
            }
            failed_loot_attempts += 1;
        }

        // Check whether the player is already in combat
        // If the user is in combat, but not by its own volition (auto-retaliate, go loot), otherwise, wait
        if (get_combat_status()) {
            if (user_initiated_combat)
                return actions.get("Default Wait");
        }else{
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
        if (get_combat_status()) {
            return actions.get("Default Wait");
        }

        // Attack new kraken spawn
        if (check_kraken(ImageParser.get_color(new Point(1055, 348)))
                || check_water(ImageParser.get_color(new Point(938, 405)))
                || check_water(ImageParser.get_color(new Point(1055, 348)))){
            // Kraken not yet spawned
            return actions.get("Default Wait");
        }

        // Quit if we're out of food
        if (client.get_health() < 50 && client.get_food_slot() != null) {
            failsafe_counter = Integer.MAX_VALUE;
            return null;
        }

        failed_loot_attempts = 0;
        user_initiated_combat = true;
        action_queue.add(actions.get("Attack Kraken"));
        return new InventoryAction(mouse, 1, 1, 300, "Fishing Explosive");
    }
}
