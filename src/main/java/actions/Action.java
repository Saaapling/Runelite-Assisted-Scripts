package actions;

import java.awt.*;
import java.util.Arrays;

public abstract class Action {

    Point[] bounds; //Rhombus: Top/Bottom/Right/Left
    int wait_time;
    String name = null;

    public Action(int wait_time){
        this.wait_time = wait_time;
    }

    public int get_wait_time(){
        return wait_time;
    }

    public String get_name(){
        return name;
    }

    public abstract void execute() throws InterruptedException, AWTException;
}
