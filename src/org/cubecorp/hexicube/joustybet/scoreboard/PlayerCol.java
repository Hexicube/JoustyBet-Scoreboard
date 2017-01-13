package org.cubecorp.hexicube.joustybet.scoreboard;

import com.badlogic.gdx.graphics.Color;

public enum PlayerCol
{
	BLUE("0000FF", Color.WHITE),
	YELLOW(Color.YELLOW,							Color.BLACK),
	GREEN(Color.GREEN,								Color.BLACK),
	PURPLE(Color.PURPLE,							Color.BLACK),
	ORANGE(Color.ORANGE.lerp(Color.BLACK, 0.25f),	Color.BLACK),
	PINK(new Color(Color.PURPLE).lerp(Color.WHITE, 0.5f),		Color.BLACK),
	CYAN(Color.CYAN,								Color.BLACK),
	MAGENTA(Color.MAGENTA,							Color.BLACK);
	
	private static int hexVal(char c)
	{
		final char[] hexValues = "0123456789ABCDEF".toCharArray();
		for(int a = 0; a < 16; a++)
		{
			if(c == hexValues[a]) return a;
		}
		return -1;
	}
	
	private static Color hexToCol(String hex)
	{
		char[] data = hex.toCharArray();
		int r = hexVal(data[0]) * 16 + hexVal(data[1]);
		int g = hexVal(data[2]) * 16 + hexVal(data[3]);
		int b = hexVal(data[4]) * 16 + hexVal(data[5]);
		return new Color((r << 24) + (g << 16) + (b << 8) + 255);
	}
	
	public final Color col, textCol, lightCol, darkCol;
	
	private PlayerCol(Color main, Color text)
	{
		col = main;
		textCol = text;
		
		lightCol = new Color(col).lerp(Color.WHITE, 0.5f);
		darkCol = new Color(col).lerp(Color.BLACK, 0.5f);
	}
	
	private PlayerCol(String main, Color text)
	{
		this(hexToCol(main), text);
	}
}