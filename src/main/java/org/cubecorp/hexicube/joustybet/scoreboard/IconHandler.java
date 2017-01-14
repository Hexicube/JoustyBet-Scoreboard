package org.cubecorp.hexicube.joustybet.scoreboard;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class IconHandler
{
	public final int x, y, w, h;
	public IconHandler(int x, int y, int w, int h)
	{
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	public void render(SpriteBatch batch, Texture tex, int xPos, int yPos)
	{
		batch.draw(tex, xPos, yPos, w, h, x, y, w, h, false, false);
	}
}