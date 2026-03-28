package team;

public class Player {
    private final String name;
    private int runs;
    private int ballsFaced;
    private int fours;
    private int sixes;
    private boolean isOut;
    private String battingStatus;
    private int wicketsTaken;
    private int runsConceded;

    public Player(String name) {
        this.name = name;
        this.battingStatus = "not out";
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
        this.battingStatus = out ? "out" : "not out";
    }

    public void setBattingStatus(String battingStatus, boolean out) {
        this.isOut = out;
        if (battingStatus == null || battingStatus.trim().isEmpty()) {
            this.battingStatus = out ? "out" : "not out";
            return;
        }
        this.battingStatus = battingStatus.trim().toLowerCase();
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

    public String getBattingStatus() {
        return battingStatus;
    }

    // bowling stats
    public void addBowlingRuns(int runs) {
        this.runsConceded += runs;
    }

    public void addWicketTaken() {
        this.wicketsTaken++;
    }

    public int getWicketsTaken() {
        return wicketsTaken;
    }

    public int getRunsConceded() {
        return runsConceded;
    }
}
