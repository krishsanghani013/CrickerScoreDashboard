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
    private static final int SUPER_OVER_BALLS = Match.BALLS_PER_OVER;
    private static final int SUPER_OVER_WICKETS = 2;

    private final Match match;
    private final Scanner scanner;
    private final boolean autoMode;
    private final Random random;
    private final ScorecardFileManager scorecardFileManager;
    private final ScorecardFormatter scorecardFormatter;

    private String matchResultText;
    private String superOverSummaryText;

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
        this.matchResultText = "";
        this.superOverSummaryText = "";
    }

    public void startMatch(Team firstBattingTeam) {
        Team team1 = firstBattingTeam;
        Team team2 = team1 == match.getTeamA() ? match.getTeamB() : match.getTeamA();

        InningsScore inn1 = playInnings(team1, team2, -1, match.getOversPerInnings() * Match.BALLS_PER_OVER,
                Match.MAX_WICKETS, "First Innings");
        int target = inn1.getTotalRuns() + 1;
        System.out.println("\nTarget for " + team2.getName() + ": " + target);

        InningsScore inn2 = playInnings(team2, team1, target, match.getOversPerInnings() * Match.BALLS_PER_OVER,
                Match.MAX_WICKETS, "Second Innings");

        matchResultText = scorecardFormatter.buildMatchResult(inn1, inn2);
        superOverSummaryText = "";
        if (inn1.getTotalRuns() == inn2.getTotalRuns()) {
            SuperOverResult superOverResult = playSuperOver(team1, team2);
            matchResultText = superOverResult.resultText;
            superOverSummaryText = superOverResult.summaryText;
        }

        displayMatchResult(inn1, inn2);
        displayOverallStats(inn1, inn2);
        askTeamForPlayerScores(match.getTeamA(), match.getTeamB());
        askToSaveScorecard(inn1, inn2);
    }

    private InningsScore playInnings(Team bat, Team bowl, int target, int maxLegalBalls, int wicketLimit,
            String inningsLabel) {
        InningsScore inningsScore = new InningsScore(bat, bowl);
        OverStats overStats = new OverStats();
        boolean freeHit = false;
        Player currentBowler = null;
        Player previousOverBowler = null;

        showInningsStart(bat, bowl, inningsLabel, maxLegalBalls, wicketLimit);

        while (inningsScore.getLegalBalls() < maxLegalBalls && inningsScore.canContinue(wicketLimit)) {
            if (target > 0 && inningsScore.getTotalRuns() >= target) {
                break;
            }

            if (!autoMode && !scanner.hasNextLine()) {
                System.out.println("End of input reached.");
                break;
            }

            int overNum = inningsScore.getLegalBalls() / Match.BALLS_PER_OVER + 1;
            int ballNum = inningsScore.getLegalBalls() % Match.BALLS_PER_OVER + 1;

            if (ballNum == 1 && currentBowler == null) {
                currentBowler = selectBowler(bowl, overNum, previousOverBowler);
            }

            showBallHeader(overNum, ballNum, inningsScore.getStriker().getName(), freeHit);
            if (currentBowler != null) {
                System.out.println("Bowler: " + currentBowler.getName());
            }

            try {
                freeHit = takeBallInput(inningsScore, overStats, freeHit, overNum, ballNum, currentBowler, target,
                        maxLegalBalls, wicketLimit);
            } catch (InvalidBallInputException e) {
                System.out.println("Wrong input: " + e.getMessage());
                continue;
            }

            if (inningsScore.getLegalBalls() > 0 && inningsScore.getLegalBalls() % Match.BALLS_PER_OVER == 0) {
                inningsScore.completeOver();
                displayOverStats(overStats, inningsScore.getLegalBalls() / Match.BALLS_PER_OVER);
                overStats = new OverStats();
                previousOverBowler = currentBowler;
                currentBowler = null;
            }

            showScoreLine(inningsScore);
        }

        if (inningsScore.getLegalBalls() % Match.BALLS_PER_OVER != 0) {
            System.out.println("\nPartial Over Summary:");
            displayOverStats(overStats, inningsScore.getLegalBalls() / Match.BALLS_PER_OVER + 1);
        }

        displayFinalStats(inningsScore);
        return inningsScore;
    }

    private SuperOverResult playSuperOver(Team team1, Team team2) {
        System.out.println("\nMatch tied after the main innings. Super Over will decide the winner.");
        Team selectedOriginal = chooseSuperOverFirstBatting(team1, team2);
        Team superTeam1 = cloneTeam(team1);
        Team superTeam2 = cloneTeam(team2);
        Team firstBatting = selectedOriginal == team1 ? superTeam1 : superTeam2;
        Team secondBatting = selectedOriginal == team1 ? superTeam2 : superTeam1;

        InningsScore superOver1 = playInnings(firstBatting, secondBatting, -1, SUPER_OVER_BALLS, SUPER_OVER_WICKETS,
                "Super Over 1");
        int superTarget = superOver1.getTotalRuns() + 1;
        System.out.println("\nSuper Over target for " + secondBatting.getName() + ": " + superTarget);

        InningsScore superOver2 = playInnings(secondBatting, firstBatting, superTarget, SUPER_OVER_BALLS,
                SUPER_OVER_WICKETS, "Super Over 2");

        String summary = buildSuperOverSummary(superOver1, superOver2);
        String result;
        if (superOver2.getTotalRuns() > superOver1.getTotalRuns()) {
            int wicketsLeft = SUPER_OVER_WICKETS - superOver2.getWickets();
            result = "Super Over Winner: " + secondBatting.getName() + " by " + wicketsLeft + " wicket(s).";
        } else if (superOver1.getTotalRuns() > superOver2.getTotalRuns()) {
            int runMargin = superOver1.getTotalRuns() - superOver2.getTotalRuns();
            result = "Super Over Winner: " + firstBatting.getName() + " by " + runMargin + " run(s).";
        } else {
            result = "Match tied even after the Super Over.";
        }

        return new SuperOverResult(result, summary);
    }

    private Team cloneTeam(Team original) {
        Player[] originalPlayers = original.getPlayers();
        String[] playerNames = new String[originalPlayers.length];
        for (int i = 0; i < originalPlayers.length; i++) {
            playerNames[i] = originalPlayers[i].getName();
        }
        return new Team(original.getName(), playerNames);
    }

    private Team chooseSuperOverFirstBatting(Team team1, Team team2) {
        if (autoMode) {
            return random.nextBoolean() ? team1 : team2;
        }
        try {
            int choice = readMenuChoice("Choose Super Over batting first team:", new String[] {
                    team1.getName(),
                    team2.getName()
            });
            return choice == 1 ? team1 : team2;
        } catch (InvalidBallInputException e) {
            System.out.println("Invalid Super Over choice. Defaulting to " + team1.getName() + ".");
        }
        return team1;
    }

    private String buildSuperOverSummary(InningsScore first, InningsScore second) {
        StringBuilder summary = new StringBuilder();
        summary.append("\nSUPER OVER SUMMARY").append(System.lineSeparator());
        summary.append(first.getBattingTeam().getName()).append(": ").append(first.getTotalRuns()).append("/")
                .append(first.getWickets()).append(" (").append(first.getOversRepresentation()).append(")")
                .append(System.lineSeparator());
        summary.append(second.getBattingTeam().getName()).append(": ").append(second.getTotalRuns()).append("/")
                .append(second.getWickets()).append(" (").append(second.getOversRepresentation()).append(")");
        return summary.toString();
    }

    private void showInningsStart(Team bat, Team bowl, String inningsLabel, int maxLegalBalls, int wicketLimit) {
        System.out.println("======================================");
        System.out.println(inningsLabel + ": " + bat.getName() + " batting");
        System.out.println("Bowling Team: " + bowl.getName());
        System.out.println("Limits: " + (maxLegalBalls / Match.BALLS_PER_OVER) + " over(s), " + wicketLimit
                + " wicket(s)");
        if (!autoMode) {
            System.out.println("Manual scoring mode is menu-based.");
            System.out.println("For each ball, choose a main event number and then a sub-option when needed.");
        }
        System.out.println("======================================");
    }

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

    private boolean takeBallInput(InningsScore inn, OverStats overStats, boolean freeHit)
            throws InvalidBallInputException {
        return takeBallInput(inn, overStats, freeHit, 0, 0, null, -1, Match.BALLS_PER_OVER, Match.MAX_WICKETS);
    }

    private boolean takeBallInput(InningsScore inn, OverStats overStats, boolean freeHit, int overNum, int ballNum,
            Player currentBowler, int target, int maxLegalBalls, int wicketLimit) throws InvalidBallInputException {
        if (autoMode) {
            String line = generateAutoEvent(inn, freeHit, target, maxLegalBalls, wicketLimit);
            System.out.println("Enter event: " + line + " (auto)");
            return processEventLine(inn, overStats, freeHit, overNum, ballNum, currentBowler, line);
        }

        return takeManualBallInput(inn, overStats, freeHit, overNum, ballNum, currentBowler);
    }

    private boolean processEventLine(InningsScore inn, OverStats overStats, boolean freeHit, int overNum, int ballNum,
            Player currentBowler, String line) throws InvalidBallInputException {
        String strikerName = inn.getStriker().getName();
        String normalized = line.trim();
        if (normalized.isEmpty()) {
            throw new InvalidBallInputException("Empty input");
        }

        String[] parts = normalized.split("\\s+");
        String command = normalizeKeyword(parts[0]);

        if (isKeyword(command, "WD", "WIDE")) {
            int runs = parts.length > 1 ? parseStrictInt(parts[1], "wide runs") : 1;
            recordWide(inn, runs, overStats, currentBowler);
            System.out.println("Wide ball +" + runs);
            return freeHit;
        }

        if (isKeyword(command, "NB", "NOBALL")) {
            return handleNoBallInput(parts, inn, overStats, currentBowler);
        }

        if (isKeyword(command, "LB", "LEGBYE")) {
            int runs = parts.length > 1 ? parseStrictInt(parts[1], "leg bye runs") : 1;
            recordLegBye(inn, runs);
            System.out.println("Leg byes " + runs + " run(s)");
            return false;
        }

        if (isKeyword(command, "B", "BYE")) {
            int runs = parts.length > 1 ? parseStrictInt(parts[1], "bye runs") : 1;
            recordBye(inn, runs);
            System.out.println("Byes " + runs + " run(s)");
            return false;
        }

        if (isKeyword(command, "OT", "OVERTHROW")) {
            handleOverthrowInput(parts, inn, overStats, currentBowler, strikerName);
            return false;
        }

        if (isKeyword(command, "P", "PENALTY")) {
            int runs = parts.length > 1 ? parseStrictInt(parts[1], "penalty runs") : 5;
            if (runs <= 0) {
                throw new InvalidBallInputException("Penalty runs must be greater than zero.");
            }
            inn.addPenaltyRuns(runs);
            System.out.println("Penalty runs added: " + runs);
            return freeHit;
        }

        if (isKeyword(command, "RET", "RETIRED")) {
            DismissalType retirementType = parseRetirementType(parts);
            WicketEvent event = inn.recordDismissal(overNum, ballNum, currentBowler, retirementType);
            System.out.println(event.getDescription());
            return freeHit;
        }

        if (isKeyword(command, "W", "WICKET")) {
            DismissalType dismissalType = parseDismissalType(parts);
            if (freeHit && !dismissalType.isAllowedOnFreeHit()) {
                System.out.println("Free hit - only run out can dismiss the batter. Dot ball recorded.");
                inn.recordDotBall();
                return false;
            }
            WicketEvent event = recordWicket(inn, overStats, overNum, ballNum, currentBowler, dismissalType);
            System.out.println(event.getDescription());
            return false;
        }

        try {
            int runs = Integer.parseInt(normalized);
            recordRuns(inn, runs, overStats, currentBowler);
            System.out.println(strikerName + " scored " + runs + " run(s)");
            return false;
        } catch (NumberFormatException e) {
            throw new InvalidBallInputException("Unknown event: " + normalized);
        }
    }

    private boolean takeManualBallInput(InningsScore inn, OverStats overStats, boolean freeHit, int overNum,
            int ballNum, Player currentBowler) throws InvalidBallInputException {
        int eventChoice = readMenuChoice("Choose ball event:", new String[] {
                "Batter runs / dot ball",
                freeHit ? "Wicket (run out only on free hit)" : "Wicket",
                "Wide",
                "No ball",
                "Bye",
                "Leg bye",
                "Overthrow",
                "Penalty runs",
                "Retired batter"
        });

        switch (eventChoice) {
            case 1:
                return handleManualRuns(inn, overStats, currentBowler);
            case 2:
                return handleManualWicket(inn, overStats, freeHit, overNum, ballNum, currentBowler);
            case 3:
                return handleManualWide(inn, overStats, freeHit, currentBowler);
            case 4:
                return handleManualNoBall(inn, overStats, currentBowler);
            case 5:
                return handleManualBye(inn);
            case 6:
                return handleManualLegBye(inn);
            case 7:
                return handleManualOverthrow(inn, overStats, currentBowler);
            case 8:
                return handleManualPenalty(inn, freeHit);
            case 9:
                return handleManualRetirement(inn, freeHit, overNum, ballNum, currentBowler);
            default:
                throw new InvalidBallInputException("Unsupported menu choice.");
        }
    }

    private boolean handleManualRuns(InningsScore inn, OverStats overStats, Player currentBowler)
            throws InvalidBallInputException {
        String strikerName = inn.getStriker().getName();
        int runs = chooseBatRuns();
        recordRuns(inn, runs, overStats, currentBowler);
        System.out.println(strikerName + " scored " + runs + " run(s)");
        return false;
    }

    private boolean handleManualWicket(InningsScore inn, OverStats overStats, boolean freeHit, int overNum, int ballNum,
            Player currentBowler) throws InvalidBallInputException {
        DismissalType dismissalType;
        if (freeHit) {
            System.out.println("Free hit rule: only run out can dismiss the batter.");
            dismissalType = DismissalType.RUN_OUT;
        } else {
            int choice = readMenuChoice("Choose wicket type:", new String[] {
                    "Bowled",
                    "Caught",
                    "Run out",
                    "Stumped",
                    "LBW",
                    "Hit wicket"
            });
            dismissalType = dismissalTypeFromMenu(choice);
        }

        WicketEvent event = recordWicket(inn, overStats, overNum, ballNum, currentBowler, dismissalType);
        System.out.println(event.getDescription());
        return false;
    }

    private boolean handleManualWide(InningsScore inn, OverStats overStats, boolean freeHit, Player currentBowler)
            throws InvalidBallInputException {
        int runs = chooseWideRuns();
        recordWide(inn, runs, overStats, currentBowler);
        System.out.println("Wide ball +" + runs);
        return freeHit;
    }

    private boolean handleManualNoBall(InningsScore inn, OverStats overStats, Player currentBowler)
            throws InvalidBallInputException {
        int choice = readMenuChoice("Choose no-ball type:", new String[] {
                "No-ball only (+1)",
                "No-ball with bat runs",
                "No-ball with byes",
                "No-ball with leg byes"
        });

        switch (choice) {
            case 1:
                recordNoBallBatRuns(inn, 0, overStats, currentBowler);
                System.out.println("No ball +1, next ball free hit");
                return true;
            case 2:
                int batRuns = chooseBatRuns();
                recordNoBallBatRuns(inn, batRuns, overStats, currentBowler);
                System.out.println("No ball +1" + (batRuns > 0 ? " and " + batRuns + " run(s) off the bat" : "")
                        + ", next ball free hit");
                return true;
            case 3:
                int byeRuns = chooseExtraRunsAllowZero("Choose bye runs on the no-ball:");
                recordNoBallBye(inn, byeRuns, overStats, currentBowler);
                System.out.println("No ball + byes " + byeRuns + ", next ball free hit");
                return true;
            case 4:
                int legByeRuns = chooseExtraRunsAllowZero("Choose leg-bye runs on the no-ball:");
                recordNoBallLegBye(inn, legByeRuns, overStats, currentBowler);
                System.out.println("No ball + leg byes " + legByeRuns + ", next ball free hit");
                return true;
            default:
                throw new InvalidBallInputException("Unsupported no-ball choice.");
        }
    }

    private boolean handleManualBye(InningsScore inn) throws InvalidBallInputException {
        int runs = chooseExtraRunsAllowZero("Choose bye runs:");
        recordBye(inn, runs);
        System.out.println("Byes " + runs + " run(s)");
        return false;
    }

    private boolean handleManualLegBye(InningsScore inn) throws InvalidBallInputException {
        int runs = chooseExtraRunsAllowZero("Choose leg-bye runs:");
        recordLegBye(inn, runs);
        System.out.println("Leg byes " + runs + " run(s)");
        return false;
    }

    private boolean handleManualOverthrow(InningsScore inn, OverStats overStats, Player currentBowler)
            throws InvalidBallInputException {
        int choice = readMenuChoice("Choose overthrow type:", new String[] {
                "Bat runs + overthrow runs",
                "Byes + overthrow runs",
                "Leg byes + overthrow runs"
        });

        switch (choice) {
            case 1:
                int batRuns = chooseBatRuns();
                int overthrowRuns = chooseOverthrowRuns();
                recordBatOverthrow(inn, batRuns, overthrowRuns, overStats, currentBowler);
                System.out.println("Bat runs " + batRuns + " with overthrow " + overthrowRuns);
                return false;
            case 2:
                int byeRuns = chooseExtraRunsAllowZero("Choose completed bye runs before the overthrow:");
                int byeOverthrows = chooseOverthrowRuns();
                recordByeOverthrow(inn, byeRuns, byeOverthrows);
                System.out.println("Byes " + byeRuns + " with overthrow " + byeOverthrows);
                return false;
            case 3:
                int legByeRuns = chooseExtraRunsAllowZero("Choose completed leg-bye runs before the overthrow:");
                int legByeOverthrows = chooseOverthrowRuns();
                recordLegByeOverthrow(inn, legByeRuns, legByeOverthrows);
                System.out.println("Leg byes " + legByeRuns + " with overthrow " + legByeOverthrows);
                return false;
            default:
                throw new InvalidBallInputException("Unsupported overthrow choice.");
        }
    }

    private boolean handleManualPenalty(InningsScore inn, boolean freeHit) throws InvalidBallInputException {
        int runs = choosePenaltyRuns();
        inn.addPenaltyRuns(runs);
        System.out.println("Penalty runs added: " + runs);
        return freeHit;
    }

    private boolean handleManualRetirement(InningsScore inn, boolean freeHit, int overNum, int ballNum,
            Player currentBowler) throws InvalidBallInputException {
        int choice = readMenuChoice("Choose retirement type:", new String[] {
                "Retired out",
                "Retired hurt"
        });
        DismissalType retirementType = choice == 1 ? DismissalType.RETIRED_OUT : DismissalType.RETIRED_HURT;
        WicketEvent event = inn.recordDismissal(overNum, ballNum, currentBowler, retirementType);
        System.out.println(event.getDescription());
        return freeHit;
    }

    private boolean handleNoBallInput(String[] parts, InningsScore inn, OverStats overStats, Player bowler)
            throws InvalidBallInputException {
        if (parts.length == 1) {
            recordNoBallBatRuns(inn, 0, overStats, bowler);
            System.out.println("No ball +1, next ball free hit");
            return true;
        }

        String secondToken = normalizeKeyword(parts[1]);
        if (isKeyword(secondToken, "B", "BYE")) {
            int runs = parts.length > 2 ? parseStrictInt(parts[2], "no-ball bye runs") : 1;
            recordNoBallBye(inn, runs, overStats, bowler);
            System.out.println("No ball + byes " + runs + ", next ball free hit");
            return true;
        }

        if (isKeyword(secondToken, "LB", "LEGBYE")) {
            int runs = parts.length > 2 ? parseStrictInt(parts[2], "no-ball leg bye runs") : 1;
            recordNoBallLegBye(inn, runs, overStats, bowler);
            System.out.println("No ball + leg byes " + runs + ", next ball free hit");
            return true;
        }

        int batRuns = parseStrictInt(parts[1], "no-ball bat runs");
        recordNoBallBatRuns(inn, batRuns, overStats, bowler);
        System.out.println("No ball +1" + (batRuns > 0 ? " and " + batRuns + " run(s) off the bat" : "")
                + ", next ball free hit");
        return true;
    }

    private void handleOverthrowInput(String[] parts, InningsScore inn, OverStats overStats, Player bowler,
            String strikerName) throws InvalidBallInputException {
        if (parts.length < 3) {
            throw new InvalidBallInputException(
                    "Use OT <batRuns> <overthrows> or OT B/LB <runs> <overthrows>.");
        }

        String secondToken = normalizeKeyword(parts[1]);
        if (isKeyword(secondToken, "B", "BYE")) {
            int byeRuns = parseStrictInt(parts[2], "bye runs");
            int overthrowRuns = parts.length > 3 ? parseStrictInt(parts[3], "overthrow runs") : 0;
            recordByeOverthrow(inn, byeRuns, overthrowRuns);
            System.out.println("Byes " + byeRuns + " with overthrow " + overthrowRuns);
            return;
        }

        if (isKeyword(secondToken, "LB", "LEGBYE")) {
            int legByeRuns = parseStrictInt(parts[2], "leg bye runs");
            int overthrowRuns = parts.length > 3 ? parseStrictInt(parts[3], "overthrow runs") : 0;
            recordLegByeOverthrow(inn, legByeRuns, overthrowRuns);
            System.out.println("Leg byes " + legByeRuns + " with overthrow " + overthrowRuns);
            return;
        }

        int batRuns = parseStrictInt(parts[1], "bat runs");
        int overthrowRuns = parseStrictInt(parts[2], "overthrow runs");
        recordBatOverthrow(inn, batRuns, overthrowRuns, overStats, bowler);
        System.out.println(strikerName + " scored " + batRuns + " with overthrow " + overthrowRuns);
    }

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

    private WicketEvent recordWicket(InningsScore inn, OverStats overStats, int overNum, int ballNum, Player bowler,
            DismissalType dismissalType) {
        WicketEvent event = inn.recordDismissal(overNum, ballNum, bowler, dismissalType);
        if (dismissalType.countsAsWicket() && dismissalType.consumesBall()) {
            overStats.incrementWickets();
        }
        return event;
    }

    private void recordWide(InningsScore inn, int runs, OverStats overStats, Player bowler)
            throws InvalidBallInputException {
        if (runs <= 0) {
            throw new InvalidBallInputException("Wide runs must be >= 1.");
        }
        inn.addWide(runs);
        if (bowler != null) {
            bowler.addBowlingRuns(runs);
        }
        overStats.incrementWides();
    }

    private void recordNoBallBatRuns(InningsScore inn, int batRuns, OverStats overStats, Player bowler)
            throws InvalidBallInputException {
        if (batRuns < 0) {
            throw new InvalidBallInputException("No-ball bat runs cannot be negative.");
        }
        inn.addNoBallBatRuns(batRuns);
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

    private void recordNoBallBye(InningsScore inn, int runs, OverStats overStats, Player bowler)
            throws InvalidBallInputException {
        if (runs < 0) {
            throw new InvalidBallInputException("No-ball bye runs cannot be negative.");
        }
        inn.addNoBallBye(runs);
        if (bowler != null) {
            bowler.addBowlingRuns(1 + runs);
        }
        overStats.incrementNoBalls();
    }

    private void recordNoBallLegBye(InningsScore inn, int runs, OverStats overStats, Player bowler)
            throws InvalidBallInputException {
        if (runs < 0) {
            throw new InvalidBallInputException("No-ball leg bye runs cannot be negative.");
        }
        inn.addNoBallLegBye(runs);
        if (bowler != null) {
            bowler.addBowlingRuns(1 + runs);
        }
        overStats.incrementNoBalls();
    }

    private void recordBye(InningsScore inn, int runs) throws InvalidBallInputException {
        if (runs < 0) {
            throw new InvalidBallInputException("Bye runs cannot be negative.");
        }
        inn.addBye(runs);
    }

    private void recordLegBye(InningsScore inn, int runs) throws InvalidBallInputException {
        if (runs < 0) {
            throw new InvalidBallInputException("Leg bye runs cannot be negative.");
        }
        inn.addLegBye(runs);
    }

    private void recordBatOverthrow(InningsScore inn, int batRuns, int overthrowRuns, OverStats overStats,
            Player bowler) throws InvalidBallInputException {
        validateRuns(batRuns);
        if (overthrowRuns < 0) {
            throw new InvalidBallInputException("Overthrow runs cannot be negative.");
        }
        inn.addOverthrowBatRuns(batRuns, overthrowRuns);
        if (bowler != null) {
            bowler.addBowlingRuns(batRuns + overthrowRuns);
        }
        if (batRuns == 4) {
            overStats.incrementFours();
        } else if (batRuns == 6) {
            overStats.incrementSixes();
        }
    }

    private void recordByeOverthrow(InningsScore inn, int byeRuns, int overthrowRuns) throws InvalidBallInputException {
        if (byeRuns < 0 || overthrowRuns < 0) {
            throw new InvalidBallInputException("Bye and overthrow runs cannot be negative.");
        }
        inn.addByeWithOverthrow(byeRuns, overthrowRuns);
    }

    private void recordLegByeOverthrow(InningsScore inn, int legByeRuns, int overthrowRuns)
            throws InvalidBallInputException {
        if (legByeRuns < 0 || overthrowRuns < 0) {
            throw new InvalidBallInputException("Leg-bye and overthrow runs cannot be negative.");
        }
        inn.addLegByeWithOverthrow(legByeRuns, overthrowRuns);
    }

    private String generateAutoEvent(InningsScore inn, boolean freeHit, int target, int maxLegalBalls,
            int wicketLimit) {
        int aggression = calculateAggressionLevel(inn, target, maxLegalBalls, wicketLimit);
        int wicketsLeft = Math.max(0, wicketLimit - inn.getWickets());
        int roll = random.nextInt(1000);

        if (freeHit) {
            if (roll < 560) {
                return String.valueOf(randomRunsForAggression(aggression + 2, true));
            }
            if (roll < 650) {
                return randomOverthrowEvent(aggression + 1);
            }
            if (roll < 730) {
                return randomNoBallEvent(aggression);
            }
            if (roll < 790) {
                return randomWideEvent();
            }
            if (roll < 930) {
                return randomFieldingExtraEvent();
            }
            return wicketsLeft > 0 ? "W RUN OUT" : String.valueOf(randomRunsForAggression(aggression + 1, true));
        }

        int wicketThreshold = wicketsLeft > 0 ? 70 + Math.max(0, aggression - 1) * 12 : 0;
        int retirementThreshold = wicketsLeft > 1 ? wicketThreshold + 4 : wicketThreshold;
        int penaltyThreshold = retirementThreshold + 2;
        int wideThreshold = penaltyThreshold + 50;
        int noBallThreshold = wideThreshold + 38;
        int fieldingExtraThreshold = noBallThreshold + 58;
        int overthrowThreshold = fieldingExtraThreshold + 28;
        int battingThreshold = overthrowThreshold + 694;

        if (roll < wicketThreshold) {
            return randomWicketCommand();
        }
        if (roll < retirementThreshold) {
            return "Ret Hurt";
        }
        if (roll < penaltyThreshold) {
            return "P 5";
        }
        if (roll < wideThreshold) {
            return randomWideEvent();
        }
        if (roll < noBallThreshold) {
            return randomNoBallEvent(aggression);
        }
        if (roll < fieldingExtraThreshold) {
            return randomFieldingExtraEvent();
        }
        if (roll < overthrowThreshold) {
            return randomOverthrowEvent(aggression);
        }
        if (roll < battingThreshold) {
            return String.valueOf(randomRunsForAggression(aggression, false));
        }
        return String.valueOf(randomRunsForAggression(aggression + 1, false));
    }

    private String randomWicketCommand() {
        int roll = random.nextInt(100);
        DismissalType dismissalType;
        if (roll < 36) {
            dismissalType = DismissalType.CAUGHT;
        } else if (roll < 60) {
            dismissalType = DismissalType.BOWLED;
        } else if (roll < 76) {
            dismissalType = DismissalType.LBW;
        } else if (roll < 88) {
            dismissalType = DismissalType.RUN_OUT;
        } else if (roll < 96) {
            dismissalType = DismissalType.STUMPED;
        } else {
            dismissalType = DismissalType.HIT_WICKET;
        }
        return "W " + dismissalType.getDisplayName().toUpperCase();
    }

    private int calculateAggressionLevel(InningsScore inn, int target, int maxLegalBalls, int wicketLimit) {
        int legalBalls = inn.getLegalBalls();
        double inningsProgress = maxLegalBalls == 0 ? 0.0 : (double) legalBalls / maxLegalBalls;
        int aggression = inningsProgress < 0.25 ? 0 : inningsProgress < 0.75 ? 1 : 2;

        if (target > 0) {
            int ballsRemaining = Math.max(1, maxLegalBalls - legalBalls);
            int runsNeeded = Math.max(0, target - inn.getTotalRuns());
            double requiredRate = (runsNeeded * 6.0) / ballsRemaining;
            if (requiredRate >= 9.0) {
                aggression++;
            }
            if (requiredRate >= 12.0) {
                aggression++;
            }
        }

        int wicketsLeft = Math.max(0, wicketLimit - inn.getWickets());
        if (wicketsLeft <= 2) {
            aggression--;
        } else if (wicketsLeft >= 7 && inningsProgress > 0.55) {
            aggression++;
        }

        if (aggression < 0) {
            return 0;
        }
        if (aggression > 4) {
            return 4;
        }
        return aggression;
    }

    private int randomRunsForAggression(int aggression, boolean freeHit) {
        int level = Math.max(0, Math.min(4, aggression));
        int[][] runChoices = {
                { 0, 0, 0, 1, 1, 1, 2, 2, 3, 4 },
                { 0, 0, 1, 1, 1, 2, 2, 3, 4, 4, 6 },
                { 0, 1, 1, 2, 2, 3, 4, 4, 6, 6 },
                { 0, 1, 1, 2, 3, 4, 4, 6, 6, 6 },
                { 0, 1, 2, 3, 4, 4, 6, 6, 6, 6 }
        };
        int[] pool = runChoices[level];
        if (freeHit && random.nextInt(100) < 35) {
            int[] freeHitPool = { 1, 2, 4, 4, 6, 6, 6 };
            return freeHitPool[random.nextInt(freeHitPool.length)];
        }
        return pool[random.nextInt(pool.length)];
    }

    private String randomWideEvent() {
        int[] wideRuns = { 1, 1, 1, 1, 2, 2, 3 };
        int runs = wideRuns[random.nextInt(wideRuns.length)];
        return runs == 1 ? "Wd" : "Wd " + runs;
    }

    private String randomNoBallEvent(int aggression) {
        int variant = random.nextInt(100);
        if (variant < 52) {
            int batRuns = randomRunsForAggression(Math.max(1, aggression), true);
            return batRuns == 0 ? "Nb" : "Nb " + batRuns;
        }
        if (variant < 76) {
            int byeRuns = random.nextInt(3) + 1;
            return "Nb B " + byeRuns;
        }
        int legByeRuns = random.nextInt(3) + 1;
        return "Nb LB " + legByeRuns;
    }

    private String randomFieldingExtraEvent() {
        if (random.nextBoolean()) {
            int byeRuns = random.nextInt(4);
            return "B " + byeRuns;
        }
        int legByeRuns = random.nextInt(4);
        return "LB " + legByeRuns;
    }

    private String randomOverthrowEvent(int aggression) {
        int kind = random.nextInt(100);
        int overthrowRuns = random.nextInt(3) + 1;
        if (kind < 55) {
            int batRuns = randomRunsForAggression(Math.max(0, aggression - 1), false);
            return "OT " + batRuns + " " + overthrowRuns;
        }
        if (kind < 78) {
            int byeRuns = random.nextInt(3);
            return "OT B " + byeRuns + " " + overthrowRuns;
        }
        int legByeRuns = random.nextInt(3);
        return "OT LB " + legByeRuns + " " + overthrowRuns;
    }

    private Player selectBowler(Team bowlingTeam, int overNum, Player previousBowler) {
        if (autoMode) {
            return selectAutoBowler(bowlingTeam, previousBowler);
        }
        Player[] players = bowlingTeam.getPlayers();
        String[] options = new String[players.length];
        for (int i = 0; i < players.length; i++) {
            options[i] = players[i].getName();
        }

        try {
            int choice = readMenuChoice("Choose bowler for over " + overNum + ":", options);
            return players[choice - 1];
        } catch (InvalidBallInputException e) {
            System.out.println("No bowler selected. Over will continue without bowler tracking.");
            return null;
        }
    }

    private Player selectAutoBowler(Team bowlingTeam, Player previousBowler) {
        Player[] players = bowlingTeam.getPlayers();
        int candidateCount = Math.min(6, players.length);
        Player[] candidates = new Player[candidateCount];
        int usable = 0;

        for (int i = 0; i < candidateCount; i++) {
            Player candidate = players[i];
            if (previousBowler != null && candidate.getName().equalsIgnoreCase(previousBowler.getName())
                    && candidateCount > 1) {
                continue;
            }
            candidates[usable++] = candidate;
        }

        if (usable == 0) {
            return players[random.nextInt(candidateCount)];
        }
        return candidates[random.nextInt(usable)];
    }

    private boolean isKeyword(String command, String... options) {
        for (int i = 0; i < options.length; i++) {
            if (command.equals(options[i])) {
                return true;
            }
        }
        return false;
    }

    private String normalizeKeyword(String text) {
        return text == null ? "" : text.replaceAll("[^A-Za-z]", "").toUpperCase();
    }

    private int parseStrictInt(String token, String description) throws InvalidBallInputException {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new InvalidBallInputException("Invalid " + description + ": " + token);
        }
    }

    private DismissalType parseDismissalType(String[] parts) throws InvalidBallInputException {
        if (parts.length == 1) {
            return DismissalType.BOWLED;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(parts[i]);
        }
        DismissalType dismissalType = DismissalType.fromInput(builder.toString());
        if (dismissalType.isRetirement()) {
            throw new InvalidBallInputException("Use 'Ret Out' or 'Ret Hurt' for retired batters.");
        }
        return dismissalType;
    }

    private DismissalType parseRetirementType(String[] parts) throws InvalidBallInputException {
        if (parts.length < 2) {
            throw new InvalidBallInputException("Use 'Ret Out' or 'Ret Hurt'.");
        }
        StringBuilder builder = new StringBuilder("Retired ");
        for (int i = 1; i < parts.length; i++) {
            if (i > 1) {
                builder.append(' ');
            }
            builder.append(parts[i]);
        }
        DismissalType dismissalType = DismissalType.fromInput(builder.toString());
        if (!dismissalType.isRetirement()) {
            throw new InvalidBallInputException("Use 'Ret Out' or 'Ret Hurt'.");
        }
        return dismissalType;
    }

    private DismissalType dismissalTypeFromMenu(int choice) throws InvalidBallInputException {
        switch (choice) {
            case 1:
                return DismissalType.BOWLED;
            case 2:
                return DismissalType.CAUGHT;
            case 3:
                return DismissalType.RUN_OUT;
            case 4:
                return DismissalType.STUMPED;
            case 5:
                return DismissalType.LBW;
            case 6:
                return DismissalType.HIT_WICKET;
            default:
                throw new InvalidBallInputException("Unsupported wicket choice.");
        }
    }

    private int readMenuChoice(String title, String[] options) throws InvalidBallInputException {
        while (true) {
            System.out.println(title);
            for (int i = 0; i < options.length; i++) {
                System.out.println((i + 1) + "- " + options[i]);
            }
            System.out.print("Enter choice: ");

            if (!scanner.hasNextLine()) {
                throw new InvalidBallInputException("End of input.");
            }

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                System.out.println("Choice cannot be empty.");
                continue;
            }

            try {
                int choice = Integer.parseInt(line);
                if (choice >= 1 && choice <= options.length) {
                    return choice;
                }
            } catch (NumberFormatException e) {
                // handled below with a user-facing message
            }

            System.out.println("Invalid choice. Enter a number between 1 and " + options.length + ".");
        }
    }

    private int chooseBatRuns() throws InvalidBallInputException {
        int choice = readMenuChoice("Choose batter runs:", new String[] {
                "Dot ball (0 runs)",
                "1 run",
                "2 runs",
                "3 runs",
                "4 runs",
                "6 runs"
        });
        switch (choice) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            case 5:
                return 4;
            case 6:
                return 6;
            default:
                throw new InvalidBallInputException("Unsupported run choice.");
        }
    }

    private int chooseWideRuns() throws InvalidBallInputException {
        int choice = readMenuChoice("Choose total wide runs:", new String[] {
                "1 wide",
                "2 wides",
                "3 wides",
                "4 wides",
                "Custom runs"
        });
        switch (choice) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return readNumberWithRetry("Enter custom wide runs (minimum 1): ", 1);
            default:
                throw new InvalidBallInputException("Unsupported wide choice.");
        }
    }

    private int chooseExtraRunsAllowZero(String title) throws InvalidBallInputException {
        int choice = readMenuChoice(title, new String[] {
                "0 runs",
                "1 run",
                "2 runs",
                "3 runs",
                "4 runs",
                "Custom runs"
        });
        switch (choice) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            case 5:
                return 4;
            case 6:
                return readNumberWithRetry("Enter custom runs (minimum 0): ", 0);
            default:
                throw new InvalidBallInputException("Unsupported extra-runs choice.");
        }
    }

    private int chooseOverthrowRuns() throws InvalidBallInputException {
        int choice = readMenuChoice("Choose overthrow runs:", new String[] {
                "0 runs",
                "1 run",
                "2 runs",
                "3 runs",
                "4 runs",
                "Custom runs"
        });
        switch (choice) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            case 5:
                return 4;
            case 6:
                return readNumberWithRetry("Enter custom overthrow runs (minimum 0): ", 0);
            default:
                throw new InvalidBallInputException("Unsupported overthrow choice.");
        }
    }

    private int choosePenaltyRuns() throws InvalidBallInputException {
        int choice = readMenuChoice("Choose penalty runs:", new String[] {
                "5 runs (standard penalty)",
                "Custom runs"
        });
        if (choice == 1) {
            return 5;
        }
        return readNumberWithRetry("Enter custom penalty runs (minimum 1): ", 1);
    }

    private int readNumberWithRetry(String prompt, int minimumValue) throws InvalidBallInputException {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                throw new InvalidBallInputException("End of input.");
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                System.out.println("Value cannot be empty.");
                continue;
            }
            try {
                int value = Integer.parseInt(line);
                if (value >= minimumValue) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // handled below
            }
            System.out.println("Invalid value. Enter a number greater than or equal to " + minimumValue + ".");
        }
    }

    private void validateRuns(int runs) throws InvalidBallInputException {
        for (int i = 0; i < VALID_RUNS.length; i++) {
            if (VALID_RUNS[i] == runs) {
                return;
            }
        }
        throw new InvalidBallInputException("Runs must be one of 0,1,2,3,4,6.");
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
            System.out.println("- " + p.getName() + ": " + p.getRuns() + " (" + p.getBallsFaced()
                    + ") 4s:" + p.getFours() + " 6s:" + p.getSixes() + " [" + inn.getBattingStatus(p) + "]");
        }

        if (!inn.getWicketEvents().isEmpty()) {
            System.out.println("\nBatting Events:");
            for (WicketEvent w : inn.getWicketEvents()) {
                System.out.println(" - " + w.getOverBall() + ": " + w.getDescription());
            }
        }
    }

    @Override
    public double calculateRunRate(int runs, int balls) {
        if (balls == 0) {
            return 0.0;
        }
        return (runs * 6.0) / balls;
    }

    private void displayMatchResult(InningsScore inn1, InningsScore inn2) {
        System.out.println(matchResultText == null || matchResultText.trim().isEmpty()
                ? scorecardFormatter.buildMatchResult(inn1, inn2)
                : matchResultText);
        if (superOverSummaryText != null && !superOverSummaryText.trim().isEmpty()) {
            System.out.println(superOverSummaryText);
        }
    }

    private void displayOverallStats(InningsScore inn1, InningsScore inn2) {
        System.out.println(scorecardFormatter.buildOverallStats(inn1, inn2));
    }

    private void askTeamForPlayerScores(Team teamA, Team teamB) {
        System.out.println("\nShow player scores:");
        if (!scanner.hasNextLine()) {
            return;
        }

        try {
            int ch = readMenuChoice("Choose scorecard view:", new String[] {
                    teamA.getName(),
                    teamB.getName(),
                    "Both teams",
                    "Skip"
            });
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

        int scoreChoice;
        try {
            scoreChoice = readMenuChoice("Which scorecard do you want to save?", new String[] {
                    inn1.getBattingTeam().getName(),
                    inn2.getBattingTeam().getName(),
                    "Both innings"
            });
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

        String content = scorecardFormatter.buildFileContent(scoreChoice, inn1, inn2, matchResultText,
                superOverSummaryText);
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

    private static class SuperOverResult {
        private final String resultText;
        private final String summaryText;

        private SuperOverResult(String resultText, String summaryText) {
            this.resultText = resultText;
            this.summaryText = summaryText;
        }
    }
}
