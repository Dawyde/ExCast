package fr.exensoft.audioserver.core.client.transmiters;

import fr.exensoft.audioserver.AudioServer;
import fr.exensoft.audioserver.core.AudioStreamManager;
import fr.exensoft.audioserver.core.buffers.BytePacket;
import fr.exensoft.audioserver.core.client.MainClient;
import fr.exensoft.audioserver.core.protocol.http.HTTPRequest;
import fr.exensoft.audioserver.core.protocol.http.HTTPResponse;
import fr.exensoft.audioserver.core.stream.format.IcecastStreamFormat;

public class IcecastTransmiter extends ClientDataTransmiter{
	
	private final static int METADATA_METAINT = 16000;

	private IcecastStreamFormat mIcecastStreamFormat;
	
	private boolean mMetadataActive = false;
	
	private int mMetadataCounter = 0;
	
	private byte[] mMetadataBuffer = null;
	
	public IcecastTransmiter(AudioServer audioServer, AudioStreamManager audioStreamManager, MainClient client, IcecastStreamFormat icecastStreamFormat) {
		super(audioServer, audioStreamManager, client);
		mIcecastStreamFormat = icecastStreamFormat;
	}

	@Override
	public void close() {
		mIcecastStreamFormat.removeClient(this);
	}

	@Override
	public HTTPResponse newHTTPRequest(HTTPRequest request){
		System.out.println(request);
		//HTTPResponse response = new HTTPResponse(200, "OK", "HTTP/1.1");
		HTTPResponse response = new HTTPResponse(200, "OK", "HTTP/1.1");

		//response.set("Accept-Ranges","bytes");
		response.set("Access-Control-Allow-Headers","Origin, Accept, X-Requested-With, Content-Type");
		response.set("Access-Control-Allow-Methods","GET, OPTIONS, HEAD");
		response.set("Access-Control-Allow-Origin", "*");
		response.set("Cache-Control", "no-cache, no-store");
		response.set("Connection", "Close");
		response.set("Expires","Mon, 26 Jul 1997 05:00:00 GMT");
		response.set("Pragma", "no-cache");
		response.set("Server", "SERVERNAME2");
		response.set("ice-audio-info", "ice-samplerate=44100;ice-channels=2;ice-bitrate=128");
		response.set("icy-br", "128");
		
		if(request.get("icy-metadata") != null && request.get("icy-metadata").trim().equals("1")){
			System.out.println("Metadata !!!!!!!!!!!!!!");
			mMetadataActive = true;
			mMetadataBuffer = new byte[4096];
			response.set("icy-metaint", String.valueOf(METADATA_METAINT));
		}
		response.set("content-type", "audio/aac");
		
		mClient.getSocketWriter().sendHTTPResponse(response);
		
		mIcecastStreamFormat.registerClient(this);
		
		return null;
	}

	int mValue = 1;
	public synchronized void sendIcecastPacket(BytePacket p) {
		if(mMetadataActive){
			if(mMetadataCounter+p.getLength() > METADATA_METAINT){
				int len_before = METADATA_METAINT-mMetadataCounter;
				System.out.println(mMetadataCounter+" -- "+METADATA_METAINT);
				int len_after = p.getLength()-len_before;
				int offset = 0;
				//On place les données avant les métadonnées
				if(len_before > 0){
					System.arraycopy(p.getBytes(), 0, mMetadataBuffer, 0, len_before);
					offset += len_before;
				}
				//On place les métadonnées
				String data = String.format("StreamTitle='t123456789-%05d';\0", mValue);
				mValue++;
				mMetadataBuffer[offset] = 2;
				offset++;
				
				System.arraycopy(data.getBytes(), 0, mMetadataBuffer, offset, data.length());
				offset+=data.length();
				System.out.println(data.length());
				//On place les données apres les métadonnées
				if(len_after > 0){
					System.arraycopy(p.getBytes(), len_before, mMetadataBuffer, offset, len_after);
					offset += len_after;
				}
				mClient.getSocketWriter().write(mMetadataBuffer, 0, offset);
				System.out.println(len_before+" - "+len_after+" -- "+p.getLength()+" -- "+offset);
				mMetadataCounter = len_after;
				return;
			}
			else{
				mMetadataCounter += p.getLength();
			}
		}
		mClient.getSocketWriter().write(p.getBytes(), 0, p.getLength());
	}
}
