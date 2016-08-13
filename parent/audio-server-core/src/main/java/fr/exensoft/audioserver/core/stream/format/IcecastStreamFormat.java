package fr.exensoft.audioserver.core.stream.format;

import java.util.LinkedList;

import fr.exensoft.audioserver.core.AudioStreamManager;
import fr.exensoft.audioserver.core.buffermanager.BaseBufferManager;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.core.buffers.BytePacket;
import fr.exensoft.audioserver.core.buffers.RTPPacket;
import fr.exensoft.audioserver.core.client.transmiters.IcecastTransmiter;
import fr.exensoft.audioserver.core.client.transmiters.RTPInterleavedTransmiter;
import fr.exensoft.audioserver.core.client.transmiters.RTPUnicastTransmiter;
import fr.exensoft.audioserver.format.encoder.IcecastFormatEncoder;
import fr.exensoft.audioserver.format.encoder.RTPFormatEncoder;
import fr.exensoft.audioserver.format.encoder.StreamFormat;

public class IcecastStreamFormat extends AudioStreamFormat{
	
	/**
	 * Samples encodés en attente d'envoie
	 */
	private LinkedList<BytePacket> mReadyPackets;
	
	/**
	 * Samples déjà envoyés pour les retardataires
	 */
	private LinkedList<BytePacket> mSentPackets;
	
	/**
	 * Clients abonnés au flux
	 */
	private LinkedList<IcecastTransmiter> mClients;
	
	private AudioStreamManager mAudioStreamManager;
	
	private BaseBufferManager<BytePacket> mIcecastPacketManager;
	
	private IcecastFormatEncoder mIcecastFormatEncoder;
	
	private LinkedList<IcecastTransmiter> mNewClients = new LinkedList<>();
	
	private BytePacket mCurrentPacket = null;
	
	private int mCurrentOffset = 0;
	
	//On va mettre une valeur max de packet pour rester au dessous du MTU
	private final static int MAX_DATA = 250;
	
	private final static int MAX_RTP_DELAY = 20;

	public IcecastStreamFormat(AudioStreamManager audioStreamManager, IcecastFormatEncoder encoder) {
		mReadyPackets = new LinkedList<>();
		mSentPackets = new LinkedList<>();
		mClients = new LinkedList<>();
		
		mAudioStreamManager = audioStreamManager;
		
		BufferManager bufferManager = audioStreamManager.getBufferManager();
		bufferManager.registerBufferManager(BytePacket.class, new BytePacket.IcecastPacketFactory());
		mIcecastPacketManager = bufferManager.getBaseBufferManager(BytePacket.class);
		
		mIcecastFormatEncoder = encoder;
		//mRTPFormatEncoder.setStreamFormatDataListener((buffer, length, start, end)->this.newRTPPacket(buffer, length, start, end));
		mIcecastFormatEncoder.setStreamFormatDataListener(this::newRTPPacket);
		
	}

	@Override
	public StreamFormat getFormat() {
		return StreamFormat.RTP;
	}

	
	public void newRTPPacket(byte[] buffer, int length, long start, long end){
		
		if(mCurrentPacket != null && mCurrentOffset+length > MAX_DATA){
			//On met à jour la taille max du packet
			mCurrentPacket.setLength(mCurrentOffset);
			
			//On ajoute le packet au buffer
			mReadyPackets.add(mCurrentPacket);
			
			//On remet les variables à 0
			mCurrentOffset = 0;
			mCurrentPacket = null;
		}
		
		if(mCurrentPacket == null){
			//On récupère un packet RTP
			mCurrentPacket = mIcecastPacketManager.getPacket();
			
			//On indique le début
			mCurrentPacket.setStartByte(start);
		}

		//On met les offsets
		mCurrentPacket.setEndByte(end);
		
		//On copie les données
		System.arraycopy(buffer, 0, mCurrentPacket.getBytes(), mCurrentOffset, length);
		
		//On met à jour l'offset pour le prochain packet
		mCurrentOffset += length;
	}

	@Override
	public void init() {
		mIcecastFormatEncoder.init();
	}
	
	@Override
	public void close() {
		mIcecastFormatEncoder.close();
		while(!mReadyPackets.isEmpty()){
			mIcecastPacketManager.releasePacket(mReadyPackets.poll());
		}
		while(!mReadyPackets.isEmpty()){
			mIcecastPacketManager.releasePacket(mReadyPackets.poll());
		}
	}

	@Override
	public void sendAudioSamples(long currentBytePosition) {
		if(mReadyPackets.isEmpty()){
			return;
		}
		BytePacket packet;
		
		if(!mNewClients.isEmpty()){
			//Renvoie des derniers packets aux nouveaux clients
			for(IcecastTransmiter client : mNewClients){
				for(BytePacket p : mSentPackets){
					client.sendIcecastPacket(p);
				}
				mClients.add(client);
			}
			mNewClients.clear();
		}
		
		while(!mReadyPackets.isEmpty() && mReadyPackets.getFirst().getStartByte() <= currentBytePosition){
			packet = mReadyPackets.poll();
			
			sendIcecastPacket(packet);
			
			//On met le packet RTP dans la liste des packets déjà envoyés
			mSentPackets.add(packet);
		}
		
		//On libère les packets en trop
		while(mSentPackets.size() > MAX_RTP_DELAY){
			mIcecastPacketManager.releasePacket(mSentPackets.poll());
		}
	}
	
	
	public String getContentType(){
		return mIcecastFormatEncoder.getContentType();
	}
	
	public synchronized void sendIcecastPacket(BytePacket packet){
		for(IcecastTransmiter client : mClients){
			client.sendIcecastPacket(packet);
		}
	}
	
	
	public synchronized void registerClient(IcecastTransmiter client){
		mNewClients.add(client);
	}
	
	public synchronized void removeClient(IcecastTransmiter client){
		mClients.remove(client);
		mNewClients.remove(client);
	}
}
