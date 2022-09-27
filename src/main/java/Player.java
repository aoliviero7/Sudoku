
import java.io.Serializable;

public class Player implements Serializable {

    private String nickname;
    private int score;

    public Player(String nickname){
        this.nickname = nickname;
        this.score = 0;
    }

    public String getNickname(){
        return nickname;
    }

    public int getScore(){
        return score;
    }

    public void setScore(int score){
        this.score = score;
    }
}
