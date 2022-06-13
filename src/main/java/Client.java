import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.*;

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

    }

    public void set_window(int status){
        main.user32.ShowWindow(hWnd, status);
    }

    public Rectangle get_dimensions(){
        return dimensions;
    }

}
