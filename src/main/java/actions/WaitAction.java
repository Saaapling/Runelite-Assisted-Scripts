package actions;

public class WaitAction extends Action{

    String display_text;

    public WaitAction(int wait_time, String name, String text) {
        this.wait_time = wait_time;
        this.name = name;
        display_text = text;
    }

    @Override
    public void execute() throws InterruptedException {
        System.out.println(display_text);
    }

}
