public class Main {
    public static void main(String[] args) {
        ChessBoard chessBoard = new ChessBoard();

        chessBoard.printBoard();

        System.out.println("\nTesting moves:");

        System.out.println("Pawn e2 to e4: " +
                chessBoard.movePiece(6, 4, 4, 4));

        System.out.println("Knight b1 to c3: " +
                chessBoard.movePiece(7, 1, 5, 2));

        System.out.println("Rook a1 to a3: " +
                chessBoard.movePiece(7, 0, 5, 0));

        System.out.println("\nUpdated board:");
        chessBoard.printBoard();
    }
}