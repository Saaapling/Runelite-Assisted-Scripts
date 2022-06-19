package tasks;

import actions.Action;
import base.Client;

import java.util.ArrayList;

public abstract class Task extends Thread {

    String task_name;
    Client client;
    ArrayList<Action> actions;

    public Task(Client client){
        this.client = client;
    }

    public String get_name() {
        return task_name;
    }

    public int get_sleep_time(int base_wait_time){
        return base_wait_time + (int) (Math.random() * Math.min(5000, Math.max(base_wait_time, 50) / 5));
    }
}
