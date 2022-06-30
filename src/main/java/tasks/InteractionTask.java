package tasks;

import base.Client;
import base.MouseController;

import java.util.concurrent.locks.ReentrantLock;

public abstract class InteractionTask extends Task{

    public ReentrantLock lock;
    public MouseController mouse;

    public InteractionTask(Client client, MouseController mouse, ReentrantLock lock) {
        super(client);
        this.lock = lock;
        this.mouse = mouse;
    }

    public void fetch_lock(){
        if (!lock.isHeldByCurrentThread())
            lock.lock();
    }

}
