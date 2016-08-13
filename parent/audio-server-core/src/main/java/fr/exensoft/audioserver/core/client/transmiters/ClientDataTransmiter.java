package fr.exensoft.audioserver.core.client.transmiters;

import java.io.IOException;

import fr.exensoft.audioserver.AudioServer;
import fr.exensoft.audioserver.core.AudioStreamManager;
import fr.exensoft.audioserver.core.client.MainClient;
import fr.exensoft.audioserver.core.protocol.http.HTTPRequest;
import fr.exensoft.audioserver.core.protocol.http.HTTPResponse;
import fr.exensoft.audioserver.core.protocol.rtsp.InterleavedPacket;
import fr.exensoft.audioserver.core.protocol.rtsp.RTSPRequest;
import fr.exensoft.audioserver.core.protocol.rtsp.RTSPResponse;
import fr.exensoft.audioserver.core.router.RequestHandler;

public abstract class ClientDataTransmiter implements RequestHandler{
	
	protected AudioStreamManager mAudioStreamManager;
	
	protected MainClient mClient;
	
	protected AudioServer mAudioServer;
	
	public ClientDataTransmiter(AudioServer audioServer, AudioStreamManager audioStreamManager, MainClient client){
		mClient = client;
		mAudioStreamManager = audioStreamManager;
		mAudioServer =audioServer;
	}
	
	public abstract void close();
	
	public RTSPResponse newRTSPRequest(RTSPRequest rtspRequest) throws IOException{
		return null;
	}
	
	public HTTPResponse newHTTPRequest(HTTPRequest rtspRequest) throws IOException{
		return null;
	}

	public void newInterleavedPacket(InterleavedPacket interleavedPacket) {
	}
	
	public void newRawData(byte[] buffer, int length){
	}
	
}
