package org.tilegames.hexicube.joustybet.scoreboard;

import java.util.Comparator;

public class Better implements Comparable<Better>, Comparator<Better>
{
	public String name, id;
	public int score, streak, total;
	private int oldScore, oldTotal;
	
	public PlayerCol guess;
	public boolean guessed, correct;
	
	public float acc, uncertaintyAcc;
	
	public void calcAcc()
	{
		acc = (float)score / (float)total * 100;
		uncertaintyAcc = (float)score / (float)(total + 3) * 100;
		
		oldScore = score;
		oldTotal = total;
	}
	
	@Override
	public int compareTo(Better o)
	{
		if(oldScore != score || oldTotal != total) calcAcc();
		if(o.oldScore != o.score || o.oldTotal != o.total) o.calcAcc();
		
		return Float.compare(o.uncertaintyAcc, uncertaintyAcc);
	}
	
	@Override
	public int compare(Better o1, Better o2)
	{
		return Integer.compare(o2.streak, o1.streak);
	}
}