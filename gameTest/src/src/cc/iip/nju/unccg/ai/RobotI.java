package cc.iip.nju.unccg.ai;

public class RobotI extends RobotAI{
	
	@Override
	public String step() {
		boolean isThisStep = false;
		
		// find the first invalid piece and reverse it
		if (!isThisStep) {
			isThisStep = findFirstInvalidPieceAndReverse();
		}
		
		// find the first piece that can be eated
		if (!isThisStep) {
			isThisStep = findFirstPieceToEat();
		}
		
		// no step is last step
		if (!isThisStep) {
			noStep();
		}
		
		return thisStep;
	}
    
	private boolean findFirstInvalidPieceAndReverse() {
		if (isAllValid) {
			return false;
		}
		for (int r = 0; r < ROWS; r++) {
			for (int c = 0; c < COLS; c++) {
				if (!board[r][c].empty && !board[r][c].valid) {
					reversePiece(r, c);
					return true;
				}
			}
		}
		isAllValid = true;
		return false;
	}
	
	private boolean findFirstPieceToEat() {
		for (int r = 0; r < ROWS; r++) {
			for (int c = 0; c < COLS; c++) {
				if (!board[r][c].empty && board[r][c].valid && board[r][c].color == ownColor) {
					// bing eat shuai
					if (board[r][c].piece == 0) {		
						int desRow = r - 1, desCol = c;
						if (desRow >= 0 && desRow < ROWS && board[desRow][desCol].color == oppositeColor && board[desRow][desCol].piece == 6) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
						desRow = r;
						desCol = c + 1;
						if (desCol >= 0 && desCol < COLS && board[desRow][desCol].color == oppositeColor && board[desRow][desCol].piece == 6) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
						desRow = r + 1;
						desCol = c;
						if (desRow >= 0 && desRow < ROWS && board[desRow][desCol].color == oppositeColor && board[desRow][desCol].piece == 6) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
						desRow = r;
						desCol = c - 1;
						if (desCol >= 0 && desCol < COLS && board[desRow][desCol].color == oppositeColor && board[desRow][desCol].piece == 6) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
					}
					if (board[r][c].piece == 1) {
						// own pao
						int desRow = r - 2, desCol = c;
						int crossRow = r - 1, crossCol = c;
						if (desRow >= 0 && desRow < ROWS && board[desRow][desCol].color == oppositeColor && !board[crossRow][crossCol].empty) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
						desRow = r;
						desCol = c + 2;
						crossRow = r;
						crossCol = c + 1;
						if (desCol >= 0 && desCol < COLS && board[desRow][desCol].color == oppositeColor && !board[crossRow][crossCol].empty) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
						desRow = r + 2;
						desCol = c;
						crossRow = r + 1;
						crossCol = c;
						if (desRow >= 0 && desRow < ROWS && board[desRow][desCol].color == oppositeColor && !board[crossRow][crossCol].empty) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
						desRow = r;
						desCol = c - 2;
						crossRow = r;
						crossCol = c - 1;
						if (desCol >= 0 && desCol < COLS && board[desRow][desCol].color == oppositeColor && !board[crossRow][crossCol].empty) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
					} else if (board[r][c].piece == 6) {		
						int desRow = r - 1, desCol = c;
						if (desRow >= 0 && desRow < ROWS && board[desRow][desCol].color == oppositeColor && board[desRow][desCol].piece != 0) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
						desRow = r;
						desCol = c + 1;
						if (desCol >= 0 && desCol < COLS && board[desRow][desCol].color == oppositeColor && board[desRow][desCol].piece != 0) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
						desRow = r + 1;
						desCol = c;
						if (desRow >= 0 && desRow < ROWS && board[desRow][desCol].color == oppositeColor && board[desRow][desCol].piece != 0) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
						desRow = r;
						desCol = c - 1;
						if (desCol >= 0 && desCol < COLS && board[desRow][desCol].color == oppositeColor && board[desRow][desCol].piece != 0) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
					} else {
						int desRow = r - 1, desCol = c;
						if (desRow >= 0 && desRow < ROWS && board[desRow][desCol].color == oppositeColor && board[desRow][desCol].piece <= board[r][c].piece) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
						desRow = r;
						desCol = c + 1;
						if (desCol >= 0 && desCol < COLS && board[desRow][desCol].color == oppositeColor && board[desRow][desCol].piece <= board[r][c].piece) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
						desRow = r + 1;
						desCol = c;
						if (desRow >= 0 && desRow < ROWS && board[desRow][desCol].color == oppositeColor && board[desRow][desCol].piece <= board[r][c].piece) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
						desRow = r;
						desCol = c - 1;
						if (desCol >= 0 && desCol < COLS && board[desRow][desCol].color == oppositeColor && board[desRow][desCol].piece <= board[r][c].piece) {
							movePiece(r, c, desRow, desCol);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
}
