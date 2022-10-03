package Sudoku;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

import Class.Player;
import Interface.MessageListener;
import Interface.SudokuGame;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;


public class SudokuGameImpl implements SudokuGame{
    final private Peer peer;
	final private PeerDHT _dht;
	final private int DEFAULT_MASTER_PORT=4000;
	
	private ArrayList<String> rooms = new ArrayList<String>();
    private HashMap<PeerAddress, Player> gamePeers = new HashMap<PeerAddress, Player>();
    private HashMap<String, Integer> peerScore = new HashMap<String, Integer>();


	public SudokuGameImpl( int _id, String _master_peer, final MessageListener _listener) throws Exception {
		 peer= new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT+_id).start();
		_dht = new PeerBuilderDHT(peer).start();	
		FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).ports(DEFAULT_MASTER_PORT).start();
		fb.awaitUninterruptibly();
		if(fb.isSuccess()) {
			peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
		}else {
			throw new Exception("Error in master peer bootstrap.");
		}
		peer.objectDataReply(new ObjectDataReply() {
			public Object reply(PeerAddress sender, Object request) throws Exception {
				return _listener.parseMessage(request);
			}
		});
	}

    @Override
    public Integer[][] generateNewSudoku(String _game_name) {
        try{
            FutureGet futureGet = _dht.get(Number160.createHash(_game_name)).start();
            futureGet.awaitUninterruptibly();
            FutureGet room = _dht.get(Number160.MAX_VALUE.createHash("rooms")).start();
            room.awaitUninterruptibly();
            if (futureGet.isSuccess() && futureGet.isEmpty()) {
                if(room.isSuccess() && !room.isEmpty()){
                    rooms = (ArrayList<String>) room.dataMap().values().iterator().next().object();
                    if(rooms.contains(_game_name))
                        return null;
                } 
                rooms.add(_game_name);
                SudokuRoom sudokuRoom = new SudokuRoom(_game_name);
                _dht.put(Number160.createHash(_game_name)).data(new Data(sudokuRoom)).start().awaitUninterruptibly();
                _dht.put(Number160.createHash("rooms")).data(new Data(rooms)).start().awaitUninterruptibly();
                return sudokuRoom.getSudoku().getRawSudoku();
            }
        } catch (Exception e) {
			e.printStackTrace();
		}        
        return null;
    }

    @Override
    public boolean join(String _game_name, String _nickname) {
        try {
            FutureGet futureGet = _dht.get(Number160.createHash(_game_name)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                if (futureGet.isEmpty()) return false;
                SudokuRoom sudokuRoom;
                sudokuRoom = (SudokuRoom) futureGet.dataMap().values().iterator().next().object();
                if (sudokuRoom.addPeer(_dht.peer().peerAddress(), _nickname)) {
                    _dht.put(Number160.createHash(_game_name)).data(new Data(sudokuRoom)).start().awaitUninterruptibly();
                    String message = "[" + _game_name + "] " + _nickname + " joined.";
                    sendMessage(message, sudokuRoom);
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Integer[][] getSudoku(String _game_name) {
        try {
            FutureGet futureGet = _dht.get(Number160.createHash(_game_name)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                if (futureGet.isEmpty()) 
                    return null;
                SudokuRoom sudokuRoom;
                sudokuRoom = (SudokuRoom) futureGet.dataMap().values().iterator().next().object();
                return sudokuRoom.getSudoku().getRawSudoku();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override 
    /*
        return: - 1, if the number it's correctly placed
                - 0, if the number has already been entered
                - -1, if the number is wrong
                - 10, if the number makes you complete the game 
    */
    public Integer placeNumber(String _game_name, int _i, int _j, int _number) {
        try {
            FutureGet futureGet = _dht.get(Number160.createHash(_game_name)).start();
            futureGet.awaitUninterruptibly();
            FutureGet score = _dht.get(Number160.createHash("peerScore")).start();
            score.awaitUninterruptibly();

            SudokuRoom sudokuRoom;
            sudokuRoom = (SudokuRoom) futureGet.dataMap().values().iterator().next().object();
            if (futureGet.isSuccess() && !futureGet.isEmpty() && score.isSuccess()) {
                if (sudokuRoom.checkNumber(_number, _i, _j)) 
                    return 0;
                else if (sudokuRoom.insertNumber(_number, _i, _j)) {
                    _dht.put(Number160.createHash(_game_name)).data(new Data(sudokuRoom)).start().awaitUninterruptibly();

                    for (PeerAddress peerAddress : gamePeers.keySet())
                        if (peerAddress.equals(peer.peerAddress())) {
                            Player p = gamePeers.get(peerAddress);
                            p.setScore(p.getScore()+1);
                            gamePeers.put(peerAddress, p);
                            sudokuRoom.getPeerScore().put(p.getNickname(), p.getScore());
                            peerScore.put(p.getNickname(), p.getScore());
                            _dht.put(Number160.createHash("peerScore")).data(new Data(peerScore)).start().awaitUninterruptibly();
                            String message = "[" + _game_name + "] " + p.getNickname() + " insert number " + _number + " in position: (" + _i + "," + _j + "). (Score = " + p.getScore() + ")";
                            sendMessage(message, sudokuRoom);
                        }
                    if (sudokuRoom.checkSudoku()) {
                        String winner = getWinner(_game_name);
                        String message = "[" + _game_name + "] Congratulation " + winner + ", you win!";
                        sendMessage(message, sudokuRoom);
                        return 10;
                    }
                    else {
                        return 1;
                    }
                } else { 
                    for (PeerAddress peerAddress : gamePeers.keySet())
                        if (peerAddress.equals(peer.peerAddress())) {
                            Player p = gamePeers.get(peerAddress);
                            p.setScore(p.getScore()-1);
                            gamePeers.put(peerAddress, p);
                            sudokuRoom.getPeerScore().put(p.getNickname(), p.getScore());
                            peerScore.put(p.getNickname(), p.getScore());
                            _dht.put(Number160.createHash("peerScore")).data(new Data(peerScore)).start().awaitUninterruptibly();
                            String message = "[" + _game_name + "] " + p.getNickname() + " insert wrong number in position: (" + _i + "," + _j + "). (Score = " + p.getScore() + ")";
                            sendMessage(message, sudokuRoom);
                        }
                    return -1;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    public void sendMessage(String message, SudokuRoom sudokuRoom) {
        for (PeerAddress peerAddress : sudokuRoom.getGamePeers().keySet()) {
            //System.out.println(peerAddress);
            if (_dht.peer().peerAddress() != peerAddress) {
                FutureDirect futureDirect = _dht.peer().sendDirect(peerAddress).object(message).start();
                futureDirect.awaitUninterruptibly();
            }
        }
    }

    public void addPlayer(Player player) throws IOException {
        gamePeers.put(peer.peerAddress(), player);
        _dht.put(Number160.createHash("gamePeers")).data(new Data(gamePeers)).start().awaitUninterruptibly();
    }

    public boolean addCreator(Player player, String _game_name) throws IOException {
        try {
            FutureGet futureGet = _dht.get(Number160.createHash(_game_name)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                if (futureGet.isEmpty()) return false;
                SudokuRoom sudokuRoom;
                sudokuRoom = (SudokuRoom) futureGet.dataMap().values().iterator().next().object();
                if (sudokuRoom.addPeer(_dht.peer().peerAddress(), player.getNickname())) {
                    _dht.put(Number160.createHash(_game_name)).data(new Data(sudokuRoom)).start().awaitUninterruptibly();
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<String> roomsActive() {
        try {
            FutureGet room = _dht.get(Number160.createHash("rooms")).start();
            room.awaitUninterruptibly();
            if(room.isEmpty()) return new ArrayList<String>();
            if (room.isSuccess()) {
                if(room.isEmpty())
                    return new ArrayList<String>();
                ArrayList<String> result = (ArrayList<String>) room.dataMap().values().iterator().next().object();
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<String>();
    }

    public ArrayList<String> roomsActiveByPlayer(Player player) {
        try {
            FutureGet rooms = _dht.get(Number160.createHash("rooms")).start();
            rooms.awaitUninterruptibly();
            if(rooms.isEmpty()) return new ArrayList<String>();
            if (rooms.isSuccess()) {
                if(rooms.isEmpty())
                    return new ArrayList<String>();
                ArrayList<String> result = (ArrayList<String>) rooms.dataMap().values().iterator().next().object();
                ArrayList<String> result2 = new ArrayList<String>();
                for(String r : result){
                    FutureGet room = _dht.get(Number160.createHash(r)).start();
                    room.awaitUninterruptibly();
                    if(room.isEmpty()) return new ArrayList<String>();
                    if (room.isSuccess()) {
                        if(room.isEmpty())
                            return new ArrayList<String>();
                        SudokuRoom sudokuRoom = (SudokuRoom) room.dataMap().values().iterator().next().object();
                        HashMap<PeerAddress, String> players = sudokuRoom.getGamePeers();
                        for(PeerAddress peerAddress : players.keySet())
                            if(players.get(peerAddress).equalsIgnoreCase(player.getNickname()))
                                result2.add(r);
                    }
                }
                return result2;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<String>();
    }
    
    public HashMap<PeerAddress, Player> playersActive() {
        try {
            FutureGet players = _dht.get(Number160.createHash("gamePeers")).start();
            players.awaitUninterruptibly();
            if(players.isEmpty()) return new HashMap<PeerAddress, Player>();
            if (players.isSuccess()) {
                if(players.isEmpty())
                    return new HashMap<PeerAddress, Player>();
                HashMap<PeerAddress, Player> result = (HashMap<PeerAddress, Player>) players.dataMap().values().iterator().next().object();
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<PeerAddress, Player>();
    }

    public boolean exit(Player player, String _game_name, boolean gameFlag) {
        if (gameFlag) {
            try {
                FutureGet futureGet = _dht.get(Number160.createHash(_game_name)).start();
                futureGet.awaitUninterruptibly();
                if (futureGet.isSuccess())
                    if (futureGet.isEmpty()) return false;
                SudokuRoom sudokuRoom;
                sudokuRoom = (SudokuRoom) futureGet.dataMap().values().iterator().next().object();
                if (sudokuRoom.removePeer(_dht.peer().peerAddress(), player.getNickname())) {
                    for (PeerAddress peerAddress : gamePeers.keySet())
                        if (peerAddress.equals(peer.peerAddress())) {
                            player.setScore(0);
                            gamePeers.put(peerAddress, player);
                            sudokuRoom.getPeerScore().put(player.getNickname(), 0);
                            peerScore.put(player.getNickname(), 0);
                            _dht.put(Number160.createHash("peerScore")).data(new Data(peerScore)).start().awaitUninterruptibly();
                        }
                    String message = "[" + _game_name + "] " + player.getNickname() + " exited.";
                    sendMessage(message, sudokuRoom);
                    _dht.peer().announceShutdown().start().awaitUninterruptibly();
                    return true;
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        _dht.peer().announceShutdown().start().awaitUninterruptibly();

        return false;
    }

    /*public String getWinner(String _game_name) {
        int max = 0;
        String result = "";
        try {
            FutureGet score = _dht.get(Number160.createHash("peerScore")).start();
            score.awaitUninterruptibly();
            if(score.isEmpty()) return null;
            if (score.isSuccess()) {
                if(score.isEmpty())
                    return null;
                HashMap<String, Integer> peerScore = (HashMap<String, Integer>) score.dataMap().values().iterator().next().object();
                for(String p : peerScore.keySet()){
                    if(peerScore.get(p)>max){
                        max = peerScore.get(p);
                        result = p;
                    }
                    peerScore.remove(p);
                }
                if(!result.equals(""))
                    return result;
                else
                    return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

    public String getWinner(String _game_name) {
        int max = 0;
        String result = "";
        try {
            FutureGet score = _dht.get(Number160.createHash("peerScore")).start();
            score.awaitUninterruptibly();
            FutureGet futureGet = _dht.get(Number160.createHash(_game_name)).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess())
                if (futureGet.isEmpty()) return null;
            SudokuRoom sudokuRoom;
            sudokuRoom = (SudokuRoom) futureGet.dataMap().values().iterator().next().object();
            if(score.isEmpty()) return null;
            if (score.isSuccess()) {
                if(score.isEmpty())
                    return null;
                HashMap<String, Integer> peerScore = (HashMap<String, Integer>) score.dataMap().values().iterator().next().object();
                for(String p : peerScore.keySet()){
                    if(peerScore.get(p)>max){
                        max = peerScore.get(p);
                        result = p;
                    }
                    //peerScore.remove(p);
                    if (sudokuRoom.removePeer(_dht.peer().peerAddress(), p)) {
                        for (PeerAddress peerAddress : gamePeers.keySet()){
                            Player player = gamePeers.get(peerAddress);
                            player.setScore(0);
                            gamePeers.put(peerAddress, player);
                            sudokuRoom.getPeerScore().put(player.getNickname(), 0);
                            peerScore.put(player.getNickname(), 0);
                            _dht.put(Number160.createHash("peerScore")).data(new Data(peerScore)).start().awaitUninterruptibly();
                        }
                        _dht.peer().announceShutdown().start().awaitUninterruptibly();
                    }
                }
                System.out.println(result + " ha fatto " + max);
                if(!result.equals(""))
                    return result;
                else
                    return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
