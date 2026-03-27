package scoreboard;

import match.Match;
import team.Player;
import team.Team;
import java.util.ArrayList;
import java.util.List;

public class InningsScore {
    private Team battingTeam;
    private Team bowlingTeam;

    private int totalRuns;
    private int wickets;
    private int legalBalls;
    private int wideDeliveries;
    private int noBallDeliveries;
    private int wideRuns;
    private int noBallPenaltyRuns;
    private int byeRuns;
    private int legByeRuns;
    private int penaltyRuns;
    private int fours;
    private int sixes;
    private List<WicketEvent> wicketEvents;

    private int strikerIndex;
    private int nonStrikerIndex;
    private int nextBatsmanIndex;

    public InningsScore(Team battingTeam, Team bowlingTeam) {
        this.battingTeam = battingTeam;
        this.bowlingTeam = bowlingTeam;
        this.strikerIndex = 0;
        this.nonStrikerIndex = 1;
        this.nextBatsmanIndex = 2;
        this.wicketEvents = new ArrayList<>();
    }

    public void addWide(int runs) {
        this.totalRuns += runs;
        this.wideRuns += runs;
        this.wideDeliveries++;
    }

    public void addNoBall(int batRuns) {
        this.totalRuns += 1 + batRuns;
        this.noBallPenaltyRuns += 1;
        this.noBallDeliveries++;

        if (batRuns > 0) {
            Player striker = getStriker();
            striker.addRuns(batRuns);
            if (batRuns == 4) {
                this.fours++;
            } else if (batRuns == 6) {
                this.sixes++;
            }
            if (batRuns % 2 != 0) {
                rotateStrike();
            }
        }
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

    public void addBye(int runs) {
        this.totalRuns += runs;
        this.byeRuns += runs;
        this.legalBalls++;
        if (runs % 2 != 0) {
            rotateStrike();
        }
        getStriker().faceDotBall();
    }

    public void addLegBye(int runs) {
        this.totalRuns += runs;
        this.legByeRuns += runs;
        this.legalBalls++;
        if (runs % 2 != 0) {
            rotateStrike();
        }
        getStriker().faceDotBall();
    }

    public void addPenaltyRuns(int runs) {
        this.totalRuns += runs;
        this.penaltyRuns += runs;
    }

    public void recordWicket(int overNumber, int ballNumber, Player bowler) {
        Player striker = getStriker();
        striker.faceDotBall();
        striker.setOut(true);

        this.wickets++;
        this.legalBalls++;
        String bowlerName = bowler != null ? bowler.getName() : "Unknown";
        this.wicketEvents.add(new WicketEvent(striker.getName(), bowlerName, overNumber, ballNumber));
        if (bowler != null) {
            bowler.addWicketTaken();
        }

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
        return legalBalls + wideDeliveries + noBallDeliveries;
    }

    public int getWides() {
        return wideRuns;
    }

    public int getNoBalls() {
        return noBallPenaltyRuns;
    }

    public int getFours() {
        return fours;
    }

    public int getSixes() {
        return sixes;
    }

    public int getByeRuns() {
        return byeRuns;
    }

    public int getLegByeRuns() {
        return legByeRuns;
    }

    public int getPenaltyRuns() {
        return penaltyRuns;
    }

    public int getWideDeliveries() {
        return wideDeliveries;
    }

    public int getNoBallDeliveries() {
        return noBallDeliveries;
    }

    public List<WicketEvent> getWicketEvents() {
        return wicketEvents;
    }
}
