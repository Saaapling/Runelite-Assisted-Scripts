package actions;

import base.InputController;
import image_parsing.Offsets;

import java.awt.*;

public class InventoryAction extends Action{

    InputController mouse;
    int row;
    int col;
    boolean shift_click = false;

    public InventoryAction(InputController mouse, int row, int col, int wait_time, String name){
        this.mouse = mouse;
        this.row = row;
        this.col = col;
        this.wait_time = wait_time;
        this.name = name;
    }

    public InventoryAction(InputController mouse, int row, int col, int wait_time, boolean shift, String name){
        this.mouse = mouse;
        this.row = row;
        this.col = col;
        this.wait_time = wait_time;
        this.shift_click = shift;
        this.name = name;
    }

    public void execute() throws InterruptedException, AWTException {
        Point item_center = Offsets.get_inventory_coordinate(row, col);
        Action equivalent_action;
        if (shift_click) {
            equivalent_action = new MouseShiftClickAction(mouse, item_center, Offsets.inventory_item_size - 3, 300, this.name);
        } else{
            equivalent_action = new MouseLeftClickAction(mouse, item_center, Offsets.inventory_item_size - 3, 300, this.name);
        }
        equivalent_action.execute();
    }
}
