package cc.iip.nju.unccg.info;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import cc.iip.nju.unccg.ai.RobotAI;
import cc.iip.nju.unccg.ai.RobotAIFactory;
import cc.iip.nju.unccg.ai.RobotAIModel;

public class RobotPlayerAdapter extends Player{
	
	private RobotAI robot;

	public RobotPlayerAdapter(String id, String name, RobotAIModel model) {
		super(id, name);
		this.robot = RobotAIFactory.produceRobotAIof(model);
	}
	
	@Override
	public void initial(Socket socket, BufferedInputStream bfin) throws IOException {
		
	}
	
	@Override
	public void send(String msg) throws IOException {
		robot.receiveMsg(msg);
	}

	@Override
	public byte[] receive() throws SocketTimeoutException, IOException {
		Arrays.fill(recvBuffer, (byte)0);
		byte[] step = robot.step().getBytes();
		System.arraycopy(step, 0, recvBuffer, 0, step.length);
		return recvBuffer;
	}
	
	@Override
	public void clear() {
		
	}
}
