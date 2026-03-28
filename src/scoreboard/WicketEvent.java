package scoreboard;

public class WicketEvent {
    private final String batter;
    private final String bowler;
    private final DismissalType dismissalType;
    private final int over;
    private final int ball;
    private final boolean ballConsumed;

    public WicketEvent(String batter, String bowler, DismissalType dismissalType, int over, int ball,
            boolean ballConsumed) {
        this.batter = batter;
        this.bowler = bowler;
        this.dismissalType = dismissalType;
        this.over = over;
        this.ball = ball;
        this.ballConsumed = ballConsumed;
    }

    public String getBatter() {
        return batter;
    }

    public String getBowler() {
        return bowler;
    }

    public DismissalType getDismissalType() {
        return dismissalType;
    }

    public String getOverBall() {
        String marker = over + "." + ball;
        return ballConsumed ? marker : marker + " (before ball)";
    }

    public String getDescription() {
        StringBuilder description = new StringBuilder();
        description.append(batter).append(" - ").append(dismissalType.getDisplayName());
        if (dismissalType.isCreditedToBowler() && bowler != null && !bowler.isEmpty()) {
            description.append(" (bowler: ").append(bowler).append(")");
        }
        return description.toString();
    }
}
