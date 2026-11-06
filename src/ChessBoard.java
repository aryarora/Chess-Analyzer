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
}