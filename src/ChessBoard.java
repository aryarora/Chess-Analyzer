import java.util.ArrayList;
import java.util.List;

public class ChessBoard {
    private char[][] board = new char[8][8];
    //whose turn it is
    private boolean whiteTurn = true;

    //castling rights: {white king side, white queen side,
    //black king side, black queen side}
    private boolean[] castling = {true, true, true, true};

    //square a pawn could capture onto en passant right now (row -1 = none)
    private int epRow = -1;
    private int epCol = -1;

    public ChessBoard() {
        initializeBoard();
    }

    //in chess notation n = knight since k already is for king
    public void initializeBoard() {
        char[] pieces = {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'};
        //lowercase letters = black's pieces
        for (int i = 0; i < 8; i++) {
            board[0][i] = pieces[i];
            board[1][i] = 'p';
            board[6][i] = 'P';
            board[7][i] = Character.toUpperCase(pieces[i]);
        }

        for (int i = 2; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = '.';
            }
        }

        //full castling rights, no en passant square
        castling = new boolean[] {true, true, true, true};
        epRow = -1;
        epCol = -1;
    }

    public void printBoard() {
        //row labels
        for (int i = 0; i < 8; i++) {
            System.out.print((8 - i) + " ");

            for (int j = 0; j < 8; j++) {
                System.out.print(board[i][j] + " ");
            }

            System.out.println();
        }
        //column labels
        System.out.println("  a b c d e f g h");
    }

    //loads a position from FEN notation, e.g.
    //"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    //uses the piece placement, side to move, castling rights, and en passant
    //square (a missing castling/en passant field means none), the move
    //counters are ignored
    //throws IllegalArgumentException (and leaves the board unchanged) if invalid
    public void loadFEN(String fen) {
        String[] parts = fen.trim().split("\\s+");
        //FEN lists rank 8 first, which matches board[0]
        String[] ranks = parts[0].split("/");

        if (ranks.length != 8) {
            throw new IllegalArgumentException("FEN must have 8 ranks");
        }

        //build on a temporary board so a bad FEN doesn't corrupt this one
        char[][] newBoard = new char[8][8];

        for (int row = 0; row < 8; row++) {
            int col = 0;

            for (char c : ranks[row].toCharArray()) {
                if (Character.isDigit(c)) {
                    //a digit means that many empty squares
                    int empty = c - '0';
                    if (empty < 1 || empty > 8 || col + empty > 8) {
                        throw new IllegalArgumentException(
                            "Bad empty-square count in rank " + (8 - row));
                    }
                    for (int k = 0; k < empty; k++) {
                        newBoard[row][col++] = '.';
                    }
                } else if ("rnbqkpRNBQKP".indexOf(c) != -1) {
                    if (col >= 8) {
                        throw new IllegalArgumentException(
                            "Too many squares in rank " + (8 - row));
                    }
                    newBoard[row][col++] = c;
                } else {
                    throw new IllegalArgumentException(
                        "Invalid character '" + c + "' in FEN");
                }
            }

            if (col != 8) {
                throw new IllegalArgumentException(
                    "Rank " + (8 - row) + " doesn't have 8 squares");
            }
        }

        //side to move defaults to white if left out
        boolean white = true;
        if (parts.length > 1) {
            if (parts[1].equals("w")) {
                white = true;
            } else if (parts[1].equals("b")) {
                white = false;
            } else {
                throw new IllegalArgumentException(
                    "Side to move must be 'w' or 'b'");
            }
        }

        //castling rights, "-" or any of KQkq
        boolean[] newCastling = new boolean[4];
        if (parts.length > 2 && !parts[2].equals("-")) {
            for (char c : parts[2].toCharArray()) {
                int index = "KQkq".indexOf(c);
                if (index == -1) {
                    throw new IllegalArgumentException(
                        "Invalid castling field '" + parts[2] + "'");
                }
                newCastling[index] = true;
            }
        }

        //en passant square, "-" or something like "e3"
        int newEpRow = -1;
        int newEpCol = -1;
        if (parts.length > 3 && !parts[3].equals("-")) {
            String ep = parts[3];
            if (ep.length() != 2 || ep.charAt(0) < 'a' || ep.charAt(0) > 'h' ||
                (ep.charAt(1) != '3' && ep.charAt(1) != '6')) {
                throw new IllegalArgumentException(
                    "Invalid en passant square '" + ep + "'");
            }
            newEpCol = ep.charAt(0) - 'a';
            newEpRow = 8 - (ep.charAt(1) - '0');
        }

        board = newBoard;
        whiteTurn = white;
        castling = newCastling;
        epRow = newEpRow;
        epCol = newEpCol;
    }

    //exports the current position as FEN
    //the move counters aren't tracked, so they are written as 0 1
    public String getFEN() {
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < 8; row++) {
            int empty = 0;

            for (int col = 0; col < 8; col++) {
                if (board[row][col] == '.') {
                    empty++;
                } else {
                    if (empty > 0) {
                        sb.append(empty);
                        empty = 0;
                    }
                    sb.append(board[row][col]);
                }
            }

            if (empty > 0) {
                sb.append(empty);
            }
            if (row < 7) {
                sb.append('/');
            }
        }

        sb.append(whiteTurn ? " w" : " b");

        sb.append(' ');
        String rights = (castling[0] ? "K" : "") + (castling[1] ? "Q" : "") +
            (castling[2] ? "k" : "") + (castling[3] ? "q" : "");
        sb.append(rights.isEmpty() ? "-" : rights);

        sb.append(' ');
        if (epRow == -1) {
            sb.append('-');
        } else {
            sb.append((char) ('a' + epCol)).append(8 - epRow);
        }

        sb.append(" 0 1");
        return sb.toString();
    }

    public boolean isInsideBoard(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    //checks if a path is clear (for queen, rooks, bishops)
    public boolean isPathClear(int startRow, int startCol,
                               int endRow, int endCol) {
        int rowChange = Integer.compare(endRow, startRow);
        int colChange = Integer.compare(endCol, startCol);

        int row = startRow + rowChange;
        int col = startCol + colChange;

        while (row != endRow || col != endCol) {
            if (board[row][col] != '.') {
                return false;
            }

            row += rowChange;
            col += colChange;
        }

        return true;
    }

    //checks if a piece is white
    public boolean isWhitePiece(char piece) {
        return piece != '.' && Character.isUpperCase(piece);
    }

    //checks that a move follows the movement rules of the piece
    //(does NOT consider turns or whether it leaves your king in check,
    //use isLegalMove for that)
    public boolean isValidMove(int startRow, int startCol,
                               int endRow, int endCol) {
        if (!isInsideBoard(startRow, startCol) ||
            !isInsideBoard(endRow, endCol)) {
            return false;
        }
        //if startPos = endPos return false
        if (startRow == endRow && startCol == endCol) {
            return false;
        }

        char piece = board[startRow][startCol];
        char destination = board[endRow][endCol];

        if (piece == '.') {
            return false;
        }

        // Cannot capture your own pieces
        if (destination != '.' && isWhitePiece(piece) == isWhitePiece(destination)) {
            return false;
        }

        // Kings are never captured, they can only be put in check
        if (Character.toLowerCase(destination) == 'k') {
            return false;
        }

        //kings move two squares sideways to castle
        if (Character.toLowerCase(piece) == 'k' && startRow == endRow &&
            Math.abs(endCol - startCol) == 2) {
            return isValidCastle(startRow, startCol, endCol);
        }

        //pawns are the only piece whose movement differs from how they attack
        if (Character.toLowerCase(piece) == 'p') {
            return isValidPawnMove(startRow, startCol, endRow, endCol);
        }

        //every other piece moves the same way it attacks
        return canAttack(startRow, startCol, endRow, endCol);
    }

    //pawns move forward but capture diagonally
    private boolean isValidPawnMove(int startRow, int startCol,
                                    int endRow, int endCol) {
        char piece = board[startRow][startCol];
        char destination = board[endRow][endCol];

        //establishing the direction they can move in (white moves up the array)
        boolean white = isWhitePiece(piece);
        int direction = white ? -1 : 1;
        int startingRow = white ? 6 : 1;

        int rowChange = endRow - startRow;
        int colChange = endCol - startCol;

        //they cannot capture by moving forward
        if (colChange == 0) {
            if (destination != '.') {
                return false;
            }
            //1 square move forward
            if (rowChange == direction) {
                return true;
            }
            //at the starting position, pawns can move 2 squares
            return startRow == startingRow &&
                rowChange == 2 * direction &&
                board[startRow + direction][startCol] == '.';
        }

        //diagonal move is only allowed when capturing (en passant included)
        if (!canAttack(startRow, startCol, endRow, endCol)) {
            return false;
        }
        if (destination != '.') {
            return true;
        }

        //en passant: capture onto the square the enemy pawn just skipped over
        return endRow == epRow && endCol == epCol &&
            endRow == (white ? 2 : 5) &&
            board[startRow][endCol] == (white ? 'p' : 'P');
    }

    //castling needs the rights, the rook in its corner, and nothing in between
    //(not castling out of, through, or into check is handled in isSafeMove)
    private boolean isValidCastle(int row, int startCol, int endCol) {
        boolean white = isWhitePiece(board[row][startCol]);
        int homeRow = white ? 7 : 0;

        if (row != homeRow || startCol != 4) {
            return false;
        }

        boolean kingSide = endCol > startCol;
        int rightsIndex = (white ? 0 : 2) + (kingSide ? 0 : 1);
        if (!castling[rightsIndex]) {
            return false;
        }

        int rookCol = kingSide ? 7 : 0;
        if (board[row][rookCol] != (white ? 'R' : 'r')) {
            return false;
        }

        return isPathClear(row, startCol, row, rookCol);
    }

    //checks if the piece at the start square attacks the end square
    //(ignores what is on the end square, which is why pawns attack
    //diagonally here even if the square is empty)
    private boolean canAttack(int startRow, int startCol,
                              int endRow, int endCol) {
        char piece = board[startRow][startCol];

        int rowChange = endRow - startRow;
        int colChange = endCol - startCol;

        switch (Character.toLowerCase(piece)) {
            //diagonal attack like a bishop but only 1 square
            //and only ahead (for the respective color)
            case 'p':
                int direction = isWhitePiece(piece) ? -1 : 1;
                return rowChange == direction &&
                    Math.abs(colChange) == 1;
            //bishop, rook, and queen need a path clear check
            //check if its only moving left/right or up/down
            case 'r':
                return (rowChange == 0 || colChange == 0) &&
                    isPathClear(startRow, startCol, endRow, endCol);
            //check if its only moving diagonally
            case 'b':
                return Math.abs(rowChange) == Math.abs(colChange) &&
                    isPathClear(startRow, startCol, endRow, endCol);
            //check if its either a bishop/rook direction
            case 'q':
                return (rowChange == 0 || colChange == 0 ||
                        Math.abs(rowChange) == Math.abs(colChange)) &&
                    isPathClear(startRow, startCol, endRow, endCol);
            case 'n':
                return (Math.abs(rowChange) == 2 &&
                        Math.abs(colChange) == 1) ||
                    (Math.abs(rowChange) == 1 &&
                        Math.abs(colChange) == 2);
            //can only move 1 square
            case 'k':
                return Math.abs(rowChange) <= 1 &&
                    Math.abs(colChange) <= 1;

            default:
                return false;
        }
    }

    //checks if the king is in check
    public boolean isKingInCheck(boolean white) {
        //depending on which king we are looking to see in check, it makes
        // char king capital/lowercase for white/black
        char king = white ? 'K' : 'k';
        //placeholder
        int kingRow = -1;
        int kingCol = -1;

        // Find the king
        search:
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == king) {
                    kingRow = i;
                    kingCol = j;
                    break search;
                }
            }
        }
        //no king on the board means nothing to be in check
        if (kingRow == -1) {
            return false;
        }

        // Check whether an opponent's piece can attack the king
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                char piece = board[i][j];

                if (piece != '.' && isWhitePiece(piece) != white) {
                    if (canAttack(i, j, kingRow, kingCol)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    //a move is legal if it follows the piece's rules, it's that color's turn,
    //and it doesn't leave your own king in check
    public boolean isLegalMove(int startRow, int startCol,
                               int endRow, int endCol) {
        if (!isValidMove(startRow, startCol, endRow, endCol)) {
            return false;
        }

        //can't move out of turn
        if (isWhitePiece(board[startRow][startCol]) != whiteTurn) {
            return false;
        }

        return isSafeMove(startRow, startCol, endRow, endCol);
    }

    //assumes the move already follows the piece's rules (isValidMove)
    //tries the move, sees if your own king is safe, then always undoes it
    //(ignores turns so it can be used for either color)
    private boolean isSafeMove(int startRow, int startCol,
                               int endRow, int endCol) {
        char piece = board[startRow][startCol];
        boolean white = isWhitePiece(piece);

        //castling isn't allowed out of check or through an attacked square
        if (Character.toLowerCase(piece) == 'k' &&
            Math.abs(endCol - startCol) == 2) {
            if (isKingInCheck(white)) {
                return false;
            }

            //test the king standing on the square it passes over
            int midCol = (startCol + endCol) / 2;
            board[startRow][midCol] = piece;
            board[startRow][startCol] = '.';
            boolean passesThroughCheck = isKingInCheck(white);
            board[startRow][startCol] = piece;
            board[startRow][midCol] = '.';

            if (passesThroughCheck) {
                return false;
            }
        }

        int[] move = {startRow, startCol, endRow, endCol};
        Undo undo = makeMove(move);
        boolean leavesKingInCheck = isKingInCheck(white);
        undoMove(move, undo);

        return !leavesKingInCheck;
    }

    //checks if the given color has at least one move that doesn't
    //leave its own king in check (works for either color, ignores turns)
    public boolean hasLegalMove(boolean white) {
        for (int startRow = 0; startRow < 8; startRow++) {
            for (int startCol = 0; startCol < 8; startCol++) {
                char piece = board[startRow][startCol];

                if (piece == '.' || isWhitePiece(piece) != white) {
                    continue;
                }

                for (int endRow = 0; endRow < 8; endRow++) {
                    for (int endCol = 0; endCol < 8; endCol++) {
                        if (isValidMove(startRow, startCol, endRow, endCol) &&
                            isSafeMove(startRow, startCol, endRow, endCol)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    //in check and no way out
    public boolean isCheckmate(boolean white) {
        return isKingInCheck(white) && !hasLegalMove(white);
    }

    //not in check, but no legal moves (a draw)
    public boolean isStalemate(boolean white) {
        return !isKingInCheck(white) && !hasLegalMove(white);
    }

    //plays a move for the side to move, returns false if it isn't legal
    //a pawn reaching the last row promotes to a queen
    public boolean movePiece(int startRow, int startCol,
                             int endRow, int endCol) {
        return movePiece(startRow, startCol, endRow, endCol, 'q');
    }

    //same, but lets you choose the promotion piece (q, r, b, or n)
    public boolean movePiece(int startRow, int startCol,
                             int endRow, int endCol, char promotion) {
        if (!isLegalMove(startRow, startCol, endRow, endCol)) {
            return false;
        }

        char promo = Character.toLowerCase(promotion);
        if ("qrbn".indexOf(promo) == -1) {
            promo = 'q';
        }

        makeMove(new int[] {startRow, startCol, endRow, endCol, promo});
        return true;
    }

    //---------------- evaluation ----------------

    //piece-square tables, written from white's point of view with row 0
    //at the top (rank 8), the same way the board array is laid out
    //a higher number means that square is better for that piece
    private static final int[][] PAWN_TABLE = {
        {  0,  0,  0,  0,  0,  0,  0,  0},
        { 50, 50, 50, 50, 50, 50, 50, 50},
        { 10, 10, 20, 30, 30, 20, 10, 10},
        {  5,  5, 10, 25, 25, 10,  5,  5},
        {  0,  0,  0, 20, 20,  0,  0,  0},
        {  5, -5,-10,  0,  0,-10, -5,  5},
        {  5, 10, 10,-20,-20, 10, 10,  5},
        {  0,  0,  0,  0,  0,  0,  0,  0}
    };

    private static final int[][] KNIGHT_TABLE = {
        {-50,-40,-30,-30,-30,-30,-40,-50},
        {-40,-20,  0,  0,  0,  0,-20,-40},
        {-30,  0, 10, 15, 15, 10,  0,-30},
        {-30,  5, 15, 20, 20, 15,  5,-30},
        {-30,  0, 15, 20, 20, 15,  0,-30},
        {-30,  5, 10, 15, 15, 10,  5,-30},
        {-40,-20,  0,  5,  5,  0,-20,-40},
        {-50,-40,-30,-30,-30,-30,-40,-50}
    };

    private static final int[][] BISHOP_TABLE = {
        {-20,-10,-10,-10,-10,-10,-10,-20},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  5, 10, 10,  5,  0,-10},
        {-10,  5,  5, 10, 10,  5,  5,-10},
        {-10,  0, 10, 10, 10, 10,  0,-10},
        {-10, 10, 10, 10, 10, 10, 10,-10},
        {-10,  5,  0,  0,  0,  0,  5,-10},
        {-20,-10,-10,-10,-10,-10,-10,-20}
    };

    private static final int[][] ROOK_TABLE = {
        {  0,  0,  0,  0,  0,  0,  0,  0},
        {  5, 10, 10, 10, 10, 10, 10,  5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        { -5,  0,  0,  0,  0,  0,  0, -5},
        {  0,  0,  0,  5,  5,  0,  0,  0}
    };

    private static final int[][] QUEEN_TABLE = {
        {-20,-10,-10, -5, -5,-10,-10,-20},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  5,  5,  5,  5,  0,-10},
        { -5,  0,  5,  5,  5,  5,  0, -5},
        {  0,  0,  5,  5,  5,  5,  0, -5},
        {-10,  5,  5,  5,  5,  5,  0,-10},
        {-10,  0,  5,  0,  0,  0,  0,-10},
        {-20,-10,-10, -5, -5,-10,-10,-20}
    };

    //middlegame king table: stay tucked away behind your pawns
    private static final int[][] KING_TABLE = {
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-20,-30,-30,-40,-40,-30,-30,-20},
        {-10,-20,-20,-20,-20,-20,-20,-10},
        { 20, 20,  0,  0,  0,  0, 20, 20},
        { 20, 30, 10,  0,  0, 10, 30, 20}
    };

    //material value in centipawns (100 = one pawn)
    //kings are 0 since both sides always have one, and checkmate
    //should be handled by the search rather than the evaluation
    private static int pieceValue(char type) {
        switch (type) {
            case 'p': return 100;
            case 'n': return 320;
            case 'b': return 330;
            case 'r': return 500;
            case 'q': return 900;
            default:  return 0;
        }
    }

    //row/col here are from the piece owner's point of view
    private static int positionValue(char type, int row, int col) {
        switch (type) {
            case 'p': return PAWN_TABLE[row][col];
            case 'n': return KNIGHT_TABLE[row][col];
            case 'b': return BISHOP_TABLE[row][col];
            case 'r': return ROOK_TABLE[row][col];
            case 'q': return QUEEN_TABLE[row][col];
            case 'k': return KING_TABLE[row][col];
            default:  return 0;
        }
    }

    //scores the position in centipawns from white's point of view
    //positive = white is better, negative = black is better
    public int evaluate() {
        int score = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board[row][col];

                if (piece == '.') {
                    continue;
                }

                boolean white = isWhitePiece(piece);
                char type = Character.toLowerCase(piece);
                //the tables are from white's side, so flip the rows for black
                int tableRow = white ? row : 7 - row;

                int value = pieceValue(type) +
                    positionValue(type, tableRow, col);

                score += white ? value : -value;
            }
        }

        return score;
    }

    //---------------- search ----------------

    //how many moves ahead the search looks (1 = just your own next move)
    //this will go up to 3 once we're ready for deeper searching
    private static final int SEARCH_DEPTH = 1;

    //score for checkmate, much bigger than any evaluation
    private static final int MATE_SCORE = 100000;

    //score of the best move from the last findBestMove call
    private int lastBestScore = 0;

    public int getLastBestScore() {
        return lastBestScore;
    }

    //all legal moves for the side to move, as
    //{startRow, startCol, endRow, endCol, promotion}
    //promotion is 0 for normal moves, or q/r/b/n when a pawn promotes
    public List<int[]> getLegalMoves() {
        List<int[]> moves = new ArrayList<>();

        for (int startRow = 0; startRow < 8; startRow++) {
            for (int startCol = 0; startCol < 8; startCol++) {
                char piece = board[startRow][startCol];

                if (piece == '.' || isWhitePiece(piece) != whiteTurn) {
                    continue;
                }

                for (int endRow = 0; endRow < 8; endRow++) {
                    for (int endCol = 0; endCol < 8; endCol++) {
                        if (!isLegalMove(startRow, startCol, endRow, endCol)) {
                            continue;
                        }

                        //a pawn reaching the last row can become 4 different pieces
                        if (Character.toLowerCase(piece) == 'p' &&
                            (endRow == 0 || endRow == 7)) {
                            for (char promo : new char[] {'q', 'r', 'b', 'n'}) {
                                moves.add(new int[] {startRow, startCol,
                                    endRow, endCol, promo});
                            }
                        } else {
                            moves.add(new int[] {startRow, startCol,
                                endRow, endCol, 0});
                        }
                    }
                }
            }
        }

        return moves;
    }

    //everything makeMove changes, so undoMove can put it all back
    private static class Undo {
        char movedPiece;      //the piece before moving (a pawn if it promoted)
        char captured;        //what was captured ('.' if nothing)
        int capturedRow;      //where it stood (not the end square for en passant)
        int capturedCol;
        boolean[] castling;   //castling rights before the move
        int epRow;            //en passant square before the move
        int epCol;
    }

    //plays a move (castling, en passant, and promotion included), updates the
    //castling rights and en passant square, and passes the turn
    //the move is {startRow, startCol, endRow, endCol} plus an optional
    //promotion piece, and it must already be known to be valid
    private Undo makeMove(int[] move) {
        int startRow = move[0];
        int startCol = move[1];
        int endRow = move[2];
        int endCol = move[3];

        char piece = board[startRow][startCol];
        boolean white = isWhitePiece(piece);
        char type = Character.toLowerCase(piece);

        Undo undo = new Undo();
        undo.movedPiece = piece;
        undo.captured = board[endRow][endCol];
        undo.capturedRow = endRow;
        undo.capturedCol = endCol;
        undo.castling = castling.clone();
        undo.epRow = epRow;
        undo.epCol = epCol;

        //en passant: a pawn moving diagonally onto an empty square
        //captures the pawn standing beside it instead
        if (type == 'p' && startCol != endCol && board[endRow][endCol] == '.') {
            undo.capturedRow = startRow;
            undo.capturedCol = endCol;
            undo.captured = board[startRow][endCol];
            board[startRow][endCol] = '.';
        }

        board[endRow][endCol] = piece;
        board[startRow][startCol] = '.';

        //promotion (a queen unless told otherwise)
        if (type == 'p' && (endRow == 0 || endRow == 7)) {
            char promo = (move.length > 4 && move[4] != 0)
                ? Character.toLowerCase((char) move[4]) : 'q';
            board[endRow][endCol] = white ? Character.toUpperCase(promo) : promo;
        }

        //castling: the rook jumps over the king
        if (type == 'k' && Math.abs(endCol - startCol) == 2) {
            int rookFromCol = endCol > startCol ? 7 : 0;
            int rookToCol = (startCol + endCol) / 2;
            board[endRow][rookToCol] = board[endRow][rookFromCol];
            board[endRow][rookFromCol] = '.';
        }

        //moving from or capturing on a king/rook home square loses that right
        clearCastlingRights(startRow, startCol);
        clearCastlingRights(endRow, endCol);

        //a pawn moving two squares can be captured en passant on the next move
        if (type == 'p' && Math.abs(endRow - startRow) == 2) {
            epRow = (startRow + endRow) / 2;
            epCol = startCol;
        } else {
            epRow = -1;
            epCol = -1;
        }

        whiteTurn = !whiteTurn;
        return undo;
    }

    private void undoMove(int[] move, Undo undo) {
        int startRow = move[0];
        int startCol = move[1];
        int endRow = move[2];
        int endCol = move[3];

        board[startRow][startCol] = undo.movedPiece;
        board[endRow][endCol] = '.';
        board[undo.capturedRow][undo.capturedCol] = undo.captured;

        //put the castling rook back
        if (Character.toLowerCase(undo.movedPiece) == 'k' &&
            Math.abs(endCol - startCol) == 2) {
            int rookFromCol = endCol > startCol ? 7 : 0;
            int rookToCol = (startCol + endCol) / 2;
            board[endRow][rookFromCol] = board[endRow][rookToCol];
            board[endRow][rookToCol] = '.';
        }

        castling = undo.castling;
        epRow = undo.epRow;
        epCol = undo.epCol;
        whiteTurn = !whiteTurn;
    }

    //a king or rook leaving (or being captured on) its home square
    //means that castling right is gone for good
    private void clearCastlingRights(int row, int col) {
        if (row == 7 && col == 4) {
            castling[0] = false;
            castling[1] = false;
        } else if (row == 7 && col == 7) {
            castling[0] = false;
        } else if (row == 7 && col == 0) {
            castling[1] = false;
        } else if (row == 0 && col == 4) {
            castling[2] = false;
            castling[3] = false;
        } else if (row == 0 && col == 7) {
            castling[2] = false;
        } else if (row == 0 && col == 0) {
            castling[3] = false;
        }
    }

    //converts a move to text like "e2e4" (or "e7e8q" for a promotion)
    public static String moveToString(int[] move) {
        String text = "" + (char) ('a' + move[1]) + (8 - move[0]) +
            (char) ('a' + move[3]) + (8 - move[2]);

        if (move.length > 4 && move[4] != 0) {
            text += (char) move[4];
        }

        return text;
    }

    //scores the position (from white's point of view) after looking
    //"depth" more moves ahead, white picks the highest score and
    //black picks the lowest
    private int minimax(int depth) {
        boolean white = whiteTurn;

        //no legal moves means the game is over for the side to move
        if (!hasLegalMove(white)) {
            if (isKingInCheck(white)) {
                //checkmate, a bigger depth left means it was found sooner
                int mate = MATE_SCORE + depth;
                return white ? -mate : mate;
            }
            //stalemate
            return 0;
        }

        if (depth == 0) {
            return evaluate();
        }

        int best = white ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int[] move : getLegalMoves()) {
            Undo undo = makeMove(move);
            int score = minimax(depth - 1);
            undoMove(move, undo);

            if (white) {
                best = Math.max(best, score);
            } else {
                best = Math.min(best, score);
            }
        }

        return best;
    }

    //finds the best move for the side to move as
    //{startRow, startCol, endRow, endCol, promotion}, or null if there are
    //no legal moves
    //the score of the move is available from getLastBestScore()
    public int[] findBestMove() {
        boolean white = whiteTurn;
        int[] bestMove = null;
        int bestScore = 0;

        for (int[] move : getLegalMoves()) {
            Undo undo = makeMove(move);
            int score = minimax(SEARCH_DEPTH - 1);
            undoMove(move, undo);

            if (bestMove == null ||
                (white && score > bestScore) ||
                (!white && score < bestScore)) {
                bestMove = move;
                bestScore = score;
            }
        }

        //no moves: score is checkmate or stalemate
        lastBestScore = (bestMove == null) ? minimax(0) : bestScore;
        return bestMove;
    }
}