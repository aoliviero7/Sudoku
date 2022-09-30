package Sudoku;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import Class.Player;
import net.tomp2p.peers.PeerAddress;

public class Main {

    @Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
    private static String master;

    @Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
    private static int id;
    
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        final CmdLineParser parser = new CmdLineParser(main);
        parser.parseArgument(args);
        System.out.println("  ========================");
        System.out.println("      SUDOKU P2P GAME");
        System.out.println("  ========================");
        SudokuGameImpl sudokuGameImpl = new SudokuGameImpl(id, master, new MessageListenerImpl(id));
        HashMap<PeerAddress, Player> players = sudokuGameImpl.playersActive();
        Scanner scanner = new Scanner(System.in);
        String nickname = "";
        boolean nicknameFlag = false;
        System.out.println("Enter your nickname: ");
        while(true){
            nickname = scanner.next();
            for(PeerAddress peerAddress : players.keySet())
                if(players.get(peerAddress).getNickname().equalsIgnoreCase(nickname)){
                    nicknameFlag = true;
                    break;
                }
            if(nicknameFlag){
                System.out.println("Nickname has already been taken. Try another one:"); 
                nicknameFlag = false;
            }
            else
                break;
        }
        Player player = new Player(nickname);
        sudokuGameImpl.addPlayer(player);
        Game game = new Game(sudokuGameImpl, id, player);
        game.gameLoop();
    }
}
