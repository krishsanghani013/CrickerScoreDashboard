package filemanagement;

import java.util.ArrayList;
import java.util.List;
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
        return buildFileContent(scoreChoice, inn1, inn2, null, null);
    }

    public String buildFileContent(int scoreChoice, InningsScore inn1, InningsScore inn2, String resultText,
            String additionalSummary) {
        StringBuilder content = new StringBuilder();
        content.append("Cricket Scoreboard").append(System.lineSeparator());
        content.append("Match Type: ").append(match.getMatchType()).append(System.lineSeparator());
        content.append("Overs Per Innings: ").append(match.getOversPerInnings()).append(System.lineSeparator());
        content.append(resultText == null || resultText.trim().isEmpty() ? buildMatchResult(inn1, inn2) : resultText)
                .append(System.lineSeparator());
        if (additionalSummary != null && !additionalSummary.trim().isEmpty()) {
            content.append(additionalSummary).append(System.lineSeparator());
        }
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

        MVPResult mvp = determineMVP(inn1, inn2);
        if (mvp != null) {
            appendMVPSection(stats, mvp);
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
            String status = inn.getBattingStatus(p);
            scorecard.append("- ").append(p.getName()).append(": ").append(p.getRuns()).append(" (")
                    .append(p.getBallsFaced()).append(") 4s:").append(p.getFours()).append(" 6s:")
                    .append(p.getSixes()).append(" | Wkts:").append(p.getWicketsTaken()).append(" RunsConceded:")
                    .append(p.getRunsConceded()).append(" [").append(status).append("]")
                    .append(System.lineSeparator());
        }

        if (!inn.getWicketEvents().isEmpty()) {
            scorecard.append(System.lineSeparator()).append("Batting Events:").append(System.lineSeparator());
            for (WicketEvent w : inn.getWicketEvents()) {
                scorecard.append(" - ").append(w.getOverBall()).append(": ").append(w.getDescription())
                        .append(System.lineSeparator());
            }
        }

        return scorecard.toString().trim();
    }

    private MVPResult determineMVP(InningsScore inn1, InningsScore inn2) {
        MVPResult best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        Team winningTeam = determineWinningTeam(inn1, inn2);

        for (Player p : teamA.getPlayers()) {
            MVPResult result = evaluatePlayer(p, inn1, inn2, winningTeam);
            if (result.getFinalScore() > bestScore) {
                best = result;
                bestScore = result.getFinalScore();
            }
        }

        for (Player p : teamB.getPlayers()) {
            MVPResult result = evaluatePlayer(p, inn1, inn2, winningTeam);
            if (result.getFinalScore() > bestScore) {
                best = result;
                bestScore = result.getFinalScore();
            }
        }

        return best;
    }

    private MVPResult evaluatePlayer(Player player, InningsScore inn1, InningsScore inn2, Team winningTeam) {
        Team playerTeam = findTeamForPlayer(player);
        InningsScore battingInnings = findBattingInnings(playerTeam, inn1, inn2);
        List<String> reasons = new ArrayList<>();

        double battingPoints = computeBattingPoints(player, battingInnings, reasons);
        double bowlingPoints = computeBowlingPoints(player, reasons);
        double allRoundBonus = computeAllRoundBonus(player, reasons);
        double winnerBonus = winningTeam != null && winningTeam == playerTeam ? 10.0 : 0.0;

        if (winningTeam != null && winningTeam == playerTeam && battingInnings != null
                && isTopScorerForTeam(playerTeam, player) && player.getRuns() >= 20) {
            winnerBonus += 6.0;
            reasons.add("Top scorer for winning team");
        }

        if (winningTeam != null && winningTeam == playerTeam && isTopWicketTakerForTeam(playerTeam, player)
                && player.getWicketsTaken() > 0) {
            winnerBonus += 3.0;
            reasons.add("Leading wicket taker for winning team");
        }

        if (winningTeam != null && winningTeam == playerTeam && battingInnings != null
                && isWinningChaseInnings(battingInnings, inn1, inn2) && player.getRuns() >= 15) {
            winnerBonus += 4.0;
            reasons.add("Strong contribution in winning chase");
            if (player.getBallsFaced() > 0 && ((player.getRuns() * 100.0) / player.getBallsFaced()) >= 140.0) {
                winnerBonus += 3.0;
                reasons.add("Fast scoring in successful chase");
            }
        }

        if (winningTeam != null && winningTeam == playerTeam) {
            reasons.add("Winning team impact bonus");
        }

        double finalScore = battingPoints + bowlingPoints + allRoundBonus + winnerBonus;
        return new MVPResult(player, battingPoints, bowlingPoints, allRoundBonus, winnerBonus, finalScore, reasons);
    }

    private double computeBattingPoints(Player player, InningsScore battingInnings, List<String> reasons) {
        if (player.getBallsFaced() == 0) {
            return 0.0;
        }

        double runs = player.getRuns();
        double strikeRate = (runs * 100.0) / player.getBallsFaced();
        double contribution = battingInnings == null || battingInnings.getTotalRuns() == 0
                ? 0.0
                : runs / battingInnings.getTotalRuns();

        double score = runs * 1.15;
        score += contribution * 38.0;
        score += (player.getFours() * 0.8) + (player.getSixes() * 1.6);

        if (runs >= 50) {
            score += 12.0;
            reasons.add("Half-century or better");
        } else if (runs >= 30) {
            score += 7.0;
            reasons.add("Strong batting contribution");
        } else if (runs >= 20) {
            score += 4.0;
            reasons.add("Useful batting contribution");
        }

        if (strikeRate >= 180) {
            score += 10.0;
            reasons.add("Explosive strike rate");
        } else if (strikeRate >= 150) {
            score += 7.0;
            reasons.add("Very high strike rate");
        } else if (strikeRate >= 120) {
            score += 4.0;
            reasons.add("Positive strike rate");
        } else if (strikeRate < 80 && runs < 15) {
            score -= 3.0;
        }

        if (contribution >= 0.35) {
            reasons.add("Large share of team total");
        }

        if (!player.isOut() && runs >= 10) {
            score += 3.0;
            reasons.add("Finished unbeaten");
        }

        return score;
    }

    private double computeBowlingPoints(Player player, List<String> reasons) {
        if (player.getWicketsTaken() == 0 && player.getRunsConceded() == 0) {
            return 0.0;
        }

        double score = player.getWicketsTaken() * 24.0;

        if (player.getWicketsTaken() >= 4) {
            score += 14.0;
            reasons.add("Match-defining wicket haul");
        } else if (player.getWicketsTaken() == 3) {
            score += 9.0;
            reasons.add("Three-wicket spell");
        } else if (player.getWicketsTaken() == 2) {
            score += 5.0;
            reasons.add("Two wickets");
        } else if (player.getWicketsTaken() == 1) {
            reasons.add("Picked up a wicket");
        }

        if (player.getRunsConceded() <= 8) {
            score += 6.0;
            reasons.add("Very economical bowling");
        } else if (player.getRunsConceded() <= 15) {
            score += 4.0;
            reasons.add("Economical bowling");
        } else if (player.getRunsConceded() <= 24) {
            score += 2.0;
        } else if (player.getRunsConceded() >= 40) {
            score -= 6.0;
        } else if (player.getRunsConceded() >= 30) {
            score -= 3.0;
        }

        if (player.getWicketsTaken() > 0) {
            double wicketEfficiency = player.getRunsConceded() / (double) player.getWicketsTaken();
            if (wicketEfficiency <= 8.0) {
                score += 6.0;
                reasons.add("High wicket efficiency");
            } else if (wicketEfficiency <= 12.0) {
                score += 4.0;
            } else if (wicketEfficiency <= 18.0) {
                score += 2.0;
            }
        }

        return score;
    }

    private double computeAllRoundBonus(Player player, List<String> reasons) {
        if (player.getRuns() >= 30 && player.getWicketsTaken() >= 1) {
            reasons.add("Strong all-round performance");
            return 10.0;
        }
        if (player.getRuns() >= 20 && player.getWicketsTaken() >= 1) {
            reasons.add("Useful all-round contribution");
            return 7.0;
        }
        if (player.getRuns() >= 10 && player.getWicketsTaken() >= 2) {
            reasons.add("Balanced batting and bowling impact");
            return 8.0;
        }
        return 0.0;
    }

    private Team determineWinningTeam(InningsScore inn1, InningsScore inn2) {
        if (inn1.getTotalRuns() > inn2.getTotalRuns()) {
            return inn1.getBattingTeam();
        }
        if (inn2.getTotalRuns() > inn1.getTotalRuns()) {
            return inn2.getBattingTeam();
        }
        return null;
    }

    private Team findTeamForPlayer(Player player) {
        for (Player candidate : teamA.getPlayers()) {
            if (candidate == player) {
                return teamA;
            }
        }
        return teamB;
    }

    private InningsScore findBattingInnings(Team team, InningsScore inn1, InningsScore inn2) {
        if (inn1.getBattingTeam() == team) {
            return inn1;
        }
        if (inn2.getBattingTeam() == team) {
            return inn2;
        }
        return null;
    }

    private boolean isTopScorerForTeam(Team team, Player player) {
        int bestRuns = Integer.MIN_VALUE;
        for (Player candidate : team.getPlayers()) {
            if (candidate.getRuns() > bestRuns) {
                bestRuns = candidate.getRuns();
            }
        }
        return player.getRuns() == bestRuns;
    }

    private boolean isTopWicketTakerForTeam(Team team, Player player) {
        int bestWickets = Integer.MIN_VALUE;
        for (Player candidate : team.getPlayers()) {
            if (candidate.getWicketsTaken() > bestWickets) {
                bestWickets = candidate.getWicketsTaken();
            }
        }
        return player.getWicketsTaken() == bestWickets;
    }

    private boolean isWinningChaseInnings(InningsScore battingInnings, InningsScore inn1, InningsScore inn2) {
        return battingInnings == inn2 && inn2.getTotalRuns() > inn1.getTotalRuns();
    }

    private void appendMVPSection(StringBuilder stats, MVPResult mvp) {
        Player player = mvp.getPlayer();
        stats.append(System.lineSeparator()).append(System.lineSeparator());
        stats.append("MVP (Player of the Match): ").append(player.getName()).append(System.lineSeparator());
        stats.append("Runs: ").append(player.getRuns()).append(System.lineSeparator());
        stats.append("Balls: ").append(player.getBallsFaced()).append(System.lineSeparator());
        stats.append("Strike Rate: ").append(formatStrikeRate(player)).append(System.lineSeparator());
        stats.append("Wickets: ").append(player.getWicketsTaken()).append(System.lineSeparator());
        stats.append("Runs Conceded: ").append(player.getRunsConceded()).append(System.lineSeparator());
        stats.append("Batting Impact: ").append(formatScore(mvp.getBattingImpact())).append(System.lineSeparator());
        stats.append("Bowling Impact: ").append(formatScore(mvp.getBowlingImpact())).append(System.lineSeparator());
        stats.append("All-Round Bonus: ").append(formatScore(mvp.getAllRoundBonus())).append(System.lineSeparator());
        stats.append("Winning Bonus: ").append(formatScore(mvp.getWinningBonus())).append(System.lineSeparator());
        stats.append("Final MVP Score: ").append(formatScore(mvp.getFinalScore())).append(System.lineSeparator());
        stats.append("Reason:");
        if (mvp.getReasons().isEmpty()) {
            stats.append(System.lineSeparator()).append("- Overall match impact");
            return;
        }
        for (String reason : mvp.getReasons()) {
            stats.append(System.lineSeparator()).append("- ").append(reason);
        }
    }

    private String formatStrikeRate(Player player) {
        if (player.getBallsFaced() == 0) {
            return "0.00";
        }
        return String.format("%.2f", (player.getRuns() * 100.0) / player.getBallsFaced());
    }

    private String formatScore(double score) {
        return String.format("%.2f", score);
    }

    private String formatRunRate(int runs, int balls) {
        if (balls == 0) {
            return "0.00";
        }
        return String.format("%.2f", (runs * 6.0) / balls);
    }
}
