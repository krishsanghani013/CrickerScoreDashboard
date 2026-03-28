package scoreboard;

import exception.InvalidBallInputException;

public enum DismissalType {
    BOWLED("bowled", true, true, true, false),
    CAUGHT("caught", true, true, true, false),
    RUN_OUT("run out", true, false, true, true),
    STUMPED("stumped", true, true, true, false),
    LBW("lbw", true, true, true, false),
    HIT_WICKET("hit wicket", true, true, true, false),
    RETIRED_OUT("retired out", true, false, false, false),
    RETIRED_HURT("retired hurt", false, false, false, false);

    private final String displayName;
    private final boolean countsAsWicket;
    private final boolean creditedToBowler;
    private final boolean consumesBall;
    private final boolean allowedOnFreeHit;

    DismissalType(String displayName, boolean countsAsWicket, boolean creditedToBowler, boolean consumesBall,
            boolean allowedOnFreeHit) {
        this.displayName = displayName;
        this.countsAsWicket = countsAsWicket;
        this.creditedToBowler = creditedToBowler;
        this.consumesBall = consumesBall;
        this.allowedOnFreeHit = allowedOnFreeHit;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean countsAsWicket() {
        return countsAsWicket;
    }

    public boolean isCreditedToBowler() {
        return creditedToBowler;
    }

    public boolean consumesBall() {
        return consumesBall;
    }

    public boolean isAllowedOnFreeHit() {
        return allowedOnFreeHit;
    }

    public boolean isRetirement() {
        return this == RETIRED_OUT || this == RETIRED_HURT;
    }

    public static DismissalType fromInput(String text) throws InvalidBallInputException {
        String normalized = text == null ? "" : text.replaceAll("[^A-Za-z]", "").toUpperCase();
        if (normalized.isEmpty()) {
            return BOWLED;
        }

        switch (normalized) {
            case "BOWLED":
            case "B":
                return BOWLED;
            case "CAUGHT":
            case "C":
                return CAUGHT;
            case "RUNOUT":
            case "RO":
                return RUN_OUT;
            case "STUMPED":
            case "ST":
                return STUMPED;
            case "LBW":
                return LBW;
            case "HITWICKET":
            case "HW":
                return HIT_WICKET;
            case "RETIREDOUT":
            case "RETIRED":
            case "RETOUT":
                return RETIRED_OUT;
            case "RETIREDHURT":
            case "RETHURT":
                return RETIRED_HURT;
            default:
                throw new InvalidBallInputException("Unknown dismissal type: " + text);
        }
    }
}
