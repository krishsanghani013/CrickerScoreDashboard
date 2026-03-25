package match;

import exception.InvalidBallInputException;
import team.Team;

public class ODIMatch extends Match {
    private static final int MAX_OVERS = 50;

    public ODIMatch(Team teamA, Team teamB, int oversPerInnings) throws InvalidBallInputException {
        super(teamA, teamB, oversPerInnings);
    }

    @Override
    public String getMatchType() {
        return "ODI";
    }

    @Override
    public int getMaximumOversAllowed() {
        return MAX_OVERS;
    }
}
