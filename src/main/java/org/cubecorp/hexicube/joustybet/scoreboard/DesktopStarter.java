package org.cubecorp.hexicube.joustybet.scoreboard;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import org.apache.commons.cli.*;

public class DesktopStarter
{
	public static LwjglApplicationConfiguration config;
	
	public static void main(String[] args)
	{

	    Options options = new Options();

	    Option url = new Option("u", "url", true, "URL of websocket to hit");
	    url.setRequired(true);

	    Option fullscreen = new Option("f", "fullscreen", false, "Force fullscreen mode");

        options.addOption(url);
        options.addOption(fullscreen);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("scoreboard", options);

            System.exit(1);
            return;
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
		config.samples = 2;

		if (cmd.hasOption("fullscreen")) {
            config.fullscreen = true;
        }

		new LwjglApplication(new Game(cmd.getOptionValue("url")), config);
	}
}