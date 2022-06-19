package actions;

import com.github.joonasvali.naturalmouse.api.MouseMotion;
import com.github.joonasvali.naturalmouse.api.MouseMotionFactory;

import static com.github.joonasvali.naturalmouse.util.FactoryTemplates.createFastGamerMotionFactory;

public class MouseController {

    MouseMotionFactory factory;

    public MouseController(){
        factory = createFastGamerMotionFactory();
    }

    public void move(Point point) throws InterruptedException {
        MouseMotion motion = factory.build((int) point.x, (int) point.y);
        motion.move();
    }
}
