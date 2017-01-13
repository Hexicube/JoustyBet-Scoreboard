package org.cubecorp.hexicube.joustybet.scoreboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Game implements ApplicationListener
{
	public static IconHandler[] accuracyIcons;
	public static IconHandler[] chainIcons;
	public static IconHandler[] placeIcons;
	public static IconHandler upArrow, upArrowLit, downArrow, downArrowLit;
	
	public static final String gameName = "JoustyBet Scoreboard";
	
	private static Texture background, icons;
	private static SpriteBatch spriteBatch;
	
	private static ArrayList<Better> betters;
	private static boolean needsRendering, roundActive;
	private static PlayerCol lastWinner;
	
	Random r = new Random();
	
	@Override
	public void create()
	{
		background = loadImage("background");
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
		
		spriteBatch = new SpriteBatch();
		
		FontHolder.prep();
		
		Gdx.graphics.setTitle(gameName);
		
		betters = new ArrayList<Better>();
		
		needsRendering = true;
		
		for(int a = 0; a < 100; a++)
		{
			Better b = find(""+a);
			int total = r.nextInt(21)+10;
			int score = r.nextInt(total+1);
			setData(setName(b, "User " + (a+1)), score, (score==total)?score:(r.nextInt(score/3+1)+r.nextInt(score/3+1)+r.nextInt(score/3+1)), total);
		}
		
		new Thread(){
			@SuppressWarnings("resource")
			@Override
			public void run()
			{
				WebSocket sock = null;
				try
				{
					sock = new WebSocket("echo.websocket.org", 80, "http://echo.websocket.org/?encoding=text", "permessage-deflate");
				}
				catch(IOException e)
				{
					e.printStackTrace();
					System.exit(0);
				}
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				while(true)
				{
					try
					{
						while(!in.ready())
						{
							try{Thread.sleep(1);}catch(InterruptedException e){}
						}
						String inLine = in.readLine();
						inLine = inLine.substring(0, inLine.length() - 1); //remove new-line at end
						if(inLine.equals("update")) needsRendering = true;
						else if(inLine.equals("roundstart"))
						{
							roundActive = true;
							for(Better b : betters)
							{
								b.guessed = false;
							}
							lastWinner = null;
						}
						else
						{
							String[] data = inLine.split(" ");
							if(data[0].equals("winner"))
							{
								roundActive = false;
								lastWinner = PlayerCol.valueOf(data[1]);
								for(Better b : betters)
								{
									if(b.guess != null)
									{
										b.guessed = true;
										if(b.guess == lastWinner)
										{
											b.correct = true;
											b.score++;
										}
										else b.correct = false;
										b.total++;
									}
									else b.guessed = false;
								}
							}
							else if(data[0].equals("name"))
							{
								String name = data[2];
								for(int a = 3; a < data.length; a++) name += " " + data[a];
								Game.setName(find(data[1]), name);
							}
							else if(data[0].equals("data"))
							{
								Game.setData(find(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[4]), Integer.parseInt(data[3]));
							}
							else if(data[0].equals("guess"))
							{
								PlayerCol guess = PlayerCol.valueOf(data[2]);
								find(data[1]).guess = guess;
							}
							else System.out.println("Unknown command: " + data[0]);
						}
					}
					catch(IOException | NumberFormatException e)
					{
						e.printStackTrace();
					}
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
	
	@Override
	public void render()
	{
		if(!needsRendering) return;
		
		needsRendering = false;
		
		spriteBatch.begin();
		
		spriteBatch.draw(background, 0, 0);
		
		betters.sort(null);
		
		int numToShow = Math.min(15, betters.size());
		if(numToShow > 0)
		{
			float topAcc, bottomAcc;
			topAcc = betters.get(0).uncertaintyAcc;
			bottomAcc = betters.get(numToShow - 1).uncertaintyAcc;
			float halfAcc = (topAcc + bottomAcc) / 2;
			float topHalfAcc = (topAcc + halfAcc) / 2;
			float bottomHalfAcc = (bottomAcc + halfAcc) / 2;
			for(int a = 0; a < numToShow; a++)
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
			
			betters.sort(betters.get(0));
			
			int topChain, bottomChain;
			topChain = betters.get(0).streak;
			bottomChain = betters.get(numToShow - 1).streak;
			int halfChain = (topChain + bottomChain) / 2;
			int topHalfChain = (topChain * 3 + bottomChain) / 4;
			int bottomHalfChain = (topChain + bottomChain * 3) / 4;
			for(int a = 0; a < numToShow; a++)
			{
				Better b = betters.get(a);
				int chain = b.streak;
				
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
			
			PlayerCol[] cols = PlayerCol.values();
			int[] counts = new int[cols.length];
			for(Better b : betters)
			{
				if(b.guess != null)
				{
					for(int a = 0; a < cols.length; a++)
					{
						if(b.guess == cols[a]) counts[a]++;
					}
				}
			}
			
			if(roundActive)
			{
				//TODO: favoured players
			}
			else
			{
				//TODO: vote percentiles
				if(lastWinner != null)
				{
					//TODO: show last winner
				}
			}
		}
		
		spriteBatch.end();
	}
	
	private void printData(SpriteBatch batch, Better b, int x, int y)
	{
		Color c = batch.getColor();
		batch.setColor(Color.BLACK);
		
		FontHolder.render(batch, FontHolder.getCharList(b.name), x+35, y+33, true);
		FontHolder.render(batch, FontHolder.getCharList("Acc: "+b.acc+"%"), x+35, y+18, false);
		FontHolder.render(batch, FontHolder.getCharList("Acc: "+b.uncertaintyAcc+"%"), x+35, y+8, false);
		FontHolder.render(batch, FontHolder.getCharList("Streak: "+b.streak), x+135, y+18, false);
		FontHolder.render(batch, FontHolder.getCharList("Score: "+b.score+"/"+b.total), x+135, y+8, false);
		
		batch.setColor(c);
	}
	
	@Override
	public void resize(int width, int height)
	{
		spriteBatch = new SpriteBatch();
	}
	
	@Override
	public void resume()
	{}
	
	public static Texture loadImage(String name)
	{
		name = "images/" + name;
		if(!File.separator.equals("/")) name.replace("/", File.separator);
		return new Texture(Gdx.files.internal(name + ".png"));
	}
	
	public static String numToStr(double val)
	{
		int whole = (int)val;
		double dec = (double)Math.round((val - whole) * 1000) / 1000;
		String temp = String.valueOf(whole);
		String[] digits = temp.split("");
		String result = "";
		int mod = digits.length % 3;
		for(int a = 1; a < digits.length;)
		{
			result += digits[a];
			a++;
			if(a % 3 == mod && a < digits.length) result += ",";
		}
		if(dec > 0) result += "."+String.valueOf(dec).substring(2);
		return result;
	}
	
	public static int nextPowerTwo(int val)
	{
		return (int) Math.pow(2, Math.ceil(Math.log(val) / Math.log(2)));
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
	
	public static Better setName(Better b, String name)
	{
		b.name = name;
		return b;
	}
	
	public static void setData(Better b, int score, int streak, int total)
	{
		b.score = score;
		b.streak = streak;
		b.total = total;
	}
}