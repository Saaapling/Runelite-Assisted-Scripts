package base;

import actions.*;
import actions.Point;
import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import tasks.DefaultTask;
import tasks.EdgevilleCrafting;
import tasks.Task;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Controller {

    static User32 user32 = User32.INSTANCE;
    static List<Client> clients;
    static MouseController mouse;
    static ReentrantLock lock = new ReentrantLock();

    // Because I'm dumb
    public static void print(Object x){
        System.out.println(x);
    }

    public static void initialize_clients(MouseController mouse) throws IOException, AWTException {
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
            print(x.get_scale());
        }
    }

    public static void get_health_test() throws IOException, AWTException, InterruptedException {
        while (true){
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

    public static void main(String[]args) throws Exception {
        mouse = new MouseController();
        initialize_clients(mouse);

        for (Client client : clients){
            Task task = new EdgevilleCrafting(client, mouse, lock);
            task.start();
        }

//        get_health_test();


//        while (true) {
//            Robot robot = new Robot();
//            Point[] bounds = {new Point(1243, 299), new Point(1303, 415), new Point(1217, 485), new Point(1153, 391)};
//            Action test_action = new MouseMoveAction(mouse, bounds, new Point(0, 0), 0, 5000);
//
//            test_action.execute();
//            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
//            test_action.execute();
//            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
//
//            Thread.sleep(5000);
//        }
    }

}
