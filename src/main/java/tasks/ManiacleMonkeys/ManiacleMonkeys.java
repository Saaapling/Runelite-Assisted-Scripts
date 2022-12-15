package tasks.ManiacleMonkeys;

import actions.Action;
import actions.KeyboardAction;
import actions.MouseLeftClickAction;
import actions.Point;
import base.Client;
import base.InputController;
import image_parsing.ImageParser;
import tasks.InteractionTask;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class ManiacleMonkeys extends InteractionTask {

    public ManiacleMonkeys(Client client, InputController mouse, ReentrantLock lock) {
        super(client, mouse, lock);

        populate_actions();
    }

    private void populate_actions(){
        // Loot Bones
        actions.put("Pick up Bones 1", new MouseLeftClickAction(mouse, new Point(920, 350), 15, 1500, "Pick up Bones 1"));
        actions.put("Pick up Bones 2", new MouseLeftClickAction(mouse, new Point(745, 335), 15, 1500, "Pick up Bones 2"));
        actions.put("Pick up Bones 3", new MouseLeftClickAction(mouse, new Point(740, 790), 15, 2000, "Pick up Bones 3"));

        // Move to Trap Position
        actions.put("Move to Trap", new MouseLeftClickAction(mouse, new Point(1295, 635), 25, 2000, "Move to Trap"));

        // Set Trap
        actions.put("Set Trap", new MouseLeftClickAction(mouse, new Point(810, 465), 50, 2000, "Set Trap"));

        // Reset Camera (600 zoom position)
        actions.put("Reset Camera", new KeyboardAction(mouse, "ctrl", "Reset Camera"));
    }

    /*
        Steps:
            1. Check inventory
                - If 3 inventory spaces, loot bones (if available)
                    - Determine whether bones can be looted based on the 3rd bone spawn
                    - 4 step rotation, 3 bones, 1 to the original spot
                - If no bananas, cast bones to bananas
                - If there are monkey tails, drop them
            2. Check Trap
                - If trap is available, set up the trap
            3. Default wait (3 seconds)
    */
    public Action get_next_action() {
        Action next_action = action_queue.poll();

        if (next_action != null)
            return next_action;

        try {
            client.update_inventory();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Drop Monkey Tails

        // Take a screenshot
        BufferedImage image = ImageParser.get_screenshot();

        // Check whether bones can be looted
        if (client.get_empty_slots() > 3){
            // Check if bones have respawned
        }

        // Cast Bones to Bananas (If no bananas exist)

        // Check Trap

        return null;
    }

    public void run() {

    }

    }
