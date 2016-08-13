package fr.exensoft.audioserver.core.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPSocketServer{
	private int mServerPort;
	
	private DatagramSocket mDatagramSocket;
	
	public UDPSocketServer(int port){
		mServerPort = port;
		try {
			mDatagramSocket = new DatagramSocket(mServerPort);
			mDatagramSocket.setSendBufferSize(4096*4096);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getPort(){
		return mServerPort;
	}
	
	public void sendPacket(InetAddress client, int port, byte[] data, int length) throws IOException{
		DatagramPacket packet = new DatagramPacket(data, length, client, port);
		mDatagramSocket.send(packet);
	}
}
