package actions;

import base.MouseController;

import java.awt.*;

public class BankAllAction extends Action{

    MouseController mouse;

    public BankAllAction(MouseController mouse, int wait_time){
        this.mouse = mouse;
        this.wait_time = wait_time;
        this.name = "Bank All";
    }
    @Override
    public void execute() throws InterruptedException, AWTException {
        Point center = new Point(1018,820);
        Action move_action = new MouseLeftClickAction(mouse, center, 10, 500, "Bank All");
        move_action.execute();
    }
}
