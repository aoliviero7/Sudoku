import java.io.Serializable;
import java.util.HashMap;

import net.tomp2p.peers.PeerAddress;

public class SudokuChallenge implements Serializable {

    private Sudoku sudoku;
    private String _game_name;
    private HashMap<PeerAddress, String> gamePeers = new HashMap<PeerAddress, String>();
    private HashMap<String, Integer> peerScore = new HashMap<String, Integer>();

    public Sudoku getSudoku() {
        return this.sudoku;
    }

    public String get_game_name() {
        return this._game_name;
    }

    public HashMap<PeerAddress,String> getGamePeers() {
		return this.gamePeers;
	}

    public HashMap<String, Integer> getPeerScore() {
		return this.peerScore;
	}

    public SudokuChallenge(String _game_name) throws Exception {
        this._game_name = _game_name;
        sudoku = new Sudoku(_game_name);
        sudoku.generateSudoku();
    }

    public boolean addPeer(PeerAddress peerAddress, String name) {
        for(PeerAddress peer : gamePeers.keySet())
            if(peer.equals(peerAddress))
                return false;

        gamePeers.put(peerAddress, name);
        peerScore.put(name, 0);
        return true;
    }

    public boolean removePeer(PeerAddress peerAddress, String name) {
        for(PeerAddress peer : gamePeers.keySet())
            if(peer.equals(peerAddress)){
                gamePeers.remove(peer);
                peerScore.remove(name);
                return true;
            }

        return false;
    }

    public boolean insertNumber(int number, int row, int column) {
        Integer[][] solvedSudoku = sudoku.getSolvedSudoku();
        if(solvedSudoku[row][column] == number){
            sudoku.insert(number, row, column);
            return true;
        }

        return false;
    }

    public boolean checkNumber(int number, int row, int column) {
        Integer[][] rawSudoku = sudoku.getRawSudoku();
        if(rawSudoku[row][column] == number)
            return true;

        return false;
    }
}