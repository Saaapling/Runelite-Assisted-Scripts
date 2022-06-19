package tasks;

import actions.Action;
import actions.DefaultAction;
import base.Client;

import java.awt.*;
import java.util.ArrayList;

public class DefaultTask extends Task {

    ArrayList<Action> actions = new ArrayList<>();

    public DefaultTask(Client client){
        super(client);
        task_name = "Default tasks.Task";
        actions.add(new DefaultAction(5000));
    }

    public void run() {
        System.out.println( client.get_name() + ": Starting tasks.Task (" + task_name + ")");

        while (true){
            for (Action action : actions){
                try {
                    action.execute();
                    Thread.sleep(action.get_wait_time());
                } catch (InterruptedException | AWTException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
