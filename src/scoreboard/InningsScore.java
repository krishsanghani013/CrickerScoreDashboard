package scoreboard;

import match.Match;
import team.Player;
import team.Team;

public class InningsScore {
    private Team battingTeam;
    private Team bowlingTeam;

    private int totalRuns;
    private int wickets;
    private int legalBalls;
    private int wides;
    private int noBalls;
    private int fours;
    private int sixes;

    private int strikerIndex;
    private int nonStrikerIndex;
    private int nextBatsmanIndex;

    public InningsScore(Team battingTeam, Team bowlingTeam) {
        this.battingTeam = battingTeam;
        this.bowlingTeam = bowlingTeam;
        this.strikerIndex = 0;
        this.nonStrikerIndex = 1;
        this.nextBatsmanIndex = 2;
    }

    public void addWide() {
        this.totalRuns++;
        this.wides++;
    }

    public void addNoBall() {
        this.totalRuns++;
        this.noBalls++;
    }

    public void addRuns(int runs) {
        Player striker = getStriker();
        striker.addRuns(runs);

        this.totalRuns += runs;
        this.legalBalls++;
        if (runs == 4) {
            this.fours++;
        } else if (runs == 6) {
            this.sixes++;
        }

        if (runs % 2 != 0) {
            rotateStrike();
        }
    }

    public void recordDotBall() {
        getStriker().faceDotBall();
        this.legalBalls++;
    }

    public void recordWicket() {
        Player striker = getStriker();
        striker.faceDotBall();
        striker.setOut(true);

        this.wickets++;
        this.legalBalls++;

        if (wickets < Match.MAX_WICKETS && nextBatsmanIndex < Match.PLAYERS_PER_TEAM) {
            strikerIndex = nextBatsmanIndex;
            nextBatsmanIndex++;
        }
    }

    public boolean isAllOut() {
        return wickets >= Match.MAX_WICKETS;
    }

    public void completeOver() {
        rotateStrike();
    }

    public String getOversRepresentation() {
        return (legalBalls / Match.BALLS_PER_OVER) + "." + (legalBalls % Match.BALLS_PER_OVER);
    }

    private void rotateStrike() {
        int temp = this.strikerIndex;
        this.strikerIndex = this.nonStrikerIndex;
        this.nonStrikerIndex = temp;
    }

    public Player getStriker() {
        return battingTeam.getPlayers()[strikerIndex];
    }

    public Team getBattingTeam() {
        return battingTeam;
    }

    public Team getBowlingTeam() {
        return bowlingTeam;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public int getWickets() {
        return wickets;
    }

    public int getLegalBalls() {
        return legalBalls;
    }

    // total balls
    public int getTotalBalls() {
        return legalBalls + wides + noBalls;
    }

    public int getWides() {
        return wides;
    }

    public int getNoBalls() {
        return noBalls;
    }

    public int getFours() {
        return fours;
    }

    public int getSixes() {
        return sixes;
    }
}
