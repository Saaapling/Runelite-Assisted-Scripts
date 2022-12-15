package base;

import actions.Point;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import image_parsing.ImageParser;

import java.awt.*;
import java.io.IOException;

import static image_parsing.ImageParser.get_player_status;

public class Client{

    Rectangle dimensions;
    Point offset;
    Point scale;
    HWND hWnd;
    Player player;
    InputController mouse;
    int state = User32.SW_SHOWMINIMIZED;


    public Client(InputController mouse, HWND window, String client_name){
        this.mouse = mouse;
        hWnd = window;

        // Grab base.Client Dimensions
        set_window(User32.SW_SHOWMINIMIZED);
        set_window(User32.SW_RESTORE);
        WinDef.RECT rect = new WinDef.RECT();
        Controller.user32.GetWindowRect(hWnd, rect);

        dimensions = rect.toRectangle();
        offset = new Point(dimensions.x, dimensions.y);
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        scale = new Point((double) dimensions.width / size.width, (double) dimensions.height / size.height);

        player = new Player();
        player.name = client_name.replace("RuneLite - ", "");
    }

    public void set_window(int status){
        Controller.user32.ShowWindow(hWnd, status);
    }

    // Returns true if the client is minimized or not visible
    public boolean in_focus() {
        WinDef.RECT runelite_rect = new WinDef.RECT();
        Controller.user32.GetWindowRect(hWnd, runelite_rect);

//        if (runelite_rect.toRectangle().getX() < 0){
//            return false;
//        }

        // Get Foreground Window
        int MAX_TITLE_LENGTH = 1024;
        char[] buffer = new char[MAX_TITLE_LENGTH * 2];
        Controller.user32.GetWindowText(Controller.user32.GetForegroundWindow(), buffer, MAX_TITLE_LENGTH);
        String active_window =  Native.toString(buffer);

        return active_window.contains(get_name());
    }

    public Rectangle get_dimensions(){
        return dimensions;
    }

    public Point get_offset(){
        return offset;
    }

    public Point get_scale(){
        return scale;
    }

    public void show(){
        set_window(User32.SW_SHOWMINIMIZED);
        set_window(User32.SW_RESTORE);
    }

    public void minimize(){
        set_window(User32.SW_SHOWMINIMIZED);
    }

    public void update_status() throws IOException {
        boolean minimize = false;
        WinDef.RECT rect = new WinDef.RECT();
        Controller.user32.GetWindowRect(hWnd, rect);
        if (rect.toRectangle().getX() < 0)
            minimize = true;

        if (!in_focus())
            show();

        int[] status = get_player_status(dimensions);
        player.health = status[0];
        player.prayer = status[1];
        player.stamina = status[2];

        if (minimize)
            set_window(User32.SW_SHOWMINIMIZED);
    }

    public void update_inventory() throws IOException {
        player.update_inventory(ImageParser.get_inventory());
    }

    public void update_inventory_slot(int row, int col) throws IOException{
        player.update_inventory_slot(ImageParser.get_inventory_slot(row, col), row, col);
    }

    public int get_empty_slots() { return player.get_empty_slots(); }

    public int get_health(){
        return player.health;
    }

    public int get_prayer(){
        return player.prayer;
    }

    public int get_stamina(){
        return player.stamina;
    }

    public String get_name() {
        return player.name;
    }

    public Point get_food_slot(){
        return player.get_food_slot();
    }

    public Point get_prayer_slot(){
        return player.get_prayer_slot();
    }

    public void consume_item(int row, int col){
        player.consume_item(row, col);
    }

    public Point check_consumes(){
        return player.check_consumes();
    }

    public Point check_alchemy() { return player.get_alch_slot(); }

    public boolean check_availability(){
        return player.check_empty();
    }

    public void loot_item() {
        Point inven_slot = player.get_empty_slot();
        try {
            update_inventory_slot((int) inven_slot.getX(), (int) inven_slot.getY());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // For testing purposes
    public void print_inventory(){
        for (int row = 1; row <= 7; row++) {
            for (int col = 1; col <= 4; col++) {
                System.out.print(player.inventory[row-1][col-1] + "\t");
            }
            System.out.println();
        }
    }
}
