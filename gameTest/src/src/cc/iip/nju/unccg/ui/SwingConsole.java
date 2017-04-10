package cc.iip.nju.unccg.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import cc.iip.nju.unccg.config.ServerProperties;

public class SwingConsole {
	
	public static void run(final JFrame f){
		int width = Integer.valueOf(ServerProperties.instance().getProperty("ui.width"));
		int height = Integer.valueOf(ServerProperties.instance().getProperty("ui.height"));
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				f.pack();
				f.setTitle("UNChineseChessGame-Server-0.3.0");
				f.setLocationRelativeTo(null);
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setSize(width, height);
				f.setVisible(true);
			}
		});
	}
}
