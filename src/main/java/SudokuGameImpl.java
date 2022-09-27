import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
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
	
	final private ArrayList<String> s_topics=new ArrayList<String>();

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
        try{
            FutureGet futureGet = _dht.get(Number160.createHash(_game_name)).start();
                futureGet.awaitUninterruptibly();
                if (futureGet.isSuccess() && futureGet.isEmpty()) 
                    _dht.put(Number160.createHash(_game_name)).data(new Data(new HashSet<PeerAddress>())).start().awaitUninterruptibly();
        } catch (Exception e) {
			e.printStackTrace();
		}        
        return null;
    }

    @Override
    public boolean join(String _game_name, String _nickname) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Integer[][] getSudoku(String _game_name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer placeNumber(String _game_name, int _i, int _j, int _number) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
