package main;

import exception.InvalidBallInputException;
import match.Match;
import match.ODIMatch;
import match.T20Match;
import scoreboard.ScoreboardManager;
import team.Team;

import java.util.Scanner;

public class CricketScoreboardApplication {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

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

            ScoreboardManager manager = new ScoreboardManager(match, sc);
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
}
