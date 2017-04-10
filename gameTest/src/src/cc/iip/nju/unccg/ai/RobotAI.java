package cc.iip.nju.unccg.ai;

import cc.iip.nju.unccg.chess.Square;
import cc.iip.nju.unccg.config.ServerProperties;

public abstract class RobotAI {
	
	protected static final int ROWS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.rows"));
	protected static final int COLS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.cols"));
	
	// game board
	protected Square[][] board;
	protected int ownColor = -1;
	protected int oppositeColor = -1;
	protected int round = 0;
	protected boolean isAllValid = false;
	
	protected String thisStep;
	
	public RobotAI() {
		this.board = new Square[ROWS][COLS];
		for (int r = 0; r < ROWS; r++) {
			for (int c = 0; c < COLS; c++) {
				this.board[r][c] = new Square();
			}
		}
	}
	
	protected void reversePiece(int row, int col) {
		StringBuilder step = new StringBuilder("SR");
		step.append(row);
		step.append(col);
		step.append(row);
		step.append(col);
		thisStep = step.toString();
	}
	
	protected void movePiece(int srcRow, int srcCol, int desRow, int desCol) {
		StringBuilder step = new StringBuilder("SM");
		step.append(srcRow);
		step.append(srcCol);
		step.append(desRow);
		step.append(desCol);
		thisStep = step.toString();
	}
	
	protected void noStep() {
		thisStep = "SN";
	}
	
	protected void updateLastStep(String step) {
		switch (step.charAt(0)) {
		case 'R':
		{
			int srcRow = step.charAt(1) - '0', srcCol = step.charAt(2) - '0';
			int color = step.charAt(5) - '0';
			int piece = step.charAt(6) - '0';
			board[srcRow][srcCol].valid = true;
			board[srcRow][srcCol].color = color;
			board[srcRow][srcCol].piece = piece;
			break;
		}
		case 'M':
		{
			int srcRow = step.charAt(1) - '0', srcCol = step.charAt(2) - '0';
			int desRow = step.charAt(3) - '0', desCol = step.charAt(4) - '0';
			if (board[srcRow][srcCol].piece == board[desRow][desCol].piece) {
				board[srcRow][srcCol].clear();
				board[desRow][desCol].clear();
			} else {
				board[srcRow][srcCol].moveTo(board[desRow][desCol]);
			}
			break;
		}
		case 'N':
			break;
		default:
			break;
		}
	}
	
	protected void roundStart(int color) {
		this.ownColor = color;
		this.oppositeColor = 1 - color;
		this.isAllValid = false;
	}
	
	protected void roundOver() {
		this.round++;
		for (int r = 0; r < ROWS; r++) {
			for (int c = 0; c < COLS; c++) {
				this.board[r][c].reset();
			}
		}
	}
	
	protected void gameOver() {
		
	}
	
	/**
	 * receive msg
	 * 
	 * @param msg
	 */
	public void receiveMsg(String msg) {
		switch (msg.charAt(0)) {
		case 'B':
			// one round begin and the piece color assigned
			switch (msg.charAt(1)) {
			case 'R':
				roundStart(0);
				break;
			case 'B':
				roundStart(1);
				break;
			default:
				break;
			}
			break;
		case 'R':
			// return code
			switch (msg.charAt(1)) {
			case '0':
				// step is valid and update board
				updateLastStep(msg.substring(2));
				break;
			default:
				System.out.println("Round " + round +  " Error Code " + msg.charAt(1));
				break;
			}
			break;
		case 'E':
			// round or contest end
			switch (msg.charAt(1)) {
			case '0':
				gameOver();
				break;
			case '1':
				roundOver();
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * the next step, you must be sure that the lastStep is returned
	 * 
	 * @return
	 */
	public abstract String step();
}
