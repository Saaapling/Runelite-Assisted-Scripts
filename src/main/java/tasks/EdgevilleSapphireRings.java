package tasks;

import actions.*;
import actions.Point;
import base.Client;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class EdgevilleSapphireRings extends Task{

    ReentrantLock lock;
    MouseController mouse;

    public EdgevilleSapphireRings(Client client, MouseController mouse, ReentrantLock lock) {
        super(client);
        this.lock = lock;
        this.mouse = mouse;
        task_name = "Edgeville Crafting (Rings)";

        actions = new ArrayList<>();
        set_task("Gold Rings");
    }

    public void set_task(String task){
        Point[] bank_bounds = {new Point(435, 716), new Point(454, 713), new Point(445, 738), new Point(426, 735)};
        actions.add(new MouseLeftClickAction(mouse, bank_bounds, 10000, "Move to Bank"));
        actions.add(new BankAllAction(mouse, 500));

        int wait_time = 10000;
        Point make_item = new Point(0, 0);
        int make_item_size = 8;
        switch (task) {
            case "Sapphire Rings" -> {
                actions.add(new WithdrawAction(mouse, 1, 2, 500, "Withdraw Ring Mould"));
                actions.add(new WithdrawXAction(mouse, 1, 3, 500, "Withdraw Gold"));
                actions.add(new WithdrawXAction(mouse, 1, 4, 500, "Withdraw Sapphires"));
                wait_time = 27000;
                make_item = new Point(685, 375);
            }
            case "Bronze Bars" -> {
                actions.add(new WithdrawXAction(mouse, 1, 3, 500, "Withdraw Copper"));
                actions.add(new WithdrawXAction(mouse, 1, 4, 500, "Withdraw Tin"));
                wait_time = 45000;
                make_item = new Point(40, 963);
                make_item_size = 20;
            }
            case "Gold Rings" -> {
                actions.add(new WithdrawAction(mouse, 1, 2, 500, "Withdraw Ring Mould"));
                actions.add(new WithdrawXAction(mouse, 1, 3, 500, "Withdraw Gold"));
                wait_time = 54000;
                make_item = new Point(635, 375);
            }
        }

        Point[] furnace_bounds = {new Point(1378, 361), new Point(1389, 373), new Point(1354, 405), new Point(1340, 386)};
        actions.add(new MouseLeftClickAction(mouse, furnace_bounds, 10000, "Move to Furnace"));
        actions.add(new MouseLeftClickAction(mouse, make_item, make_item_size, wait_time, "Make " + task));
    }

    public void fetch_lock(boolean lock_state){
        if (!lock_state)
            lock.lock();
    }

    public void run() {
        System.out.println("Starting Task: Edgeville Crafting (" + client.get_name() + ")");
        int cycles = 200;
        boolean locked = false;


        try {
            for (int i = 1; i < cycles; i++){
                for (Action action : actions) {
                    fetch_lock(locked);
                    locked = true;

                    // Todo: Fix me, get_window_status does not properly recognize when runelite is not in focus
                    boolean minimized = client.get_window_status();
                    if (minimized) {
                        client.show();
                        Thread.sleep(300);
                    }

                    System.out.println("Cycle " + i + " (" + client.get_name() + ": " + action.get_name());
                    action.execute();

                    if (action.get_wait_time() > 5000) {
                        if (minimized)
                            client.minimize();
                        lock.unlock();
                        locked = false;
                    }

                    int rand_sleep = get_sleep_time(action.get_wait_time());
                    System.out.println("Sleeping (" + client.get_name() + "): " + rand_sleep + "ms");
                    Thread.sleep(rand_sleep);
                }
            }
        } catch (InterruptedException | AWTException e) {
                throw new RuntimeException(e);
        }
    }
}
