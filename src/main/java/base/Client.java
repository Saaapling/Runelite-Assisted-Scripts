package base;

import actions.*;
import actions.Point;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.*;
import java.io.IOException;

import static status_parser.image_parser.get_player_status;

public class Client{

    Rectangle dimensions;
    Point offset;
    double x_scale;
    double y_scale;
    HWND hWnd;
    Player player;
    MouseController mouse;
    int state = User32.SW_SHOWMINIMIZED;


    public Client(MouseController mouse, HWND window, String client_name){
        this.mouse = mouse;
        hWnd = window;

        // Grab base.Client Dimensions
        set_window(User32.SW_SHOWMINIMIZED);
        set_window(User32.SW_RESTORE);
        WinDef.RECT rect = new WinDef.RECT();
        Controller.user32.GetWindowRect(hWnd, rect);
//        set_window(User32.SW_SHOWMINIMIZED);

        dimensions = rect.toRectangle();
        offset = new Point(dimensions.x, dimensions.y);
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        x_scale = (double) dimensions.width / size.width;
        y_scale = (double) dimensions.height / size.height;

        player = new Player();
        player.name = client_name.replace("RuneLite - ", "");
    }

    public void set_window(int status){
        Controller.user32.ShowWindow(hWnd, status);
    }

    //Todo: Has difficulty recognizing when runelite is minimized vs when user clicks another window without minimizing
    public boolean get_window_status() {
        WinDef.RECT rect = new WinDef.RECT();
        Controller.user32.GetWindowRect(hWnd, rect);
        return rect.toRectangle().getX() < 0;
    }

    public Rectangle get_dimensions(){
        return dimensions;
    }

    public Point get_offset(){
        return offset;
    }

    public Point get_scale(){
        return new Point(x_scale, y_scale);
    }

    public void show(){
        set_window(User32.SW_SHOWMINIMIZED);
        set_window(User32.SW_RESTORE);
    }

    public void minimize(){
        set_window(User32.SW_SHOWMINIMIZED);
    }

    public int[] update_status() throws AWTException, IOException {
        boolean minimize = false;
        WinDef.RECT rect = new WinDef.RECT();
        Controller.user32.GetWindowRect(hWnd, rect);
        if (rect.toRectangle().getX() < 0)
            minimize = true;

        show();

        int[] status = get_player_status(dimensions);
        player.health = status[0];
        player.prayer = status[1];
        player.stamina = status[2];

        if (minimize)
            set_window(User32.SW_SHOWMINIMIZED);

        return status;
    }

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

}
