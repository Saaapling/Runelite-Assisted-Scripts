package tasks.EdgevilleCrafting;

import actions.*;
import actions.Point;
import base.Client;
import base.MouseController;
import tasks.Task;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static image_parsing.ImageParser.compare_images;
import static image_parsing.ImageParser.get_inventory_image;

public class EdgevilleCrafting extends Task {

    ReentrantLock lock;
    MouseController mouse;
    static String default_task = "Bronze Bars";
    String task_type;
    int failsafe_counter = 0;

    public EdgevilleCrafting(Client client, MouseController mouse, ReentrantLock lock) {
        super(client);
        this.lock = lock;
        this.mouse = mouse;
        task_name = "Edgeville Crafting (Rings)";
        task_type = default_task;

        populate_actions();
    }

    private void populate_actions(){
        Point[] bank_bounds = {new Point(435, 716), new Point(454, 713), new Point(445, 738), new Point(426, 735)};
        actions.put("Move to Bank", new MouseLeftClickAction(mouse, bank_bounds, 11000, "Move to Bank"));
        actions.put("Bank All", new BankAllAction(mouse, 500));

        // Bank Actions
        actions.put("Withdraw Ring Mould", new WithdrawAction(mouse, 1, 2, 500, "Withdraw Ring Mould"));
        actions.put("Withdraw Gold", new WithdrawXAction(mouse, 1, 3, 500, "Withdraw Gold"));
        actions.put("Withdraw Sapphires", new WithdrawXAction(mouse, 1, 4, 500, "Withdraw Sapphires"));
        actions.put("Withdraw Copper", new WithdrawXAction(mouse, 1, 5, 500, "Withdraw Copper"));
        actions.put("Withdraw Tin", new WithdrawXAction(mouse, 1, 6, 500, "Withdraw Tin"));

        Point[] furnace_bounds = {new Point(1378, 361), new Point(1389, 373), new Point(1354, 405), new Point(1340, 386)};
        actions.put("Move to Furnace", new MouseLeftClickAction(mouse, furnace_bounds, 11000, "Move to Furnace"));

        // Make Actions
        actions.put("Make Sapphire Rings", new MouseLeftClickAction(mouse, new Point(685, 375), 8, 22000, "Make Sapphire Rings"));
        actions.put("Make Gold Rings", new MouseLeftClickAction(mouse, new Point(635, 375), 8, 45000, "Make Gold Rings"));
        actions.put("Make Bronze Bars", new MouseLeftClickAction(mouse, new Point(40, 963), 20, 45000, "Make Bronze Bars"));
    }

    public void populate_action_queue(String task){
        action_queue.add(actions.get("Move to Bank"));
        action_queue.add(actions.get("Bank All"));

        Action make_action = actions.get("Make Sapphire Rings");
        switch (task) {
            case "Sapphire Rings" -> {
                action_queue.add(actions.get("Withdraw Ring Mould"));
                action_queue.add(actions.get("Withdraw Sapphires"));
                action_queue.add(actions.get("Withdraw Gold"));
            }
            case "Gold Rings" -> {
                action_queue.add(actions.get("Withdraw Ring Mould"));
                action_queue.add(actions.get("Withdraw Gold"));
                make_action = actions.get("Make Gold Rings");
            }
            case "Bronze Bars" -> {
                action_queue.add(actions.get("Withdraw Copper"));
                action_queue.add(actions.get("Withdraw Tin"));
                make_action = actions.get("Make Bronze Bars");
            }
        }

        action_queue.add(actions.get("Move to Furnace"));
        action_queue.add(make_action);
    }


    private boolean check_task_health(String task){
        String base_path = ".\\src\\main\\java\\tasks\\EdgevilleCrafting\\InventoryImages\\";

        ArrayList<BufferedImage> expected = new ArrayList<>();
        ArrayList<BufferedImage> actual =  new ArrayList<>();
        try {
            switch (task) {
                case "Sapphire Rings" -> {
                    BufferedImage base_ring_mould = ImageIO.read(new File(base_path + "1_1_RingMould.png"));
                    BufferedImage base_sapphire = ImageIO.read(new File(base_path + "1_2_Sapphire.png"));
                    BufferedImage base_gold = ImageIO.read(new File(base_path + "7_4_GoldBar.png"));
                    expected.addAll(List.of(new BufferedImage[]{base_ring_mould, base_sapphire, base_gold}));

                    BufferedImage ring_mould = get_inventory_image(1, 1);
                    BufferedImage sapphire = get_inventory_image(1, 2);
                    BufferedImage gold = get_inventory_image(7, 4);
                    actual.addAll(List.of(new BufferedImage[]{ring_mould, sapphire, gold}));
                }
                case "Gold Rings" -> {
                    BufferedImage base_ring_mould = ImageIO.read(new File(base_path + "1_1_RingMould.png"));
                    BufferedImage base_gold = ImageIO.read(new File(base_path + "7_4_GoldBar.png"));
                    expected.addAll(List.of(new BufferedImage[]{base_ring_mould, base_gold}));

                    BufferedImage ring_mould = get_inventory_image(1, 1);
                    BufferedImage gold = get_inventory_image(7, 4);
                    actual.addAll(List.of(new BufferedImage[]{ring_mould, gold}));
                }
                case "Bronze Bars" -> {
                    BufferedImage base_copper = ImageIO.read(new File(base_path + "1_1_CopperOre.png"));
                    BufferedImage base_tin = ImageIO.read(new File(base_path + "7_4_TinOre.png"));
                    expected.addAll(List.of(new BufferedImage[]{base_copper, base_tin}));

                    BufferedImage copper = get_inventory_image(1, 1);
                    BufferedImage tin = get_inventory_image(7, 4);
                    actual.addAll(List.of(new BufferedImage[]{copper, tin}));
                }
            }
        } catch (AWTException | IOException e){
            failsafe_counter += 1;
            return false;
        }

        for (int i = 0; i < expected.size(); i++){
            if (!compare_images(expected.get(i), actual.get(i))) {
                failsafe_counter += 1;
                return false;
            }
        }

        failsafe_counter = Math.min(0, failsafe_counter - 1);
        return true;
    }

    public Action get_next_action(){
        Action next_action = action_queue.poll();

        if (next_action == null)
            return null;

        action_queue.addLast(next_action);
        if (next_action.get_name().contains("Make")){
            // If the task is in an unhealthy state, skip the make step and try to fetch items from the bank again
            if (!check_task_health(task_type)) {
                if (failsafe_counter > 2){
                    System.out.println("Task state is unhealthy, exiting current task");
                    return null;
                }

                System.out.println("Task state is unhealthy, skipping current step: " + next_action.get_name());
                System.out.println("Consecutive failures: " + failsafe_counter);
                return get_next_action();
            }
        }

        return next_action;
    }

    public void fetch_lock(boolean lock_state){
        if (!lock_state)
            lock.lock();
    }

    public static void set_default_task(String type){
        default_task = type;
    }

    public void set_task(String type){
        task_type = type;
    }

    public void run() {
        System.out.println("Starting Task: Edgeville Crafting (" + client.get_name() + ")");
        populate_action_queue(task_type);

        Action next_action = get_next_action();
        boolean in_focus = true;
        boolean locked = true;
        fetch_lock(false);

        while (next_action != null) {
            try {
                System.out.println(client.get_name() + ": " + next_action.get_name());
                next_action.execute();
                int wait_time = next_action.get_wait_time();

                if (wait_time > 5000) {
                    if (!in_focus)
                        client.minimize();
                    lock.unlock();
                    locked = false;
                }

                int rand_sleep = get_sleep_time(wait_time);
                System.out.println("Sleeping (" + client.get_name() + "): " + rand_sleep + "ms");
                Thread.sleep(rand_sleep);

                fetch_lock(locked);
                locked = true;

                in_focus = client.in_focus();
                if (!in_focus) {
                    client.show();
                    Thread.sleep(300);
                }
                next_action = get_next_action();
            } catch (Exception e){
                lock.unlock();
                locked = false;
            }
        }

        System.out.println("Task finished: " + client.get_name());
    }
}
