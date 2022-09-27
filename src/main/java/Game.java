import java.util.HashMap;

public class Game {
    private Player player;
    private SudokuGameImpl peer;
    private int peerID;
    private String join_game;
    private boolean join;
    private int count_help = 3;
    private HashMap<String, Integer> peerScore= new HashMap<String,Integer>();

    public Game(SudokuGameImpl peer, int peerID, Player player) {
        this.peer = peer;
        this.peerID = peerID;
        this.player = player;
    }

    static public void printSudoku(Integer[][] sudoku,  String _game_name, HashMap<String, Integer> peerScore) {
        System.out.println("   0 1 2   3 4 5   6 7 8");      
        System.out.println("  ========================");
        for (int rows = 0; rows < 9; rows++) {
            System.out.print(rows+" |");
            for (int cols = 0; cols < 9; cols++) {             
                System.out.print(sudoku[rows][cols] + " ");            
                if ((cols+1) % 3 == 0) {
                    System.out.print("| ");
                }
            }         
            System.out.println();
            if ((rows+1) %3 == 0) {
                System.out.println("  ========================");
            } 
        }
    }
}
