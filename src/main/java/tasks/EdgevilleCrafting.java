package tasks;

import actions.Action;
import actions.MouseController;
import actions.MouseLeftClickAction;
import actions.Point;
import base.Client;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class EdgevilleCrafting extends Task{

    ReentrantLock lock;

    public EdgevilleCrafting(Client client, MouseController mouse, ReentrantLock lock) {
        super(client);
        task_name = "Edgeville Crafting (Rings)";
        this.lock = lock;

        actions = new ArrayList<>();
        System.out.println(client.get_offset());
//        Point[] bank_bounds = {new Point(433, 714), new Point(456, 711), new Point(447, 740), new Point(424, 737)};
//        actions.add(new MouseLeftClickAction(mouse, bank_bounds, 6000, "Bank Action"));
//
//        Point[] furnace_bounds = {new Point(1378, 361), new Point(1389, 373), new Point(1354, 405), new Point(1340, 386)};
//        actions.add(new MouseLeftClickAction(mouse, furnace_bounds, 6000, "Furnace Action"));

        Point center = new Point(client.get_dimensions().x + client.get_dimensions().width / 2, client.get_dimensions().y + client.get_dimensions().height / 2);
        actions.add(new MouseLeftClickAction(mouse, center, 0, 6000, "Furnace Action"));
    }

    public void run() {
        System.out.println("Starting Task: Edgeville Crafting (" + client.get_name() + ")");
        try {
            while (true) {
                for (Action action : actions) {
                    lock.lock();

                    boolean minimize = client.get_window_status();
                    if (minimize)
                        client.show();

                    System.out.println("Executing Action: " + action.get_name());
                    action.execute();

                    if (minimize)
                        client.minimize();

                    lock.unlock();

                    int rand_sleep = action.get_wait_time() + (int) (Math.random() * Math.min(5000, Math.max(action.get_wait_time(), 50) / 5));
                    Thread.sleep(rand_sleep);
                }
            }
        } catch (InterruptedException | AWTException e) {
                throw new RuntimeException(e);
        }
    }
}
