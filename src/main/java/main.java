import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;


import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.util.Locale;

public class main {

    // Because I'm a fucking pepega
    public static void print(Object x){
        System.out.println(x);
    }

    public static void main(String[]args) throws InterruptedException {

        List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
        User32 user32 = User32.INSTANCE;
        List<HWND> hwnds = new ArrayList<>();

        for (DesktopWindow desktopWindow: windows){
            if (desktopWindow.getTitle().contains("RuneLite")) {
                print("Application Found: " + desktopWindow.getTitle());

                // Get HWND and display window
                HWND hWnd = desktopWindow.getHWND();
                hwnds.add(hWnd);
                user32.ShowWindow(hWnd, User32.SW_SHOWMINIMIZED);
                user32.ShowWindow(hWnd, User32.SW_RESTORE);

                // Window Coordinates
                WinDef.RECT rect = new WinDef.RECT();
                user32.GetWindowRect(hWnd, rect);
                print(rect.toRectangle());
            }
        }



    }

}
