package statistics;

public class OverStats {
    private int fours;
    private int sixes;
    private int wides;
    private int noBalls;
    private int wickets;

    public void incrementFours() {
        this.fours++;
    }

    public void incrementSixes() {
        this.sixes++;
    }

    public void incrementWides() {
        this.wides++;
    }

    public void incrementNoBalls() {
        this.noBalls++;
    }

    public void incrementWickets() {
        this.wickets++;
    }

    public int getFours() {
        return fours;
    }

    public int getSixes() {
        return sixes;
    }

    public int getWides() {
        return wides;
    }

    public int getNoBalls() {
        return noBalls;
    }

    public int getWickets() {
        return wickets;
    }
}
