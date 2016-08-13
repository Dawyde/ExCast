package fr.exensoft.audioserver.core.server;

import fr.exensoft.audioserver.AudioServer;
import fr.exensoft.audioserver.core.buffers.BaseBuffer;
import fr.exensoft.audioserver.core.buffers.BaseBufferFactory;

public class MainSocketPacket implements BaseBuffer{
	
	public static class RTSPSocketPacketFactory implements BaseBufferFactory<MainSocketPacket>{

		@Override
		public MainSocketPacket create() {
			return new MainSocketPacket(AudioServer.PACKET_LENGTH);
		}
		
	}
	
	private byte[] mData;
	private int mIndex;
	private int mLength;

	public MainSocketPacket(int length) {
		mData = new byte[length];
		mIndex = 0;
		mLength = length;
	}

	public void wrap(byte[] source, int offset, int length) {
		System.arraycopy(source, offset, mData, 0, length);
		mLength = length;
		mIndex = 0;
	}

	public int getLength() {
		return mLength;
	}

	public int getIndex() {
		return mIndex;
	}

	public byte[] getData() {
		return mData;
	}

	public int getMaxLength() {
		return mData.length;
	}

	public void addIndex(int written) {
		mIndex += written;
	}

	public void remove() {
		mData = null;
		mIndex = 0;
		mLength = 0;
	}
}
