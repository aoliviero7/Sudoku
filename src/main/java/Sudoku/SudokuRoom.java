package Sudoku;
import java.io.Serializable;
import java.util.HashMap;

import Class.Sudoku;
import net.tomp2p.peers.PeerAddress;

public class SudokuRoom implements Serializable {

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

    public SudokuRoom(String _game_name) throws Exception {
        this._game_name = _game_name;
        sudoku = new Sudoku(_game_name);
        sudoku.generateSudoku();
    }

    public boolean addPeer(PeerAddress peerAddress, String nickname) {
        for(PeerAddress peer : gamePeers.keySet())
            if(peer.equals(peerAddress))
                return false;

        gamePeers.put(peerAddress, nickname);
        peerScore.put(nickname, 0);
        return true;
    }

    public boolean removePeer(PeerAddress peerAddress, String nickname) {
        for(PeerAddress peer : gamePeers.keySet())
            if(peer.equals(peerAddress)){
                gamePeers.remove(peer);
                peerScore.remove(nickname);
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

    public boolean checkSudoku(){
        Integer[][] rawSudoku = sudoku.getRawSudoku();
        Integer[][] solvedSudoku = sudoku.getSolvedSudoku();
        for(int rows=0; rows<9; rows++)
            for(int columns=0; columns<9; columns++)
                if (!(rawSudoku[rows][columns].equals(solvedSudoku[rows][columns])))
                    return false;

        return true;
    }
}