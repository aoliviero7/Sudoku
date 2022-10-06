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
    }

    @Test
    public void testAddPlayer() throws IOException {
        player0 = new Player("player0");
        player1 = new Player("player1");
        player2 = new Player("player2");
        player3 = new Player("player3");

        peer0.addPlayer(player0);
        peer1.addPlayer(player1);
        peer2.addPlayer(player2);
        peer3.addPlayer(player3);
    }

    @Test
    public void testCreationSudoku() throws Exception {
        Integer[][] sudoku0 = peer0.generateNewSudoku("game10");
        assertNotEquals(null, sudoku0);
        peer0.getWinner("game0");
    }

    @Test
    public void testJoin() throws IOException {
        Player player1 = new Player("player1");
        peer0.addPlayer(player1);
        peer0.generateNewSudoku("game0");
        boolean join = peer0.join("game0", player1.getNickname());
        assertTrue(join);
        peer0.getWinner("game0");
    }

    @Test
    public void testAddCreator() throws IOException {
        Player player0 = new Player("player0");
        peer0.addPlayer(player0);
        peer0.generateNewSudoku("game0");
        boolean addCreator = peer0.addCreator(player0, "game0");
        assertTrue(addCreator);
        peer0.getWinner("game0");
    }

    @Test
    public void testGetSudoku() throws Exception {
        peer0.generateNewSudoku("game0");
        Integer[][] sudoku0 = peer0.getSudoku("game0");
        assertNotEquals(null, sudoku0);
        peer0.getWinner("game0");
    }

    @Test
    public void testRoomsActive() throws Exception {
        peer0.generateNewSudoku("game0");
        peer1.generateNewSudoku("game1");
        ArrayList<String> rooms = peer0.roomsActive();
        assertNotEquals(0, rooms.size());
        assertEquals(rooms.size(), peer1.roomsActive().size());
        peer0.getWinner("game0");
        peer1.getWinner("game1");
    }

    @Test
    public void testPlayersActive() throws Exception {
        Player player0 = new Player("player0");
        Player player1 = new Player("player1");
        peer0.addPlayer(player0);
        peer1.addPlayer(player1);
        HashMap<PeerAddress, Player> playersActive = peer0.playersActive();
        assertNotEquals(0, playersActive.size());
    }

    @Test
    public void testExit() throws Exception {
        Player player0 = new Player("player0");
        Player player1 = new Player("player1");
        peer0.addPlayer(player0);
        peer1.addPlayer(player1);
        peer0.generateNewSudoku("game0");
        peer0.addCreator(player0, "game0");
        peer0.join("game0", player1.getNickname());
        boolean exit = peer0.exit(player1, "game0", true);
        assertTrue(exit);
        peer0.getWinner("game0");
    }

    /*
     * @AfterAll
    static void afterAll() {
        peer0.exit(player0, );
        peer1.exit(player1, );
        peer2.exit(player2, );
        peer3.exit(player3, );
    }
     */
    
}
