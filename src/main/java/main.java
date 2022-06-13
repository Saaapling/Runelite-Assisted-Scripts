import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.util.ArrayList;
import java.util.List;

public class main {

    static User32 user32 = User32.INSTANCE;

    // Because I'm dumb
    public static void print(Object x){
        System.out.println(x);
    }

    public static void execute(){
        List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
        List<Client> clients = new ArrayList<>();

        for (DesktopWindow desktopWindow: windows){
            if (desktopWindow.getTitle().contains("RuneLite")) {
                print("Application Found: " + desktopWindow.getTitle());

                // Get HWND and display window
                HWND hWnd = desktopWindow.getHWND();
                clients.add(new Client(hWnd));
            }
        }

        for (Client x: clients){
            print(x.get_dimensions());
        }
    }
    public static void main(String[]args) throws InterruptedException {
        execute();
    }

}
