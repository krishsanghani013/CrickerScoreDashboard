package scoreboard;

import match.Match;
import java.util.ArrayList;
import java.util.List;
import team.Player;
import team.Team;

public class InningsScore {
    private final Team battingTeam;
    private final Team bowlingTeam;

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
    private final List<WicketEvent> wicketEvents;

    private int strikerIndex;
    private int nonStrikerIndex;
    private int nextBatsmanIndex;
    private boolean inningsClosed;

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

    public void addNoBallBatRuns(int batRuns) {
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

    public void addNoBallBye(int runs) {
        addNoBallExtraRuns(runs, true);
    }

    public void addNoBallLegBye(int runs) {
        addNoBallExtraRuns(runs, false);
    }

    public void addRuns(int runs) {
        addBatRunsWithExtras(runs, 0);
    }

    public void addOverthrowBatRuns(int batRuns, int overthrowRuns) {
        addBatRunsWithExtras(batRuns, overthrowRuns);
    }

    public void recordDotBall() {
        getStriker().faceDotBall();
        this.legalBalls++;
    }

    public void addBye(int runs) {
        addByeRuns(runs, 0);
    }

    public void addByeWithOverthrow(int byeCompletedRuns, int overthrowRuns) {
        addByeRuns(byeCompletedRuns, overthrowRuns);
    }

    public void addLegBye(int runs) {
        addLegByeRuns(runs, 0);
    }

    public void addLegByeWithOverthrow(int legByeCompletedRuns, int overthrowRuns) {
        addLegByeRuns(legByeCompletedRuns, overthrowRuns);
    }

    public void addPenaltyRuns(int runs) {
        this.totalRuns += runs;
        this.penaltyRuns += runs;
    }

    public WicketEvent recordDismissal(int overNumber, int ballNumber, Player bowler, DismissalType dismissalType) {
        DismissalType effectiveDismissal = dismissalType == null ? DismissalType.BOWLED : dismissalType;
        Player striker = getStriker();
        if (effectiveDismissal.consumesBall()) {
            striker.faceDotBall();
            this.legalBalls++;
        }

        striker.setOut(effectiveDismissal.countsAsWicket());
        if (effectiveDismissal.countsAsWicket()) {
            this.wickets++;
        }

        if (effectiveDismissal.isCreditedToBowler() && bowler != null) {
            bowler.addWicketTaken();
        }

        String bowlerName = effectiveDismissal.isCreditedToBowler() && bowler != null ? bowler.getName() : "";
        WicketEvent event = new WicketEvent(striker.getName(), bowlerName, effectiveDismissal, overNumber, ballNumber,
                effectiveDismissal.consumesBall());
        this.wicketEvents.add(event);
        replaceStriker();
        return event;
    }

    public boolean isAllOut() {
        return !canContinue(Match.MAX_WICKETS);
    }

    public boolean canContinue(int wicketLimit) {
        return !inningsClosed && wickets < wicketLimit;
    }

    public void completeOver() {
        if (!inningsClosed) {
            rotateStrike();
        }
    }

    public String getOversRepresentation() {
        return (legalBalls / Match.BALLS_PER_OVER) + "." + (legalBalls % Match.BALLS_PER_OVER);
    }

    private void rotateStrike() {
        int temp = this.strikerIndex;
        this.strikerIndex = this.nonStrikerIndex;
        this.nonStrikerIndex = temp;
    }

    private void addNoBallExtraRuns(int runs, boolean byeType) {
        this.totalRuns += 1 + runs;
        this.noBallPenaltyRuns += 1;
        this.noBallDeliveries++;
        if (byeType) {
            this.byeRuns += runs;
        } else {
            this.legByeRuns += runs;
        }
        if (runs % 2 != 0) {
            rotateStrike();
        }
    }

    private void addBatRunsWithExtras(int batRuns, int extraRuns) {
        Player striker = getStriker();
        striker.addRuns(batRuns);
        this.totalRuns += batRuns + extraRuns;
        this.legalBalls++;
        if (batRuns == 4) {
            this.fours++;
        } else if (batRuns == 6) {
            this.sixes++;
        }

        if ((batRuns + extraRuns) % 2 != 0) {
            rotateStrike();
        }
    }

    private void addByeRuns(int completedRuns, int overthrowRuns) {
        int totalByeRuns = completedRuns + overthrowRuns;
        this.totalRuns += totalByeRuns;
        this.byeRuns += totalByeRuns;
        this.legalBalls++;
        if (totalByeRuns % 2 != 0) {
            rotateStrike();
        }
        getStriker().faceDotBall();
    }

    private void addLegByeRuns(int completedRuns, int overthrowRuns) {
        int totalLegByeRuns = completedRuns + overthrowRuns;
        this.totalRuns += totalLegByeRuns;
        this.legByeRuns += totalLegByeRuns;
        this.legalBalls++;
        if (totalLegByeRuns % 2 != 0) {
            rotateStrike();
        }
        getStriker().faceDotBall();
    }

    private void replaceStriker() {
        if (nextBatsmanIndex < Match.PLAYERS_PER_TEAM) {
            strikerIndex = nextBatsmanIndex;
            nextBatsmanIndex++;
            return;
        }
        inningsClosed = true;
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

    public String getBattingStatus(Player player) {
        if (player == null) {
            return "not out";
        }

        for (int i = wicketEvents.size() - 1; i >= 0; i--) {
            WicketEvent event = wicketEvents.get(i);
            if (event.getBatter().equals(player.getName())) {
                return event.getDismissalType().getDisplayName();
            }
        }

        return player.isOut() ? "out" : "not out";
    }
}
