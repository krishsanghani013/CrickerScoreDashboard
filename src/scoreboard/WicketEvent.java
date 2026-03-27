package scoreboard;

public class WicketEvent {
    private final String batter;
    private final String bowler;
    private final int over;
    private final int ball;

    public WicketEvent(String batter, String bowler, int over, int ball) {
        this.batter = batter;
        this.bowler = bowler;
        this.over = over;
        this.ball = ball;
    }

    public String getBatter() {
        return batter;
    }

    public String getBowler() {
        return bowler;
    }

    public String getOverBall() {
        return over + "." + ball;
    }
}
