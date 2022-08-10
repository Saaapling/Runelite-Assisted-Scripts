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
        Point[] trap1_move_bounds = {new Point(1260, 984), new Point(1284, 985), new Point(1286, 1001), new Point( 1260, 1001)};
        actions.put("Move to Trap 1", new MouseLeftClickAction(mouse, trap1_move_bounds, 5000, "Move to Trap 1"));
        Point[] trap1_step_bounds = {new Point(935, 600), new Point(960, 600), new Point(960, 620), new Point(935, 620)};
        actions.put("Step to Trap 1", new MouseLeftClickAction(mouse, trap1_step_bounds, 1000, "Step to Trap 1"));

        Point[] trap2_move_bounds = {new Point(698, 212), new Point(720, 212), new Point(719, 230), new Point(696, 227)};
        actions.put("Move to Trap 2", new MouseLeftClickAction(mouse, trap2_move_bounds, 4300, "Move to Trap 2"));
        Point[] trap2_step_bounds = {new Point(910, 458), new Point(955, 458), new Point(955, 498), new Point(910, 498)};
        actions.put("Step to Trap 2", new MouseLeftClickAction(mouse, trap2_step_bounds, 1000, "Step to Trap 2"));

        Point[] trap3_move_bounds = {new Point(823, 497), new Point(837, 496), new Point(837, 507), new Point(822, 502)};
        actions.put("Move to Trap 3", new MouseLeftClickAction(mouse, trap3_move_bounds, 3000, "Move to Trap 3"));
        Point[] trap3_step_bounds = {new Point(870, 467), new Point(907, 467), new Point(907, 480), new Point(870, 479)};
        actions.put("Step to Trap 3", new MouseLeftClickAction(mouse, trap3_step_bounds, 2000, "Step to Trap 3"));

        Point[] trap4_move_bounds = {new Point(1117, 428), new Point(1137, 429), new Point(1140, 452), new Point(1119, 454)};
        actions.put("Move to Trap 4", new MouseLeftClickAction(mouse, trap4_move_bounds, 3500, "Move to Trap 4"));
        Point[] trap4_step_bounds = {new Point(1000, 495), new Point(1030, 495), new Point(1030, 525), new Point(1000, 525)};
        actions.put("Step to Trap 4", new MouseLeftClickAction(mouse, trap4_step_bounds, 1000, "Step to Trap 4"));

        // Set Trap Actionss
        Point[] trap1_set_bounds = {new Point(942, 559), new Point(960, 561), new Point(959, 597), new Point(941, 601)};
        actions.put("Set Trap 1", new MouseLeftClickAction(mouse, trap1_set_bounds, 3200, "Set Trap 1"));
        Point[] trap2_set_bounds = {new Point(992, 492), new Point(1005, 494), new Point(1005, 536), new Point(991, 531)};
        actions.put("Set Trap 2", new MouseLeftClickAction(mouse, trap2_set_bounds, 3200, "Set Trap 2"));
        Point[] trap3_set_bounds = {new Point(926, 548), new Point(943, 551), new Point(945, 580), new Point(927, 578)};
        actions.put("Set Trap 3", new MouseLeftClickAction(mouse, trap3_set_bounds, 3200, "Set Trap 3"));
        Point[] trap4_set_bounds = {new Point(934, 432), new Point(959, 430), new Point(954, 474), new Point(941, 474)};
        actions.put("Set Trap 4", new MouseLeftClickAction(mouse, trap4_set_bounds, 8000, "Set Trap 4"));

        // Pick up Bounds
        Point[] trap1_item_bounds = {new Point(933, 530), new Point(959, 529), new Point(961, 542), new Point(933, 540)};
        actions.put("Pick up Item 1", new MouseLeftClickAction(mouse, trap1_item_bounds, 500, "Pick up Item 1"));
        Point[] trap2_item_bounds = {new Point(921, 515), new Point(949, 516), new Point(947, 542), new Point(919, 539)};
        actions.put("Pick up Item 2", new MouseLeftClickAction(mouse, trap2_item_bounds, 500, "Pick up Item 2"));
        Point[] trap3_item_bounds = {new Point(921, 514), new Point(945, 514), new Point(952, 527), new Point(920, 530)};
        actions.put("Pick up Item 3", new MouseLeftClickAction(mouse, trap3_item_bounds, 500, "Pick up Item 3"));
        Point[] trap4_item_bounds = {new Point(937, 515), new Point(964, 517), new Point(964, 541), new Point(934, 541)};
        actions.put("Pick up Item 4", new MouseLeftClickAction(mouse, trap4_item_bounds, 500, "Pick up Item 4"));

        // Evaluation Actions
        actions.put("Evaluate Action 1", new DefaultAction(500, "Evaluate Action 1"));
        actions.put("Evaluate Action 2", new DefaultAction(500, "Evaluate Action 2"));
        actions.put("Evaluate Action 3", new DefaultAction(500, "Evaluate Action 3"));
        actions.put("Evaluate Action 4", new DefaultAction(500, "Evaluate Action 4"));
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
            char trap_num = action_name.charAt(action_name.length() - 1);
            try {
                String base_path = ".\\src\\main\\java\\tasks\\RedSalamanderHunter\\InventoryImages\\";
                for (int row = 1; row <= 7; row++) {
                    for (int col = 1; col <= 4; col++) {
                        BufferedImage base_inventory_slot = ImageIO.read(new File(base_path + "Red_Salamander.png"));
                        BufferedImage inventory_slot = get_inventory_image(row, col);

                        if (image_similarity(base_inventory_slot, inventory_slot) > 0.95) {
                            failsafe_counter = 0;
                            action_queue.addFirst(actions.get("Step to Trap " + trap_num));
                            return new InventoryAction(mouse, row, col, 500, true, "Drop Salamander");
                        }
                    }
                }
                failsafe_counter += 1;
                return actions.get("Pick up Item " + trap_num);

            } catch(IOException e){
                return null;
            }
        }

        if (action_name.startsWith("Set") || action_name.startsWith("Move")){
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
