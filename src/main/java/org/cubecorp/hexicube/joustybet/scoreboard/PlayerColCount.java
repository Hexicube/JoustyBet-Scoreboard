package org.cubecorp.hexicube.joustybet.scoreboard;

public class PlayerColCount implements Comparable<PlayerColCount> {
    public final PlayerCol col;
    public int count;
    
    public PlayerColCount(PlayerCol col) {
        this.col = col;
    }
    
    @Override
    public int compareTo(PlayerColCount o) {
        return Integer.compare(o.count, count);
    }
}