package actions;

import java.awt.*;

import static image_parsing.Offsets.*;

public class WithdrawXAction extends Action{

    MouseController mouse;
    Point center;

    public WithdrawXAction(MouseController mouse, int row, int col, int wait_time, String name) {
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
        mouse.right_click();

        Point withdraw_x_center = rand_point_on_item.add(new Point(0, bank_withdraw_x_height_offset));
        bounds = Point.generate_rectangle(withdraw_x_center, bank_withdraw_x_width, bank_withdraw_x_height);

        move_action = new MouseMoveAction(mouse, bounds, 500);
        move_action.execute();
        mouse.left_click();
    }
}
