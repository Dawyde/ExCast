package fr.exensoft.audioserver.core.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import fr.exensoft.audioserver.AudioServer;

public class MainSocketServer implements Runnable{

	private ServerSocketChannel mChannelServer;
	private boolean mIsRunning;

	private MainSocketProcessRead mSocketProcess;

	private Thread mThread;

	public MainSocketServer(AudioServer audioServer, int port) {
		try {
			mChannelServer = ServerSocketChannel.open();
			mChannelServer.bind(new InetSocketAddress(port));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Démarage du serveur RTSP sur le port "+port);
		mIsRunning = true;
		mSocketProcess = new MainSocketProcessRead(audioServer);
		mThread = new Thread(this);
		mThread.start();
	}

	@Override
	public void run() {
		while (mIsRunning) {
			try {
				System.out.println("Waiting for connection " + mSocketProcess.size());
				SocketChannel channel = mChannelServer.accept();
				mSocketProcess.newSocket(channel);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Thread getThread() {
		return mThread;
	}
}
