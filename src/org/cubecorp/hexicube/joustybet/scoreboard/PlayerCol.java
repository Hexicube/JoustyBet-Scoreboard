package org.cubecorp.hexicube.joustybet.scoreboard;

import com.badlogic.gdx.graphics.Color;

public enum PlayerCol
{
	BLUE(Color.BLUE,								Color.WHITE),
	YELLOW(Color.YELLOW,							Color.BLACK),
	GREEN(Color.GREEN,								Color.BLACK),
	PURPLE(Color.PURPLE,							Color.BLACK),
	ORANGE(Color.ORANGE.lerp(Color.BLACK, 0.25f),	Color.BLACK),
	PINK(new Color(Color.PURPLE).lerp(Color.WHITE, 0.5f),		Color.BLACK),
	CYAN(Color.CYAN,								Color.BLACK),
	MAGENTA(Color.MAGENTA,							Color.BLACK);
	
	public final Color col, textCol, lightCol, darkCol;
	
	private PlayerCol(Color c, Color tc)
	{
		col = c;
		textCol = tc;
		
		lightCol = new Color(c).lerp(Color.WHITE, 0.5f);
		darkCol = new Color(c).lerp(Color.BLACK, 0.5f);
	}
}