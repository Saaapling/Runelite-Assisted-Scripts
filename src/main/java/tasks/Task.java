package tasks;

import actions.Action;
import base.Client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;

public abstract class Task extends Thread {

    public String task_name;
    public Client client;
    public HashMap<String, Action> actions;
    public Deque<Action> action_queue;

    public Task(Client client){
        this.client = client;
        actions = new HashMap<>();
        action_queue = new ArrayDeque<>();
    }

    public String get_name() {
        return task_name;
    }

    public int get_sleep_time(int base_wait_time){
        if (base_wait_time == 0){
            return 0;
        }
        return base_wait_time + (int) (Math.random() * Math.min(5000, Math.max(base_wait_time, 50) / 15));
    }

    public boolean focus_client() throws InterruptedException {
        boolean in_focus = client.in_focus();
        if (!in_focus) {
            client.show();
            Thread.sleep(300);
            return true;
        }
        return false;
    }

    public Action get_next_action(){
        Action next_action = action_queue.poll();

        if (next_action != null){
            action_queue.addLast(next_action);
        }

        return next_action;
    }

}
