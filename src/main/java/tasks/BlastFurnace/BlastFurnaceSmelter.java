package tasks.BlastFurnace;

import actions.*;
import base.Client;
import base.InputController;
import image_parsing.Offsets;
import tasks.InteractionTask;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import static base.Utils.print;
import static image_parsing.ImageParser.compare_images;
import static image_parsing.ImageParser.get_inventory_image;

public class BlastFurnaceSmelter extends InteractionTask {

    int failsafe_counter = 0;
    int extra_steps = 0;

    public BlastFurnaceSmelter(Client client, InputController mouse, ReentrantLock lock) {
        super(client, mouse, lock);

        populate_actions();
        populate_action_queue();
    }

    private void populate_actions(){
        // Bank Actions
        actions.put("Withdraw Coal Bag", new WithdrawAction(mouse, 2, 7, 500, "Withdraw Coal Bag"));
        actions.put("Withdraw Iron Ore", new WithdrawAction(mouse, 2, 1, 500, "Withdraw Iron Ore"));
        actions.put("Withdraw Coal", new WithdrawAction(mouse, 2, 2, 500, "Withdraw Coal"));
        actions.put("Withdraw Coal (Insurance)", new WithdrawAction(mouse, 2, 2, 100, "Withdraw Coal"));
        actions.put("Withdraw Stamina Potion", new WithdrawAction(mouse, 12, 1, 500, "Withdraw Coal"));

        // Inventory Actions
        actions.put("Fill Coal Bag", new InventoryAction(mouse, 1, 1, 500, "Fill Coal Bag"));
        actions.put("Empty Coal Bag", new InventoryAction(mouse, 1, 1, 500, true, "Empty Coal Bag"));
        actions.put("Deposit Items", new InventoryAction(mouse, 1, 2, 500, "Deposit Items"));
        actions.put("Drink Stamina Potion", new InventoryAction(mouse, 1, 3, 500, "Drink Stamina Potion"));

        // Movement Actions
        Point[] belt_bounds = {new Point(789, 243), new Point(818, 239), new Point(820, 288), new Point(798, 279)};
        actions.put("Move to Belt", new MouseLeftClickAction(mouse, belt_bounds, 6000, "Move to Belt"));
        actions.put("Mouseover Coal Bag", new MouseMoveAction(mouse, Offsets.get_inventory_coordinate(1,1), 15, 100, "Mouseover Coal Bag"));
        actions.put("Move to Collector", new MouseLeftClickAction(mouse, new Point(880, 660), 12, 3750, "Move to Collector"));
        Point[] bank_bounds = {new Point(1240, 801), new Point(1274, 805), new Point(1279, 824), new Point(1244, 823)};
        actions.put("Move to Bank", new MouseLeftClickAction(mouse, bank_bounds, 5250, "Move to Bank"));

        // Misc Actions
        actions.put("Close Bank", new KeyboardAction(mouse, "escape", "Close Bank"));
        Point[] bank_bounds_adjacent = {new Point(931, 548), new Point(958, 548), new Point(934, 569), new Point(961, 572)};
        actions.put("Open Bank", new MouseLeftClickAction(mouse, bank_bounds_adjacent, 500, "Open Bank"));
        actions.put("Collect Bars", new MouseLeftClickAction(mouse, new Point(260, 960), 30, 1000, "Collect Bars"));
        actions.put("Belt Deposit (Adjacent)", new MouseLeftClickAction(mouse, new Point(975, 530), 10, 1250, "Belt Deposit (Adjacent)"));
        actions.put("Bar Collection (Adjacent)", new MouseLeftClickAction(mouse, new Point(945, 560), 12, 750, "Bar Collection (Adjacent)"));
    }

    public void populate_action_queue(){
        // Bank Actions
//        action_queue.add(actions.get("Withdraw Coal (Insurance)"));
        action_queue.add(actions.get("Withdraw Coal"));
        action_queue.add(actions.get("Close Bank"));
        action_queue.add(actions.get("Fill Coal Bag"));
        action_queue.add(actions.get("Open Bank"));
        action_queue.add(actions.get("Withdraw Iron Ore"));
        action_queue.add(actions.get("Close Bank"));

        // Movement and Smelting
        action_queue.add(actions.get("Move to Belt"));
        action_queue.add(actions.get("Mouseover Coal Bag"));
        action_queue.add(actions.get("Empty Coal Bag"));
        action_queue.add(actions.get("Belt Deposit (Adjacent)"));
        action_queue.add(actions.get("Move to Collector"));
        action_queue.add(actions.get("Bar Collection (Adjacent)"));
        action_queue.add(actions.get("Collect Bars"));
        action_queue.add(actions.get("Move to Bank"));

        // Deposit Bars
        action_queue.add(actions.get("Deposit Items"));
    }

    public Action get_next_action(){
        Action next_action = action_queue.poll();

        if (next_action == null)
            return null;

        if (extra_steps == 0) {
            failsafe_counter = 0;
            action_queue.addLast(next_action);
        } else{
            extra_steps -= 1;
        }

        // Check Stamina after returning to the bank
        if (next_action.get_name().equals("Withdraw Coal")){
            try {
                client.update_status();
                // Drink a stamina potion
                if (client.get_stamina() < 40){
                    extra_steps = 5;
                    action_queue.addFirst(actions.get("Withdraw Coal"));
                    action_queue.addFirst(actions.get("Deposit Items"));
                    action_queue.addFirst(actions.get("Open Bank"));
                    action_queue.addFirst(actions.get("Drink Stamina Potion"));
                    action_queue.addFirst(actions.get("Close Bank"));
                    return actions.get("Withdraw Stamina Potion");
                }
            } catch (IOException e) {
                return null;
            }
        }

        try {
            String base_path = ".\\src\\main\\java\\tasks\\BlastFurnace\\InventoryImages\\";

            // Check whether the ore was successfully deposited onto the conveyor belt
            if (next_action.get_name().equals("Empty Coal Bag") || next_action.get_name().equals("Move to Collector")) {
                BufferedImage base_inventory_slot = ImageIO.read(new File(base_path + "1_2_Empty.png"));
                BufferedImage inventory_slot = get_inventory_image(1, 2);

                if (!compare_images(base_inventory_slot, inventory_slot)) {
                    failsafe_counter += 1;
                    extra_steps += 1;
                    action_queue.addFirst(actions.get(next_action.get_name()));
                    return actions.get("Belt Deposit (Adjacent)");
                }
            }

            // Check whether the bars were successfully picked up from the collector
            if (next_action.get_name().equals("Move to Bank")) {
                BufferedImage base_inventory_slot = ImageIO.read(new File(base_path + "1_2_Empty.png"));
                BufferedImage inventory_slot = get_inventory_image(1, 2);

                if (compare_images(base_inventory_slot, inventory_slot)) {
                    failsafe_counter += 1;
                    extra_steps += 2;
                    action_queue.addFirst(actions.get("Move to Bank"));
                    action_queue.addFirst(actions.get("Collect Bars"));
                    return actions.get("Bar Collection (Adjacent)");
                }
            }
        } catch (IOException e){
            return null;
        }

        return next_action;
    }

    public void run() {
        System.out.println("Starting Task: Combat Helper (" + client.get_name() + ")");

        fetch_lock();
        try {
            focus_client();
            Action next_action = get_next_action();
            while (failsafe_counter < 3){
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
