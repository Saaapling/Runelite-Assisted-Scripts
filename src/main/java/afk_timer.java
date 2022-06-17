import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.util.*;
import java.util.List;

import static status_parser.image_parser.generate_image_bank;

public class afk_timer {

    static User32 user32 = User32.INSTANCE;
    static List<Client> clients;

    public static void execute(){
        List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
        clients = new ArrayList<>();

        for (DesktopWindow desktopWindow: windows){
            if (desktopWindow.getTitle().contains("RuneLite")) {

                // Get HWND and display window
                HWND hWnd = desktopWindow.getHWND();
                clients.add(new Client(hWnd));
            }
        }
    }
    public static void main(String[]args) throws Exception {
        execute();

        while (true){
            for (Client client : clients){
                client.show();
            }
            int afk_timer = 90;
            Thread.sleep(1000 * afk_timer);
        }
    }

}
