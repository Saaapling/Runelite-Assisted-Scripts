import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static status_parser.image_parser.generate_image_bank;

public class main {

    static User32 user32 = User32.INSTANCE;
    static List<Client> clients;

    // Because I'm dumb
    public static void print(Object x){
        System.out.println(x);
    }

    public static void initialize_clients(){
        List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
        clients = new ArrayList<>();

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

    public static void get_health_test() throws IOException, AWTException, InterruptedException {
        while (true){
            int i = 0;
            for (Client client : clients){
                int[] status = client.update_status();
                print("Client " + i + ": ");
                print("Health: " + status[0]);
                print("Prayer: " + status[1]);
                print("Stamina: " + status[2]);
                i += 1;
            }

            Thread.sleep(5000);
        }
    }

    public static void main(String[]args) throws Exception {
        initialize_clients();

        get_health_test();
    }

}
