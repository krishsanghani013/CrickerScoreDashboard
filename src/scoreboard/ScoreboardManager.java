package scoreboard;

import exception.InvalidBallInputException;
import filemanagement.ScorecardFileManager;
import filemanagement.ScorecardFormatter;
import java.nio.file.Path;
import java.util.Random;
import java.util.Scanner;
import match.Match;
import statistics.OverStats;
import statistics.Statistics;
import team.Player;
import team.Team;

public class ScoreboardManager implements Statistics {
    private static final int[] VALID_RUNS = { 0, 1, 2, 3, 4, 6 };

    private final Match match;
    private final Scanner scanner;
    private final boolean autoMode;
    private final Random random;
    private final ScorecardFileManager scorecardFileManager;
    private final ScorecardFormatter scorecardFormatter;

    public ScoreboardManager(Match match, Scanner scanner) {
        this(match, scanner, false, new Random());
    }

    public ScoreboardManager(Match match, Scanner scanner, boolean autoMode, Random random) {
        this.match = match;
        this.scanner = scanner;
        this.autoMode = autoMode;
        this.random = random == null ? new Random() : random;
        this.scorecardFileManager = new ScorecardFileManager();
        this.scorecardFormatter = new ScorecardFormatter(match, match.getTeamA(), match.getTeamB());
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
        askToSaveScorecard(inn1, inn2);
    }

    private InningsScore playInnings(Team bat, Team bowl, int target) {
        InningsScore inn = new InningsScore(bat, bowl);
        OverStats overStats = new OverStats();
        boolean freeHit = false;
        Player currentBowler = null;

        showInningsStart(bat, bowl);

        int limitBalls = match.getOversPerInnings() * Match.BALLS_PER_OVER;

        while (inn.getLegalBalls() < limitBalls && !inn.isAllOut()) {
            if (target > 0 && inn.getTotalRuns() >= target) {
                break;
            }

            if (!autoMode && !scanner.hasNextLine()) {
                System.out.println("End of input reached.");
                break;
            }

            int overNum = inn.getLegalBalls() / Match.BALLS_PER_OVER + 1;
            int ballNum = inn.getLegalBalls() % Match.BALLS_PER_OVER + 1;

            if ((ballNum == 1)) {
                currentBowler = selectBowler(bowl, overNum);
            }

            showBallHeader(overNum, ballNum, inn.getStriker().getName(), freeHit);
            if (currentBowler != null) {
                System.out.println("Bowler: " + currentBowler.getName());
            }

            try {
                freeHit = takeBallInput(inn, overStats, freeHit, overNum, ballNum, currentBowler);
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
        return takeBallInput(inn, overStats, freeHit, 0, 0, null);
    }

    private boolean takeBallInput(InningsScore inn, OverStats overStats, boolean freeHit, int overNum, int ballNum,
            Player currentBowler) throws InvalidBallInputException {
        String name = inn.getStriker().getName();
        String line;

        if (autoMode) {
            line = generateAutoEvent(inn, freeHit);
            System.out.println("Enter event: " + line + " (auto)");
        } else {
            System.out.print("Enter event: ");
            line = scanner.nextLine().trim();
        }

        String normalized = line.trim();
        if (normalized.isEmpty()) {
            throw new InvalidBallInputException("Empty input");
        }

        String upper = normalized.toUpperCase();

        // Wide with optional runs
        if (upper.startsWith("WD")) {
            int runs = parseTrailingNumber(normalized, 1);
            recordWide(inn, runs, overStats, currentBowler);
            System.out.println("Wide ball +" + runs);
            return freeHit;
        }

        // No ball with optional bat runs
        if (upper.startsWith("NB")) {
            int batRuns = parseTrailingNumber(normalized, 0);
            recordNoBall(inn, batRuns, overStats, currentBowler);
            System.out.println("No ball +1" + (batRuns > 0 ? " and " + batRuns + " run(s) off the bat" : "")
                    + ", next ball free hit");
            return true;
        }

        // Byes
        if (upper.startsWith("B")) {
            int runs = parseTrailingNumber(normalized, 1);
            recordBye(inn, runs, overStats);
            System.out.println("Byes " + runs + " run(s)");
            return false;
        }

        // Leg byes
        if (upper.startsWith("LB")) {
            int runs = parseTrailingNumber(normalized, 1);
            recordLegBye(inn, runs, overStats);
            System.out.println("Leg byes " + runs + " run(s)");
            return false;
        }

        // Penalty runs
        if (upper.startsWith("P")) {
            int runs = parseTrailingNumber(normalized, 5);
            inn.addPenaltyRuns(runs);
            System.out.println("Penalty runs added: " + runs);
            return freeHit;
        }

        // Wicket
        if (upper.equals("W") || upper.equals("WICKET")) {
            if (freeHit) {
                System.out.println("Free hit - wicket not counted, dot ball only");
                inn.recordDotBall();
                return false;
            }
            recordWicket(inn, overStats, overNum, ballNum, currentBowler);
            System.out.println(name + " is OUT");
            return false;
        }

        // Numeric runs
        try {
            int runs = Integer.parseInt(normalized);
            recordRuns(inn, runs, overStats, currentBowler);
            System.out.println(name + " scored " + runs + " run(s)");
            return false;
        } catch (NumberFormatException e) {
            throw new InvalidBallInputException("Unknown event: " + normalized);
        }
    }

    // runs
    private void recordRuns(InningsScore inn, int runs, OverStats overStats, Player bowler)
            throws InvalidBallInputException {
        validateRuns(runs);
        inn.addRuns(runs);
        if (bowler != null) {
            bowler.addBowlingRuns(runs);
        }
        if (runs == 4) {
            overStats.incrementFours();
        } else if (runs == 6) {
            overStats.incrementSixes();
        }
    }

    private void recordWicket(InningsScore inn, OverStats overStats, int overNum, int ballNum, Player bowler) {
        inn.recordWicket(overNum, ballNum, bowler);
        overStats.incrementWickets();
    }

    private void recordWide(InningsScore inn, int runs, OverStats overStats, Player bowler)
            throws InvalidBallInputException {
        if (runs <= 0) {
            throw new InvalidBallInputException("Wide runs must be >=1");
        }
        inn.addWide(runs);
        if (bowler != null) {
            bowler.addBowlingRuns(runs);
        }
        overStats.incrementWides();
    }

    private void recordNoBall(InningsScore inn, int batRuns, OverStats overStats, Player bowler)
            throws InvalidBallInputException {
        if (batRuns < 0) {
            throw new InvalidBallInputException("No-ball bat runs cannot be negative");
        }
        inn.addNoBall(batRuns);
        if (bowler != null) {
            bowler.addBowlingRuns(1 + batRuns);
        }
        if (batRuns == 4) {
            overStats.incrementFours();
        } else if (batRuns == 6) {
            overStats.incrementSixes();
        }
        overStats.incrementNoBalls();
    }

    private void recordBye(InningsScore inn, int runs, OverStats overStats) throws InvalidBallInputException {
        if (runs < 0) {
            throw new InvalidBallInputException("Bye runs cannot be negative");
        }
        inn.addBye(runs);
    }

    private void recordLegBye(InningsScore inn, int runs, OverStats overStats) throws InvalidBallInputException {
        if (runs < 0) {
            throw new InvalidBallInputException("Leg bye runs cannot be negative");
        }
        inn.addLegBye(runs);
    }

    private String generateAutoEvent(InningsScore inn, boolean freeHit) {
        int roll = random.nextInt(100);

        if (!freeHit && roll >= 80 && roll < 88 && inn.getWickets() < Match.MAX_WICKETS) {
            return "W";
        }

        if (roll < 10) {
            int runs = randomRuns();
            return String.valueOf(runs);
        } else if (roll < 25) {
            int runs = randomRuns();
            return String.valueOf(runs);
        } else if (roll < 35) {
            int runs = random.nextInt(3) + 1; // 1-3 wides
            return runs == 1 ? "Wd" : "Wd " + runs;
        } else if (roll < 50) {
            int batRuns = random.nextInt(4); // 0-3 bat runs on no-ball
            if (batRuns == 3 && random.nextBoolean()) {
                batRuns = 4; // occasional boundary on no-ball
            }
            return batRuns == 0 ? "Nb" : "Nb " + batRuns;
        } else if (roll < 65) {
            int runs = random.nextInt(4); // 0-3 byes
            return "B " + runs;
        } else if (roll < 80) {
            int runs = random.nextInt(3); // 0-2 leg byes
            return "LB " + runs;
        }

        int runs = randomRuns();
        return String.valueOf(runs);
    }

    private int randomRuns() {
        int[] choices = { 0, 0, 1, 1, 1, 2, 3, 4, 6 };
        return choices[random.nextInt(choices.length)];
    }

    private int parseTrailingNumber(String text, int defaultValue) throws InvalidBallInputException {
        String trimmed = text.trim();
        String digits = trimmed.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            throw new InvalidBallInputException("Invalid number in input: " + text);
        }
    }

    private Player selectBowler(Team bowlingTeam, int overNum) {
        if (autoMode) {
            Player[] players = bowlingTeam.getPlayers();
            return players[random.nextInt(players.length)];
        }
        System.out.print("Enter bowler for over " + overNum + ": ");
        String name = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        if (name.isEmpty()) {
            return null;
        }
        for (Player p : bowlingTeam.getPlayers()) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        // if not matched, create a temp placeholder bowler name
        return new Player(name);
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
        if (!scanner.hasNextLine()) {
            throw new InvalidBallInputException("End of input.");
        }
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
        System.out.println("Balls delivered: " + inn.getTotalBalls() + " (legal " + inn.getLegalBalls()
                + ", wides " + inn.getWideDeliveries() + ", no-balls " + inn.getNoBallDeliveries() + ")");
        System.out.println(
                "Avg per over: " + String.format("%.2f", calculateRunRate(inn.getTotalRuns(), inn.getLegalBalls())));
        int extras = inn.getWides() + inn.getNoBalls() + inn.getByeRuns() + inn.getLegByeRuns()
                + inn.getPenaltyRuns();
        System.out.println("Extras: " + extras + " (Wides " + inn.getWides() + ", No Balls " + inn.getNoBalls()
                + ", Byes " + inn.getByeRuns() + ", Leg Byes " + inn.getLegByeRuns() + ", Penalties "
                + inn.getPenaltyRuns() + ")");

        System.out.println("\nIndividual Player Scores:");
        for (Player p : bat.getPlayers()) {
            String status = p.isOut() ? "out" : "not out";
            System.out.println("- " + p.getName() + ": " + p.getRuns() + " (" + p.getBallsFaced()
                    + ") 4s:" + p.getFours() + " 6s:" + p.getSixes() + " [" + status + "]");
        }

        if (!inn.getWicketEvents().isEmpty()) {
            System.out.println("\nWickets:");
            for (WicketEvent w : inn.getWicketEvents()) {
                System.out.println(" - " + w.getOverBall() + ": " + w.getBatter() + " b " + w.getBowler());
            }
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
        System.out.println(scorecardFormatter.buildMatchResult(inn1, inn2));
    }

    private void displayOverallStats(InningsScore inn1, InningsScore inn2) {
        System.out.println(scorecardFormatter.buildOverallStats(inn1, inn2));
    }

    // team suummary
    private void showTeamLine(InningsScore inn) {
        System.out.println(inn.getBattingTeam().getName() + ": " + inn.getTotalRuns() + "/"
                + inn.getWickets() + " (" + inn.getOversRepresentation() + " overs, balls " + inn.getTotalBalls()
                + ")");
        System.out.println("Avg per over: " + String.format("%.2f",
                calculateRunRate(inn.getTotalRuns(), inn.getLegalBalls())));
        System.out.println("4s:" + inn.getFours() + " 6s:" + inn.getSixes() + " Wides:" + inn.getWides()
                + " No Balls:" + inn.getNoBalls() + " Byes:" + inn.getByeRuns() + " Leg Byes:" + inn.getLegByeRuns()
                + " Penalties:" + inn.getPenaltyRuns() + " Wickets:" + inn.getWickets());
        System.out.println();
    }

    private Player determineMVP(Team teamA, Team teamB) {
        Player best = null;
        int bestScore = Integer.MIN_VALUE;
        for (Player p : teamA.getPlayers()) {
            int sc = computeScore(p);
            if (sc > bestScore) {
                best = p;
                bestScore = sc;
            }
        }
        for (Player p : teamB.getPlayers()) {
            int sc = computeScore(p);
            if (sc > bestScore) {
                best = p;
                bestScore = sc;
            }
        }
        return best;
    }

    private int computeScore(Player p) {
        // Simple heuristic: runs + wickets*25 - runsConceded/2
        return p.getRuns() + (p.getWicketsTaken() * 25) - (p.getRunsConceded() / 2);
    }

    // show players
    private void askTeamForPlayerScores(Team teamA, Team teamB) {
        System.out.println("\nShow player scores:");
        System.out.println("1- " + teamA.getName());
        System.out.println("2- " + teamB.getName());
        System.out.println("3- Both");
        System.out.println("0- Skip");

        if (!scanner.hasNextLine()) {
            return;
        }

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
                    + " | Wkts:" + p.getWicketsTaken() + " RunsConceded:" + p.getRunsConceded()
                    + " [" + status + "]");
        }
    }

    private void askToSaveScorecard(InningsScore inn1, InningsScore inn2) {
        System.out.print("\nDo you want to save the match score in a .txt file? (y/N): ");
        if (!scanner.hasNextLine()) {
            return;
        }

        String saveChoice = scanner.nextLine().trim();
        if (saveChoice.isEmpty()) {
            return;
        }

        char answer = Character.toLowerCase(saveChoice.charAt(0));
        if (answer != 'y' && answer != '1' && answer != 't') {
            return;
        }

        System.out.println("\nWhich team's score do you want to save?");
        System.out.println("1- " + inn1.getBattingTeam().getName());
        System.out.println("2- " + inn2.getBattingTeam().getName());
        System.out.println("3- Both");

        int scoreChoice;
        try {
            scoreChoice = readInt("Enter choice: ");
        } catch (InvalidBallInputException e) {
            System.out.println("Invalid choice. Score not saved.");
            return;
        }

        System.out.print("Enter file name: ");
        if (!scanner.hasNextLine()) {
            System.out.println("File name not provided. Score not saved.");
            return;
        }

        String fileName = scanner.nextLine().trim();
        if (fileName.isEmpty()) {
            System.out.println("Invalid file name. Score not saved.");
            return;
        }

        String content = scorecardFormatter.buildFileContent(scoreChoice, inn1, inn2);
        if (content == null) {
            System.out.println("Invalid choice. Score not saved.");
            return;
        }

        try {
            Path savedPath = scorecardFileManager.saveScorecard(fileName, content);
            System.out.println("Score saved successfully in " + savedPath);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid file name. Score not saved.");
        } catch (Exception e) {
            System.out.println("Error while saving file: " + e.getMessage());
        }
    }
}
