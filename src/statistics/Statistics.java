package statistics;

import scoreboard.InningsScore;

public interface Statistics {
    void displayOverStats(OverStats overStats, int overNumber);

    void displayFinalStats(InningsScore inningsScore);

    double calculateRunRate(int runs, int balls);
}
