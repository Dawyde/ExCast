package fr.exensoft.audioserver.core.buffers;

public class AudioSampleBuffer implements BaseBuffer{
	
	public static class AudioSampleBufferFactory implements BaseBufferFactory<AudioSampleBuffer>{

		private int mLength;
		
		public AudioSampleBufferFactory(int length) {
			mLength = length;
		}
		
		@Override
		public AudioSampleBuffer create() {
			return new AudioSampleBuffer(mLength);
		}
		
	}
	
	private byte[] mBuffer;
	
	private int mLength;
	
	private long mStartByte;
	
	private long mEndByte;
	
	private int mUsage = 0;
	
	public AudioSampleBuffer(int length){
		mLength = length;
		mBuffer = new byte[length];
	}
	
	public synchronized void acquire(){
		mUsage++;
	}
	
	public synchronized void release(){
		mUsage--;
	}
	
	public synchronized int getUsage(){
		return mUsage;
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
	
	public void setStartByte(long startByte){
		mStartByte = startByte;
	}
	
	public long getEndByte(){
		return mEndByte;
	}
	
	public void setEndByte(long endByte){
		mEndByte = endByte;
	}
	
	public byte[] getBuffer(){
		return mBuffer;
	}
	
	public void write(byte[] source, int sourceOffset, int length){
		System.arraycopy(source, sourceOffset, mBuffer, 0, length);
	}

	public void reset() {
		mUsage = 0;
	}
}
