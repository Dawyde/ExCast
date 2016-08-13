package fr.exensoft.audioserver;

import fr.exensoft.audioserver.codec.manager.CodecManager;
import fr.exensoft.audioserver.core.AudioStreamManager;
import fr.exensoft.audioserver.core.buffermanager.BufferManager;
import fr.exensoft.audioserver.core.client.MainClient;
import fr.exensoft.audioserver.core.protocol.RequestContext;
import fr.exensoft.audioserver.core.router.RequestHandler;
import fr.exensoft.audioserver.core.router.RequestRouter;
import fr.exensoft.audioserver.core.server.UDPSocketServer;

public class AudioServer {
	public final static int PACKET_LENGTH = 1800;
	
	private BufferManager mBufferManager;
	
	private AudioStreamManager mAudioStreamManager;
	
	private UDPSocketServer mRTPSocketServer;
	
	private RequestRouter mRequestRouter;
	
	private CodecManager mCodecManager;
	
	public AudioServer(){
		mBufferManager = new BufferManager();
		
		mRTPSocketServer = new UDPSocketServer(6000);
		
		mRequestRouter = new RequestRouter();
	}
	
	public void setCodecManager(CodecManager codecManager){
		mCodecManager = codecManager;
	}
	
	public CodecManager getCodecManager(){
		return mCodecManager;
	}
	
	public void close(){
		mAudioStreamManager.close();
	}
	
	public RequestRouter getRequestRouter(){
		return mRequestRouter;
	}

	public BufferManager getBufferManager() {
		return mBufferManager;
	}
	
	public void setAudioStreamManager(AudioStreamManager audioStreamManager){
		mAudioStreamManager = audioStreamManager;
	}
	
	public RequestHandler createRequestHandler(MainClient client, RequestContext requestContext){
		return mRequestRouter.createRequestHandler(client, requestContext);
	}
	
	public UDPSocketServer getRTPSocketServer(){
		return mRTPSocketServer;
	}
	
	public void setRTPSocketServer(UDPSocketServer rtpSocketServer){
		mRTPSocketServer = rtpSocketServer;
	}
}
