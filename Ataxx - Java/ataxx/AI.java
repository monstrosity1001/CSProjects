/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.ArrayList;
import java.util.Random;

import static ataxx.PieceColor.*;
import static java.lang.Math.min;
import static java.lang.Math.max;

/** A Player that computes its own moves.
 *  @author Varun Mittal
 */
class AI extends Player {

    /**
     * Maximum minimax search depth before going to static evaluation.
     */
    private static final int MAX_DEPTH = 4;
    /**
     * A position magnitude indicating a win (for red if positive, blue
     * if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     * a random-number generator for use in move computations.  Identical
     * seeds produce identical behaviour.
     */
    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(getBoard());
        _aiColor = myColor();
        b.whoseMoveSetter(_aiColor);
        minMaxHelper(b, MAX_DEPTH, true,  -INFTY, INFTY);
        return _bestMove;
    }

    private int minMaxHelper(Board board, int depth, boolean isAiPlayer,
                             int alpha, int beta) {

        ArrayList<Move> legalMoveList = getLegalMoves(board);
        int highestMoveMade = -INFTY;
        int lowestMoveMade = INFTY;

        if (depth == 0 || board.getWinner() != null
                || legalMoveList.isEmpty()) {
            return staticScore(board, WINNING_VALUE + depth);
        }

        if (isAiPlayer) {
            for (Move moveUsed : legalMoveList) {
                if (board.legalMove(moveUsed)) {
                    board.makeMove(moveUsed);
                    int moveMade = minMaxHelper(board, depth - 1,
                            false, alpha, beta);
                    if (moveMade >= highestMoveMade) {
                        _bestMove = moveUsed;
                        highestMoveMade = moveMade;
                    }
                    board.undo();
                    alpha = max(alpha, moveMade);
                    if (alpha > beta) {
                        break;
                    }
                }
            }
            return highestMoveMade;
        } else {
            for (Move moveUsed : legalMoveList) {
                board.makeMove(moveUsed);
                int moveMade = minMaxHelper(board, depth - 1,
                        true, alpha, beta);
                lowestMoveMade = min(lowestMoveMade, moveMade);
                beta = min(beta, moveMade);
                board.undo();
                if (alpha > beta) {
                    break;
                }

            }
            return lowestMoveMade;
        }
    }


    private ArrayList<Move> getLegalMoves(Board board) {
        ArrayList<Move> returner = new ArrayList<Move>();

        for (char colIndex = 'a'; colIndex < ('g' + 1); colIndex++) {
            for (char rowIndex = '1'; rowIndex < ('7' + 1); rowIndex++) {
                if (board.get(colIndex, rowIndex).equals(myColor())) {
                    returner.addAll(legalMoveHelper(colIndex, rowIndex, board));
                }
            }
        }

        return returner;
    }

    private ArrayList<Move> legalMoveHelper(char colIndex, char rowIndex,
                                            Board board) {
        ArrayList<Move> returner = new ArrayList<>();
        for (int addCol = -2; addCol < 3; addCol++) {
            for (int addRow = -2; addRow < 3; addRow++) {
                char newCol = (char) (colIndex + addCol);
                char newRow = (char) (rowIndex + addRow);
                Move tempMove = Move.move(colIndex, rowIndex, newCol, newRow);
                if (board.legalMove(tempMove)) {
                    returner.add(tempMove);
                }
            }
        }
        return returner;
    }

    /** Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     *  won positions, and 0 for ties. */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner != null) {
            return switch (winner) {
            case RED -> winningValue;
            case BLUE -> -winningValue;
            default -> 0;
            };
        } else {
            if (_aiColor.equals(BLUE)) {
                return board.bluePieces() - board.redPieces();
            } else {
                return board.redPieces() - board.bluePieces();
            }
        }
    }

    /** Pseudo-random number generator for move computation. */
    private Random _random = new Random();

    /** Helps to keep track of what the colour of the AI is. */
    private PieceColor _aiColor;

    /** Keeps track of what the best possible move is. */
    private Move _bestMove;
}
