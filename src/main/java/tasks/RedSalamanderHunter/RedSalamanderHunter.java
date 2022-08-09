package tasks.RedSalamanderHunter;

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
import static image_parsing.ImageParser.*;

public class RedSalamanderHunter extends InteractionTask {

    int failsafe_counter = 0;
    boolean caught = false;

    public RedSalamanderHunter(Client client, InputController mouse, ReentrantLock lock) {
        super(client, mouse, lock);

        populate_actions();
    }

    private void populate_actions(){
        // Movement Actions
        Point[] trap1_move_bounds = {new Point(1112, 757), new Point(1129, 757), new Point(1129, 770), new Point( 1117, 769)};
        actions.put("Move to Trap 1", new MouseLeftClickAction(mouse, trap1_move_bounds, 5000, "Move to Trap 1"));
        Point[] trap1_step_bounds = {new Point(932, 554), new Point(960, 556), new Point(958, 569), new Point(933, 567)};
        actions.put("Step to Trap 1", new MouseLeftClickAction(mouse, trap1_step_bounds, 1000, "Step to Trap 1"));

        Point[] trap2_move_bounds = {new Point(811, 359), new Point(827, 357), new Point(827, 371), new Point(812, 371)};
        actions.put("Move to Trap 2", new MouseLeftClickAction(mouse, trap2_move_bounds, 4300, "Move to Trap 2"));
        Point[] trap2_step_bounds = {new Point(925, 482), new Point(948, 482), new Point(949, 501), new Point(926, 502)};
        actions.put("Step to Trap 2", new MouseLeftClickAction(mouse, trap2_step_bounds, 1000, "Step to Trap 2"));

        Point[] trap3_move_bounds = {new Point(867, 504), new Point(890, 504), new Point(890, 508), new Point(867, 508)};
        actions.put("Move to Trap 3", new MouseLeftClickAction(mouse, trap3_move_bounds, 3000, "Move to Trap 3"));
        Point[] trap3_step_bounds = {new Point(895, 494), new Point(922, 493), new Point(922, 497), new Point(895, 497)};
        actions.put("Step to Trap 3", new MouseLeftClickAction(mouse, trap3_step_bounds, 2000, "Step to Trap 3"));

        Point[] trap4_move_bounds = {new Point(1029, 479), new Point(1045, 479), new Point(1046, 493), new Point(1029, 491)};
        actions.put("Move to Trap 4", new MouseLeftClickAction(mouse, trap4_move_bounds, 3500, "Move to Trap 4"));
        Point[] trap4_step_bounds = {new Point(964, 507), new Point(991, 505), new Point(992, 528), new Point(966, 524)};
        actions.put("Step to Trap 4", new MouseLeftClickAction(mouse, trap4_step_bounds, 1000, "Step to Trap 4"));

        // Set Trap Actionss
        Point[] trap1_set_bounds = {new Point(941, 545), new Point(953, 547), new Point(950, 561), new Point(941, 563)};
        actions.put("Set Trap 1", new MouseLeftClickAction(mouse, trap1_set_bounds, 3000, "Set Trap 1"));
        Point[] trap2_set_bounds = {new Point(966, 507), new Point(977, 506), new Point(976, 527), new Point(966, 531)};
        actions.put("Set Trap 2", new MouseLeftClickAction(mouse, trap2_set_bounds, 3000, "Set Trap 2"));
        Point[] trap3_set_bounds = {new Point(932, 533), new Point(943, 532), new Point(947, 553), new Point(935, 557)};
        actions.put("Set Trap 3", new MouseLeftClickAction(mouse, trap3_set_bounds, 3000, "Set Trap 3"));
        Point[] trap4_set_bounds = {new Point(939, 479), new Point(949, 477), new Point(949, 495), new Point(940, 495)};
        actions.put("Set Trap 4", new MouseLeftClickAction(mouse, trap4_set_bounds, 3000, "Set Trap 4"));

        // Pick up Bounds
        Point[] ground_item_bounds = {new Point(931, 525), new Point(947, 525), new Point(947, 531), new Point(931, 531)};
        actions.put("Pick up Item", new MouseLeftClickAction(mouse, ground_item_bounds, 500, "Pick up Item"));
        actions.put("Pick up Item (Trap 3)", new MouseLeftClickAction(mouse, ground_item_bounds, 500, "Pick up Item (Trap 3)"));

        // Evaluation Actions
        actions.put("Evaluate Action 1", new DefaultAction(500, "Evaluate Action 1"));
        actions.put("Evaluate Action 2", new DefaultAction(500, "Evaluate Action 2"));
        actions.put("Evaluate Action 3", new DefaultAction(500, "Evaluate Action 3"));
        actions.put("Evaluate Action 4", new DefaultAction(500, "Evaluate Action 4`"));
    }

    public void populate_action_queue(){
        // Movement Actions
        action_queue.add(actions.get("Move to Trap 1"));
        action_queue.add(actions.get("Evaluate Action 1"));
        action_queue.add(actions.get("Set Trap 1"));

        action_queue.add(actions.get("Move to Trap 2"));
        action_queue.add(actions.get("Evaluate Action 2"));
        action_queue.add(actions.get("Set Trap 2"));

        action_queue.add(actions.get("Move to Trap 3"));
        action_queue.add(actions.get("Evaluate Action 3"));
        action_queue.add(actions.get("Pick up Item (Trap 3)"));
        action_queue.add(actions.get("Pick up Item (Trap 3)"));
        action_queue.add(actions.get("Set Trap 3"));

        action_queue.add(actions.get("Move to Trap 4"));
        action_queue.add(actions.get("Evaluate Action 4"));
        action_queue.add(actions.get("Set Trap 4"));
    }

    public Action get_next_action() {
        Action next_action = action_queue.poll();

        if (next_action == null)
            return null;

        String action_name = next_action.get_name();
        if (action_name.startsWith("Evaluate Action")) {
            action_queue.addLast(next_action);
            try {
                String base_path = ".\\src\\main\\java\\tasks\\RedSalamanderHunter\\InventoryImages\\";
                for (int row = 1; row <= 7; row++) {
                    for (int col = 1; col <= 4; col++) {
                        BufferedImage base_inventory_slot = ImageIO.read(new File(base_path + "Red_Salamander.png"));
                        BufferedImage inventory_slot = get_inventory_image(row, col);

                        if (image_similarity(base_inventory_slot, inventory_slot) > 0.95) {
                            failsafe_counter = 0;
                            action_queue.addFirst(actions.get("Step to Trap " + action_name.charAt(action_name.length() - 1)));
                            return new InventoryAction(mouse, row, col, 500, true, "Drop Salamander");
                        }
                    }
                }
                failsafe_counter += 1;
                return actions.get("Pick up Item");

            } catch(IOException e){
                return null;
            }
        }

        if (action_name.startsWith("Set") || action_name.startsWith("Move") || action_name.endsWith("(Trap 3)")){
            action_queue.addLast(next_action);
        }


        return next_action;
    }

    public void run() {
        populate_action_queue();
        System.out.println("Starting Task: Red Salamander Hunter (" + client.get_name() + ")");

        fetch_lock();
        try {
            focus_client();
            Action next_action = get_next_action();
            while (failsafe_counter < 12){
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
