package main;

import exception.InvalidBallInputException;
import match.Match;
import match.ODIMatch;
import match.T20Match;
import scoreboard.ScoreboardManager;
import team.Team;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

public class CricketScoreboardApplication {
    public static void main(String[] args) {
        Scanner sc;
        if (args.length > 0) {
            try {
                sc = new Scanner(new File(args[0]));
            } catch (FileNotFoundException e) {
                System.out.println("Error: Script file not found: " + args[0]);
                return;
            }
        } else {
            sc = new Scanner(System.in);
        }

        try {
            System.out.println("===== Cricket Scoreboard Application =====");

            // input
            System.out.print("Enter Team A name: ");
            String teamAName = sc.nextLine().trim();
            System.out.print("Enter Team B name: ");
            String teamBName = sc.nextLine().trim();

            String[] teamAPlayers = readPlayers(sc, teamAName);
            String[] teamBPlayers = readPlayers(sc, teamBName);

            Team teamA = new Team(teamAName, teamAPlayers);
            Team teamB = new Team(teamBName, teamBPlayers);

            System.out.println("Select match type: 1-ODI, 2-T20");
            System.out.print("Enter choice: ");
            int matchType = readInt(sc);

            System.out.print("Enter overs to be played: ");
            int overs = readInt(sc);

            Match match;
            // creating match
            if (matchType == 1) {
                match = new ODIMatch(teamA, teamB, overs);
            } else if (matchType == 2) {
                match = new T20Match(teamA, teamB, overs);
            } else {
                throw new InvalidBallInputException("Invalid match type selected.");
            }

            System.out.println("Created match type: " + match.getMatchType());

            System.out.println("Choose batting first team: 1-" + teamA.getName() + "  2-" + teamB.getName());
            System.out.print("Enter choice: ");
            int battingChoice = readInt(sc);

            Team firstBattingTeam;
            if (battingChoice == 1) {
                firstBattingTeam = teamA;
            } else if (battingChoice == 2) {
                firstBattingTeam = teamB;
            } else {
                throw new InvalidBallInputException("Invalid batting team selection.");
            }

            boolean autoMode = readYesNo(sc, "Enable auto-simulation mode (random events without manual input)? (y/N): ");
            Random random = new Random();

            ScoreboardManager manager = new ScoreboardManager(match, sc, autoMode, random);
            manager.startMatch(firstBattingTeam);

        } catch (InvalidBallInputException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            sc.close();
        }
    }

    // player list
    private static String[] readPlayers(Scanner sc, String teamName) {
        String[] players = new String[11];
        System.out.println("Enter 11 player names for " + teamName + ":");
        for (int i = 0; i < 11; i++) {
            System.out.print("Player " + (i + 1) + ": ");
            players[i] = sc.nextLine().trim();
        }
        return players;
    }

    private static int readInt(Scanner sc) throws InvalidBallInputException {
        if (!sc.hasNextLine()) {
            throw new InvalidBallInputException("End of input.");
        }
        String line = sc.nextLine().trim();
        if (line.isEmpty()) {
            throw new InvalidBallInputException("Empty input.");
        }
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            throw new InvalidBallInputException("Expected a number.");
        }
    }

    private static boolean readYesNo(Scanner sc, String prompt) {
        System.out.print(prompt);
        if (!sc.hasNextLine()) {
            return false;
        }
        String line = sc.nextLine().trim();
        if (line.isEmpty()) {
            return false;
        }
        char c = Character.toLowerCase(line.charAt(0));
        return c == 'y' || c == '1' || c == 't';
    }
}
