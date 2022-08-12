package base;

public class DefaultSleepValues {

    static int random_time_between_clicks(){
        return (int) (100 + Math.random() * 10);
    }

    static int random_time_between_move_and_click(){
        return (int) (65 + Math.random() * 15);
    }

    static int random_time_between_key_strokes(){
        return (int) (75 + Math.random() * 15);
    }

}
