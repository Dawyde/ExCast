package fr.exensoft.audioserver.core.client.transmiters;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import fr.exensoft.audioserver.AudioServer;
import fr.exensoft.audioserver.core.AudioStreamManager;
import fr.exensoft.audioserver.core.buffers.RTPPacket;
import fr.exensoft.audioserver.core.client.MainClient;
import fr.exensoft.audioserver.core.protocol.MainProtocol;
import fr.exensoft.audioserver.core.protocol.rtsp.RTSPRequest;
import fr.exensoft.audioserver.core.protocol.rtsp.RTSPResponse;
import fr.exensoft.audioserver.core.protocol.rtsp.TransportString;
import fr.exensoft.audioserver.core.stream.format.RTPUnicastStreamFormat;

public class RTPUnicastTransmiter extends ClientDataTransmiter {

	protected RTPUnicastStreamFormat mRTPStreamFormat;

	private InetAddress mClientAdress;
	
	private int mClientPort = -1;
	
	private byte[] mSendBuffer = new byte[2048];
	
	public RTPUnicastTransmiter(AudioServer audioServer, AudioStreamManager audioStreamManager, MainClient client, RTPUnicastStreamFormat rtpStreamFormat) {
		super(audioServer, audioStreamManager, client);
		mRTPStreamFormat = rtpStreamFormat;
		
		//On passe en mode rtsp
		mClient.getSocketReader().setProtocol(MainProtocol.RTSP);
	}

	@Override
	public void close() {
		
	}

	public RTSPResponse setup(RTSPRequest request) throws IOException{
		mClientAdress = ((InetSocketAddress) mClient.getSocketChannel().getRemoteAddress()).getAddress();
		
		TransportString data = new TransportString(request.get("Transport"));

		String[] value = data.getString("client_port").split("-");
		mClientPort = Integer.parseInt(value[0]);
		
		data.set("server_port", mAudioServer.getRTPSocketServer().getPort()+"-"+(mAudioServer.getRTPSocketServer().getPort()+1));
		
		RTSPResponse response = new RTSPResponse(200, "OK");
		response.set("Transport", data.toString());
		response.set("CSeq", request.get("CSeq"));
		response.set("Session", "Patate");
		return response;
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
		packet.writePacket(mSendBuffer, 0);
		try {
			mAudioServer.getRTPSocketServer().sendPacket(mClientAdress, mClientPort, mSendBuffer, length);
		} catch (IOException e) {
			mClient.close();
		}
	}
}
