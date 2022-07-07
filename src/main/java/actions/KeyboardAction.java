package actions;

import base.InputController;

import java.awt.*;
import java.awt.event.KeyEvent;

public class KeyboardAction extends Action{

    InputController keyboard;
    int key;
    String action = "press_and_release";

    public KeyboardAction(InputController keyboard, int key, String name) {
        this.keyboard = keyboard;
        this.name = name;
        this.key = key;
    }

    public KeyboardAction(InputController keyboard, String key, String name){
        this.keyboard = keyboard;
        this.name = name;
        switch (key.toLowerCase()) {
            case "shift":
                this.key = KeyEvent.VK_SHIFT;
                break;
            case "alt":
                this.key = KeyEvent.VK_ALT;
                break;
            default: //Defaults to escape
                this.key = KeyEvent.VK_ESCAPE;
        }
    }

    public void set_action(String action){
        this.action = action;
    }

    public void execute() throws InterruptedException, AWTException {
        if (action.equals("press")) {
            keyboard.press_key(key);
        }else if (action.equals("release")){
            keyboard.release_key(key);
        }else {
            keyboard.press_and_release_key(key);
        }
    }
}
