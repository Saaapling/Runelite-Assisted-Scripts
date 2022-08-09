package tasks.BlastFurnace;

import actions.Action;
import base.Client;
import base.InputController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import static image_parsing.ImageParser.compare_images;
import static image_parsing.ImageParser.get_inventory_image;

public class BlastFurnaceMithril extends BlastFurnaceSmelter{

    public BlastFurnaceMithril(Client client, InputController mouse, ReentrantLock lock) {
        super(client, mouse, lock);
    }

    public void populate_action_queue(){
        // First pass through (Coal + Coal)
        action_queue.add(actions.get("Withdraw Coal"));
        action_queue.add(actions.get("Close Bank"));
        action_queue.add(actions.get("Fill Coal Bag"));
        action_queue.add(actions.get("Open Bank"));
        action_queue.add(actions.get("Withdraw Coal"));
        action_queue.add(actions.get("Close Bank"));

        action_queue.add(actions.get("Move to Belt"));
        action_queue.add(actions.get("Mouseover Coal Bag"));
        action_queue.add(actions.get("Empty Coal Bag"));
        action_queue.add(actions.get("Belt Deposit (Adjacent)"));
        action_queue.add(actions.get("Move to Bank (Conveyor)"));

        // Second pass through (Coal + Mithril)
        action_queue.add(actions.get("Withdraw Coal"));
        action_queue.add(actions.get("Close Bank"));
        action_queue.add(actions.get("Fill Coal Bag"));
        action_queue.add(actions.get("Open Bank"));
        action_queue.add(actions.get("Withdraw Mithril Ore"));
        action_queue.add(actions.get("Close Bank"));

        action_queue.add(actions.get("Move to Belt"));
        action_queue.add(actions.get("Mouseover Coal Bag"));
        action_queue.add(actions.get("Empty Coal Bag"));
        action_queue.add(actions.get("Belt Deposit (Adjacent)"));
        action_queue.add(actions.get("Move to Collector"));
        action_queue.add(actions.get("Bar Collection (Adjacent)"));
        action_queue.add(actions.get("Collect Bars"));
        action_queue.add(actions.get("Move to Bank"));
        action_queue.add(actions.get("Deposit Items"));

        // Third pass through (Coal + Mithril)
        action_queue.add(actions.get("Withdraw Coal"));
        action_queue.add(actions.get("Close Bank"));
        action_queue.add(actions.get("Fill Coal Bag"));
        action_queue.add(actions.get("Open Bank"));
        action_queue.add(actions.get("Withdraw Mithril Ore"));
        action_queue.add(actions.get("Close Bank"));

        action_queue.add(actions.get("Move to Belt"));
        action_queue.add(actions.get("Mouseover Coal Bag"));
        action_queue.add(actions.get("Empty Coal Bag"));
        action_queue.add(actions.get("Belt Deposit (Adjacent)"));
        action_queue.add(actions.get("Move to Collector"));
        action_queue.add(actions.get("Bar Collection (Adjacent)"));
        action_queue.add(actions.get("Collect Bars"));
        action_queue.add(actions.get("Move to Bank"));
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

            // Check whether the coal was taken out of the bag
            if (next_action.get_name().equals("Belt Deposit (Adjacent)")) {
                BufferedImage base_inventory_slot = ImageIO.read(new File(base_path + "1_2_Empty.png"));
                BufferedImage inventory_slot = get_inventory_image(1, 2);

                if (compare_images(base_inventory_slot, inventory_slot)) {
                    failsafe_counter += 1;
                    extra_steps += 1;
                    action_queue.addFirst(actions.get(next_action.get_name()));
                    return actions.get("Empty Coal Bag");
                }
            }

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

}
