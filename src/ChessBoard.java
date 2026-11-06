public class ChessBoard {
    private char[][] board = new char[8][8];

    public ChessBoard() {
        initializeBoard();
    }
    //in chess notation n = knight since k alread is for king
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
        if (destination != '.' && Character.isUpperCase(piece) ==
            Character.isUpperCase(destination)) {
            return false;
        }

        int rowChange = endRow - startRow;
        int colChange = endCol - startCol;
        //switch to lowercase since white pieces are uppper
        switch (Character.toLowerCase(piece)) {
            //if at starting position, pawns can move either one or two squares up
            case 'p':
                //basically checking if its white or black
                //and establishing the direction they can move in
                int direction = Character.isUpperCase(piece) ? -1 : 1;
                int startingRow = Character.isUpperCase(piece) ? 6 : 1;
                //they cannot capture by moving up
                if (colChange == 0 && destination == '.') {
                    //1 square move up
                    if (rowChange == direction) {
                        return true;
                    }
                    //the 2 square move up check
                    if (startRow == startingRow &&
                        rowChange == 2 * direction &&
                        board[startRow + direction][startCol] == '.') {
                        return true;
                    }
                }

                if (Math.abs(colChange) == 1 &&
                    rowChange == direction &&
                    destination != '.') {
                    return true;
                }

                return false;
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
            //we clear all invalid moves before switch case
            //so we just need to move the knight
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
    public boolean movePiece(int startRow, int startCol,
                            int endRow, int endCol) {
        if (!isValidMove(startRow, startCol, endRow, endCol)) {
            return false;
        }
        board[endRow][endCol] = board[startRow][startCol];
        board[startRow][startCol] = '.';
        return true;
    }
}