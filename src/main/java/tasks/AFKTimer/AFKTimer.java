package tasks.AFKTimer;

import actions.Action;
import actions.DefaultAction;
import base.Client;
import tasks.Task;

import java.awt.*;

public class AFKTimer extends Task {


    public AFKTimer(Client client){
        super(client);
        task_name = "AFK Timer";
        actions.put("Default Action", new DefaultAction(90000));
        action_queue.add(actions.get("Default Action"));
    }

    public AFKTimer(Client client, Integer sleep_time){
        super(client);
        task_name = "AFK Timer";
        actions.put("Default Action", new DefaultAction(sleep_time));
        action_queue.add(actions.get("Default Action"));
    }

    public void run() {
        System.out.println( client.get_name() + ": Starting tasks.Task (" + task_name + ")");

        Action next_action = get_next_action();
        while (next_action != null) {
            try {
                next_action.execute();
                client.show();
                Thread.sleep(next_action.get_wait_time());
            } catch (InterruptedException | AWTException e) {
                throw new RuntimeException(e);
            }
            next_action = get_next_action();
        }
    }
}
