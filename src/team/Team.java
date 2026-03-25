package team;

import match.Match;

public class Team {
    private String name;
    private Player[] players;

    public Team(String name, String[] playerNames) {
        this.name = name;
        this.players = new Player[Match.PLAYERS_PER_TEAM]; // players list
        for (int i = 0; i < Match.PLAYERS_PER_TEAM; i++) {
            String temp = "";
            if (i < playerNames.length) {
                temp = playerNames[i];
            }
            if (temp == null || temp.trim().isEmpty()) {
                temp = name + "_Player" + (i + 1);
            }
            this.players[i] = new Player(temp.trim());
        }
    }

    public String getName() {
        return name;
    }

    public Player[] getPlayers() {
        return players;
    }
}
