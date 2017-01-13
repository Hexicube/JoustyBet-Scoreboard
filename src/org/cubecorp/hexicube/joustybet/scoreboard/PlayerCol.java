package org.cubecorp.hexicube.joustybet.scoreboard;

import com.badlogic.gdx.graphics.Color;

public enum PlayerCol
{
	BLUE(Color.BLUE),
	YELLOW(Color.YELLOW),
	GREEN(Color.GREEN),
	PURPLE(Color.PURPLE),
	ORANGE(Color.ORANGE),
	PINK(Color.PINK),
	CYAN(Color.CYAN),
	MAGENTA(Color.MAGENTA);
	
	public Color col;
	
	private PlayerCol(Color c)
	{
		col = c;
	}
}