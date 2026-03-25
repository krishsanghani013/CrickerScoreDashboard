package match;

import exception.InvalidBallInputException;
import team.Team;

public class T20Match extends Match {
    private static final int MAX_OVERS = 20;

    public T20Match(Team teamA, Team teamB, int oversPerInnings) throws InvalidBallInputException {
        super(teamA, teamB, oversPerInnings);
    }

    @Override
    public String getMatchType() {
        return "T20";
    }

    @Override
    public int getMaximumOversAllowed() {
        return MAX_OVERS;
    }
}
