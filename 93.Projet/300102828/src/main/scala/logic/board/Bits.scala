package logic.board

/**
  * Created by to-boreal on 19-04-17.
  */
class Bits {

  val IS_WHITE: Long = 0x4000000000000000L
  val CAPTURED: Long = 0x8000000000000000L

  val  INITIAL_BOT: Long = 0x0000000011103757211L
  val INITIAL_TOP: Long = 0x0103300311117L
  val TOP_ROW: Long = 0x0770612007315L
  val BOTTOM_ROW: Long = 0x0000000000000002603L
  val LEFT_COL: Long = 0x0434251307146L
  val RIGHT_COL: Long = 0x0000443136113432L
  val DIAGONAL: Long = 0x0543722537261L
  val ON_BOARD: Long = 0x0770773106426L
  val CENTER: Long = 0x0000000001140000000L
  def at ( row: Int ,col: Int ) {
    return 1L << (10 * (4-row)) +(8 - col);
  }
  def lastBit(bitboard: Long) {
    return bitboard & - bitboard
  }
  val SHIFT_VERTICAL: Int = 10
  val SHIFT_HORIZONTAL: Int = 1
  val SHIFT_SLAN: Int = 11
  val SHIFT_BACKSLANT: Int = 9
  val ONES: Long = 0x5555555555555555L
  val TWOS: Long = 0x3333333333333333L
  val  FOURS: Int = 0x0f0f0f0f
  def count( set: Long) {
    set -= (set >>> 1) & ONES
    set = (set & TWOS) + ((set >>> 2) & TWOS)
    int result = (int) set + (int) (set >>> 32)
    return (((result & FOURS) + ((result >>> 4) & FOURS)) * 0x01010101) >>> 24
  }


}


