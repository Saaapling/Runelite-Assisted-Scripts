package base;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;

public class CoordinateParser implements NativeMouseInputListener, NativeKeyListener {
    public void nativeMouseClicked(NativeMouseEvent e) {
//        System.out.println("Mouse Cicked: " + e.getButton());

    }

    public void nativeMousePressed(NativeMouseEvent e) {
        if (e.getButton() == 2)
            System.out.println("Mouse Click: " + e.getX() + ", " + e.getY());
    }

    public void nativeMouseReleased(NativeMouseEvent e) {
//        System.out.println("Mouse Released: " + e.getButton());
    }

    public void nativeMouseMoved(NativeMouseEvent e) {
//        System.out.println("Mouse Moved: " + e.getX() + ", " + e.getY());
    }

    public void nativeMouseDragged(NativeMouseEvent e) {
//        System.out.println("Mouse Dragged: " + e.getX() + ", " + e.getY());
    }

    public void nativeKeyPressed(NativeKeyEvent e){
//        System.out.println(e.getKeyCode());

        if (e.getKeyCode() == 1){
            System.exit(0);
        }
    }

    public static void main(String[] args) {

        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new CoordinateParser());
        }
        catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }

        // Construct the example object.
        CoordinateParser example = new CoordinateParser();

        // Add the appropriate listeners.
        GlobalScreen.addNativeMouseListener(example);
        GlobalScreen.addNativeMouseMotionListener(example);
    }
}
