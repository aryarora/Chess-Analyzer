public class Main {
    public static void main(String[] args) {
        ChessBoard b = new ChessBoard();
        int[] move = b.findBestMove();
        System.out.println(ChessBoard.moveToString(move) + "  score: " + b.getLastBestScore());

        // mate in one: white to move, Qh5xf7 is checkmate
        b.loadFEN("r1bqkbnr/pppp1ppp/2n5/4p2Q/2B1P3/8/PPPP1PPP/RNB1K1NR w");
        System.out.println(ChessBoard.moveToString(b.findBestMove()));  // expect h5f7
    }
}