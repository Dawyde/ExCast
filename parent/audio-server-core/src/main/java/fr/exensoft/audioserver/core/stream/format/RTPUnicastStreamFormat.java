package fr.exensoft.audioserver.core.stream.format;

import java.util.LinkedList;

import fr.exensoft.audioserver.core.AudioStreamManager;
import fr.exensoft.audioserver.core.buffermanager.BaseBufferManager;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.core.buffers.RTPPacket;
import fr.exensoft.audioserver.core.client.transmiters.RTPUnicastTransmiter;
import fr.exensoft.audioserver.format.encoder.RTPFormatEncoder;
import fr.exensoft.audioserver.format.encoder.StreamFormat;

public class RTPUnicastStreamFormat extends AudioStreamFormat{
	
	/**
	 * Samples encodés en attente d'envoie
	 */
	private LinkedList<RTPPacket> mReadyPackets;
	
	/**
	 * Samples déjà envoyés pour les retardataires
	 */
	private LinkedList<RTPPacket> mSentPackets;
	
	/**
	 * Clients abonnés au flux
	 */
	private LinkedList<RTPUnicastTransmiter> mClients;
	
	private AudioStreamManager mAudioStreamManager;
	
	private int mSequenceNumber = 0;
	
	private BaseBufferManager<RTPPacket> mRTPPacketManager;
	
	private RTPFormatEncoder mRTPFormatEncoder;
	
	private LinkedList<RTPUnicastTransmiter> mNewClients = new LinkedList<>();
	
	private final static int MAX_RTP_DELAY = 10;

	public RTPUnicastStreamFormat(AudioStreamManager audioStreamManager, RTPFormatEncoder encoder) {
		// TODO Auto-generated constructor stub
		mReadyPackets = new LinkedList<>();
		mSentPackets = new LinkedList<>();
		mClients = new LinkedList<>();
		
		mAudioStreamManager = audioStreamManager;
		
		BufferManager bufferManager = audioStreamManager.getBufferManager();
		bufferManager.registerBufferManager(RTPPacket.class, new RTPPacket.RTPPacketFactory());
		mRTPPacketManager = bufferManager.getBaseBufferManager(RTPPacket.class);
		
		mRTPFormatEncoder = encoder;
		//mRTPFormatEncoder.setStreamFormatDataListener((buffer, length, start, end)->this.newRTPPacket(buffer, length, start, end));
		mRTPFormatEncoder.setStreamFormatDataListener(this::newRTPPacket);
		
	}

	@Override
	public StreamFormat getFormat() {
		return StreamFormat.RTP;
	}

	
	public void newRTPPacket(byte[] buffer, int length, long start, long end){
		//On récupère un packet RTP
		RTPPacket rtpPacket = mRTPPacketManager.getPacket();
		//On met le payload
		rtpPacket.setPayload(buffer, length);
		//On met les offsets
		rtpPacket.setStartByte(start);
		rtpPacket.setEndByte(end);
		//Mise à jour du numéro de séquence
		rtpPacket.setSequenceNumber(mSequenceNumber);
		mSequenceNumber++;
		//On met le timestamp (90000 unités par second)
		long timestamp = 90000*start/mAudioStreamManager.getBytesPerSecond();
		rtpPacket.setTimestamp(timestamp);
		//On met le type à jours
		rtpPacket.setPayloadType(mRTPFormatEncoder.getPayloadType());
		
		//Puis on met le paquet dans la queue
		mReadyPackets.add(rtpPacket);
	}

	@Override
	public void init() {
		mRTPFormatEncoder.init();
	}
	
	@Override
	public void close() {
		mRTPFormatEncoder.close();
		//TODO : libérer les packets
	}

	@Override
	public void sendAudioSamples(long currentBytePosition) {
		if(mReadyPackets.isEmpty()){
			return;
		}
		RTPPacket packet;
		
		if(!mNewClients.isEmpty()){
			//Renvoie des derniers packets aux nouveaux clients
			for(RTPUnicastTransmiter client : mNewClients){
				for(RTPPacket p : mSentPackets){
					client.sendRTPPacket(p);
				}
				mClients.add(client);
			}
			mNewClients.clear();
		}
		
		while(!mReadyPackets.isEmpty() && mReadyPackets.getFirst().getStartByte() <= currentBytePosition){
			packet = mReadyPackets.poll();
			
			sendRTPPacket(packet);
			
			//On met le packet RTP dans la liste des packets déjà envoyés
			mSentPackets.add(packet);
		}
		
		//On libère les packets en trop
		while(mSentPackets.size() > MAX_RTP_DELAY){
			mRTPPacketManager.releasePacket(mSentPackets.poll());
		}
	}
	
	
	public String getSDPDescription(){
		return mRTPFormatEncoder.getDescribe("- 0 0 IN IP4 192.168.1.26", "Test");
	}
	
	public synchronized void sendRTPPacket(RTPPacket packet){
		for(RTPUnicastTransmiter client : mClients){
			client.sendRTPPacket(packet);
		}
	}
	
	public void sendDelayedPackets(RTPUnicastTransmiter client){
		if(mSentPackets.size() < 5){
			return;
		}
		for(int i=5;i<mSentPackets.size();i++){
			RTPPacket packet = mSentPackets.get(i);
			if(packet != null){
				client.sendRTPPacket(packet);
			}
		}
	}
	
	public synchronized void registerClient(RTPUnicastTransmiter client){
		mNewClients.add(client);
	}
	
	public synchronized void removeClient(RTPUnicastTransmiter client){
		mClients.remove(client);
		mNewClients.remove(client);
	}
}
