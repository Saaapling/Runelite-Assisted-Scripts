package base;

import actions.Point;
import com.github.joonasvali.naturalmouse.api.MouseMotion;
import com.github.joonasvali.naturalmouse.api.MouseMotionFactory;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static com.github.joonasvali.naturalmouse.util.FactoryTemplates.createFastGamerMotionFactory;

public class InputController {

    MouseMotionFactory factory;
    Robot robot;

    public InputController() throws AWTException {
        factory = createFastGamerMotionFactory();
        robot = new Robot();
    }

    public void move(Point point) throws InterruptedException {
        MouseMotion motion = factory.build((int) point.getX(), (int) point.getY());
        motion.move();
        Thread.sleep(DefaultSleepValues.random_time_between_move_and_click());
    }

    public void left_click() throws InterruptedException {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(DefaultSleepValues.random_time_between_clicks());
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(DefaultSleepValues.random_time_between_clicks());
    }

    public void shift_click() throws  InterruptedException {
        press_key(KeyEvent.VK_SHIFT);
        Thread.sleep(DefaultSleepValues.random_time_between_clicks() * 2L);

        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(DefaultSleepValues.random_time_between_clicks());
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        Thread.sleep(DefaultSleepValues.random_time_between_clicks());
        release_key(KeyEvent.VK_SHIFT);
    }

    public void right_click() throws InterruptedException {
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        Thread.sleep(DefaultSleepValues.random_time_between_clicks());
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        Thread.sleep(100 + DefaultSleepValues.random_time_between_clicks());
    }

    public void press_key(int key) {
        robot.keyPress(key);
    }

    public void release_key(int key) {
        robot.keyRelease(key);
    }

    public void press_and_release_key(int key) throws InterruptedException{
        press_key(key);
        Thread.sleep(DefaultSleepValues.random_time_between_key_strokes());
        release_key(key);
    }
}
