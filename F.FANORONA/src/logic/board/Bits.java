package logic.board;

// Game of Fanorona
// David Eppstein, UC Irvine, 11 Jun 1997
//
// Bit manipulation
//
// We represent Fanorona positions using two bitboards (64-bit longs)
// one for the pieces of each player.  The bits in the bitboard form
// six 10-bit groups: an empty group followed by one for each row of the board.
// Each row has an empty bit followed by a bit for each position in the row.
// The empty bits help avoid extra range checking while finding captures.
//
// The top four bits are free for other purposes; we use one
// to determine which color pieces are on each side, and one to determine
// whether the side on move's most recent move was a capture.
//
// This class is not instantiable; instead it consists of constants
// for various board configurations, used in game setup, move generation,
// and evaluation.

public abstract class Bits {
	public static final long IS_WHITE       = 0x4000000000000000L; // 1L << 62
	public static final long CAPTURED       = 0x8000000000000000L; // 1L << 63; sign
                                                                   // bit speeds
																   // tests
    // HEX Annotation
    public static final long INITIAL_TOP	= 0x0001ff7fd4a00000L;
    public static final long INITIAL_BOT	= 0x000000000a57fdffL;
    public static final long TOP_ROW		= 0x0001ff0000000000L;
    public static final long BOTTOM_ROW		= 0x00000000000001ffL;
    public static final long LEFT_COL		= 0x0001004010040100L;
    public static final long RIGHT_COL		= 0x0000010040100401L;
    public static final long DIAGONAL		= 0x0001552a9552a955L;
    public static final long ON_BOARD       = 0x0001ff7fdff7fdffL;
    public static final long CENTER		    = 0x0000000007c00000L;

	// OCTAL Annotation (no longer used)
	public static final long INITIAL_BOT_8 = 000000001225776777L;
	public static final long INITIAL_TOP_8 = 017767772450000000L;
	public static final long TOP_ROW_8 = 017760000000000000L;
	public static final long BOTTOM_ROW_8 = 000000000000000777L;
	public static final long LEFT_COL_8 = 010004002001000400L;
	public static final long RIGHT_COL_8 = 000020010004002001L;
	public static final long DIAGONAL_8 = 012522522524524525L;
	public static final long ON_BOARD_8 = 017767773775776777L;
	public static final long CENTER_8 = 000000000760000000L;


	// turn screen coordinates into bit position
	public static final long at(int row, int col) {
		return 1L << (10 * (4 - row)) + (8 - col);
	}

	// isolate one of the bits from a bitboard
	public static final long lastBit(long bitboard) {
		return bitboard & -bitboard;
	}

	// how much to shift from coordinates
	public static final int SHIFT_VERTICAL = 10;
	public static final int SHIFT_HORIZONTAL = 1;
	public static final int SHIFT_SLANT = 11;
	public static final int SHIFT_BACKSLANT = 9;

	// count number of set bits in a word
	static final long ONES = 0x5555555555555555L; // A series of 0101 0101 0101 on all 64 bits
	static final long TWOS = 0x3333333333333333L; // A series of 0011 0011 0011 on all 64 bits
	static final int FOURS = 0x0f0f0f0f; // A series 0f 0000 1111 0000 1111 on only last 32 bits

	public static final int count(long set) {
		set -= (set >>> 1) & ONES;
		set = (set & TWOS) + ((set >>> 2) & TWOS);
		int result = (int) set + (int) (set >>> 32);
		return (((result & FOURS) + ((result >>> 4) & FOURS)) * 0x01010101) >>> 24;
	}
}
