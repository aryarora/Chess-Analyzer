public class Main {
    public static void main(String[] args) {
        ChessBoard b = new ChessBoard();
        b.movePiece(6, 5, 5, 5); // f2-f3
        b.movePiece(1, 4, 3, 4); // e7-e5
        b.movePiece(6, 6, 4, 6); // g2-g4
        b.movePiece(0, 3, 4, 7); // Qd8-h4
        System.out.println(b.isCheckmate(true));  // true
        System.out.println(b.isCheckmate(false)); // false
    }
}