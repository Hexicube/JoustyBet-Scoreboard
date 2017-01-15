package org.cubecorp.hexicube.joustybet.scoreboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Game implements ApplicationListener
{
	public static IconHandler[] accuracyIcons;
	public static IconHandler[] chainIcons;
	public static IconHandler[] placeIcons;
	public static IconHandler upArrow, upArrowLit, downArrow, downArrowLit;
	
	public static final String gameName = "JoustyBet Scoreboard";
	
	private static Texture background, chartBackground, icons, pixel;
	private static SpriteBatch spriteBatch;
	
	private static List<Better> betters;
	private static boolean roundActive;
	private static PlayerCol lastWinner;
	
	Random r = new Random();

	final String url;

	public Game(String url) {
	    this.url = url;
    }
	
	private static char[][] displayText;
	
	@Override
	public void create()
	{
		background = loadImage("background");
		chartBackground = loadImage("chartbackground");
		icons = loadImage("icons");
		
		accuracyIcons = new IconHandler[]{
			new IconHandler(36, 36, 34, 34),
			new IconHandler(36, 1, 34, 34),
			new IconHandler(1, 71, 34, 34),
			new IconHandler(1, 36, 34, 34),
			new IconHandler(1, 1, 34, 34)
		};
		
		chainIcons = new IconHandler[]{
			new IconHandler(106, 36, 34, 34),
			new IconHandler(106, 1, 34, 34),
			new IconHandler(71, 71, 34, 34),
			new IconHandler(71, 36, 34, 34),
			new IconHandler(71, 1, 34, 34)
		};
		
		placeIcons = new IconHandler[]{
			new IconHandler(141, 1,  28, 16),
			new IconHandler(141, 18, 28, 16),
			new IconHandler(141, 35, 28, 16),
			new IconHandler(141, 52, 28, 16),
			new IconHandler(141, 69, 28, 16),
			
			new IconHandler(170, 1,  28, 16),
			new IconHandler(170, 18, 28, 16),
			new IconHandler(170, 35, 28, 16),
			new IconHandler(170, 52, 28, 16),
			new IconHandler(170, 69, 28, 16),
			
			new IconHandler(199, 1,  28, 16),
			new IconHandler(199, 18, 28, 16),
			new IconHandler(199, 35, 28, 16),
			new IconHandler(199, 52, 28, 16),
			new IconHandler(199, 69, 28, 16)
		};

		upArrow      = new IconHandler(45, 71, 8, 13);
		upArrowLit   = new IconHandler(36, 71, 8, 13);
		downArrow    = new IconHandler(45, 85, 8, 13);
		downArrowLit = new IconHandler(36, 85, 8, 13);
		
		Pixmap px = new Pixmap(1, 1, Format.RGB888);
		px.drawPixel(0, 0, Color.WHITE.toIntBits());
		pixel = new Texture(px);
		px.dispose();
		
		spriteBatch = new SpriteBatch();
		
		FontHolder.prep();
		displayText = new char[][]{
			FontHolder.getCharList("Join in at"),
            FontHolder.getCharList("http://joustybet.com"),
            FontHolder.getCharList(""),
            FontHolder.getCharList("Hack by @LtHummus"),
            FontHolder.getCharList("UI by @Hexicube"),
            FontHolder.getCharList(""),
            FontHolder.getCharList("JoustyBet is an unofficial"),
            FontHolder.getCharList("mod for Johann Sebastian"),
            FontHolder.getCharList("Joust. It is not approved"),
            FontHolder.getCharList("by Die Gute Fabrik."),
		};
		
		Gdx.graphics.setTitle(gameName);
		
		betters = new ArrayList<>();

		new Thread(){
			@Override
			public void run()
			{
				Socket sock = null;
                try
                {
                    System.out.println("Attempting connection");
                    System.out.println(String.format("Using URL: '%s'", url));
                    sock = IO.socket(url);
                    sock.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                        @Override
                        public void call(Object... objects) {
                            System.out.println("connected to websocket");
                        }
                    }).on("data_update", new Emitter.Listener() {
                        @Override
                        public void call(Object... objects) {
                            System.out.println(objects[0]);
                            StateAdapter sa = new StateAdapter((String)objects[0]);
                            betters = sa.getBetters();
                            roundActive = sa.isRoundActive();
                            lastWinner = sa.getLastWinner();
                            
                            renderCounter = 2;
                            fullUpdate = true;
                        }
                    }).on("data_vote", new Emitter.Listener()
					{
						@Override
						public void call(Object... objects)
						{
							System.out.println(objects[0]);
					        Scanner scan = new Scanner((String)objects[0]);
					        while (scan.hasNextLine()) {
					            String curr = scan.nextLine();
					            String[] parts = curr.split("\\s+", 2);
					            find(parts[0]).guess = PlayerCol.getFromString(parts[1]);
					        }
					        scan.close();
					        
					        renderCounter = 2;
					        //Don't set fullUpdate, so that it does a full update if data_update was recently called.
						}
					});
                    sock.connect();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    System.out.println("Unable to connect");
                }
			}
		}.start();
	}
	
	@Override
	public void dispose()
	{}
	
	@Override
	public void pause()
	{}
	
	private static int renderCounter = 2;
	private static boolean fullUpdate = true;
	
	@Override
	public void render()
	{
		if(renderCounter == 0) return;
		renderCounter--;
		
		synchronized(betters)
		{
			spriteBatch.begin();
			
			spriteBatch.setColor(Color.WHITE);
			if(fullUpdate) spriteBatch.draw(background, 0, 0);
			else spriteBatch.draw(chartBackground, 0, 0);
			
			ArrayList<PlayerColCount> counts = new ArrayList<PlayerColCount>();
			int sum = 0;
			
			PlayerCol[] cols = PlayerCol.values();
			for(PlayerCol c : cols) counts.add(new PlayerColCount(c));
			for(Better b : betters)
			{
				if(b.guess != null)
				{
					for(int a = 0; a < cols.length; a++)
					{
						if(b.guess == cols[a])
						{
							for(PlayerColCount c : counts)
							{
								if(c.col == cols[a])
								{
									c.count++;
									sum++;
									break;
								}
							}
						}
					}
				}
			}
			
			if(roundActive) counts.sort(null);
			int largest = 0;
			for(PlayerColCount c : counts) largest = Math.max(largest, c.count);
			for(int a = 0; a < counts.size(); a++)
			{
				PlayerColCount c = counts.get(a);
				int amount = 254;
				if(sum > 0) amount = c.count * amount / largest;
				
				if(amount > 0)
				{
					if(amount < 3) amount = 3;
					
					spriteBatch.setColor(c.col.col);
					drawPixel(spriteBatch, 273, 412 - a * 22, amount, 20);
					
					spriteBatch.setColor(c.col.lightCol);
					drawPixel(spriteBatch, 273, 413 - a * 22, 1, 19);
					drawPixel(spriteBatch, 273, 431 - a * 22, amount, 1);
					
					spriteBatch.setColor(c.col.darkCol);
					drawPixel(spriteBatch, 273, 412 - a * 22, amount-1, 1);
					drawPixel(spriteBatch, 272 + amount, 412 - a * 22, 1, 19);
				}
				
				spriteBatch.setColor(c.col.textCol);
				FontHolder.render(spriteBatch, FontHolder.getCharList(""+c.count), 276, 429 - a*22, true);
			}
			
			spriteBatch.setColor(Color.BLACK);
			FontHolder.render(spriteBatch, FontHolder.getCharList((lastWinner == null)?(roundActive?"Game on!":"No winner yet..."):("Previous winner: "+lastWinner)), 276, 253, true);
			
			if(!fullUpdate)
			{
				spriteBatch.end();
				return;
			}
			
			betters.sort(BetterComparator.get());
			betters.sort(null);

            spriteBatch.setColor(Color.BLACK);

            int y = 228;
            for(char[] cList : displayText)
            {
            	FontHolder.render(spriteBatch, cList, 275, y, true);
            	y -= 20;
            }

            spriteBatch.setColor(Color.WHITE);

			int numToShow = Math.min(15, betters.size());
			if(numToShow > 0)
			{
				float topAcc, bottomAcc;
				topAcc = betters.get(0).uncertaintyAcc;
				bottomAcc = betters.get(numToShow - 1).uncertaintyAcc;
				float halfAcc = (topAcc + bottomAcc) / 2;
				float topHalfAcc = (topAcc*3 + bottomAcc) / 4;
				float bottomHalfAcc = (bottomAcc*3 + topAcc) / 4;
				for(int a = 0; a < 15; a++)
				{
					if(a < numToShow)
					{
						Better b = betters.get(a);
						float acc = b.uncertaintyAcc;
						
						int tier;
						if(acc >= topAcc) tier = 4;
						else if(acc >= topHalfAcc) tier = 3;
						else if(acc >= halfAcc) tier = 2;
						else if(acc >= bottomHalfAcc) tier = 1;
						else tier = 0;
						
						int place = a;
						for(int z = a; z >= 0; z--)
						{
							if(betters.get(z).uncertaintyAcc == acc) place = z;
							else break;
						}
						
						accuracyIcons[tier].render(spriteBatch, icons, 229, 549 - a*38);
						placeIcons[place].render(spriteBatch, icons, 23, 558 - a*38);
						((b.guessed && b.correct)?upArrowLit:upArrow).render(spriteBatch, icons, 18, 569 - a*38);
						((b.guessed && !b.correct)?downArrowLit:downArrow).render(spriteBatch, icons, 18, 550 - a*38);
						printData(spriteBatch, b, 23, 549 - a*38);
					}
					else
					{
						upArrow.render(spriteBatch, icons, 18, 569 - a*38);
						downArrow.render(spriteBatch, icons, 18, 550 - a*38);
						chainIcons[0].render(spriteBatch, icons, 749, 549 - a*38);
					}
				}
				
				betters.sort(BetterComparator.get());
				
				int topChain, bottomChain;
				topChain = betters.get(0).streak;
				bottomChain = betters.get(numToShow - 1).streak;
				for(int a = numToShow - 2; a > 0; a--)
				{
					if(bottomChain < 2) bottomChain = betters.get(a).streak;
					else break;
				}
				int halfChain = (topChain + bottomChain + 1) / 2;
				int topHalfChain = (topChain * 3 + bottomChain + 2) / 4;
				int bottomHalfChain = (topChain + bottomChain * 3 + 2) / 4;
				for(int a = 0; a < 15; a++)
				{
					if(a < numToShow)
					{
						Better b = betters.get(a);
						int chain = b.streak;
						if(chain < 2) //Limit chain list to actual chains
						{
							upArrow.render(spriteBatch, icons, 538, 569 - a*38);
							downArrow.render(spriteBatch, icons, 538, 550 - a*38);
							chainIcons[0].render(spriteBatch, icons, 749, 549 - a*38);
							continue;
						}
						
						int tier;
						if(chain >= topChain) tier = 4;
						else if(chain >= topHalfChain) tier = 3;
						else if(chain >= halfChain) tier = 2;
						else if(chain >= bottomHalfChain) tier = 1;
						else tier = 0;
						
						int place = a;
						for(int z = a; z >= 0; z--)
						{
							if(betters.get(z).streak == chain) place = z;
							else break;
						}
						
						chainIcons[tier].render(spriteBatch, icons, 749, 549 - a*38);
						placeIcons[place].render(spriteBatch, icons, 543, 558 - a*38);
						((b.guessed && b.correct)?upArrowLit:upArrow).render(spriteBatch, icons, 538, 569 - a*38);
						((b.guessed && !b.correct)?downArrowLit:downArrow).render(spriteBatch, icons, 538, 550 - a*38);
						printData(spriteBatch, b, 543, 549 - a*38);
					}
					else
					{
						upArrow.render(spriteBatch, icons, 538, 569 - a*38);
						downArrow.render(spriteBatch, icons, 538, 550 - a*38);
						chainIcons[0].render(spriteBatch, icons, 749, 549 - a*38);
					}
				}
			}
			
			spriteBatch.end();
		}
		
		if(renderCounter == 0) fullUpdate = false;
	}
	
	private static void printData(SpriteBatch batch, Better b, int x, int y)
	{
		Color c = batch.getColor();
		batch.setColor(Color.WHITE);
		
		FontHolder.render(batch, b.nameChars, x+35, y+33, true);
		FontHolder.render(batch, FontHolder.getCharList("Acc: "+floatToStr(b.acc, 2)+"%"), x+47, y+17, false);
		FontHolder.render(batch, FontHolder.getCharList("Rating: "+floatToStr(b.uncertaintyAcc, 2)+"%"), x+35, y+8, false);
		FontHolder.render(batch, FontHolder.getCharList("Streak: "+b.streak), x+122, y+17, false);
		FontHolder.render(batch, FontHolder.getCharList("Score: "+b.score+"/"+b.total), x+125, y+8, false);
		
		batch.setColor(c);
	}
	
	@Override
	public void resize(int width, int height)
	{
		//Shouldn't get called, but if it does this will prevent stretching.
		spriteBatch = new SpriteBatch();
	}
	
	@Override
	public void resume()
	{}
	
	public static String floatToStr(float val, int dp)
	{
		if(dp <= 0) return ""+(int)val;
		
		for(int a = 0; a < dp; a++) val *= 10;
		int main = (int)val;
		int dec = 0;
		for(int a = 0; a < dp; a++)
		{
			dec = dec * 10 + main % 10;
			main /= 10;
		}
		
		String response = ""+dec;
		while(response.length() < dp) response += "0";
		return main + "." + response;
	}
	
	public static Texture loadImage(String name)
	{
		name = "images/" + name;
		if(!File.separator.equals("/")) name.replace("/", File.separator);
		return new Texture(Gdx.files.internal(name + ".png"));
	}
	
	public static Better find(String id)
	{
		for(Better b : betters)
		{
			if(b.id.equals(id)) return b;
		}
		Better b = new Better();
		b.id = id;
		betters.add(b);
		return b;
	}
	
	public static void setName(Better b, String name)
	{
		b.name = name;
	}
	
	public static void setData(Better b, int score, int streak, int total)
	{
		b.score = score;
		b.streak = streak;
		b.total = total;
	}
	
	public static void drawPixel(SpriteBatch batch, int x, int y, int w, int h)
	{
		batch.draw(pixel, x, y, w, h, 0, 0, 1, 1, false, false);
	}
}