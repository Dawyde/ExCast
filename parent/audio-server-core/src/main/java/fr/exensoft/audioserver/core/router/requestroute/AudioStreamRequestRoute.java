package fr.exensoft.audioserver.core.router.requestroute;

import java.util.LinkedList;
import java.util.List;

import fr.exensoft.audioserver.AudioServer;
import fr.exensoft.audioserver.core.AudioStreamManager;
import fr.exensoft.audioserver.core.client.MainClient;
import fr.exensoft.audioserver.core.client.transmiters.IcecastTransmiter;
import fr.exensoft.audioserver.core.client.transmiters.RTPInterleavedTransmiter;
import fr.exensoft.audioserver.core.client.transmiters.RTPUnicastTransmiter;
import fr.exensoft.audioserver.core.protocol.MainProtocol;
import fr.exensoft.audioserver.core.protocol.RequestContext;
import fr.exensoft.audioserver.core.router.RequestHandler;
import fr.exensoft.audioserver.core.stream.AudioStream;
import fr.exensoft.audioserver.core.stream.format.IcecastStreamFormat;
import fr.exensoft.audioserver.core.stream.format.RTPInterleavedStreamFormat;
import fr.exensoft.audioserver.core.stream.format.RTPUnicastStreamFormat;
import fr.exensoft.audioserver.format.encoder.StreamFormat;

public class AudioStreamRequestRoute extends AbstractRequestRoute {

	private AudioStreamManager mAudioStreamManager;
	
	private AudioStream mAudioStream;
	
	private AudioServer mAudioServer;
	
	/**
	 * Format de stream qui sera envoyé
	 */
	private StreamFormat mStreamFormat;
	
	public static class Builder{
		private AudioServer mAudioServer;
		private AudioStream mAudioStream;
		private AudioStreamManager mAudioStreamManager;
		private List<Integer> mPorts = null;
		private MainProtocol mMainProtocol;
		private String mUri;
		private StreamFormat mStreamFormat;
		
		public Builder withAudioServer(AudioServer audioServer){
			mAudioServer = audioServer;
			return this;
		}
		
		public Builder withAudioStream(AudioStream audioStream){
			mAudioStream = audioStream;
			return this;
		}
		
		public Builder withAudioStreamManager(AudioStreamManager audioStreamManager){
			mAudioStreamManager = audioStreamManager;
			return this;
		}
		
		public Builder addPort(int port){
			if(mPorts == null){
				mPorts = new LinkedList<>();
			}
			mPorts.add(port);
			return this;
		}
		
		public Builder withMainProtocol(MainProtocol mainProtocol){
			mMainProtocol = mainProtocol;
			return this;
		}
		
		public Builder withUri(String uri){
			mUri = uri;
			return this;
		}
		
		public Builder withStreamFormat(StreamFormat streamFormat){
			mStreamFormat = streamFormat;
			return this;
		}
		
		public AudioStreamRequestRoute build(){
			return new AudioStreamRequestRoute(this);
		}
	}
	
	private AudioStreamRequestRoute(Builder builder) {
		super(builder.mUri, builder.mMainProtocol, builder.mPorts);
		mAudioStream = builder.mAudioStream;
		mAudioStreamManager = builder.mAudioStreamManager;
		mAudioServer = builder.mAudioServer;
		mStreamFormat = builder.mStreamFormat;
	}
	

	@Override
	protected RequestHandler createRequestHandler(MainClient client, RequestContext context) {
		//Tout semble OK, on va créer le transmitter pour prendre en charge la diffusion
		if(mStreamFormat == StreamFormat.ICECAST){
			return new IcecastTransmiter(mAudioServer, mAudioStreamManager, client, (IcecastStreamFormat) mAudioStream.getStreamFormatEncoder(StreamFormat.ICECAST));
		}
		if(mStreamFormat == StreamFormat.INTERLEAVED_RTP){
			return new RTPInterleavedTransmiter(mAudioServer, mAudioStreamManager, client, (RTPInterleavedStreamFormat) mAudioStream.getStreamFormatEncoder(StreamFormat.INTERLEAVED_RTP));
		}
		if(mStreamFormat == StreamFormat.RTP){
			return new RTPUnicastTransmiter(mAudioServer, mAudioStreamManager, client, (RTPUnicastStreamFormat) mAudioStream.getStreamFormatEncoder(StreamFormat.RTP));
		}
		return null;
	}

}
