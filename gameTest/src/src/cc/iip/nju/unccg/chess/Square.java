package cc.iip.nju.unccg.chess;

/**
 * a square of the chess board
 * 
 * @author coldcode
 *
 */
public class Square {
	// piece or no piece in this square
	public boolean empty = false;
	// the piece is or not valid
	public boolean valid = false;
	// 0 id red, 1 is black, -1 is no color
	public int color = -1;
	// 0 bing/zu, 1 pao, 2 ma, 3 che, 4 xiang, 5 shi, 6 jiang/shuai, -1 is no piece
	public int piece = -1;
	
	public Square() {
		
	}
	
	public Square(int color, int piece) {
		this.color = color;
		this.piece = piece;
	}
	
	public void moveTo(Square des) {
		des.empty = false;
		des.color = this.color;
		des.piece = this.piece;
		
		this.clear();
	}
	
	public void moveToAndEat(Square des) {
		this.moveTo(des);
	}
	
	public void clear() {
		this.empty = true;
		this.color = -1;
		this.piece = -1;
	}
	
	public void reset() {
		this.empty = false;
		this.valid = false;
		this.color = -1;
		this.piece = -1;
	}
	
	public String toStringToRecord() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder("");
		if (this.empty) {
			sb.append("E");
		} else {
			sb.append("N");
		}
		if (this.valid) {
			sb.append("V");
		} else {
			sb.append("I");
		}
		switch (this.color) {
		case 0:
			sb.append("R");
			break;
		case 1:
			sb.append("B");
			break;
		default:
			sb.append("O");
			break;
		}
		sb.append(this.piece);
		return sb.toString();
	}
	
	public String toStringToDisplay() {
		// TODO Auto-generated method stub
		if (this.empty) {
			return "|__|";
		} else if (!this.valid){
			return "|##|";
		} else {
			StringBuilder sb = new StringBuilder("|");
			switch (this.color) {
			case 0:
				sb.append("R");
				break;
			case 1:
				sb.append("B");
				break;
			default:
				return "|  |";
			}
			sb.append(this.piece);
			sb.append("|");
			return sb.toString();
		}
	}
}
