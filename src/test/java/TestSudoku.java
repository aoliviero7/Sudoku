import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.*;

import Class.Player;
import Sudoku.MessageListenerImpl;
import Sudoku.SudokuGameImpl;
import net.tomp2p.peers.PeerAddress;

public class TestSudoku {

    private static SudokuGameImpl peer0;
    private static SudokuGameImpl peer1;
    private static SudokuGameImpl peer2;
    private static SudokuGameImpl peer3;

    private static Player player0;
    private static Player player1;
    private static Player player2;
    private static Player player3;


    @BeforeAll
    static public void setup() throws Exception {
        peer0 = new SudokuGameImpl(0, "127.0.0.1", new MessageListenerImpl(0));
        peer1 = new SudokuGameImpl(1, "127.0.0.1", new MessageListenerImpl(1));
        peer2 = new SudokuGameImpl(2, "127.0.0.1", new MessageListenerImpl(2));
        peer3 = new SudokuGameImpl(3, "127.0.0.1", new MessageListenerImpl(3));

        player0 = new Player("player0");
        player1 = new Player("player1");
        player2 = new Player("player2");
        player3 = new Player("player3");

    }


    //test creazione nuovi player e aggiunti alla sessione
    @Test
    public void testAddPlayer() throws IOException {
        peer0.addPlayer(player0);
        peer1.addPlayer(player1);
        peer2.addPlayer(player2);
        peer3.addPlayer(player3);

        HashMap<PeerAddress, Player> playersActive = peer0.playersActive();
        assertEquals(4, playersActive.size());
    }


    @Test
    public void testCreationSudoku() throws Exception {

        //test creazione nuovo sudoku
        Integer[][] sudoku0 = peer0.generateNewSudoku("testCreation");
        assertNotEquals(null, sudoku0);


        //test creazione sudoku con lo stesso nome
        Integer[][] sudoku1 = peer0.generateNewSudoku("testCreation");
        assertEquals(null, sudoku1);

    }


    @Test
    public void testJoin() throws IOException {

        //test join ad un sudoku
        peer0.addPlayer(player0);
        peer1.addPlayer(player1);
        peer0.generateNewSudoku("testJoin");
        boolean join = peer0.join("testJoin", player1.getNickname());
        assertTrue(join);


        //test join ad un sudoku inesistente
        boolean joinFalse = peer0.join("testJoinFalse", player1.getNickname());
        assertFalse(joinFalse);

    }


    @Test
    public void testAddCreator() throws IOException {

        //test aggiunta creatore del gioco
        peer0.addPlayer(player0);
        peer0.generateNewSudoku("gameAddCreator");
        boolean addCreator = peer0.addCreator(player0, "gameAddCreator");
        assertTrue(addCreator);

    }


    @Test
    public void testGetSudoku() throws Exception {

        //test get sudoku
        peer0.addPlayer(player0);
        peer0.generateNewSudoku("gameGet");
        peer0.addCreator(player0, "gameGet");
        Integer[][] sudoku0 = peer0.getSudoku("gameGet");
        assertNotEquals(null, sudoku0);


        //test get sudoku inesistente
        Integer[][] sudoku1 = peer0.getSudoku("gameInesistente");
        assertEquals(null, sudoku1);

    }


    @Test
    public void testRoomsActive() throws Exception {

        //test lista delle stanze attive
        peer0.generateNewSudoku("gameRoom0");
        peer1.generateNewSudoku("gameRoom1");
        ArrayList<String> rooms = peer0.roomsActive();
        assertNotEquals(0, rooms.size());
        assertEquals(rooms.size(), peer1.roomsActive().size());
        
    }


    @Test
    public void testPlayersActive() throws Exception {

        //test lista del player attivi
        peer0.addPlayer(player0);
        peer1.addPlayer(player1);
        HashMap<PeerAddress, Player> playersActive = peer0.playersActive();
        assertNotEquals(0, playersActive.size());
        assertEquals(playersActive.size(), peer1.playersActive().size());

    }

    /*
    //test exit
    @Test
    public void testExit() throws Exception {
        Player player0 = new Player("player0");
        peer0.generateNewSudoku("game0");
        boolean exit = peer0.exit(player0, "game0", true);
        assertTrue(exit);
        peer0.getWinner("game0");
    }
     */
    
    
    @Test
    public void testPlaceNumber() throws IOException{

        //test inserimento numero corretto
        peer0.addPlayer(player0);
        peer0.generateNewSudoku("gamePlaceNumber");
        peer0.addCreator(player0, "gamePlaceNumber");
        int resultCorrect = peer0.placeNumber("gamePlaceNumber", 0, 0, 1);
        System.out.println("resultCorrect: " + resultCorrect);
        assertEquals(1, resultCorrect);
        

        //test inserimento numero sbagliato
        int resultWrong = peer0.placeNumber("gamePlaceNumber", 0, 0, 4);
        System.out.println("resultWrong: " + resultWrong);
        assertEquals(-1, resultWrong);


        //test inserimento numero già presente
        int resultNull = peer0.placeNumber("gamePlaceNumber", 0, 4, 6);
        System.out.println("resultNull: " + resultNull);
        assertEquals(0, resultNull);


        //test inserimento numero corretto che completa il sudoku
        peer0.placeNumber("gamePlaceNumber", 0, 1, 9);
        peer0.placeNumber("gamePlaceNumber", 0, 2, 4);
        int resultWin = peer0.placeNumber("gamePlaceNumber", 0, 3, 8);
        System.out.println("resultWin: " + resultWin);
        assertEquals(10, resultWin);

    }


    @Test
    public void testGetWinner() throws IOException {

        //test vincitore della partita
        peer1.addPlayer(player1);
        peer2.addPlayer(player2);
        peer1.generateNewSudoku("gameGetWinner");
        peer1.addCreator(player1, "gameGetWinner");
        peer2.join("gameGetWinner", "player1");
        peer1.placeNumber("gameGetWinner", 0, 1, 9);
        peer1.placeNumber("gameGetWinner", 0, 2, 4);
        peer2.placeNumber("gameGetWinner", 0, 3, 8);
        int result = peer1.placeNumber("gameGetWinner", 0, 0, 1);
        assertEquals(10, result);
        String winner = peer1.getWinner("gameGetWinner");
        assertEquals("player1", winner);

    }
}
