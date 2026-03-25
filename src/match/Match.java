package match;

import exception.InvalidBallInputException;
import team.Team;

//  match class
public abstract class Match {
    public static final int PLAYERS_PER_TEAM = 11;
    public static final int BALLS_PER_OVER = 6;
    public static final int MAX_WICKETS = 10;

    private final Team teamA;
    private final Team teamB;
    private final int oversPerInnings;

    protected Match(Team teamA, Team teamB, int oversPerInnings) throws InvalidBallInputException {
        this.teamA = teamA;
        this.teamB = teamB;
        validateOvers(oversPerInnings);
        this.oversPerInnings = oversPerInnings;
    }

    public abstract String getMatchType();

    public abstract int getMaximumOversAllowed();

    public void validateOvers(int requestedOvers) throws InvalidBallInputException {
        if (requestedOvers <= 0 || requestedOvers > getMaximumOversAllowed()) {
            throw new InvalidBallInputException(
                    "Invalid over count: " + requestedOvers + ". Allowed range is 1 to " + getMaximumOversAllowed());
        }
    }

    public Team getTeamA() {
        return teamA;
    }

    public Team getTeamB() {
        return teamB;
    }

    public int getOversPerInnings() {
        return oversPerInnings;
    }
}
