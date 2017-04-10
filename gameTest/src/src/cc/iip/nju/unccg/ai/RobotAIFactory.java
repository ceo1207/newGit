package cc.iip.nju.unccg.ai;

import org.apache.log4j.Logger;

import cc.iip.nju.unccg.Main;

public class RobotAIFactory {
	
	private static final Logger LOG = Logger.getLogger(Main.class);
	
	public static RobotAI produceRobotAIof(RobotAIModel model) {
		switch (model) {
		case RobotO:
		case RobotI:
			LOG.info("Produce one Robot I");
			return new RobotI();
		case RobotII:
		case RobotIII:
		case RobotIV:
		default:
			LOG.info("Robot Factory can not produce this model Robot!");
			System.exit(0);
		}
		return null;
	}
}
