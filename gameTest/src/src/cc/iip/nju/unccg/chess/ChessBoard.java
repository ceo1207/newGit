package cc.iip.nju.unccg.chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.apache.log4j.Logger;

import cc.iip.nju.unccg.Main;
import cc.iip.nju.unccg.config.ServerProperties;

public class ChessBoard {
	
	private static final Logger LOG = Logger.getLogger(Main.class);
	private static final int ROWS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.rows"));
	private static final int COLS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.cols"));
	private static String spliter = "-------------------";
	static 
	{
		if (COLS == 8) spliter += spliter;
	}
	private static int totalScore = 0;
	static 
	{
		if (COLS == 4) totalScore = 29;
		if (COLS == 8) totalScore = 52;
	}
	
	// the chess board
	private Square[][] board = new Square[ROWS][COLS];
	private int[] scores = new int[2];

	public ChessBoard() {
		Arrays.fill(scores, 0);
	}

	public void generateChessBoardRandomly() {
		Integer[] pieces = null;
		if (ROWS == 4 && COLS == 4) {
			pieces = new Integer[]{0, 0, 7, 7, 1, 8, 2, 9, 3, 10, 4, 11, 5, 12, 6, 13};
		} else if (ROWS == 4 && COLS == 8) {
			pieces = new Integer[]{0, 0, 0, 0, 0, 7, 7, 7, 7, 7, 1, 1, 8, 8, 2, 2, 9, 9, 
					3, 3, 10, 10, 4, 4, 11, 11, 5, 5, 12, 12, 6, 13};
		} else {
			LOG.error("Error value: chess.board.rows or chess.board.cols");
			System.exit(0);
		}
		ArrayList<Integer> randomPieces = new ArrayList<Integer>();
		Collections.addAll(randomPieces, pieces);
		Collections.shuffle(randomPieces, new Random(System.currentTimeMillis()));
		for (int index = 0, i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				if (randomPieces.get(index) < 7) {
					this.board[i][j] = new Square(0, randomPieces.get(index));
				} else {
					this.board[i][j] = new Square(1, randomPieces.get(index) - 7);
				}
				index++;
			}
		}
	}

	/**
	 * 
	 * @param step
	 * @param color 0 is red, 1 is black
	 * @return returnCode R code R/M/N srcRow srcCol desRow desCol.
	 * 		   code 0 : sucess;
	 * 		   code 1 : msg format error;
	 *         code 2 : coordinate error;
	 *         code 3 : color error;
	 *         code 4 : invalid step.
	 */
	public String step(String step, int color) {
		// check the step
		if (step.charAt(0) != 'S') {
			return "R1";
		}
		
		switch (step.charAt(1)) {
		case 'M':
		{
			// move the piece
			
			// check the src and des position which is or not valid
			for (int i = 2; i <= 4; i += 2) {
				if (step.charAt(i) >= ROWS + '0' || step.charAt(i) < '0') {
					return "R2";
				}
			}
			for (int i = 3; i <= 5; i += 2) {
				if (step.charAt(i) >= COLS + '0' || step.charAt(i) < '0') {
					return "R2";
				}
			}
			int srcRow = step.charAt(2) - '0', srcCol = step.charAt(3) - '0';
			int desRow = step.charAt(4) - '0', desCol = step.charAt(5) - '0';
			
			Square srcSquare = this.board[srcRow][srcCol];
			Square desSquare = this.board[desRow][desCol];
			
			if (srcSquare.color != color) {
				// can not move the opposite piece
				return "R3";
			}
			
			if (!srcSquare.empty && srcSquare.valid && desSquare.valid) {
				// src is no empty, src and des are both valid
				if (srcSquare.piece == 1) {
					// src piece is pao
					if (desSquare.empty) {
						// move pao
						if ((srcRow == desRow && (desCol - srcCol == 1 || desCol - srcCol == -1))
								|| (srcCol == desCol && (desRow - srcRow == 1 || desRow - srcRow == -1))) {
							srcSquare.moveTo(desSquare);
							return "R0" + step.substring(1, 6);
						}
						return "R2";
					} else {
						// move pao to eat other piece
						if ((srcRow == desRow && (desCol - srcCol == 2 || desCol - srcCol == -2))
								|| (srcCol == desCol && (desRow - srcRow == 2 || desRow - srcRow == -2))) {
							if (srcSquare.color == desSquare.color) {
								// attemp to eat the own piece
								return "R3";
							}
							int crossRow = (desRow + srcRow) / 2, crossCol = (desCol + srcCol) / 2;
							if (board[crossRow][crossCol].empty) {
								// can not cross no piece to eat
								return "R4";
							}
							if (srcSquare.piece == desSquare.piece ) {
								// the two piece is same
								scores[0] += desSquare.piece + 1;
								scores[1] += desSquare.piece + 1;
								srcSquare.clear();
								desSquare.clear();
								return "R0" + step.substring(1, 6);
							} else {
								scores[color] += desSquare.piece + 1;
								srcSquare.moveToAndEat(desSquare);
								return "R0" + step.substring(1, 6);
							}
						} else {
							return "R2";
						}
					}
				} else {
					// src piece is not pao
					if ((srcRow == desRow && (desCol - srcCol == 1 || desCol - srcCol == -1))
							|| (srcCol == desCol && (desRow - srcRow == 1 || desRow - srcRow == -1))) {
						if (desSquare.empty) {
							// move the src piece
							srcSquare.moveTo(desSquare);
							return "R0" + step.substring(1, 6);
						} else if (srcSquare.color != desSquare.color) {
							// eat the opposite piece
							if (srcSquare.piece == desSquare.piece ) {
								// the two piece is same
								scores[0] += desSquare.piece + 1;
								scores[1] += desSquare.piece + 1;
								srcSquare.clear();
								desSquare.clear();
								return "R0" + step.substring(1, 6);
							}
							if (srcSquare.piece == 0 && desSquare.piece == 6) {
								// bing/zu eat jiang/shuai
								scores[color] += desSquare.piece + 1;
								srcSquare.moveToAndEat(desSquare);
								return "R0" + step.substring(1, 6);
							}
							if (srcSquare.piece == 6 && desSquare.piece == 0) {
								// jiang/shuai can not eat bing/zu
								return "R4";
							}
							if (srcSquare.piece > desSquare.piece) {
								// big eat samll
								scores[color] += desSquare.piece + 1;
								srcSquare.moveToAndEat(desSquare);
								return "R0" + step.substring(1, 6);
							}
							return "R4";
						} else {
							// attemp to eat the own piece
							return "R3";
						}
					} else {
						return "R2";
					}
				}
			}
			return "R4";
		}
		case 'R':
		{
			// reverse the piece
			
			// check the src and des position which is or not valid
			for (int i = 2; i <= 4; i += 2) {
				if (step.charAt(i) >= ROWS + '0' || step.charAt(i) < '0') {
					return "R2";
				}
			}
			for (int i = 3; i <= 5; i += 2) {
				if (step.charAt(i) >= COLS + '0' || step.charAt(i) < '0') {
					return "R2";
				}
			}
			int srcRow = step.charAt(2) - '0', srcCol = step.charAt(3) - '0';
			int desRow = step.charAt(4) - '0', desCol = step.charAt(5) - '0';
			
			if (srcRow != desRow || srcCol != desCol) {
				return "R2";
			}
			Square desSquare = this.board[desRow][desCol];
			if (!desSquare.empty && !desSquare.valid) {
				// update step result
				desSquare.valid = true; 
				return "R0" + step.substring(1, 6) + desSquare.color + desSquare.piece;
			} else {
				return "R4";
			}			
		}
		case 'N':
			return "R0N";
		default:
			return "R1";
		} 
	}
	
	/**
	 * get score of color
	 * 
	 * @param color 0 is red, 1 is black
	 * @return
	 */
	public int getScoreOf(int color) {
		return scores[color];
	}
	
	/**
	 * -1 has not winnner, 0 winner is red, 1 winner is black
	 * 
	 * @return
	 */
	public int isGeneratedWinnner() {
		for (int color = 0; color < 2; color++) {
			if (scores[color] >= totalScore) {
				return color;
			}
		}
		return -1;
	}

	public String toStringToRecord() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder(spliter);
		sb.append("\n");
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				sb.append(board[i][j].toStringToRecord());
				sb.append(" ");
			}
			sb.append("\n");
		}
		sb.append(spliter);
		return sb.toString();
	}
	
	public String toStringToDisplay() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder(spliter);
		sb.append("\n");
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				sb.append(board[i][j].toStringToDisplay());
				sb.append(" ");
			}
			sb.append("\n");
		}
		sb.append(spliter);
		return sb.toString();
	}

}
