package cc.iip.nju.unccg.info;

import cc.iip.nju.unccg.config.ServerProperties;

public class ReplayStep {
	
	private static final int ROWS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.rows"));
	private static final int COLS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.cols"));
	private static String[] redTransformer = new String[]{"兵","炮","马","车","相","仕","帅"};
	private static String[] blackTransformer = new String[]{"卒","炮","马","车","象","士","将"};
	
	public String step = null;
	public String[][] board = null;
	public boolean isValidStep = false;
	
	public ReplayStep(String step, boolean isValid) {
		this.step = step;	
		this.isValidStep = isValid;
		if (isValid) {
			board = new String[ROWS][COLS];
		}
	}
	
	public void append(int r, String line) {
		String[] row = line.split(" ");
		for (int c = 0; c < COLS; c++) {
			switch (row[c].charAt(1)) {
			case 'R':
			{
				int index = row[c].charAt(2) - '0';
				board[r][c] = "R" + redTransformer[index];
				break;
			}
			case 'B':
			{
				int index = row[c].charAt(2) - '0';
				board[r][c] = "B" + blackTransformer[index];
				break;
			}	
			default:
				board[r][c] = row[c].substring(1, 3);
				break;
			}
		}
	}
}
