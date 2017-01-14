package org.cubecorp.hexicube.joustybet.scoreboard;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopStarter
{
	public static LwjglApplicationConfiguration config;
	
	public static void main(String[] args)
	{

		if (args.length < 1) {
            System.err.println("We need a URL to hit");
            System.exit(5);
        }
		LwjglApplicationConfiguration.disableAudio = true;
		
		config = new LwjglApplicationConfiguration();
		config.title = "Loading...";
		config.width = 800;
		config.height = 600;
		config.foregroundFPS = 20;
		config.backgroundFPS = 20;
		config.resizable = false;
		config.vSyncEnabled = false;
		config.samples = 1;
		
		new LwjglApplication(new Game(args[0]), config);
	}
}