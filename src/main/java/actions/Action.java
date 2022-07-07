package actions;

import java.awt.*;

public abstract class Action {

    int wait_time;
    String name = null;

    public int get_wait_time(){
        return wait_time;
    }

    public String get_name(){
        return name;
    }

    public abstract void execute() throws InterruptedException, AWTException;
}
