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
}