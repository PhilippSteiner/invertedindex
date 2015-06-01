package models;



/**
 * Created by Philipp on 22.05.15.
 */
public class Tuple {

    private int number;
    private int position;

    public Tuple(int number, int position) {
        this.number = number;
        this.position = position;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
