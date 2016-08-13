package fr.exensoft.audioserver.core.client.transmiters;

import java.io.IOException;

import fr.exensoft.audioserver.AudioServer;
import fr.exensoft.audioserver.core.AudioStreamManager;
import fr.exensoft.audioserver.core.buffers.RTPPacket;
import fr.exensoft.audioserver.core.client.MainClient;
import fr.exensoft.audioserver.core.protocol.MainProtocol;
import fr.exensoft.audioserver.core.protocol.rtsp.InterleavedPacket;
import fr.exensoft.audioserver.core.protocol.rtsp.RTSPRequest;
import fr.exensoft.audioserver.core.protocol.rtsp.RTSPResponse;
import fr.exensoft.audioserver.core.stream.format.RTPInterleavedStreamFormat;

public class RTPInterleavedTransmiter extends ClientDataTransmiter{

	protected RTPInterleavedStreamFormat mRTPStreamFormat;
	
	private byte[] mSendBuffer = new byte[2048];
	
	public RTPInterleavedTransmiter(AudioServer audioServer, AudioStreamManager audioStreamManager, MainClient client, RTPInterleavedStreamFormat rtpStreamFormat) {
		super(audioServer, audioStreamManager, client);
		mRTPStreamFormat = rtpStreamFormat;
		
		//On passe en mode interleaved
		mClient.getSocketReader().setProtocol(MainProtocol.INTERLEAVED_RTSP);
	}

	@Override
	public void close() {
		
	}

	public RTSPResponse setup(RTSPRequest request) throws IOException{
		
		RTSPResponse response = new RTSPResponse(200, "OK");
		response.set("Transport", "RTP/AVP/TCP;interleaved=0-1");
		response.set("CSeq", request.get("CSeq"));
		response.set("Session", "Patate");
		return response;
	}
	
	@Override
	public void newInterleavedPacket(InterleavedPacket interleavedPacket) {

	}

	@Override
	public RTSPResponse newRTSPRequest(RTSPRequest request) throws IOException{
		
		if(request.getType().equals("OPTIONS")){
			RTSPResponse response = new RTSPResponse(200, "OK");
			response.set("Server", "JavaRelay");
			response.set("CSeq", request.get("CSeq"));
			response.set("Public", "DESCRIBE, SETUP, PLAY, TEARDOWN");
			return response;
		}
		else if(request.getType().equals("DESCRIBE")){
			
			String describe = mRTPStreamFormat.getSDPDescription();
			
			RTSPResponse response = new RTSPResponse(200, "OK");
			response.set("Server", "JavaRelay");
			response.set("CSeq", request.get("CSeq"));
			response.set("Content-Length", String.valueOf(describe.length()));
			response.set("Content-Type", "application/sdp");
			response.setBody(describe);
			return response;
		}
		else if (request.getType().equals("GET_PARAMETER")) {
			// On ne relaie pas les GET_PARAMETER
			RTSPResponse response = new RTSPResponse(200, "OK");
			response.set("Server", "JavaRelay");
			response.set("CSeq", request.get("CSeq"));
			response.set("Session", request.get("Session"));
			return response;
		}
		else if (request.getType().equals("SETUP")) {
			if (!request.get("Transport").contains("interleaved")) {
				RTSPResponse response = new RTSPResponse(461, "Unsupported Transport");
				response.set("CSeq", request.get("CSeq"));
				return response;
			}
			return setup(request);
		}
		else if (request.getType().equals("PLAY")) {
			play();
			RTSPResponse response = new RTSPResponse(200, "OK");
			response.set("Server", "JavaRelay");
			response.set("CSeq", request.get("CSeq"));
			return response;
		}
		else if (request.getType().equals("TEARDOWN")) {
			stop();
			RTSPResponse response = new RTSPResponse(200, "OK");
			response.set("Server", "JavaRelay");
			response.set("CSeq", request.get("CSeq"));
			return response;
		}

		
		return null;
	}

	private void stop() {
		mRTPStreamFormat.removeClient(this);
	}

	private void play() {
		mRTPStreamFormat.registerClient(this);
	}

	public void sendRTPPacket(RTPPacket packet) {
		int length = packet.getLength();
		mSendBuffer[0]='$';
		mSendBuffer[1]=0;
		mSendBuffer[2] = (byte) ((length >> 8) & 255);
		mSendBuffer[3] = (byte) (length & 255);
		packet.writePacket(mSendBuffer, 4);
		mClient.getSocketWriter().write(mSendBuffer, 0, length+4);
	}
}
