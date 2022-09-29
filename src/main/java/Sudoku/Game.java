package Sudoku;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import Class.Player;

public class Game {
    private ArrayList<String> rooms;
    private Player player;
    private SudokuGameImpl peer;
    private int peerID;
    private boolean gameFlag = false;
    private String _game_name="";
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

    private void playerInfo(Player player, int peerID) {
        System.out.println("[" + peerID + "] Nickname: " + player.getNickname() + " - Score: " + player.getScore() + ".");
    }

    public void gameLoop() {
        while(true){
            printMenu();
            Scanner scanner= new Scanner(System.in);
            int input = 0;
            while(input < 1 || input >6){
                while(!scanner.hasNextInt()){
                    System.out.println("Input wrong. You can insert only a number.");
                    scanner.next();                
                }
                input=scanner.nextInt();
                if(input < 1 || input >6)
                    System.out.println("Input wrong. Insert a number between 1 and 6.");
            }
            switch(input){
                case 1:
                    System.out.println("  ========================");
                    System.out.println("   Start new game");
                    System.out.println("  ========================");
                    rooms = peer.roomsActive();
                    _game_name="";
                    if(gameFlag){
                        System.out.println("You are already in a room");
                        break;
                    }
                    while(true){
                        System.out.println("Enter the name of the new game: ");
                        _game_name= scanner.next();
                        if(rooms.contains(_game_name))
                            System.out.println("This name has already been taken.");
                        else
                            break;
                    }
                    Integer [][] sudoku = peer.generateNewSudoku(_game_name);
                    if(sudoku == null)
                        System.out.println("An error occurred while creating the game.");
                    else{
                        System.out.println("Sudoku game room [" + _game_name + "] created successfully.");
                        gameFlag = true;
                    }
                    break;
                case 2:
                    System.out.println("  ========================");
                    System.out.println("   Join in a game room");
                    System.out.println("  ========================");
                    if(gameFlag){
                        System.out.println("You are already in a room");
                        break;
                    }
                    rooms = peer.roomsActive();
                    int size = rooms.size();
                    if(size>0){
                        System.out.println("Game rooms currently active: " + size + ".");
                        int i = 0;
                        for(String room : rooms)
                            System.out.println("[" + i++ + "]. " + room);
                    } else {
                        System.out.println("There are no active game rooms. Try to create a new one.");
                        break;
                    }
                    System.out.println("Enter the name of the game room you want to join.");
                    _game_name = "";
                    _game_name = scanner.next();
                    gameFlag = peer.join(_game_name, player.getNickname());
                    if(gameFlag)
                        System.out.println("[" + _game_name + "] " + player.getNickname() + " joined.");
                    else    
                        System.out.println("An error occurred while joining the game.");
                    break;
                case 3:
                    System.out.println("  ========================");
                    System.out.println("  Show sudoku of active game");
                    System.out.println("  ========================");
                    /* 
                    ArrayList<String> roomsPlayer = peer.roomsActiveByPlayer(player);
                    int sizePlayerRooms = roomsPlayer.size();
                    if(sizePlayerRooms>0){
                        System.out.println("Game rooms currently active: " + sizePlayerRooms + ".");
                        int i = 0;
                        for(String room : roomsPlayer)
                            System.out.println("[" + i++ + "]. " + room);
                    } else {
                        System.out.println("You are not part of any game room. Try creating a new one or joining an existing one.");
                        break;
                    }
                    */
                    if(gameFlag /*&& !_game_name.equals("")*/)
                        printSudoku(peer.getSudoku(_game_name), _game_name, peerScore);
                    else 
                        System.out.println("You are not part of any game room. Try creating a new one or joining an existing one.");
                    break;
                    
                case 4:
                    System.out.println("  ========================");
                    System.out.println("   Insert a number");
                    System.out.println("  ========================");
                    if(!gameFlag || _game_name.equals("")){
                        System.out.println("You are not part of any game room. Try creating a new one or joining an existing one.");
                        break;
                    }
                    int number = 0, row = -1, column = -1;
                    System.out.println("Enter the number you want to insert:");
                    while(number < 1 || number >9){
                        while(!scanner.hasNextInt()){
                            System.out.println("Input wrong. You can insert only a number.");
                            scanner.next();                
                        }
                        number=scanner.nextInt();
                        if(number < 1 || number >9)
                            System.out.println("Input wrong. Insert a number between 1 and 9.");
                    }
                    System.out.println("Enter the row in which you want to insert the number:");
                    while(row < 0 || row >8){
                        while(!scanner.hasNextInt()){
                            System.out.println("Input wrong. You can insert only a number.");
                            scanner.next();                
                        }
                        row=scanner.nextInt();
                        if(row < 0 || row >8)
                            System.out.println("Input wrong. Insert a number between 0 and 8.");
                    }
                    System.out.println("Enter the column in which you want to insert the number:");
                    while(column < 0 || column >8){
                        while(!scanner.hasNextInt()){
                            System.out.println("Input wrong. You can insert only a number.");
                            scanner.next();                
                        }
                        column=scanner.nextInt();
                        if(column < 0 || column >8)
                            System.out.println("Input wrong. Insert a number between 0 and 8.");
                    }
                    int result = peer.placeNumber(_game_name, row, column, number);
                    if(result == number)
                        System.out.println("[" + _game_name + "] Number: " + number + " inserted successfully in position: (" + row + "," + column + ").");
                    else if(result == 0)
                        System.out.println("[" + _game_name + "] Number: " + number + " has already been entered in position: (" + row + "," + column + ").");
                    else if(result == 10)
                        System.out.println("[" + _game_name + "] You win.");
                    else 
                        System.out.println("[" + _game_name + "] Number: " + number + " incorrect.");
                    break;
                case 5:
                    System.out.println("  ========================");
                    System.out.println("   Show my info");
                    System.out.println("  ========================");
                    playerInfo(player, peerID);
                    break;
                case 6:
                    if(peer.exit(player, _game_name, gameFlag))
                        System.exit(0);
                    System.out.println("An error occurred while exiting the game.");
                    break;
                default: break;
            }
        }
    }
}
