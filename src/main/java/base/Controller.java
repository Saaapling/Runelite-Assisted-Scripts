package base;

import actions.*;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import tasks.EdgevilleSapphireRings;
import tasks.Task;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static base.Utils.print;

public class Controller implements NativeKeyListener {

    static User32 user32 = User32.INSTANCE;
    static List<Client> clients;
    public static MouseController mouse;
    static ReentrantLock lock = new ReentrantLock();

    public static void initialize_clients(MouseController mouse) {
        List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
        clients = new ArrayList<>();

        for (DesktopWindow desktopWindow: windows){
            if (desktopWindow.getTitle().contains("RuneLite")) {
                print("Application Found: " + desktopWindow.getTitle());

                // Get HWND and display window
                HWND hWnd = desktopWindow.getHWND();
                clients.add(new Client(mouse, hWnd, desktopWindow.getTitle()));
            }
        }

        for (Client x: clients){
            print(x.get_dimensions());
        }
    }

    public static void get_health_test() throws IOException, AWTException, InterruptedException {
        for (int j = 0; j < 60; j++){
            int i = 0;
            for (Client client : clients){
                int[] status = client.update_status();
                print("base.Client " + i + ": ");
                print("Health: " + status[0]);
                print("Prayer: " + status[1]);
                print("Stamina: " + status[2]);
                i += 1;
            }

            Thread.sleep(5000);
        }
    }

    // Force quit when the ESC key is pressed
    public void nativeKeyPressed(NativeKeyEvent e){
        if (e.getKeyCode() == 1){
            System.exit(0);
        }
    }

    public static void main(String[]args) throws Exception {
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(new Controller());

        mouse = new MouseController();
        initialize_clients(mouse);

        for (Client client : clients){
            Task task = new EdgevilleSapphireRings(client, mouse, lock);
            task.start();
        }

//        get_health_test();
//        bank_screenshot();
//        bank_x_screenshot();
    }
}
