package fr.exensoft.audioserver.core.buffers;

public class BytePacket implements BaseBuffer{

	public final static int MAX_LENGTH = 2048;
	
	public static class IcecastPacketFactory implements BaseBufferFactory<BytePacket>{
		@Override
		public BytePacket create() {
			return new BytePacket();
		}
	}
	
	protected byte[] mBuffer = new byte[MAX_LENGTH];
	
	protected int mLength;
	
	protected long mStartByte = 0;
	protected long mEndByte = 0;

	public BytePacket() {

	}

	public byte[] getBytes(){
		return mBuffer;
	}
	
	public int getLength(){
		return mLength;
	}
	
	public void setLength(int length){
		mLength = length;
	}
	
	public long getStartByte(){
		return mStartByte;
	}
	
	public long getEndByte(){
		return mEndByte;
	}
	
	public void setStartByte(long startByte){
		mStartByte = startByte;
	}
	
	public void setEndByte(long endByte){
		mEndByte = endByte;
	}
}
