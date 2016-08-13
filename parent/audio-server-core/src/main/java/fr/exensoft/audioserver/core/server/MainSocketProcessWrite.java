package fr.exensoft.audioserver.core.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.LinkedList;
import java.util.Set;

import fr.exensoft.audioserver.core.client.MainClient;

public class MainSocketProcessWrite implements Runnable {

	private Thread mThread;
	private boolean mIsRunning;
	private Selector mWriteSelector;
	private ByteBuffer mWriteBuffer;

	private LinkedList<MainClient> mNewWriters = new LinkedList<MainClient>();

	private LinkedList<SelectionKey> mKeysToCancel= new LinkedList<>();

	private LinkedList<MainClient> mClosingClients = new LinkedList<>();

	public MainSocketProcessWrite() {
		try {
			mWriteSelector = Selector.open();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		mIsRunning = true;
		mThread = new Thread(this);
		mThread.start();
		mWriteBuffer = ByteBuffer.allocate(4096);
	}

	public synchronized void addWriter(MainClient writer) {
		mNewWriters.add(writer);
		mWriteSelector.wakeup();
	}

	public synchronized void addNewWriters() {
		if(!mKeysToCancel.isEmpty()){
			for(SelectionKey key : mKeysToCancel){
				if(!mNewWriters.contains(key.attachment())){
					key.cancel();
				}
				else{
					System.out.println("AVOID "+((MainClient) key.attachment()));
				}
			}
			mKeysToCancel.clear();
		}
		
		if (mNewWriters.isEmpty()) return;
		for (MainClient client : mNewWriters) {
			try {
				if(!client.isClosed()){
					client.getSocketChannel().register(mWriteSelector, SelectionKey.OP_WRITE, client);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				client.close();
				System.exit(1);
			}
		}
		mNewWriters.clear();
	}

	@Override
	public void run() {
		MainClient client = null;
		while (mIsRunning) {
			try {
				// On ajoute les connexions qui veulent écrire
				addNewWriters();
				
				// Suppression des writers
				closeClients();
				
				// On va lire les socket qui sont prête pour écrire des données
				int selected = mWriteSelector.select();
				if (selected > 0) {
					Set<SelectionKey> selectedKeys = mWriteSelector.selectedKeys();
					for (SelectionKey key : selectedKeys) {
						client = (MainClient) key.attachment();
						client.getSocketWriter().doSend(key, mWriteBuffer);
					}
					selectedKeys.clear();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}

	public void close() {
		mIsRunning = false;
	}
	
	public synchronized void removeKey(SelectionKey key){
		mKeysToCancel.add(key);
	}
	
	public synchronized void closeClients(){
		while(!mClosingClients.isEmpty()){
			MainClient client = mClosingClients.poll();
			SelectionKey key = client.getSocketChannel().keyFor(mWriteSelector);
			if(key != null){
				key.cancel();
			}
		}
	}
	
	public void removeClient(MainClient client) throws IOException {
		mClosingClients.add(client);
		mNewWriters.remove(client);
		mWriteSelector.wakeup();
	}

}
