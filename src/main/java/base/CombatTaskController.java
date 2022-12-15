package base;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import image_parsing.Offsets;
import tasks.AFKCombatHelper.AFKCombatManager;
import tasks.RedSalamanderHunter.RedSalamanderHunter;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import static image_parsing.ScreenshotManager.screenshot_inventory_item;

public class CombatTaskController extends Controller implements NativeMouseInputListener {

    protected String getRandString() {
        String salt_chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder rand_str = new StringBuilder();
        Random rnd = new Random();
        while (rand_str.length() < 10) {
            int index = (int) (rnd.nextFloat() * salt_chars.length());
            rand_str.append(salt_chars.charAt(index));
        }
        return rand_str.toString();
    }

    private void save_alchemy_image(int x, int y) throws IOException, AWTException {
        // Parse out which inventory slot the item belongs to
        // Take a screenshot of that inventory slot and add it to the image bank

        int row = (int) ((double) (y - Offsets.inventory_base_y) / Offsets.inventory_slot_height + 0.5) + 1;
        int col = (int) ((double) (x - Offsets.inventory_base_x) / Offsets.inventory_slot_width + 0.5) + 1;

        System.out.println(row + ", " + col);

//        String path = "./src/main/java/tasks/AFKCombatHelper/AlchemyTargets/" + getRandString() + ".png";
        String path = "./src/main/java/tasks/ManiacleMonkeys/InventoryImages/" + getRandString() + ".png";
        screenshot_inventory_item(row, col, path);
    }

    public void nativeMouseClicked(NativeMouseEvent e) {
        if (e.getButton() == 3){
            System.out.println("Adding item to alchemy folder");
            try {
                save_alchemy_image(e.getX(), e.getY());
            } catch (IOException | AWTException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void nativeKeyPressed(NativeKeyEvent e){
        // Pause program: `
        if (e.getKeyCode() == 41){
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }else{
                lock.lock();
            }
        }
        // Print inventory: Alt
        if (e.getKeyCode() == 56){
            try {
                clients.get("Lycindria").update_inventory();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            clients.get("Lycindria").print_inventory();
        }
        // Exit Program: Delete
        if (e.getKeyCode() == 3667){
            System.exit(0);
        }
    }

    public static void main(String[]args) throws Exception {
        CombatTaskController instance = new CombatTaskController();
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(instance);
        GlobalScreen.addNativeMouseListener(instance);
        GlobalScreen.addNativeMouseMotionListener(instance);

        mouse = new InputController();
        initialize_clients(mouse);

        Class<?>[] parameter_class = {Client.class, InputController.class, ReentrantLock.class};
        ArrayList<Object> parameters = new ArrayList<>(Arrays.asList(mouse, lock));
        start_clients(AFKCombatManager.class, parameter_class, parameters);

//        start_clients(AFKKrackenHelper.class, parameter_class, parameters);
    }
}
