package fr.exensoft.audioserver.core.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import fr.exensoft.audioserver.AudioServer;
import fr.exensoft.audioserver.core.buffermanager.BaseBufferManager;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.core.client.MainClient;

public class MainSocketProcessRead implements Runnable {

	private Thread mThread;
	private boolean mIsRunning;
	private Selector mReadSelector;
	private ByteBuffer mReadBuffer;
	private Queue<SocketChannel> mNewSocketChannels = new LinkedList<SocketChannel>();
	private AudioServer mAudioServer;
	private BaseBufferManager<MainSocketPacket> mPacketBufferManager;

	private LinkedList<MainClient> mClients = new LinkedList<>();
	
	private LinkedList<MainClient> mClosingClients = new LinkedList<>();

	private MainSocketProcessWrite mProcessWrite;


	public BaseBufferManager<MainSocketPacket> getPacketBufferManager() {
		return mPacketBufferManager;
	}
	
	public MainSocketProcessRead(AudioServer audioServer) {
		mAudioServer = audioServer;
		try {
			mReadSelector = Selector.open();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		mProcessWrite = new MainSocketProcessWrite();
		mIsRunning = true;
		mThread = new Thread(this);
		mThread.start();
		mReadBuffer = ByteBuffer.allocate(4096);
		
		BufferManager bufferManager = audioServer.getBufferManager();
		bufferManager.registerBufferManager(MainSocketPacket.class, new MainSocketPacket.RTSPSocketPacketFactory());
		mPacketBufferManager = bufferManager.getBaseBufferManager(MainSocketPacket.class);
	}

	@Override
	public void run() {
		MainClient client = null;
		while (mIsRunning) {
			try {
				// On ajoute les connexions en attente
				addSocketChannels();
				
				//Ferme les clients
				closeClients();

				// On va lire les socket qui ont des données
				int selected = mReadSelector.select();

				if (selected > 0) {
					Set<SelectionKey> selectedKeys = mReadSelector.selectedKeys();
					for (SelectionKey key : selectedKeys) {
						try {
							client = (MainClient) key.attachment();
							client.getSocketReader().read(key, mReadBuffer);
						}
						catch (Exception e) {
							client.close();
						}
					}
					selectedKeys.clear();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public int size() {
		return mClients.size();
	}

	public void newSocket(SocketChannel channel) {
		mNewSocketChannels.add(channel);
		mReadSelector.wakeup();
	}

	public void addSocketChannels() {
		SocketChannel channel = null;
		while (!mNewSocketChannels.isEmpty()) {
			channel = mNewSocketChannels.poll();
			MainClient client = new MainClient(mAudioServer, this, channel);
			mClients.add(client);
			try {
				channel.configureBlocking(false);
				channel.register(mReadSelector, SelectionKey.OP_READ, client);
			}
			catch (Exception e) {
				client.close();
			}
		}
	}
	
	public void closeClients(){
		while(!mClosingClients.isEmpty()){
			MainClient client = mClosingClients.poll();
			SelectionKey key = client.getSocketChannel().keyFor(mReadSelector);
			if(key != null){
				key.cancel();
			}
		}
	}

	public MainSocketProcessWrite getProcessWrite() {
		return mProcessWrite;
	}

	public void removeClient(MainClient client) throws IOException {
		mClients.remove(client);
		mClosingClients.add(client);
		mReadSelector.wakeup();
	}

	public void close() {
		mIsRunning = false;
	}
}
