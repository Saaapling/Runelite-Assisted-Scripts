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

    int failsafe_counter = -4;
    boolean caught = false;

    public RedSalamanderHunter(Client client, InputController mouse, ReentrantLock lock) {
        super(client, mouse, lock);

        populate_actions();
    }

    private void populate_actions(){
        // Movement Actions
        actions.put("Move to Trap 1", new MouseLeftClickAction(mouse, new Point(1191, 878), 8, 5000, "Move to Trap 1"));
        actions.put("Step to Trap 1", new MouseLeftClickAction(mouse, new Point(945, 580), 15, 1800, "Step to Trap 1"));

        actions.put("Move to Trap 2", new MouseLeftClickAction(mouse, new Point(716, 242), 8, 4300, "Move to Trap 2"));
        actions.put("Step to Trap 2", new MouseLeftClickAction(mouse, new Point(935, 465), 15, 1800, "Step to Trap 2"));

        actions.put("Move to Trap 3", new MouseLeftClickAction(mouse, new Point(834, 479), 8, 3000, "Move to Trap 3"));
        actions.put("Step to Trap 3", new MouseLeftClickAction(mouse, new Point(880, 465), 15, 1800, "Step to Trap 3"));

        actions.put("Move to Trap 4", new MouseLeftClickAction(mouse, new Point(1106, 464), 8, 3500, "Move to Trap 4"));
        actions.put("Step to Trap 4", new MouseLeftClickAction(mouse, new Point(990, 520), 15, 1800, "Step to Trap 4"));

        // Set Trap Actionss
        actions.put("Set Trap 1", new MouseLeftClickAction(mouse, new Point(950, 593), 7,3200, "Set Trap 1"));
        actions.put("Set Trap 2", new MouseLeftClickAction(mouse, new Point(995, 518), 7, 3200, "Set Trap 2"));
        actions.put("Set Trap 3", new MouseLeftClickAction(mouse, new Point(936, 580), 7, 3200, "Set Trap 3"));
        actions.put("Set Trap 4", new MouseLeftClickAction(mouse, new Point(948, 460), 7, 12200, "Set Trap 4"));

        // Pick up Bounds
        actions.put("Pick up Item 1", new MouseLeftClickAction(mouse, new Point(948, 530), 5, 300, "Pick up Item 1"));
        actions.put("Pick up Item 2", new MouseLeftClickAction(mouse, new Point(946, 534), 5, 300, "Pick up Item 2"));
        actions.put("Pick up Item 3", new MouseLeftClickAction(mouse, new Point(936, 532), 5, 300, "Pick up Item 3"));
        actions.put("Pick up Item 4", new MouseLeftClickAction(mouse, new Point(936, 522), 5, 300, "Pick up Item 4"));

        // Evaluation Actions
        actions.put("Evaluate Action 1", new DefaultAction(500, "Evaluate Action 1"));
        actions.put("Evaluate Action 2", new DefaultAction(500, "Evaluate Action 2"));
        actions.put("Evaluate Action 3", new DefaultAction(500, "Evaluate Action 3"));
        actions.put("Evaluate Action 4", new DefaultAction(500, "Evaluate Action 4"));

        // Reset Camera
        actions.put("Reset Camera", new KeyboardAction(mouse, "ctrl", "Reset Camera"));
    }

    public void populate_action_queue(){
        // Reset Camera Zoom
        action_queue.add(actions.get("Reset Camera"));

        // Movement Actions
        action_queue.add(actions.get("Move to Trap 1"));
        action_queue.add(actions.get("Evaluate Action 1"));
        action_queue.add(actions.get("Set Trap 1"));

        action_queue.add(actions.get("Move to Trap 2"));
        action_queue.add(actions.get("Evaluate Action 2"));
        action_queue.add(actions.get("Set Trap 2"));

        action_queue.add(actions.get("Move to Trap 3"));
        action_queue.add(actions.get("Evaluate Action 3"));
        action_queue.add(actions.get("Set Trap 3"));

        action_queue.add(actions.get("Move to Trap 4"));
        action_queue.add(actions.get("Evaluate Action 4"));
        action_queue.add(actions.get("Set Trap 4"));
    }

    public Action get_next_action() {
        Action next_action = action_queue.poll();

        if (next_action == null)
            return null;

        boolean found = false;
        int target_row = 0;
        int target_col = 0;
        try {
            String base_path = ".\\src\\main\\java\\tasks\\RedSalamanderHunter\\InventoryImages\\";
            for (int row = 1; row <= 7; row++) {
                for (int col = 1; col <= 4; col++) {
                    BufferedImage base_inventory_slot = ImageIO.read(new File(base_path + "Red_Salamander.png"));
                    BufferedImage inventory_slot = get_inventory_image(row, col);

                    if (image_similarity(base_inventory_slot, inventory_slot) > 0.95) {
                        failsafe_counter = 0;
                        target_row = row;
                        target_col = col;
                        found = true;
                    }
                }
            }
        } catch(IOException e){
            return null;
        }

        String action_name = next_action.get_name();
        if (action_name.startsWith("Evaluate Action")) {
            action_queue.addLast(next_action);
            char trap_num = action_name.charAt(action_name.length() - 1);
            if (found){
                action_queue.addFirst(actions.get("Step to Trap " + trap_num));
                return new InventoryAction(mouse, target_row, target_col, 500, true, "Drop Salamander");
            }else {
                failsafe_counter += 1;
                action_queue.addFirst(actions.get("Pick up Item " + trap_num));
                action_queue.addFirst(actions.get("Pick up Item " + trap_num));
                return actions.get("Pick up Item " + trap_num);
            }
        }else if (found){
            action_queue.addFirst(next_action);
            return new InventoryAction(mouse, target_row, target_col, 500, true, "Drop Salamander");
        }

        if (action_name.startsWith("Set") || action_name.startsWith("Move") || action_name.startsWith("Reset")){
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
            while (failsafe_counter < 20){
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
