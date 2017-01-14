package org.cubecorp.hexicube.joustybet.scoreboard;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StateAdapter {
    private PlayerCol lastWinner;
    private boolean roundActive;
    private List<Better> betters;
    private String data;


    private void parseData() {
        Scanner scan = new Scanner(data);
        while (scan.hasNextLine()) {
            String curr = scan.nextLine();
            String[] parts = curr.split("\\s+", 2);

            switch (parts[0]) {
                case "bets_open":
                    roundActive = !Boolean.parseBoolean(parts[1]);
                    break;
                case "last_winner":
                    lastWinner = PlayerCol.getFromString(parts[1]);
                    break;
                case "player":
                    addBetter(parts[1]);
            }
        }
    }

    public StateAdapter(String data) {
        this.data = data;
        betters = new ArrayList<>();
        roundActive = false;
        lastWinner = null;

        parseData();
    }

    private void addBetter(String input) {
        //(self.session_id, self.score, self.streak, self.current_bet, self.number_bets, self.name)
        Better b = new Better();
        String[] parts = input.split("\\s+", 6);

        b.id = parts[0];
        b.score = Integer.parseInt(parts[1]);
        b.streak = Integer.parseInt(parts[2]);
        b.guess = PlayerCol.getFromString(parts[3].toUpperCase());
        b.total = Integer.parseInt(parts[4]);
        b.name = parts[5];

        betters.add(b);
    }

    public PlayerCol getLastWinner() {
        return lastWinner;
    }

    public List<Better> getBetters() {
        return new ArrayList<>(betters);
    }

    public void setLastWinner(PlayerCol lastWinner) {
        this.lastWinner = lastWinner;
    }

    public boolean isRoundActive() {
        return roundActive;
    }

    public void setRoundActive(boolean roundActive) {
        this.roundActive = roundActive;
    }
}
