package org.cubecorp.hexicube.joustybet.scoreboard;

import java.util.Comparator;

public class BetterComparator implements Comparator<Better> {

    private BetterComparator(){}

    private static class ComparatorHolder {
        private static final BetterComparator INSTANCE = new BetterComparator();
    }

    public static BetterComparator get() {
        return ComparatorHolder.INSTANCE;
    }

    @Override
    public int compare(Better o1, Better o2)
    {
        return Integer.compare(o2.streak, o1.streak);
    }

}
