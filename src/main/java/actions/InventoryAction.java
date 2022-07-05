package actions;

import base.MouseController;
import image_parsing.Offsets;

import java.awt.*;

public class InventoryAction extends Action{

    MouseController mouse;
    int row;
    int col;

    public InventoryAction(MouseController mouse, int row, int col, int wait_time, String name){
        this.mouse = mouse;
        this.row = row;
        this.col = col;
        this.wait_time = wait_time;
        this.name = name;
    }

    public void execute() throws InterruptedException, AWTException {
        Point item_center = Offsets.get_inventory_coordinate(row, col);

        Action equivalent_action = new MouseLeftClickAction(mouse, item_center, Offsets.inventory_item_size - 3, 300, this.name);
        equivalent_action.execute();
    }
}
