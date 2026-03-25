package scoreboard;

import exception.InvalidBallInputException;
import match.Match;
import statistics.OverStats;
import statistics.Statistics;
import team.Player;
import team.Team;

import java.util.Scanner;

public class ScoreboardManager implements Statistics {
    private static final int[] VALID_RUNS = { 0, 1, 2, 3, 4, 6 };

    private final Match match;
    private final Scanner scanner;

    public ScoreboardManager(Match match, Scanner scanner) {
        this.match = match;
        this.scanner = scanner;
    }

    public void startMatch(Team firstBattingTeam) {
        Team team1 = firstBattingTeam;
        Team team2 = team1 == match.getTeamA() ? match.getTeamB() : match.getTeamA();

        // 1st innings
        InningsScore inn1 = playInnings(team1, team2, -1);
        int target = inn1.getTotalRuns() + 1;
        System.out.println("\nTarget for " + team2.getName() + ": " + target);

        // 2nd innings
        InningsScore inn2 = playInnings(team2, team1, target);

        displayMatchResult(inn1, inn2);
        displayOverallStats(inn1, inn2);
        askTeamForPlayerScores(match.getTeamA(), match.getTeamB());
    }

    private InningsScore playInnings(Team bat, Team bowl, int target) {
        InningsScore inn = new InningsScore(bat, bowl);
        OverStats overStats = new OverStats();
        boolean freeHit = false;

        showInningsStart(bat, bowl);

        int limitBalls = match.getOversPerInnings() * Match.BALLS_PER_OVER;

        while (inn.getLegalBalls() < limitBalls && !inn.isAllOut()) {
            if (target > 0 && inn.getTotalRuns() >= target) {
                break;
            }

            int overNum = inn.getLegalBalls() / Match.BALLS_PER_OVER + 1;
            int ballNum = inn.getLegalBalls() % Match.BALLS_PER_OVER + 1;

            showBallHeader(overNum, ballNum, inn.getStriker().getName(), freeHit);

            try {
                freeHit = takeBallInput(inn, overStats, freeHit);
            } catch (InvalidBallInputException e) {
                System.out.println("Wrong input: " + e.getMessage());
                continue;
            }

            if (inn.getLegalBalls() % Match.BALLS_PER_OVER == 0) {
                inn.completeOver();
                displayOverStats(overStats, inn.getLegalBalls() / Match.BALLS_PER_OVER);
                overStats = new OverStats();
            }

            showScoreLine(inn);
        }

        if (inn.getLegalBalls() % Match.BALLS_PER_OVER != 0) {
            System.out.println("\nPartial Over Summary:");
            displayOverStats(overStats, inn.getLegalBalls() / Match.BALLS_PER_OVER + 1);
        }

        displayFinalStats(inn);
        return inn;
    }

    // innings header
    private void showInningsStart(Team bat, Team bowl) {
        System.out.println("======================================");
        System.out.println("Innings: " + bat.getName() + " batting");
        System.out.println("Bowling Team: " + bowl.getName());
        System.out.println("======================================");
    }

    // ball info
    private void showBallHeader(int overNum, int ballNum, String striker, boolean freeHit) {
        System.out.println("\nOver " + overNum + ", Ball " + ballNum);
        System.out.println("Striker: " + striker);
        if (freeHit) {
            System.out.println("*** FREE HIT BALL ***");
        }
    }

    private void showScoreLine(InningsScore inn) {
        System.out.println("Current Score: " + inn.getBattingTeam().getName() + " " + inn.getTotalRuns()
                + "/" + inn.getWickets() + " (" + inn.getOversRepresentation() + ")");
    }

    // ball input
    private boolean takeBallInput(InningsScore inn, OverStats overStats, boolean freeHit)
            throws InvalidBallInputException {
        String name = inn.getStriker().getName();
        int choice = readInt("Enter event [1-Runs, 2-Wide, 3-No Ball, 4-Wicket]: ");

        if (choice == 1) {
            int runs = readInt("Enter runs (0,1,2,3,4,6): ");
            recordBall(inn, runs, overStats);
            System.out.println(name + " scored " + runs + " run(s)");
            freeHit = false;
        } else if (choice == 2) {
            recordBall(inn, "WIDE", overStats);
            System.out.println("Wide ball +1");
        } else if (choice == 3) {
            recordBall(inn, "NO_BALL", overStats);
            System.out.println("No ball +1, next ball free hit");
            freeHit = true;
        } else if (choice == 4) {
            if (freeHit) {
                System.out.println("Free hit - wicket not counted, dot ball only");
                inn.recordDotBall();
                freeHit = false;
            } else {
                recordBall(inn, "WICKET", overStats);
                System.out.println(name + " is OUT");
            }
        } else {
            throw new InvalidBallInputException("Invalid menu choice.");
        }

        return freeHit;
    }

    // runs
    private void recordBall(InningsScore inn, int runs, OverStats overStats) throws InvalidBallInputException {
        validateRuns(runs);
        inn.addRuns(runs);
        if (runs == 4) {
            overStats.incrementFours();
        } else if (runs == 6) {
            overStats.incrementSixes();
        }
    }

    // extras
    private void recordBall(InningsScore inn, String event, OverStats overStats) throws InvalidBallInputException {
        if ("WIDE".equals(event)) {
            inn.addWide();
            overStats.incrementWides();
        } else if ("NO_BALL".equals(event)) {
            inn.addNoBall();
            overStats.incrementNoBalls();
        } else if ("WICKET".equals(event)) {
            inn.recordWicket();
            overStats.incrementWickets();
        } else {
            throw new InvalidBallInputException("Unknown ball event: " + event);
        }
    }

    // check runs
    private void validateRuns(int runs) throws InvalidBallInputException {
        for (int i = 0; i < VALID_RUNS.length; i++) {
            if (VALID_RUNS[i] == runs) {
                return;
            }
        }
        throw new InvalidBallInputException("Runs must be one of 0,1,2,3,4,6.");
    }

    // read number
    private int readInt(String msg) throws InvalidBallInputException {
        System.out.print(msg);
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            throw new InvalidBallInputException("Empty input.");
        }
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            throw new InvalidBallInputException("Expected a number.");
        }
    }

    @Override
    public void displayOverStats(OverStats overStats, int overNumber) {
        System.out.println("\n--- Over " + overNumber + " Stats ---");
        System.out.println("4s: " + overStats.getFours());
        System.out.println("6s: " + overStats.getSixes());
        System.out.println("Wides: " + overStats.getWides());
        System.out.println("No Balls: " + overStats.getNoBalls());
        System.out.println("Wickets: " + overStats.getWickets());
    }

    @Override
    public void displayFinalStats(InningsScore inn) {
        Team bat = inn.getBattingTeam();
        System.out.println("\n===== Innings Complete: " + bat.getName() + " =====");
        System.out.println("Total: " + inn.getTotalRuns() + "/" + inn.getWickets()
                + " in " + inn.getOversRepresentation() + " overs");
        System.out.println("Balls played: " + inn.getTotalBalls() + " (legal " + inn.getLegalBalls()
                + ", extras " + (inn.getWides() + inn.getNoBalls()) + ")");
        System.out.println(
                "Avg per over: " + String.format("%.2f", calculateRunRate(inn.getTotalRuns(), inn.getLegalBalls())));
        System.out.println("Extras: " + (inn.getWides() + inn.getNoBalls())
                + " (Wides " + inn.getWides() + ", No Balls " + inn.getNoBalls() + ")");

        System.out.println("\nIndividual Player Scores:");
        for (Player p : bat.getPlayers()) {
            String status = p.isOut() ? "out" : "not out";
            System.out.println("- " + p.getName() + ": " + p.getRuns() + " (" + p.getBallsFaced()
                    + ") 4s:" + p.getFours() + " 6s:" + p.getSixes() + " [" + status + "]");
        }
    }

    @Override
    public double calculateRunRate(int runs, int balls) {
        if (balls == 0) {
            return 0.0;
        }
        double temp = (runs * 6.0) / balls;
        return temp;
    }

    private void displayMatchResult(InningsScore inn1, InningsScore inn2) {
        int run1 = inn1.getTotalRuns();
        int run2 = inn2.getTotalRuns();

        System.out.println("\n======================================");
        System.out.println("MATCH RESULT");
        System.out.println("======================================");

        if (run2 > run1) {
            int wk = Match.MAX_WICKETS - inn2.getWickets();
            System.out.println("Winner: " + inn2.getBattingTeam().getName() + " by " + wk + " wickets.");
        } else if (run1 > run2) {
            int diff = run1 - run2;
            System.out.println("Winner: " + inn1.getBattingTeam().getName() + " by " + diff + " runs.");
        } else {
            System.out.println("Match Tied!");
        }
    }

    private void displayOverallStats(InningsScore inn1, InningsScore inn2) {
        System.out.println("\n======================================");
        System.out.println("FINAL OVERALL STATISTICS");
        System.out.println("======================================");
        showTeamLine(inn1);
        showTeamLine(inn2);

        System.out.println("\nOverall 4s: " + (inn1.getFours() + inn2.getFours()));
        System.out.println("Overall 6s: " + (inn1.getSixes() + inn2.getSixes()));
        System.out.println("Overall Wides: " + (inn1.getWides() + inn2.getWides()));
        System.out.println("Overall No Balls: " + (inn1.getNoBalls() + inn2.getNoBalls()));
        System.out.println("Overall Wickets: " + (inn1.getWickets() + inn2.getWickets()));
    }

    // team suummary
    private void showTeamLine(InningsScore inn) {
        System.out.println(inn.getBattingTeam().getName() + ": " + inn.getTotalRuns() + "/"
                + inn.getWickets() + " (" + inn.getOversRepresentation() + " overs, balls " + inn.getTotalBalls()
                + ")");
        System.out.println("Avg per over: " + String.format("%.2f",
                calculateRunRate(inn.getTotalRuns(), inn.getLegalBalls())));
        System.out.println("4s:" + inn.getFours() + " 6s:" + inn.getSixes() + " Wides:" + inn.getWides()
                + " No Balls:" + inn.getNoBalls() + " Wickets:" + inn.getWickets());
        System.out.println();
    }

    // show players
    private void askTeamForPlayerScores(Team teamA, Team teamB) {
        System.out.println("\nShow player scores:");
        System.out.println("1- " + teamA.getName());
        System.out.println("2- " + teamB.getName());
        System.out.println("3- Both");
        System.out.println("0- Skip");

        try {
            int ch = readInt("Enter choice: ");
            if (ch == 1) {
                showPlayers(teamA);
            } else if (ch == 2) {
                showPlayers(teamB);
            } else if (ch == 3) {
                showPlayers(teamA);
                showPlayers(teamB);
            } else {
                System.out.println("Skipping player scores.");
            }
        } catch (InvalidBallInputException e) {
            System.out.println("Skipping player scores. " + e.getMessage());
        }
    }

    private void showPlayers(Team team) {
        System.out.println("\nScore Card - " + team.getName());
        Player[] players = team.getPlayers();
        for (int i = 0; i < players.length; i++) {
            Player p = players[i];
            String status = p.isOut() ? "out" : "not out";
            System.out.println((i + 1) + ". " + p.getName() + " - " + p.getRuns()
                    + " (" + p.getBallsFaced() + ") 4s:" + p.getFours() + " 6s:" + p.getSixes()
                    + " [" + status + "]");
        }
    }
}
