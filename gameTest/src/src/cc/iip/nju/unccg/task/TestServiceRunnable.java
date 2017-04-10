package cc.iip.nju.unccg.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import cc.iip.nju.unccg.ai.RobotAIModel;
import cc.iip.nju.unccg.chess.ChessBoard;
import cc.iip.nju.unccg.config.ServerProperties;
import cc.iip.nju.unccg.info.ContestResult;
import cc.iip.nju.unccg.info.ContestResults;
import cc.iip.nju.unccg.info.RobotPlayerAdapter;
import cc.iip.nju.unccg.info.Player;
import cc.iip.nju.unccg.ui.MainFrame;

public class TestServiceRunnable implements Runnable{

	private static final Logger LOG = Logger.getLogger(TestServiceRunnable.class);
	private static final int ROUNDS = Integer.valueOf(ServerProperties.instance().getProperty("contest.rounds"));
	private static final int STEPS = Integer.valueOf(ServerProperties.instance().getProperty("round.steps"));
	private static final int ERRORS = Integer.valueOf(ServerProperties.instance().getProperty("step.error.number"));
	private static final RobotAIModel ROBOT_MODEL = RobotAIModel.values()[Integer.valueOf(ServerProperties.instance().getProperty("robot.model"))];
	private static int ID = 0;
	
	private Player[] players;
	private String info;
	private ContestResult result;
	private ArrayList<ArrayList<String>> record;
	
	public TestServiceRunnable(Player user) {
		
		this.players = new Player[2];
		players[0] = user;
		players[1] = new RobotPlayerAdapter(ROBOT_MODEL.toString(), ROBOT_MODEL.toString(), ROBOT_MODEL);
		
		this.info = players[0].getId() + "vs" + players[1].getId() + "-test-" + ID;
		
		this.result = new ContestResult(ID, players[0], players[1]);
		
		this.record = new ArrayList<ArrayList<String>>();
		for (int r = 0; r < ROUNDS; r++) {
			this.record.add(new ArrayList<String>());
			record.get(r).add("TEST " + ID);
			record.get(r).add("INFO " + players[0].getId() + " VS " + players[1].getId());
		}
		
		ID++;
	}
	
	@Override
	public void run() {
	
		LOG.info(this.info + " begin!");
		MainFrame.instance().log(this.info + " begin!");
	
		byte[] recvBuffer = null;
		
		try {
			
			for (int round = 0; round < ROUNDS; round++) {
				
				record.get(round).add("ROUND_START " + round);
				LOG.info(this.info + " round " + round + " start");
				MainFrame.instance().log(this.info + " round " + round + " start");
				
				// assign color piece
				int red = round & 0x1, black = 1 - red;
				String synMsg = null;
				if (red == 0) {
					synMsg = "BR";
				} else {
					synMsg = "BB";
				}
				int num = 0;
				record.get(round).add("COLOR RED:P" + red + " BLACK:P" + black);
				
				// generate chess board randomly
				ChessBoard board = new ChessBoard();
				board.generateChessBoardRandomly();
				record.get(round).add("INITIAL CHESS BOARD\n" + board.toStringToRecord());
				
				try {
					
					try {
						players[red].send("BR");
					} catch (Exception e) {
						LOG.error(e);
						record.get(round).add("SEND_ERROR RED");
						result.winner = black;
						return;
					}
					try {
						players[black].send("BB");
					} catch (Exception e) {
						LOG.error(e);
						record.get(round).add("SEND_ERROR BLACK");
						result.winner = red;
						return;
					}
					
					int synNum = 0;
					try {
						while (synNum < 5) {
							recvBuffer = players[0].receive();
							String syn = new String(recvBuffer);
							if (syn.substring(0, 2).equals(synMsg)) {
								break;
							}
						}
					} catch (SocketTimeoutException e) {
						// step timeout
						result.errors[0][round]++;
						LOG.error(e);
						LOG.error(this.info + " ROUND " + round + " TimeoutException when synchronize red round");
						record.get(round).add("SYN_ERROR RED " + result.errors[0][round]);
						result.winner = 1;
						return;
					} catch (Exception e) {
						// other exception
						LOG.error(e);
						LOG.error(this.info + " ROUND " + round + " Unkown Exception when synchronize red round!");
						record.get(round).add("UNKOWN_EXCEPTION RED");
						result.winner = 1;
						return;
					}
					
					// begin palying chess
					for ( ; num < STEPS && board.isGeneratedWinnner() < 0; num++) {
						
						// receive red player step
						try {
							// block...
							recvBuffer = players[red].receive();
						} catch (SocketTimeoutException e) {
							// step timeout
							result.errors[red][round]++;
							LOG.error(e);
							LOG.error(this.info + " ROUND " + round + " TimeoutException when receive RED step: " + result.errors[red][round] + " time!");
							record.get(round).add("TIMEOUT RED " + result.errors[red][round]);
							while (result.errors[red][round] <= ERRORS) {
								try {
									recvBuffer = players[red].receive();
								} catch (SocketTimeoutException ee) {
									result.errors[red][round]++;
									LOG.error(e);
									LOG.error(this.info + " ROUND " + round + " TimeoutException when receive RED step: " + result.errors[red][round] + " time!");
									record.get(round).add("TIMEOUT RED " + result.errors[red][round]);
									continue;
								}
								break;
							}
							if (result.errors[red][round] > ERRORS) {
								record.get(round).add("ERROR_MAXTIME RED");
								break;
							}
						} catch (Exception e) {
							// other exception
							LOG.error(e);
							LOG.error(this.info + " ROUND " + round + " Unkown Exception when receive RED step!");
							record.get(round).add("UNKOWN_EXCEPTION RED");
							result.winner = black;
							break;
						}
						
						// test and verify the red step
						String redStep = new String(recvBuffer);
						String redReturnCode = board.step(redStep, 0);
						if (redReturnCode.charAt(1) == '0') {
							// valid step
							record.get(round).add("VALID_STEP RED " + redStep.substring(0, 6));
							record.get(round).add(board.toStringToDisplay());
							try {
								players[red].send(redReturnCode);
							} catch (Exception e) {
								LOG.error(e);
								record.get(round).add("SEND_ERROR RED");
								result.winner = black;
								return;
							}
							try {
								players[black].send(redReturnCode);
							} catch (Exception e) {
								LOG.error(e);
								record.get(round).add("SEND_ERROR BLACK");
								result.winner = red;
								return;
							}
						} else {
							// invalid step
							result.errors[red][round]++;
							record.get(round).add("ERROR_STEP RED " + result.errors[red][round] + " " + redStep.substring(0, 6));
							try {
								players[red].send(redReturnCode);
							} catch (Exception e) {
								LOG.error(e);
								record.get(round).add("SEND_ERROR RED");
								result.winner = black;
								return;
							}
							try {
								players[black].send("R0N");
							} catch (Exception e) {
								LOG.error(e);
								record.get(round).add("SEND_ERROR BLACK");
								result.winner = red;
								return;
							}
							if (result.errors[red][round] > ERRORS) {
								record.get(round).add("ERROR_MAXTIME RED");
								break;
							}
						}
						
						// receive black step
						try {
							// block...
							recvBuffer = players[black].receive();
						} catch (SocketTimeoutException e) {
							// step timeout
							result.errors[black][round]++;
							LOG.error(e);
							LOG.error(this.info + " ROUND " + round + " TimeoutException when receive BLACK step: " + result.errors[black][round] + " time!");
							record.get(round).add("TIMEOUT BLACK " + result.errors[red][round]);
							while (result.errors[black][round] <= ERRORS) {
								try {
									recvBuffer = players[black].receive();
								} catch (SocketTimeoutException ee) {
									result.errors[black][round]++;
									LOG.error(e);
									LOG.error(this.info + " ROUND " + round + " TimeoutException when receive BLACK step: " + result.errors[black][round] + " time!");
									record.get(round).add("TIMEOUT RED " + result.errors[red][round]);
									continue;
								}
								break;
							}
							if (result.errors[black][round] > ERRORS) {
								record.get(round).add("ERROR_MAXTIME BLACK");
								break;
							}
						} catch (Exception e) {
							// other exception
							LOG.error(e);
							LOG.error(this.info + " ROUND " + round + " Unkown Exception when receive BLACK step!");
							record.get(round).add("UNKOWN_EXCEPTION BLACK");
							result.winner = red;
							break;
						}
						
						// test and verify the black step
						String blackStep = new String(recvBuffer);
						String blackReturnCode = board.step(blackStep, 1);
						if (blackReturnCode.charAt(1) == '0') {
							// valid step
							record.get(round).add("VALID_STEP BLACK " + blackStep.substring(0, 6));
							record.get(round).add(board.toStringToDisplay());
							try {
								players[black].send(blackReturnCode);
							} catch (Exception e) {
								LOG.error(e);
								record.get(round).add("SEND_ERROR BLACK");
								result.winner = red;
								return;
							}
							try {
								players[red].send(blackReturnCode);
							} catch (Exception e) {
								LOG.error(e);
								record.get(round).add("SEND_ERROR RED");
								result.winner = black;
								return;
							}
						} else {
							// invalid step
							result.errors[black][round]++;
							record.get(round).add("ERROR_STEP BLACK " + result.errors[black][round] + " " + blackStep.substring(0, 6));
							try {
								players[black].send(blackReturnCode);
							} catch (Exception e) {
								LOG.error(e);
								record.get(round).add("SEND_ERROR BLACK");
								result.winner = red;
								return;
							}
							try {
								players[red].send("R0N");
							} catch (Exception e) {
								LOG.error(e);
								record.get(round).add("SEND_ERROR RED");
								result.winner = black;
								return;
							}
							if (result.errors[black][round] > ERRORS) {
								record.get(round).add("ERROR_MAXTIME BLACK");
								break;
							}
						}
					}
				} catch (Exception e) {
					// round end abnormally
					e.printStackTrace();
					LOG.error(e);
					LOG.error(this.info + " ROUND " + round + " Unkown Exception in " + round + " CONTEST");
					record.get(round).add("ROUND_ERROR " + round);
				} finally {
					// notify players that this round is over and record this round result
					try {
						players[0].send("E1");
						players[1].send("E1");
					} catch (Exception e) {
						LOG.error(e);
					}
					LOG.info(this.info + " round " + round + " end");
					MainFrame.instance().log(this.info + " round " + round + " end");
					
					// record result
					result.stepsNum[round] = num;
					result.scores[red][round] = board.getScoreOf(0);
					result.scores[black][round] = board.getScoreOf(1);
					
					record.get(round).add("ROUND_END " + round);
				}
			
			}
			
		} catch (Exception e) {
			LOG.error(e);
			LOG.error(this.info + " Unkown Exception");
		} finally {
			try {
				// notify players that contest is over and save contest result
				players[0].send("E0");
				players[1].send("E0");
			} catch (Exception e) {
				LOG.error(e);
			}
			LOG.info(this.info + " game over");
			MainFrame.instance().log(this.info + " game over");
			
			// save result
			result.evaluate();
			ContestResults.addContestResult(result);
			saveResult();
			LOG.info(this.info + " result save completed!");
			MainFrame.instance().log(this.info + " result save completed!");
			
			// store record into file
			saveRecord();
			LOG.info(this.info + " record save completed!");
			MainFrame.instance().log(this.info + " record save completed!");
			
			// release 
			players[0].clear();
			players[1].clear();
		}
		
	}
	
	private void saveRecord() {	
		String RECORD_DIR = ServerProperties.instance().getProperty("current.record.dir");
		File dir = new File(System.getProperty("user.dir") + "/record");
		if (!dir.exists()) {
			dir.mkdir();
		}
		dir = new File(System.getProperty("user.dir") + "/record/test");
		if (!dir.exists()) {
			dir.mkdir();
		}
		dir = new File(System.getProperty("user.dir") + "/record/test/" + RECORD_DIR);
		if (!dir.exists()) {
			dir.mkdir();
		}
		for (int r = 0; r < ROUNDS; r++) {
			PrintWriter out = null;
			try {
				File file = new File(System.getProperty("user.dir") + "/record/test/" + RECORD_DIR + "/" + this.info + "-round-" + r + ".record");
				if (!file.exists()) {
					file.createNewFile();
				} else {
					file.delete();
					file.createNewFile();
				}
				out = new PrintWriter(file);
				ArrayList<String> rec = record.get(r);
				for (int i = 0; i < rec.size(); i++) {
					out.println(rec.get(i));
				}
				out.flush();
			} catch (FileNotFoundException e) {
				LOG.error(e);
			} catch (IOException e) {
				LOG.error(e);
			} finally {
				record.get(r).clear();
				out.close();
			}
		}	
	}
	
	public void saveResult() {
		String RESULT_DIR = ServerProperties.instance().getProperty("current.result.dir");
		PrintWriter out = null;
		try {
			File dir = new File(System.getProperty("user.dir") + "/result");
			if (!dir.exists()) {
				dir.mkdir();
			}
			dir = new File(System.getProperty("user.dir") + "/result/test");
			if (!dir.exists()) {
				dir.mkdir();
			}
			dir = new File(System.getProperty("user.dir") + "/result/test/" + RESULT_DIR);
			if (!dir.exists()) {
				dir.mkdir();
			}
			File file = new File(System.getProperty("user.dir") + "/result/test/" + RESULT_DIR + "/" + this.info + ".result");
			if (!file.exists()) {
				file.createNewFile();
			} else {
				file.delete();
				file.createNewFile();
			}
			out = new PrintWriter(file);
			out.println(result);
			out.flush();
		} catch (FileNotFoundException e) {
			LOG.error(e);
		} catch (IOException e) {
			LOG.error(e);
		} finally {
			result = null;
			out.close();
		}
	}

}
