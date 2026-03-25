package team;

public class Player {
    private String name;
    private int runs;
    private int ballsFaced;
    private int fours;
    private int sixes;
    private boolean isOut;

    public Player(String name) {
        this.name = name;
    }

    public void addRuns(int runsScored) {
        this.runs += runsScored;
        this.ballsFaced++;
        if (runsScored == 4) {
            this.fours++;
        } else if (runsScored == 6) {
            this.sixes++;
        }
    }

    public void faceDotBall() {
        this.ballsFaced++;
    }

    public void setOut(boolean out) {
        this.isOut = out;
    }

    public String getName() {
        return name;
    }

    public int getRuns() {
        return runs;
    }

    public int getBallsFaced() {
        return ballsFaced;
    }

    public int getFours() {
        return fours;
    }

    public int getSixes() {
        return sixes;
    }

    public boolean isOut() {
        return isOut;
    }
}
