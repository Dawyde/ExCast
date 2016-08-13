package fr.exensoft.audioserver.core.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

import fr.exensoft.audioserver.AudioServer;
import fr.exensoft.audioserver.core.buffermanager.BaseBufferManager;
import fr.exensoft.audioserver.core.protocol.http.HTTPResponse;
import fr.exensoft.audioserver.core.protocol.rtsp.RTSPResponse;
import fr.exensoft.audioserver.core.server.MainSocketPacket;
import fr.exensoft.audioserver.core.server.MainSocketProcessWrite;

public class MainSocketWriter {

	private Queue<MainSocketPacket> mMessageQueue;
	private MainSocketPacket mCurrentMessage;
	private MainClient mRTSPClient;
	private SocketChannel mSocketChannel;
	private boolean mIsWriting = false;
	private MainSocketProcessWrite mMainSocketProcessWrite;
	private BaseBufferManager<MainSocketPacket> mSocketPacketManager;
	private boolean mClosed = false;
	

	public MainSocketWriter(MainClient rtspClient, MainSocketProcessWrite socketProcessWrite, SocketChannel socketChannel, BaseBufferManager<MainSocketPacket> socketPacketManager) {
		mMessageQueue = new LinkedList<MainSocketPacket>();
		mCurrentMessage = null;
		mRTSPClient = rtspClient;
		mSocketChannel = socketChannel;
		mSocketPacketManager = socketPacketManager;
		mMainSocketProcessWrite = socketProcessWrite;
	}
	public void sendRTSPResponse(RTSPResponse response){
		byte[] data = response.toString().getBytes();
		write(data, 0, data.length);
	}

	public void sendHTTPResponse(HTTPResponse response) {
		byte[] data = response.toString().getBytes();
		write(data, 0, data.length);
	}

	public synchronized void sendData(SocketChannel channel, ByteBuffer buffer) throws IOException {
		if (mCurrentMessage == null) return;

		// On calcule combien on doit écrire dans le buffer
		int length = mCurrentMessage.getLength() - mCurrentMessage.getIndex();
		if (length > buffer.limit()) length = buffer.limit();
		buffer.put(mCurrentMessage.getData(), mCurrentMessage.getIndex(), length);
		buffer.flip();
		// On envoie le buffer sur le channel
		int written = channel.write(buffer);
		buffer.clear();
		
		// On met à jour les messages
		mCurrentMessage.addIndex(written);
		if (mCurrentMessage.getIndex() >= mCurrentMessage.getLength()) {
			mSocketPacketManager.releasePacket(mCurrentMessage);
			if (mMessageQueue.isEmpty()) mCurrentMessage = null;
			else mCurrentMessage = mMessageQueue.poll();
		}
	}

	public boolean hasData() {
		return mCurrentMessage != null;
	}

	/**
	 * Ajout d'un message à écrire (ici un nombre d'octets), on crée une copie
	 * et on ajoute le message dans la file
	 * 
	 * @param buffer
	 * @param offset
	 * @param length
	 */
	public synchronized void write(byte[] buffer, int offset, int length) {

		if(length <= 0 || mClosed){
			return;
		}
		int packetLength = AudioServer.PACKET_LENGTH;
		while (length > 0) {
			MainSocketPacket packet = mSocketPacketManager.getPacket();
			if (length > packetLength) {
				packet.wrap(buffer, offset, packetLength);
				offset += packetLength;
				length -= packetLength;
			}
			else {
				packet.wrap(buffer, offset, length);
				offset += length;
				length = 0;
			}
			if (mCurrentMessage == null) mCurrentMessage = packet;
			else mMessageQueue.add(packet);
		}
		if (!mIsWriting && hasData()) {
			mMainSocketProcessWrite.addWriter(mRTSPClient);
			mIsWriting = true;
		}
	}
	


	public synchronized void doSend(SelectionKey key, ByteBuffer writeBuffer) throws IOException {
		if(mClosed){
			key.cancel();
			return;
		}
		try {
			sendData(mSocketChannel, writeBuffer);
			if (!hasData()) {
				mIsWriting = false;
				mMainSocketProcessWrite.removeKey(key);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			key.cancel();
			mRTSPClient.close();
		}
	}

	public synchronized void close() throws IOException {
		mClosed = true;
		mMainSocketProcessWrite.removeClient(mRTSPClient);
		if (mCurrentMessage != null) mSocketPacketManager.releasePacket(mCurrentMessage);
		if (mMessageQueue != null) {
			mMessageQueue.stream().forEach(packet -> {
				mSocketPacketManager.releasePacket(packet);
			});
			mMessageQueue.clear();
		}
		mMessageQueue = null;
	}
}
