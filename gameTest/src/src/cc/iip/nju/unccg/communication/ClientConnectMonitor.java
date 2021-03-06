package cc.iip.nju.unccg.communication;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import cc.iip.nju.unccg.Main;
import cc.iip.nju.unccg.config.ServerProperties;
import cc.iip.nju.unccg.task.CreatServiceRunnable;
import cc.iip.nju.unccg.ui.MainFrame;

/**
 * monitor clients access
 * 
 * @author coldcode
 *
 */
public class ClientConnectMonitor extends Thread {

	private static final Logger LOG = Logger.getLogger(Main.class);
	
	private static ClientConnectMonitor instance = new ClientConnectMonitor();
	// server ip
	private String ip;
	// server port
	private int port;
	// server socket
	private ServerSocket serverSocket = null;
	
	public static ClientConnectMonitor instance() {
		return instance;
	}

	private ClientConnectMonitor() {
		this.port = Integer.valueOf(ServerProperties.instance().getProperty("server.port"));
		try {
			this.ip = Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			LOG.error(e);
			System.exit(0);
		}
		
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			LOG.error(e);
			System.exit(0);
		} catch (SecurityException e) {
			LOG.error(e);
			System.exit(0);
		} catch (IllegalArgumentException e) {
			LOG.error(e);
			System.exit(0);
		}
		
		LOG.info("Creat server socket success!");
		MainFrame.instance().log("Creat server socket success!");
		LOG.info("Server address is: " + this.ip + ":" + this.port);
		MainFrame.instance().log("Server address is: " + this.ip + ":" + this.port);
		LOG.info("ClientConnectMonitor initialization complete!");
		MainFrame.instance().log("ClientConnectMonitor initialization complete!");
	}

	/**
	 * start monitor
	 */
	@Override
	public void run() {
		
		LOG.info("Start monitor...");
		MainFrame.instance().log("Start monitor...");

		try {
			while (this.serverSocket != null && !Thread.currentThread().isInterrupted()) {

				Socket socket = null;
				try {
					LOG.info("Waitting clients to access...");
					MainFrame.instance().log("Waitting clients to access...");
					socket = serverSocket.accept();
				} catch (IOException e) {
					LOG.error(e);
					continue;
				} catch (SecurityException e) {
					LOG.error(e);
					continue;
				}
				
				if (socket == null) {
					LOG.error("Socket is null");
					MainFrame.instance().log("Socket is null");
					continue;
				}

				CreatServiceRunnable.instance().addSocket(socket);
				LOG.info("Accepted one accession: " + socket.getInetAddress().getHostAddress()
						+ ":" + socket.getPort());
				MainFrame.instance().log("Accepted one accession: " + socket.getInetAddress().getHostAddress()
						+ ":" + socket.getPort());
			}
		} catch (Exception e) {
			LOG.error(e);
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				LOG.error(e);
			}
			LOG.error("System will exit!");
			System.exit(0);
		}
		
	}

}
