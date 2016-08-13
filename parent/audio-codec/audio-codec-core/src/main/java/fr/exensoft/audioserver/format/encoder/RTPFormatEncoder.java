package fr.exensoft.audioserver.format.encoder;

public abstract class RTPFormatEncoder extends StreamFormatEncoder{
	
	public StreamFormat getFormat(){
		return StreamFormat.RTP;
	}
	
	public abstract int getPayloadType();
	
	public abstract String getDescribe(String output, String streamname);
}
