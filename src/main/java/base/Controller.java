package base;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import image_parsing.Offsets;
import tasks.EdgevilleCrafting.EdgevilleCrafting;
import tasks.Task;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static base.Utils.print;

public class Controller implements NativeKeyListener {

    static User32 user32 = User32.INSTANCE;
    static HashMap<String, Client> clients;
    public static MouseController mouse;
    static ReentrantLock lock = new ReentrantLock();

    public static void initialize_clients(MouseController mouse) {
        List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
        clients = new HashMap<>();

        for (DesktopWindow desktopWindow: windows){
            if (desktopWindow.getTitle().contains("RuneLite")) {
                print("Application Found: " + desktopWindow.getTitle());

                // Get HWND and display window
                HWND hWnd = desktopWindow.getHWND();
                Client client = new Client(mouse, hWnd, desktopWindow.getTitle());
                Offsets.inventory_base_y = client.get_dimensions().height - 280;
                clients.put(client.get_name(), client);
            }
        }

        for (Client x: clients.values()){
            print(x.get_dimensions());
        }
    }

    public static void get_health_test() throws IOException, AWTException, InterruptedException {
        for (int j = 0; j < 60; j++){
            int i = 0;
            for (Client client : clients.values()){
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

    public static void start_clients(Class<? extends Task> task_class, Class<?>[] parameter_class, ArrayList<Object> parameters) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends Task> task_constructor = task_class.getConstructor(parameter_class);

        for (Client client : clients.values()){
            parameters.add(0, client);
            Task task = task_constructor.newInstance(parameters.toArray());
            task.start();
        }
    }

    public static void start_clients(HashMap<String, Task> client_task_map){
        for (Map.Entry<String, Task> entry : client_task_map.entrySet()){
            Task task = entry.getValue();
            task.start();
        }
    }

    public static Task set_task(HashMap<String, Task> map, String player_name, Class<? extends Task> task_class, Class<?>[] parameter_class, ArrayList<Object> parameters) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!clients.containsKey(player_name))
            return null;

        Client client = clients.get(player_name);
        Constructor<? extends Task> task_constructor = task_class.getConstructor(parameter_class);
        parameters.add(0, client);
        return task_constructor.newInstance(parameters.toArray());
    }

    public static void main(String[]args) throws Exception {
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(new Controller());

        mouse = new MouseController();
        initialize_clients(mouse);

//        Class<?>[] parameter_class = {Client.class, MouseController.class, ReentrantLock.class};
//        ArrayList<Object> parameters = new ArrayList<>(Arrays.asList(mouse, lock));
//        EdgevilleCrafting.set_default_task("Sapphire Rings");
//        start_clients(EdgevilleCrafting.class, parameter_class, parameters);

        HashMap<String, Task> client_task_map = new HashMap<>();

        Class<?>[] parameter_class = {Client.class, MouseController.class, ReentrantLock.class};
        ArrayList<Object> parameters = new ArrayList<>(Arrays.asList(mouse, lock));

        EdgevilleCrafting task_1 = (EdgevilleCrafting) set_task(client_task_map, "birchsaaap", EdgevilleCrafting.class, parameter_class, parameters);
        if (task_1 != null) {
            task_1.set_task("Sapphire Rings");
            client_task_map.put("birchsaaap", task_1);
        }

        EdgevilleCrafting task_2 = (EdgevilleCrafting) set_task(client_task_map, "willowsaaap", EdgevilleCrafting.class, parameter_class, parameters);
        if (task_2 != null) {
            task_2.set_task("Bronze Bars");
            client_task_map.put("birchsaaap", task_2);
        }

        EdgevilleCrafting task_3 = (EdgevilleCrafting) set_task(client_task_map, "maplesaaap", EdgevilleCrafting.class, parameter_class, parameters);
        if (task_3 != null) {
            task_3.set_task("Bronze Bars");
            client_task_map.put("birchsaaap", task_3);
        }

        start_clients(client_task_map);
    }
}
