package tasks;

import base.Client;
import base.InputController;

import java.util.concurrent.locks.ReentrantLock;

public abstract class InteractionTask extends Task{

    public ReentrantLock lock;
    public InputController mouse;

    public InteractionTask(Client client, InputController mouse, ReentrantLock lock) {
        super(client);
        this.lock = lock;
        this.mouse = mouse;
    }

    public void fetch_lock(){
        if (!lock.isHeldByCurrentThread())
            lock.lock();
    }

    public void release_lock(){
        if (lock.isHeldByCurrentThread())
            lock.unlock();
    }

}
