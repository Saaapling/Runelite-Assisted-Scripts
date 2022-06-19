package tasks;

import actions.*;
import actions.Point;
import base.Client;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class EdgevilleSapphireRings extends Task{

    ReentrantLock lock;

    public EdgevilleSapphireRings(Client client, MouseController mouse, ReentrantLock lock) {
        super(client);
        task_name = "Edgeville Crafting (Rings)";
        this.lock = lock;

        actions = new ArrayList<>();
        System.out.println(client.get_offset());
        Point[] bank_bounds = {new Point(433, 714), new Point(456, 711), new Point(447, 740), new Point(424, 737)};
        actions.add(new MouseLeftClickAction(mouse, bank_bounds, 6000, "Bank Action"));

        actions.add(new BankAllAction(mouse, 500));
        actions.add(new WithdrawAction(mouse, 1, 2, 500, "Withdraw Ring Mould"));
//        actions.add(new WithdrawXAction(mouse, 1, 4, 500,"Withdraw Sapphires"));
        actions.add(new WithdrawXAction(mouse, 1, 3, 500,"Withdraw Gold"));
        actions.add(new WithdrawXAction(mouse, 1, 3, 500,"Withdraw Gold"));

        Point[] furnace_bounds = {new Point(1378, 361), new Point(1389, 373), new Point(1354, 405), new Point(1340, 386)};
        actions.add(new MouseLeftClickAction(mouse, furnace_bounds, 6000, "Furnace Action"));

//        Point make_ring = new Point(685, 375);
        Point make_ring = new Point(635, 375);
        actions.add(new MouseLeftClickAction(mouse, make_ring, 8, 54000, "Make Rings"));

    }

    public void run() {
        System.out.println("Starting Task: Edgeville Crafting (" + client.get_name() + ")");

        try {
            while (true) {
                boolean locked = false;
                for (Action action : actions) {
                    if (!locked){
                        lock.lock();
                        locked = true;
                    }

                    boolean minimize = client.get_window_status();
                    // Todo: Fix me, get_window_status does not properly recognize when runelite is not in focus
                    minimize = true;
                    if (minimize) {
                        client.show();
                        Thread.sleep(300);
                    }

                    System.out.println("Executing Action " + client.get_name() + ":" + action.get_name());
                    action.execute();

                    if (minimize && action.get_wait_time() > 5000) {
                        client.minimize();
                    }


                    int rand_sleep = action.get_wait_time() + (int) (Math.random() * Math.min(5000, Math.max(action.get_wait_time(), 50) / 5));
                    System.out.println("Sleeping: " + client.get_name() + ":" + rand_sleep + "ms");

                    if (action.get_wait_time() > 5000) {
                        lock.unlock();
                        locked = false;
                    }
                    Thread.sleep(rand_sleep);
                }
            }
        } catch (InterruptedException | AWTException e) {
                throw new RuntimeException(e);
        }
    }
}
