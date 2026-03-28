package filemanagement;

import match.Match;
import scoreboard.InningsScore;
import scoreboard.WicketEvent;
import team.Player;
import team.Team;

public class ScorecardFormatter {
    private final Match match;
    private final Team teamA;
    private final Team teamB;

    public ScorecardFormatter(Match match, Team teamA, Team teamB) {
        this.match = match;
        this.teamA = teamA;
        this.teamB = teamB;
    }

    public String buildFileContent(int scoreChoice, InningsScore inn1, InningsScore inn2) {
        StringBuilder content = new StringBuilder();
        content.append("Cricket Scoreboard").append(System.lineSeparator());
        content.append("Match Type: ").append(match.getMatchType()).append(System.lineSeparator());
        content.append("Overs Per Innings: ").append(match.getOversPerInnings()).append(System.lineSeparator());
        content.append(buildMatchResult(inn1, inn2)).append(System.lineSeparator());
        content.append(buildOverallStats(inn1, inn2));

        if (scoreChoice == 1) {
            content.append(System.lineSeparator()).append(System.lineSeparator()).append(buildTeamScorecard(inn1));
        } else if (scoreChoice == 2) {
            content.append(System.lineSeparator()).append(System.lineSeparator()).append(buildTeamScorecard(inn2));
        } else if (scoreChoice == 3) {
            content.append(System.lineSeparator()).append(System.lineSeparator()).append(buildTeamScorecard(inn1));
            content.append(System.lineSeparator()).append(System.lineSeparator()).append(buildTeamScorecard(inn2));
        } else {
            return null;
        }

        return content.toString();
    }

    public String buildMatchResult(InningsScore inn1, InningsScore inn2) {
        int run1 = inn1.getTotalRuns();
        int run2 = inn2.getTotalRuns();

        StringBuilder result = new StringBuilder();
        result.append("\n======================================").append(System.lineSeparator());
        result.append("MATCH RESULT").append(System.lineSeparator());
        result.append("======================================").append(System.lineSeparator());

        if (run2 > run1) {
            int wk = Match.MAX_WICKETS - inn2.getWickets();
            result.append("Winner: ").append(inn2.getBattingTeam().getName()).append(" by ").append(wk)
                    .append(" wickets.");
        } else if (run1 > run2) {
            int diff = run1 - run2;
            result.append("Winner: ").append(inn1.getBattingTeam().getName()).append(" by ").append(diff)
                    .append(" runs.");
        } else {
            result.append("Match Tied!");
        }

        return result.toString();
    }

    public String buildOverallStats(InningsScore inn1, InningsScore inn2) {
        StringBuilder stats = new StringBuilder();
        stats.append("\n======================================").append(System.lineSeparator());
        stats.append("FINAL OVERALL STATISTICS").append(System.lineSeparator());
        stats.append("======================================").append(System.lineSeparator());
        stats.append(buildTeamLine(inn1)).append(System.lineSeparator()).append(System.lineSeparator());
        stats.append(buildTeamLine(inn2)).append(System.lineSeparator()).append(System.lineSeparator());
        stats.append("Overall 4s: ").append(inn1.getFours() + inn2.getFours()).append(System.lineSeparator());
        stats.append("Overall 6s: ").append(inn1.getSixes() + inn2.getSixes()).append(System.lineSeparator());
        stats.append("Overall Wides: ").append(inn1.getWides() + inn2.getWides()).append(System.lineSeparator());
        stats.append("Overall No Balls: ").append(inn1.getNoBalls() + inn2.getNoBalls()).append(System.lineSeparator());
        stats.append("Overall Wickets: ").append(inn1.getWickets() + inn2.getWickets());

        Player mvp = determineMVP();
        if (mvp != null) {
            stats.append(System.lineSeparator()).append(System.lineSeparator());
            stats.append("MVP (Player of the Match): ").append(mvp.getName()).append(" | Runs: ")
                    .append(mvp.getRuns()).append(", Wickets: ").append(mvp.getWicketsTaken())
                    .append(", Runs Conceded: ").append(mvp.getRunsConceded());
        }

        return stats.toString();
    }

    private String buildTeamLine(InningsScore inn) {
        StringBuilder teamLine = new StringBuilder();
        teamLine.append(inn.getBattingTeam().getName()).append(": ").append(inn.getTotalRuns()).append("/")
                .append(inn.getWickets()).append(" (").append(inn.getOversRepresentation())
                .append(" overs, balls ").append(inn.getTotalBalls()).append(")").append(System.lineSeparator());
        teamLine.append("Avg per over: ").append(formatRunRate(inn.getTotalRuns(), inn.getLegalBalls()))
                .append(System.lineSeparator());
        teamLine.append("4s:").append(inn.getFours()).append(" 6s:").append(inn.getSixes()).append(" Wides:")
                .append(inn.getWides()).append(" No Balls:").append(inn.getNoBalls()).append(" Byes:")
                .append(inn.getByeRuns()).append(" Leg Byes:").append(inn.getLegByeRuns()).append(" Penalties:")
                .append(inn.getPenaltyRuns()).append(" Wickets:").append(inn.getWickets());
        return teamLine.toString();
    }

    private String buildTeamScorecard(InningsScore inn) {
        Team battingTeam = inn.getBattingTeam();
        int extras = inn.getWides() + inn.getNoBalls() + inn.getByeRuns() + inn.getLegByeRuns() + inn.getPenaltyRuns();
        StringBuilder scorecard = new StringBuilder();
        scorecard.append("===== Innings Complete: ").append(battingTeam.getName()).append(" =====")
                .append(System.lineSeparator());
        scorecard.append("Total: ").append(inn.getTotalRuns()).append("/").append(inn.getWickets())
                .append(" in ").append(inn.getOversRepresentation()).append(" overs").append(System.lineSeparator());
        scorecard.append("Balls delivered: ").append(inn.getTotalBalls()).append(" (legal ")
                .append(inn.getLegalBalls()).append(", wides ").append(inn.getWideDeliveries())
                .append(", no-balls ").append(inn.getNoBallDeliveries()).append(")").append(System.lineSeparator());
        scorecard.append("Avg per over: ").append(formatRunRate(inn.getTotalRuns(), inn.getLegalBalls()))
                .append(System.lineSeparator());
        scorecard.append("Extras: ").append(extras).append(" (Wides ").append(inn.getWides())
                .append(", No Balls ").append(inn.getNoBalls()).append(", Byes ").append(inn.getByeRuns())
                .append(", Leg Byes ").append(inn.getLegByeRuns()).append(", Penalties ")
                .append(inn.getPenaltyRuns()).append(")").append(System.lineSeparator()).append(System.lineSeparator());
        scorecard.append("Individual Player Scores:").append(System.lineSeparator());

        for (Player p : battingTeam.getPlayers()) {
            String status = p.isOut() ? "out" : "not out";
            scorecard.append("- ").append(p.getName()).append(": ").append(p.getRuns()).append(" (")
                    .append(p.getBallsFaced()).append(") 4s:").append(p.getFours()).append(" 6s:")
                    .append(p.getSixes()).append(" | Wkts:").append(p.getWicketsTaken()).append(" RunsConceded:")
                    .append(p.getRunsConceded()).append(" [").append(status).append("]")
                    .append(System.lineSeparator());
        }

        if (!inn.getWicketEvents().isEmpty()) {
            scorecard.append(System.lineSeparator()).append("Wickets:").append(System.lineSeparator());
            for (WicketEvent w : inn.getWicketEvents()) {
                scorecard.append(" - ").append(w.getOverBall()).append(": ").append(w.getBatter())
                        .append(" b ").append(w.getBowler()).append(System.lineSeparator());
            }
        }

        return scorecard.toString().trim();
    }

    private Player determineMVP() {
        Player best = null;
        int bestScore = Integer.MIN_VALUE;

        for (Player p : teamA.getPlayers()) {
            int score = computeScore(p);
            if (score > bestScore) {
                best = p;
                bestScore = score;
            }
        }

        for (Player p : teamB.getPlayers()) {
            int score = computeScore(p);
            if (score > bestScore) {
                best = p;
                bestScore = score;
            }
        }

        return best;
    }

    private int computeScore(Player p) {
        return p.getRuns() + (p.getWicketsTaken() * 25) - (p.getRunsConceded() / 2);
    }

    private String formatRunRate(int runs, int balls) {
        if (balls == 0) {
            return "0.00";
        }
        return String.format("%.2f", (runs * 6.0) / balls);
    }
}
