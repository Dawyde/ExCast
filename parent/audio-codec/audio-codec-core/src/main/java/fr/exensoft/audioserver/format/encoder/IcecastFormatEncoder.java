package fr.exensoft.audioserver.format.encoder;

public abstract class IcecastFormatEncoder extends StreamFormatEncoder {

	@Override
	public StreamFormat getFormat() {
		return StreamFormat.ICECAST;
	}

	
	public abstract String getContentType();
	
}
