public class ChessBoard {
    private char[][] board = new char[8][8];
    //whose turn it is
    private boolean whiteTurn = true;

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
    //only the piece placement and the side to move are used, the
    //castling/en passant/move counters are ignored (not supported yet)
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

        board = newBoard;
        whiteTurn = white;
    }

    //exports the current position as FEN
    //castling, en passant, and move counters aren't tracked, so they
    //are written as placeholders
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
        sb.append(" - - 0 1");
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

        //diagonal move is only allowed when capturing
        return destination != '.' && canAttack(startRow, startCol, endRow, endCol);
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

        char captured = board[endRow][endCol];
        board[endRow][endCol] = piece;
        board[startRow][startCol] = '.';

        boolean leavesKingInCheck = isKingInCheck(white);

        board[startRow][startCol] = piece;
        board[endRow][endCol] = captured;

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

    public boolean movePiece(int startRow, int startCol,
                             int endRow, int endCol) {
        if (!isLegalMove(startRow, startCol, endRow, endCol)) {
            return false;
        }
        board[endRow][endCol] = board[startRow][startCol];
        board[startRow][startCol] = '.';
        whiteTurn = !whiteTurn;
        return true;
    }
    //---------------- evaluation ----------------

    //piece-square tables, written from white's point of view with row 0
    //at the top (rank 8), the same way the board array is laid out
    //a higher number means that square is better for that piece
    //I had to ask claude to give me a good table for each of the pieces
    //since this was something I cannot calculate myself
    //I did the evaluate method myself
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
}