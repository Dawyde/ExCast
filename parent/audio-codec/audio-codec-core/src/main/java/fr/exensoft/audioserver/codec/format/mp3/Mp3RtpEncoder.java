package fr.exensoft.audioserver.codec.format.mp3;

import fr.exensoft.audioserver.format.encoder.RTPFormatEncoder;
import fr.exensoft.audioserver.format.encoder.StreamFormat;

/**
 * Encapsulation des frames MP3 dans un format RTP classique (payload 14)
 * Ce format est plus économe en traitement et en bande passante (théoriquement)
 * Mais il n'est pas très résistant aux pertes de paquets, il vaut mieux l'utiliser pour les
 * modes de transmission "interleaved"
 */
public class Mp3RtpEncoder extends RTPFormatEncoder{

	private byte[] mBuffer = new byte[2048];
	
	@Override
	public int getPayloadType() {
		return 14;
	}

	@Override
	public String getDescribe(String output, String streamname) {
		StringBuilder sb = new StringBuilder();
		sb.append("v=0\r\n");
		sb.append("o=").append(output).append("\r\n");
		sb.append("s=").append(streamname).append("\r\n");
		sb.append("m=audio 0 RTP/AVP 14\r\n");
		sb.append("a=control:streamid=0\r\n");
		return sb.toString();
	}

	@Override
	public void encodeAudioData(byte[] buffer, int length, long startByte, long endByte) {
		System.arraycopy(buffer, 0, mBuffer, 4, length);
		/*
		mBuffer[2] = (byte) ((length>>8)&255);
		mBuffer[3] = (byte) (length&255);
		
		mBuffer[0] = 0;
		mBuffer[1] = 0;
*/		
		newPacketReady(mBuffer, length+4, startByte, endByte);
	}

	@Override
	public void init() {
		
	}

	@Override
	public void close() {
		
	}

	@Override
	public StreamFormat getFormat(){
		return StreamFormat.INTERLEAVED_RTP;
	}
}
