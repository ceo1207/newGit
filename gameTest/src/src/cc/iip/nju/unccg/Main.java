package cc.iip.nju.unccg;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import cc.iip.nju.unccg.communication.ClientConnectMonitor;
import cc.iip.nju.unccg.config.ServerProperties;
import cc.iip.nju.unccg.info.Players;
import cc.iip.nju.unccg.task.CreatServiceRunnable;
import cc.iip.nju.unccg.ui.MainFrame;
import cc.iip.nju.unccg.ui.SwingConsole;

public class Main {
	
	static {
		PropertyConfigurator.configure(System.getProperty("user.dir") + "/config/log4j.properties");
	}
	
	private static final Logger LOG = Logger.getLogger(Main.class);
	
	public static void main(String[] args) {
		
		// read config
		ServerProperties.instance();
		
		// start ui
		LOG.info("Startup the main ui");
		SwingConsole.run(MainFrame.instance());
		
		// print server propeties in ui
		ServerProperties.printServerProperties();
		
		// load users
		Players.loadPlayers();
		
		// prepare creating service
		CreatServiceRunnable.instance().start();
		
		// monitor connect
		ClientConnectMonitor.instance().start();
		
	}

}
