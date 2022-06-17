import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.*;
import java.io.IOException;

import static status_parser.image_parser.get_player_status;

public class Client {

    Rectangle dimensions;
    HWND hWnd;
    Player player;

    public Client(HWND window){
        hWnd = window;

        // Grab Client Dimensions
        set_window(User32.SW_SHOWMINIMIZED);
        set_window(User32.SW_RESTORE);
        WinDef.RECT rect = new WinDef.RECT();
        main.user32.GetWindowRect(hWnd, rect);
        dimensions = rect.toRectangle();
        set_window(User32.SW_SHOWMINIMIZED);

        player = new Player();
    }

    public void set_window(int status){
        main.user32.ShowWindow(hWnd, status);
    }

    public Rectangle get_dimensions(){
        return dimensions;
    }

    public void show(){
        set_window(User32.SW_SHOWMINIMIZED);
        set_window(User32.SW_RESTORE);
    }

    public int[] update_status() throws AWTException, IOException {
        boolean minimize = false;
        WinDef.RECT rect = new WinDef.RECT();
        main.user32.GetWindowRect(hWnd, rect);
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

}
