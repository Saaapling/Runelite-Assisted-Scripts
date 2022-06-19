package actions;

public class DefaultAction extends Action{

    public DefaultAction(int wait_time) {
        super(wait_time);
    }

    @Override
    public void execute() throws InterruptedException {
        System.out.println("Default Action: Does nothing, sleeping for " + get_wait_time() / 1000 + " seconds");
    }
}
