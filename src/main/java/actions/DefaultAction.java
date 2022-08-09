package actions;

public class DefaultAction extends Action{

    public DefaultAction(int wait_time) {
        this.wait_time = wait_time;
        this.name = "Default Action";
    }

    public DefaultAction(int wait_time, String name) {
        this.wait_time = wait_time;
        this.name = name;
    }

    @Override
    public void execute() throws InterruptedException {
        System.out.println("Default Action: Does nothing, sleeping for " + get_wait_time() / 1000 + " seconds");
    }
}
