package fr.exensoft.audioserver.core.protocol.rtsp;

public class InterleavedPacket {
	private byte[] mBuffer = new byte[2048];
	
	private int mLength;
	
	private int mOffset = 0;
	
	private int mMode = 0;
	
	private int mChannel = 0;
	
	public InterleavedPacket(){
		
	}
	
	public void reset(){
		mOffset = 0;
		mMode = 0;
		mChannel = 0;
		mLength = 0;
	}
	
	public boolean newByte(byte data){
		if(mMode == 0){//Channel
			mChannel = data;
			mMode = 1;
		}
		else if(mMode == 1){//Length 1
			mLength = ((data&255)<<8);
			mMode = 2;
		}
		else if(mMode == 2){
			mLength |= (data&255);
			mMode = 3;
		}
		else{
			mBuffer[mOffset] = data;
			mOffset++;
			if(mOffset >= mLength){
				return true;
			}
		}
		
		return false;
	}
	
	public int getChannel(){
		return mChannel;
	}
	
	public int getLength(){
		return mLength;
	}
	
	public byte[] getData(){
		return mBuffer;
	}
}
