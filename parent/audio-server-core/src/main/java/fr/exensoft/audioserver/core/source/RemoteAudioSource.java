package fr.exensoft.audioserver.core.source;

import java.util.LinkedList;

import fr.exensoft.audioserver.codec.decoder.StreamDecoder;
import fr.exensoft.audioserver.AudioServer;
import fr.exensoft.audioserver.core.AudioSourceManager;
import fr.exensoft.audioserver.core.buffermanager.BaseBufferManager;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.core.buffers.BytePacket;

public class RemoteAudioSource implements AudioSource{
	
	//Decodeur audio pour le stream
	private StreamDecoder mDecoder = null;
	
	//Buffer de packets à décoder
	private LinkedList<BytePacket> mPackets = new LinkedList<>();
	
	//Manager de packets
	private BaseBufferManager<BytePacket> mBytePacketManager;
	
	
	//Gestionnaire de flux audio source à qui on envoie nos données
	private AudioSourceManager mAudioSourceManager;
	
	public RemoteAudioSource(AudioServer audioServer, SourceDescriptor sourceDescriptor) {
		BufferManager bufferManager = audioServer.getBufferManager();
		
		mBytePacketManager = bufferManager.getBaseBufferManager(BytePacket.class);
		
		mDecoder = createDecoder(audioServer, sourceDescriptor);
		
		//Quand on a de nouvelles données audio de prête on les envoie au gestionnaire
		mDecoder.setAudioSampleListener((buffer, length)->{
			mAudioSourceManager.newAudioSamples(buffer, length);
		});
		
		mDecoder.init();
	}
	
	
	private StreamDecoder createDecoder(AudioServer audioServer, SourceDescriptor sourceDescriptor){
		StreamDecoder decoder = audioServer.getCodecManager().createDecoderFromMimeType(audioServer.getBufferManager(), sourceDescriptor.getAudioFormat());
		
		decoder.setBitrate(sourceDescriptor.getBitrate());
		return decoder;
	}
	
	public void addEncodedData(byte[] buffer, int length){
		int l;
		BytePacket packet;
		int offset = 0;
		while(length > 0){
			if(length > BytePacket.MAX_LENGTH){
				l = BytePacket.MAX_LENGTH;
			}
			else{
				l = length;
			}
			packet = mBytePacketManager.getPacket();
			System.arraycopy(buffer, offset, packet.getBytes(), 0, l);
			packet.setLength(l);
			offset += l;
			length -= l;
			mPackets.add(packet);
		}
	}

	@Override
	public void process() {
		BytePacket packet;
		int i = 0;
		int length = mPackets.size();
		if(length == 0){
			checkDelay();
		}
		while(i<length){
			packet = mPackets.poll();
			mDecoder.decodeAudioData(packet.getBytes(), 0, packet.getLength());
			mBytePacketManager.releasePacket(packet);
			i++;
		}
		
	}

	@Override
	public void close() {
		mDecoder.close();
	}

	public void checkDelay(){
		long delay = mAudioSourceManager.getBytesDelay();
		long bytesPerSecond = mAudioSourceManager.getBytesPerSecond();
		
		if(delay > bytesPerSecond/2){
			long msDelay = (delay*1000/bytesPerSecond);
			System.out.println("Délais : "+msDelay);
			mAudioSourceManager.addWhiteNoise(delay+bytesPerSecond/2);
		}
		else{
			try{
				Thread.sleep(100);
			}
			catch(Exception e){}
		}
	}

	@Override
	public void setAudioSourceManager(AudioSourceManager audioSourceManager) {
		mAudioSourceManager = audioSourceManager;
		mAudioSourceManager.addWhiteNoise(mAudioSourceManager.getBytesPerSecond());
	}

}
