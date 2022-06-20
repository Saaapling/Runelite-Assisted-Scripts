package tasks;

import actions.Action;
import actions.DefaultAction;
import base.Client;

import java.awt.*;

public class DefaultTask extends Task {


    public DefaultTask(Client client){
        super(client);
        task_name = "Default tasks.Task";
        actions.put("Default Action", new DefaultAction(5000));
        action_queue.add(actions.get("Default Action"));
    }

    public void run() {
        System.out.println( client.get_name() + ": Starting tasks.Task (" + task_name + ")");

        Action next_action = get_next_action();
        while (next_action != null) {
            try {
                next_action.execute();
                Thread.sleep(next_action.get_wait_time());
            } catch (InterruptedException | AWTException e) {
                throw new RuntimeException(e);
            }
            next_action = get_next_action();
        }
    }
}
