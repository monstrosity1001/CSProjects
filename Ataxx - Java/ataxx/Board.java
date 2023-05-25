/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Formatter;

import java.util.function.Consumer;

import static ataxx.PieceColor.*;
import static ataxx.GameException.error;

/** An Ataxx board.   The squares are labeled by column (a char value between
 *  'a' - 2 and 'g' + 2) and row (a char value between '1' - 2 and '7'
 *  + 2) or by linearized index, an integer described below.  Values of
 *  the column outside 'a' and 'g' and of the row outside '1' to '7' denote
 *  two layers of border squares, which are always blocked.
 *  This artificial border (which is never actually printed) is a common
 *  trick that allows one to avoid testing for edge conditions.
 *  For example, to look at all the possible moves from a square, sq,
 *  on the normal board (i.e., not in the border region), one can simply
 *  look at all squares within two rows and columns of sq without worrying
 *  about going off the board. Since squares in the border region are
 *  blocked, the normal logic that prevents moving to a blocked square
 *  will apply.
 *
 *  For some purposes, it is useful to refer to squares using a single
 *  integer, which we call its "linearized index".  This is simply the
 *  number of the square in row-major order (counting from 0).
 *
 *  Moves on this board are denoted by Moves.
 *  @author Varun MIttal
 */
class Board {

    /** Number of squares on a side of the board. */
    static final int SIDE = Move.SIDE;

    /** Length of a side + an artificial 2-deep border region.
     * This is unrelated to a move that is an "extend". */
    static final int EXTENDED_SIDE = Move.EXTENDED_SIDE;

    /** Number of consecutive non-extending moves before game ends. */
    static final int JUMP_LIMIT = 25;

    /** A new, cleared board in the initial configuration. */
    Board() {
        _board = new PieceColor[EXTENDED_SIDE * EXTENDED_SIDE];
        _undoSquares = new Stack<Integer>();
        _undoPieces = new Stack<PieceColor>();
        _undoJump = new Stack<>();
        _whoseMove = RED;
        _numJumps = 0;
        _numPieces[2] = 2;
        _numPieces[3] = 2;
        setNotifier(NOP);
        clear();

    }

    /** A board whose initial contents are copied from BOARD0, but whose
     *  undo history is clear, and whose notifier does nothing. */
    Board(Board board0) {
        _board = board0._board.clone();
        _undoSquares = new Stack<Integer>();
        _undoPieces = new Stack<PieceColor>();
        _undoJump = new Stack<>();
        _allMoves = new ArrayList<Move>();
        _whoseMove = RED;
        _numJumps = 0;
        _numPieces[2] = 2;
        _numPieces[3] = 2;
        setNotifier(NOP);

    }

    /** Return the linearized index of square COL ROW. */
    static int index(char col, char row) {
        return (row - '1' + 2) * EXTENDED_SIDE + (col - 'a' + 2);
    }

    /** Return the linearized index of the square that is DC columns and DR
     *  rows away from the square with index SQ. */
    static int neighbor(int sq, int dc, int dr) {
        return sq + dc + dr * EXTENDED_SIDE;
    }

    /** Clear me to my starting state, with pieces in their initial
     *  positions and no blocks. */
    void clear() {
        _whoseMove = RED;
        _allMoves = new ArrayList<Move>();
        for (int indexUsed = 0; indexUsed < _board.length; indexUsed++) {

            if (indexUsed < index('a', '1')) {
                _board[indexUsed] = BLOCKED;
            } else if (indexUsed > index('g', '7')) {
                _board[indexUsed] = BLOCKED;
            } else if (indexUsed == index('g', '7')
                    || indexUsed == index('a', '1')) {
                _board[indexUsed] = BLUE;
            } else if (indexUsed == index('g', '1')
                    || indexUsed == index('a', '7')) {
                _board[indexUsed] = RED;
            } else if (indexUsed % 11 < 2 || indexUsed % 11 > 8) {
                _board[indexUsed] = BLOCKED;
            } else {
                _board[indexUsed] = EMPTY;
            }
        }
        announce();
    }

    /** Return the winner, if there is one yet, and otherwise null.  Returns
     *  EMPTY in the case of a draw, which can happen as a result of there
     *  having been MAX_JUMPS consecutive jumps without intervening extends,
     *  or if neither player can move and both have the same number of pieces.*/
    PieceColor getWinner() {
        if (redPieces() == 0) {
            return BLUE;
        } else if (bluePieces() == 0) {
            return RED;
        } else if (!canMove(RED) && !canMove(BLUE)) {
            return winnerHelper();
        } else if (numJumps() == JUMP_LIMIT) {
            return winnerHelper();
        }
        return null;
    }

    PieceColor winnerHelper() {
        int numRed = redPieces();
        int numBlue = bluePieces();
        if (numRed == numBlue) {
            return EMPTY;
        } else if (numBlue > numRed) {
            return BLUE;
        } else {
            return RED;
        }
    }

    /** Return number of red pieces on the board. */
    int redPieces() {
        int returner = 0;
        for (PieceColor pieceColor : _board) {
            if (pieceColor == RED) {
                returner++;
            }
        }
        return returner;
    }

    /** Return number of blue pieces on the board. */
    int bluePieces() {
        int returner = 0;
        for (PieceColor pieceColor : _board) {
            if (pieceColor == BLUE) {
                returner++;
            }
        }
        return returner;
    }

    /** Return number of COLOR pieces on the board. */
    int numPieces(PieceColor color) {
        return _numPieces[color.ordinal()];
    }

    /** Increment numPieces(COLOR) by K. */
    private void incrPieces(PieceColor color, int k) {
        _numPieces[color.ordinal()] += k;
    }

    /** The current contents of square CR, where 'a'-2 <= C <= 'g'+2, and
     *  '1'-2 <= R <= '7'+2.  Squares outside the range a1-g7 are all
     *  BLOCKED.  Returns the same value as get(index(C, R)). */
    PieceColor get(char c, char r) {
        return _board[index(c, r)];
    }

    /** Return the current contents of square with linearized index SQ. */
    PieceColor get(int sq) {
        return _board[sq];
    }

    /** Set get(C, R) to V, where 'a' <= C <= 'g', and
     *  '1' <= R <= '7'. This operation is undoable. */
    private void set(char c, char r, PieceColor v) {
        set(index(c, r), v);
    }

    /** Set square with linearized index SQ to V.  This operation is
     *  undoable. */
    private void set(int sq, PieceColor v) {
        addUndo(sq);
        _board[sq] = v;
    }

    /** Set square at C R to V (not undoable). This is used for changing
     * contents of the board without updating the undo stacks. */
    private void unrecordedSet(char c, char r, PieceColor v) {
        _board[index(c, r)] = v;
    }

    /** Set square at linearized index SQ to V (not undoable). This is used
     * for changing contents of the board without updating the undo stacks. */
    private void unrecordedSet(int sq, PieceColor v) {
        _board[sq] = v;
    }

    /** Return true iff MOVE is legal on the current board. */
    boolean legalMove(Move move) {
        if (move == null) {
            return false;
        } else if (move.isPass() || move.toString() == "-") {
            return  !canMove(whoseMove());
        } else if ((Math.abs(move.row1() - move.row0()) <= 2)
                && (Math.abs(move.col1() - move.col0()) <= 2)) {
            if (get(move.col1(), move.row1()).equals(EMPTY)) {
                return get(move.col0(), move.row0()) == whoseMove();
            }
        }

        return false;
    }

    /** Return true iff C0 R0 - C1 R1 is legal on the current board. */
    boolean legalMove(char c0, char r0, char c1, char r1) {
        return legalMove(Move.move(c0, r0, c1, r1));
    }

    /** Return true iff player WHO can move, ignoring whether it is
     *  that player's move and whether the game is over. */
    boolean canMove(PieceColor who) {
        for (char currentCol = 'a'; currentCol < ('g' + 1); currentCol++) {
            for (char currentRow = '1'; currentRow < ('7' + 1); currentRow++) {
                if (get(currentCol, currentRow).equals(who)) {
                    for (int addCol = -2; addCol < 3; addCol++) {
                        for (int addRow = -2; addRow < 3; addRow++) {
                            if (get((char) (currentCol + addCol),
                                (char) (currentRow + addRow)).equals(EMPTY)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /** Return the color of the player who has the next move.  The
     *  value is arbitrary if the game is over. */
    PieceColor whoseMove() {
        return _whoseMove;
    }

    /** Return total number of moves and passes since the last
     *  clear or the creation of the board. */
    int numMoves() {
        return _allMoves.size();
    }

    /** Return number of non-pass moves made in the current game since the
     *  last extend move added a piece to the board (or since the
     *  start of the game). Used to detect end-of-game. */
    int numJumps() {
        return _numJumps;
    }

    /** Assuming MOVE has the format "-" or "C0R0-C1R1", make the denoted
     *  move ("-" means "pass"). */
    void makeMove(String move) {
        if (move.equals("-")) {
            makeMove(Move.pass());
        } else {
            makeMove(Move.move(move.charAt(0),
                    move.charAt(1), move.charAt(3), move.charAt(4)));
        }
    }

    /** Perform the move C0R0-C1R1, or pass if C0 is '-'.  For moves
     *  other than pass, assumes that legalMove(C0, R0, C1, R1). */
    void makeMove(char c0, char r0, char c1, char r1) {
        if (c0 == '-') {
            makeMove(Move.pass());
        } else {
            makeMove(Move.move(c0, r0, c1, r1));
        }
    }

    /** Make the MOVE on this Board, assuming it is legal. */
    void makeMove(Move move) {
        if (!legalMove(move)) {
            throw error("Illegal move: %s", move);
        }
        if (move.isPass()) {
            pass();
            return;
        }
        _allMoves.add(move);
        startUndo();
        PieceColor opponent = _whoseMove.opposite();

        int moveIndex = index(move.col1(), move.row1());
        int indexToIncrement = 3;
        int otherIndexToChange = 2;
        if (whoseMove().equals(RED)) {
            indexToIncrement = 2;
            otherIndexToChange = 3;
        }
        addUndo(index(move.col1(), move.row1()));
        addUndo(index(move.col0(), move.row0()));
        if (move.isExtend()) {
            _numJumps = 0;
            _board[moveIndex] = whoseMove();
            _numPieces[indexToIncrement]++;
            for (char usedCol = (char) (move.col1() - 1);
                 usedCol <= (char) (move.col1() + 1); usedCol++) {
                for (char usedRow = (char) (move.row1() - 1);
                     usedRow <= (char) (move.row1() + 1); usedRow++) {
                    int indexLookingAt = index(usedCol, usedRow);
                    if (_board[indexLookingAt] == opponent) {
                        addUndo(indexLookingAt);
                        _board[indexLookingAt] = whoseMove();
                        _numPieces[indexToIncrement]++;
                        _numPieces[otherIndexToChange]--;
                    }
                }
            }
        } else {
            _numJumps++;
            _undoJump.add(true);
            _board[moveIndex] = whoseMove();
            _board[index(move.col0(), move.row0())] = EMPTY;
            for (char usedCol = (char) (move.col1() - 1);
                 usedCol <= (char) (move.col1() + 1); usedCol++) {
                for (char usedRow = (char) (move.row1() - 1);
                     usedRow <= (char) (move.row1() + 1);  usedRow++) {
                    int indexLookingAt = index(usedCol, usedRow);
                    if (_board[indexLookingAt] == opponent) {
                        addUndo(indexLookingAt);
                        _board[indexLookingAt] = whoseMove();
                        _numPieces[indexToIncrement]++;
                        _numPieces[otherIndexToChange]--;
                    }
                }
            }
        }
        _whoseMove = opponent;
        announce();
    }

    /** Update to indicate that the current player passes, assuming it
     *  is legal to do so. Passing is undoable. */
    void pass() {
        assert !canMove(_whoseMove);
        _whoseMove = _whoseMove.opposite();
        announce();
    }

    int size() {
        return _board.length;
    }

    /** Undo the last move. */
    void undo() {
        while (true) {
            int indexToChange = _undoSquares.pop();
            PieceColor changeSquareTo = _undoPieces.pop();
            Boolean seeJump = _undoJump.pop();
            if (indexToChange == -1) {
                break;
            } else {
                _board[indexToChange] = changeSquareTo;
            }
            if (seeJump) {
                _numJumps--;
            }
        }
        _whoseMove = _whoseMove.opposite();
        _allMoves.remove(_allMoves.size() - 1);
        _winner = null;
        announce();
    }

    /** Indicate beginning of a move in the undo stack. See the
     * _undoSquares and _undoPieces instance variable comments for
     * details on how the beginning of moves are marked. */
    private void startUndo() {
        _undoSquares.add(-1);
        _undoPieces.add(null);
        _undoJump.add(false);
    }

    /** Add an undo action for changing SQ on current board. */
    private void addUndo(int sq) {
        _undoSquares.add(sq);
        _undoPieces.add(_board[sq]);
        _undoJump.add(false);
    }

    /** Return true iff it is legal to place a block at C R. */
    boolean legalBlock(char c, char r) {
        if (allMoves().size() != 0) {
            return false;
        } else {
            return get(c, r) == EMPTY;
        }
    }


    /** Return true iff it is legal to place a block at CR. */
    boolean legalBlock(String cr) {
        return legalBlock(cr.charAt(0), cr.charAt(1));
    }

    /** Set a block on the square C R and its reflections across the middle
     *  row and/or column, if that square is unoccupied and not
     *  in one of the corners. Has no effect if any of the squares is
     *  already occupied by a block.  It is an error to place a block on a
     *  piece. */
    void setBlock(char c, char r) {
        if (!legalBlock(c, r)) {
            throw error("illegal block placement");
        }

        int originalIndex = index(c, r);
        _board[originalIndex] = BLOCKED;

        int diagonalDifference = originalIndex - ((_board.length - 1) / 2);
        int diagonalIndex = ((_board.length - 1) / 2) - diagonalDifference;
        _board[diagonalIndex] = BLOCKED;

        int howFarAlong = originalIndex % 11;
        int horizontalReflector = (((_board.length - 1) / 2) - 5) + howFarAlong;
        int horizontalDifference = originalIndex - horizontalReflector;
        int horizontalIndex = horizontalReflector - horizontalDifference;
        _board[horizontalIndex] = BLOCKED;

        int verticalDiff = (howFarAlong - 5) * 2;
        int verticalIndex = originalIndex - verticalDiff;
        _board[verticalIndex] = BLOCKED;


        if (!canMove(RED) && !canMove(BLUE)) {
            _winner = EMPTY;
        }

        announce();
    }

    /** Place a block at CR. */
    void setBlock(String cr) {
        setBlock(cr.charAt(0), cr.charAt(1));
    }

    /** Return total number of unblocked squares. */
    int totalOpen() {
        int returner = 0;
        for (int index = 0; index < _board.length; index++) {
            if (_board[index] != BLOCKED) {
                returner++;
            }
        }
        return returner;
    }

    /** Return a list of all moves made since the last clear (or start of
     *  game). */
    List<Move> allMoves() {
        return _allMoves;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Board)) {
            return false;
        }
        Board other = (Board) obj;
        return Arrays.equals(_board, other._board);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(_board);
    }

    /** Return a text depiction of the board.  If LEGEND, supply row and
     *  column numbers around the edges. */
    String toString(boolean legend) {
        Formatter out = new Formatter();
        for (char r = '7'; r >= '1'; r -= 1) {
            if (legend) {
                out.format("%c", r);
            }
            out.format(" ");
            for (char c = 'a'; c <= 'g'; c += 1) {
                switch (get(c, r)) {
                case RED:
                    out.format(" r");
                    break;
                case BLUE:
                    out.format(" b");
                    break;
                case BLOCKED:
                    out.format(" X");
                    break;
                case EMPTY:
                    out.format(" -");
                    break;
                default:
                    break;
                }
            }
            out.format("%n");
        }
        if (legend) {
            out.format("   a b c d e f g");
        }
        return out.toString();
    }

    /** Set my notifier to NOTIFY. */
    public void setNotifier(Consumer<Board> notify) {
        _notifier = notify;
        announce();
    }

    /** Take any action that has been set for a change in my state. */
    private void announce() {
        _notifier.accept(this);
    }

    /** A notifier that does nothing. */
    private static final Consumer<Board> NOP = (s) -> { };

    /** Use _notifier.accept(this) to announce changes to this board. */
    private Consumer<Board> _notifier;

    /** For reasons of efficiency in copying the board,
     *  we use a 1D array to represent it, using the usual access
     *  algorithm: row r, column c => index(r, c).
     *
     *  Next, instead of using a 7x7 board, we use an 11x11 board in
     *  which the outer two rows and columns are blocks, and
     *  row 2, column 2 actually represents row 0, column 0
     *  of the real board.  As a result of this trick, there is no
     *  need to special-case being near the edge: we don't move
     *  off the edge because it looks blocked.
     *
     *  Using characters as indices, it follows that if 'a' <= c <= 'g'
     *  and '1' <= r <= '7', then row r, column c of the board corresponds
     *  to _board[(c -'a' + 2) + 11 (r - '1' + 2) ]. */
    private final PieceColor[] _board;

    /** Player that is next to move. */
    private PieceColor _whoseMove;

    /** Number of consecutive non-extending moves since the
     *  last clear or the beginning of the game. */
    private int _numJumps;

    /** Total number of unblocked squares. */
    private int _totalOpen;

    /** Number of blue and red pieces, indexed by the ordinal positions of
     *  enumerals BLUE and RED. */
    private int[] _numPieces = new int[BLUE.ordinal() + 1];

    /** Set to winner when game ends (EMPTY if tie).  Otherwise is null. */
    private PieceColor _winner;

    /** List of all (non-undone) moves since the last clear or beginning of
     *  the game. */
    private ArrayList<Move> _allMoves;

    /* The undo stack. We keep a stack of squares that have changed and
     * their previous contents.  Any given move may involve several such
     * changes, so we mark the start of the changes for each move (including
     * passes) with a null. */

    /** Stack of linearized indices of squares that have been modified and
     *  not undone. Nulls mark the beginnings of full moves. */
    private Stack<Integer> _undoSquares;
    /** Stack of pieces formally at corresponding squares in _UNDOSQUARES. */
    private Stack<PieceColor> _undoPieces;

    /** Keeps track of when we undo a jump. */
    private Stack<Boolean> _undoJump;

    void whoseMoveSetter(PieceColor color) {
        _whoseMove = color;
    }

    /** Checking if one player cannot move. */
    private boolean _oneCantMove;
}
