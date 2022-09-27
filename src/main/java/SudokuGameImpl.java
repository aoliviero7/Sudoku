import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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


	public SudokuGameImpl( int _id, String _master_peer, final MessageListener _listener) throws Exception
	{
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
        if(rooms.contains(_game_name))
            return null;
        rooms.add(_game_name);

        try{
            FutureGet futureGet = _dht.get(Number160.createHash(_game_name)).start();
            futureGet.awaitUninterruptibly();
            FutureGet room = _dht.get(Number160.MAX_VALUE.createHash("rooms")).start();
            room.awaitUninterruptibly();

            if (futureGet.isSuccess() && futureGet.isEmpty()) {
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
                    //sendMessage(message, sudokuChallenge);
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
    public Integer placeNumber(String _game_name, int _i, int _j, int _number) {
        try {
            FutureGet futureGet = _dht.get(Number160.createHash(_game_name)).start();
            futureGet.awaitUninterruptibly();
            FutureGet score = _dht.get(Number160.createHash("peerScore")).start();
            score.awaitUninterruptibly();

            SudokuRoom sudokuRoom;
            sudokuRoom = (SudokuRoom) futureGet.dataMap().values().iterator().next().object();
            if (futureGet.isSuccess() && !futureGet.isEmpty() && score.isSuccess()) {

                //Checks if the number can be entered
                if (sudokuRoom.insertNumber(_number, _i, _j)) {
                    //update sudoku in DHT
                    _dht.put(Number160.createHash(_game_name)).data(new Data(sudokuRoom)).start().awaitUninterruptibly();

                    //Add +1 to user
                    for (PeerAddress peerAddress : gamePeers.keySet())
                        if (peerAddress.equals(peer.peerAddress())) {
                            Player p = gamePeers.get(peerAddress);
                            p.setScore(p.getScore()+1);
                            gamePeers.put(peerAddress, p);
                            sudokuRoom.getPeerScore().put(p.getNickname(), p.getScore());
                            peerScore.put(p.getNickname(), p.getScore());
                            //update score in DHT
                            _dht.put(Number160.createHash("peerScore")).data(new Data(peerScore)).start().awaitUninterruptibly();
                            String message = "[" + _game_name + "] " + p.getNickname() + " insert number " + _number + " in position: (" + _i + "," + _j + ").";
                            //sendMessage(message, sudokuChallenge);
                        }
                    //Checks if the game is finished
                    if (sudokuRoom.checkSudoku()) {
                        return 2;
                    }
                    else {
                        return 1;
                    }
                } else if (sudokuRoom.checkNumber(_number, _i, _j)) { //Checks if the number has already been entered
                    //Add +0 to user
                    return 0;
                } else { //the number is wrong
                    //Remove -1 to user
                    for (PeerAddress peerAddress : gamePeers.keySet())
                        if (peerAddress.equals(peer.peerAddress())) {
                            Player p = gamePeers.get(peerAddress);
                            p.setScore(p.getScore()-1);
                            gamePeers.put(peerAddress, p);
                            sudokuRoom.getPeerScore().put(p.getNickname(), p.getScore());
                            peerScore.put(p.getNickname(), p.getScore());
                            //update score in DHT
                            _dht.put(Number160.createHash("peerScore")).data(new Data(peerScore)).start().awaitUninterruptibly();
                        }
                    return -1;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    private void sendMessage(String message, SudokuRoom sudokuRoom) {
        for (PeerAddress peerAddress : sudokuRoom.getGamePeers().keySet()) {
            if (_dht.peer().peerAddress() != peerAddress) {
                FutureDirect futureDirect = _dht.peer().sendDirect(peerAddress).object(message).start();
                futureDirect.awaitUninterruptibly();
            }
        }
    }

    public void addUser(Player player) throws IOException {
        gamePeers.put(peer.peerAddress(), player);
        _dht.put(Number160.createHash("usersInGame")).data(new Data(gamePeers)).start().awaitUninterruptibly();
    }

}
