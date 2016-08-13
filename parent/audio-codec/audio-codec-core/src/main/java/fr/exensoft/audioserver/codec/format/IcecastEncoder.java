package fr.exensoft.audioserver.codec.format;

import fr.exensoft.audioserver.format.encoder.IcecastFormatEncoder;

public class IcecastEncoder extends IcecastFormatEncoder{

	protected String mContentType;
	
	public IcecastEncoder(String contentType) {
		mContentType = contentType;
	}
	
	@Override
	public void encodeAudioData(byte[] buffer, int length, long startByte, long endByte) {
		newPacketReady(buffer, length, startByte, endByte);
	}

	@Override
	public void init() {
		
	}

	@Override
	public void close() {
		
	}

	@Override
	public String getContentType() {
		return mContentType;
	}
}
