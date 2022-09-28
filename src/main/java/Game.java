import java.util.HashMap;
import java.util.Scanner;

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

    private static void printMenu() {
        System.out.println("  ========================");
        System.out.println("   Enter your choice");
        System.out.println("  ========================");
        System.out.println("1. Start new game");
        System.out.println("2. Join in a game room");
        System.out.println("3. Show sudoku of active game");
        System.out.println("4. Insert a number");
        System.out.println("5. Show my info");
        System.out.println("6. Exit");
        System.out.println("  ========================");
    }

    public void printSudoku(Integer[][] sudoku,  String _game_name, HashMap<String, Integer> peerScore) {
        System.out.println("[Room: " + _game_name + " ]\n\n");
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

    private void playerInfo(Player player, int perrID) {
        System.out.println("[" + peerID + "] Nickname: " + player.getNickname() + " - Score: " + player.getScore() + ".");
    }

    static public void gameLoop() {
        while(true){
            printMenu();
            Scanner scanner= new Scanner(System.in);
            int input = 0;
            do{
                scanner.next();
                System.out.println("Insert a number between 1 and 6");
                input=scanner.nextInt();
                if(input < 1 || input >6)
                    System.out.println("Input wrong");
                else
                    break;
            }while(!scanner.hasNextInt());
            switch(input){
                case 1:
                    System.out.println("Start new game");
                    break;
                case 2:
                    System.out.println("Join in a game room");
                    break;
                case 3:
                    System.out.println("Show sudoku of active game");
                    break;
                case 4:
                    System.out.println("Insert a number");
                    break;
                case 5:
                    System.out.println("Show my info");
                    break;
                case 6:
                    System.out.println("Exit");
                    break;
                default: break;
            }
        }
    }
}
