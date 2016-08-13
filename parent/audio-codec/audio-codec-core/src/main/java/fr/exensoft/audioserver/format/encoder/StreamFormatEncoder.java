package fr.exensoft.audioserver.format.encoder;

public abstract class StreamFormatEncoder {
	
	protected StreamDataListener mStreamFormatDataListener;
	
	public abstract void encodeAudioData(byte[] buffer, int length, long startByte, long endByte);
	
	public abstract void init();
	
	public abstract void close();

	public abstract StreamFormat getFormat();
	
	public void setStreamFormatDataListener(StreamDataListener streamFormatDataListener){
		mStreamFormatDataListener = streamFormatDataListener;
	}
	
	protected void newPacketReady(byte[] buffer, int length, long startByte, long endByte){
		if(mStreamFormatDataListener != null){
			mStreamFormatDataListener.newPacket(buffer, length, startByte, endByte);
		}
	}
	
}
