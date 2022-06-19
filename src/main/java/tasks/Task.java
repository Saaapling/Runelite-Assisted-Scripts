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
}
