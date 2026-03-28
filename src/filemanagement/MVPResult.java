package filemanagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import team.Player;

public class MVPResult {
    private final Player player;
    private final double battingImpact;
    private final double bowlingImpact;
    private final double allRoundBonus;
    private final double winningBonus;
    private final double finalScore;
    private final List<String> reasons;

    public MVPResult(Player player, double battingImpact, double bowlingImpact, double allRoundBonus, double winningBonus,
            double finalScore, List<String> reasons) {
        this.player = player;
        this.battingImpact = battingImpact;
        this.bowlingImpact = bowlingImpact;
        this.allRoundBonus = allRoundBonus;
        this.winningBonus = winningBonus;
        this.finalScore = finalScore;
        this.reasons = Collections.unmodifiableList(new ArrayList<>(reasons));
    }

    public Player getPlayer() {
        return player;
    }

    public double getBattingImpact() {
        return battingImpact;
    }

    public double getBowlingImpact() {
        return bowlingImpact;
    }

    public double getAllRoundBonus() {
        return allRoundBonus;
    }

    public double getWinningBonus() {
        return winningBonus;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public List<String> getReasons() {
        return reasons;
    }
}
