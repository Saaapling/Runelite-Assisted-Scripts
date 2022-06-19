package actions;

public class DefaultSleepValues {

    static int random_time_between_clicks(){
        return (int) (100 + Math.random() * 10);
    }

    static int random_time_between_move_and_click(){
        return (int) (300 + Math.random() * 15);
    }

}
