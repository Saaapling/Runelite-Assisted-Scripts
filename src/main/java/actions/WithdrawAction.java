package actions;

import java.awt.*;

import static image_parsing.Offsets.*;

public class WithdrawAction extends Action{

    int amount = 1;
    MouseController mouse;
    Point center;

    public WithdrawAction(MouseController mouse, int row, int col, int amount, int wait_time, String name) {
        this.mouse = mouse;
        this.wait_time = wait_time;
        this.amount = amount;
        this.name = name;

        center = get_bank_coordinate(row, col);
    }

    public WithdrawAction(MouseController mouse, int row, int col, int wait_time, String name) {
        this.mouse = mouse;
        this.wait_time = wait_time;
        this.name = name;

        center = get_bank_coordinate(row, col);
    }

    public void execute() throws InterruptedException, AWTException {
        Point[] bounds = Point.generate_rectangle(center, 12);

        Point rand_point_on_item = Point.get_random_point(bounds);
        Action move_action = new MouseMoveAction(mouse, rand_point_on_item, 0, 500);
        move_action.execute();

        for (int i = 0; i < amount; i++){
            mouse.left_click();
        }
    }
}
