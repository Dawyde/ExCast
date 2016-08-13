package fr.exensoft.audioserver.core.client.transmiters;

import java.io.IOException;

import fr.exensoft.audioserver.AudioServer;
import fr.exensoft.audioserver.core.AudioStreamManager;
import fr.exensoft.audioserver.core.client.MainClient;
import fr.exensoft.audioserver.core.protocol.MainProtocol;
import fr.exensoft.audioserver.core.protocol.http.HTTPRequest;
import fr.exensoft.audioserver.core.protocol.http.HTTPResponse;
import fr.exensoft.audioserver.core.protocol.rtsp.TransportString;
import fr.exensoft.audioserver.core.source.RemoteAudioSource;
import fr.exensoft.audioserver.core.source.SourceDescriptor;

public class IcecastSourceTransmiter extends ClientDataTransmiter {
	
	public IcecastSourceTransmiter(AudioServer audioServer, AudioStreamManager audioStreamManager, MainClient client) {
		super(audioServer, audioStreamManager, client);
	}
	
	
	private RemoteAudioSource mRemoteAudioSource;
	
	public void newRawData(byte[] buffer, int length) {
		mRemoteAudioSource.addEncodedData(buffer, length);
	}
	@Override
	public HTTPResponse newHTTPRequest(HTTPRequest request) throws IOException {
		
		HTTPResponse response = new HTTPResponse(200, "OK", "HTTP/1.0");
		System.out.println(response);
		mClient.getSocketReader().setProtocol(MainProtocol.ICE);

		mRemoteAudioSource = new RemoteAudioSource(mAudioServer, createDescriptor(request));
		
		mClient.getSocketWriter().sendHTTPResponse(response);
		
		//On enregistre la source audio
		mAudioStreamManager.addAudioSource(mRemoteAudioSource);
		
		return null;
	}
	
	@Override
	public void close() {
		if(mRemoteAudioSource != null){
			mAudioStreamManager.removeAudioSource(mRemoteAudioSource);
			mRemoteAudioSource.close();
		}
	}



	/**
	 * Création du SourceDescriptor avec les bonnes informations
	 * Doit prendre en charge les différentes versions des protocoles Icecast (et Shoutcast si possible)
	 * @param request
	 * @return
	 */
	private SourceDescriptor createDescriptor(HTTPRequest request){
		if(request.getVersion().equals("ICE")){
			return createIcecastV2Descriptor(request);
		}
		else if(request.getType().equals("SOURCE")){
			return createIcecastV1Descriptor(request);
		}
		return null;
	}

	private SourceDescriptor createIcecastV1Descriptor(HTTPRequest request){
		String contentType = request.get("Content-Type");
		if(contentType == null){
			return null;
		}
		
		SourceDescriptor retour = new SourceDescriptor("IcecastV1", contentType.trim());
		
		if(request.get("x-audiocast-bitrate") != null){
			retour.setBitrate(request.getInt("ice-bitrate"));
		}
		
		return retour;
	}
	
	private SourceDescriptor createIcecastV2Descriptor(HTTPRequest request){
		String contentType = request.get("Content-Type");
		if(contentType == null){
			return null;
		}
		
		SourceDescriptor retour = new SourceDescriptor("IcecastV2", contentType.trim());
		
		if(request.get("ice-audio-info") != null) {
			TransportString audioInfo = new TransportString(request.get("ice-audio-info"));
			if(audioInfo.has("ice-bitrate")){
				retour.setBitrate(Integer.parseInt(audioInfo.getString("ice-bitrate")));
			}
		}
		else if(request.get("ice-bitrate") != null){
			retour.setBitrate(request.getInt("ice-bitrate"));
		}
		
		return retour;
	}
	
}
