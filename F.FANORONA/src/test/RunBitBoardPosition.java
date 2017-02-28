package test;

import logic.board.Bits;


public class RunBitBoardPosition {

	public void boardNumbers() {
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Displays Bit Positioning ");

		
		for (int row=0;row<5;row++) {
			for (int col=0;col<8;col++) {
				long b = Bits.at(row, col);
				System.out.println("row: "+row+" col: "+col+" b: "+b);
			}
		}
	}

	public void testBits() {
	
		long myPieces = Bits.INITIAL_BOT | Bits.IS_WHITE;
		long opponentPieces = Bits.INITIAL_TOP;
		System.out.println("myPieces: "+myPieces+" opponentPieces: "+opponentPieces);


		System.out.println("-------------------------------------------------------");
		System.out.println("Displays in OCTAL ");
		System.out.println("INITIAL_TOP: "+Bits.INITIAL_TOP);
		System.out.println("INITIAL_BOT: "+Bits.INITIAL_BOT);
		System.out.println("TOP_ROW: "+Bits.TOP_ROW);
		System.out.println("BOTTOM_ROW: "+Bits.BOTTOM_ROW);
		System.out.println("LEFT_COL: "+Bits.LEFT_COL);
		System.out.println("RIGHT_COL: "+Bits.RIGHT_COL);
		System.out.println("DIAGONAL: "+Bits.DIAGONAL);
		System.out.println("ON_BOARD: "+Bits.ON_BOARD);
		System.out.println("CENTER: "+Bits.CENTER);


		System.out.println("-------------------------------------------------------");
		System.out.println("Displays in OCTAL (Conversion) ");
		long eval = 562399469895680L;
		System.out.println("INITIAL_TOP: "+Long.toOctalString(eval));  // Conversion to OCTAL

		System.out.println("-------------------------------------------------------");
		System.out.println("Displays in DECIMAL ");
		System.out.println("INITIAL_TOP	: "+562399469895680L);
		System.out.println("INITIAL_BOT	: "+173538815L);
		System.out.println("TOP_ROW		: "+561850441793536L);
		System.out.println("BOTTOM_ROW	: "+511L);
		System.out.println("LEFT_COL	: "+281750123315456L);
		System.out.println("RIGHT_COL	: "+1100586419201L);
		System.out.println("DIAGONAL	: "+375116358920533L);
		System.out.println("ON_BOARD	: "+562399660211711L);
		System.out.println("CENTER		: "+130023424L);

	}
	

	
	public static void main(String [ ] args) {
		
		RunBitBoardPosition run = new RunBitBoardPosition();
		run.testBits();
		run.boardNumbers();

	}


}
