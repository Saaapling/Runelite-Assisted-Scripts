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

public class AFKCombatManager extends AFKCombatLooter{

    boolean full = false;
    boolean user_initiated_combat = false;

    public AFKCombatManager(Client client, MouseController mouse, ReentrantLock lock) throws IOException {
        super(client, mouse, lock);
        client.update_inventory();
    }

    public Action get_next_action(){
        Color npc_rgb = new Color(0 , 255, 255);
        Color item_rgb = new Color(255, 150, 255);

        // Target has been found, check that it has not moved before attacking
        Action next_action = action_queue.poll();
        if (next_action != null && next_action.get_name().equals("Attack monster")){
            MouseAction action = (MouseAction) next_action;
            actions.Point target = action.get_random_point_in_bounds();
            if (npc_rgb.equals(ImageParser.get_color(target))){
                user_initiated_combat = true;
                return next_action;
            }
        }

        // Take a screenshot (faster when computing multiple (hundreds) of points)
        BufferedImage image = ImageParser.get_screenshot();

        // Check player status (health, prayer, buffs)
        try {
            client.update_status();
            if (client.get_health() < 50){
                Point food_slot = client.get_food_slot();
                if (food_slot != null){
                    // Consume Food
                    client.consume_item((int) food_slot.getX(), (int) food_slot.getY());
                    return new InventoryAction(mouse, (int) food_slot.getX(), (int) food_slot.getY(), 1000, "Consume Food");
                }
            }
            if (client.get_prayer() < 30){
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

        // Item has been found (and right-clicked). Pick up the item
        if (next_action != null && next_action.get_name().equals("Item Search")){
            // Find the bounds to pick up the item
            actions.Point[] item_box_bounds = find_item_text_bounds(image, item_rgb);
            if (item_box_bounds != null) {
                // Scale the waiting time based on the run distance
                Rectangle dimensions = client.get_dimensions();
                actions.Point center = new actions.Point(dimensions.getX() + dimensions.width / 2d, dimensions.getY() + dimensions.height / 2d);
                actions.Point difference = center.subtract(item_box);
                double scale = Math.sqrt(Math.pow(difference.getX(), 2) + Math.pow(difference.getY(), 2)) /
                        Math.sqrt(Math.pow(center.getX(), 2) + Math.pow(center.getY(), 2));
                int wait_time = Math.max(1500, (int) (20000 * scale));

                client.loot_item();
                return new MouseLeftClickAction(mouse, item_box_bounds, wait_time, "Loot Item");
            }
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
        if (client.check_availability()) {
            full = false;
            Point target = find_target(item_rgb, image, 0.75, 15);
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
        action_queue.add(new MouseLeftClickAction(mouse, target_center, 0, 5000, "Attack monster"));
        return new MouseMoveAction(mouse, target_center, 30, 0);
    }
}
