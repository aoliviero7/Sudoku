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
    private HashMap<PeerAddress, String> gamePeers = new HashMap<PeerAddress, String>();
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
        // TODO Auto-generated method stub
        return null;
    }
    
}
