package fr.exensoft.audioserver.core.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.exensoft.audioserver.AudioServer;
import fr.exensoft.audioserver.core.protocol.MainProtocol;
import fr.exensoft.audioserver.core.protocol.RequestContext;
import fr.exensoft.audioserver.core.protocol.http.HTTPRequest;
import fr.exensoft.audioserver.core.protocol.http.HTTPResponse;
import fr.exensoft.audioserver.core.protocol.rtsp.InterleavedPacket;
import fr.exensoft.audioserver.core.protocol.rtsp.RTSPRequest;
import fr.exensoft.audioserver.core.protocol.rtsp.RTSPResponse;
import fr.exensoft.audioserver.core.router.RequestHandler;
import fr.exensoft.audioserver.core.server.MainSocketProcessRead;

public class MainClient {
	
	private MainSocketReader mSocketReader;
	
	private MainSocketWriter mSocketWriter;
	
	private SocketChannel mSocketChannel;
	
	private RequestHandler mRequestHandler = null;
	
	private AudioServer mAudioServer;
	
	private String mRemoteAddress = "";
	
	private int mServerPort = -1;
	
	private boolean mClosed = false;
	
	public final static SimpleDateFormat sDateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);
	
	public MainClient(AudioServer audioServer, MainSocketProcessRead rtspSocketProcessRead, SocketChannel channel) {
		mSocketChannel = channel;
		mAudioServer = audioServer;
		mSocketReader = new MainSocketReader(this, rtspSocketProcessRead, channel);
		mSocketWriter = new MainSocketWriter(this, rtspSocketProcessRead.getProcessWrite(), channel, rtspSocketProcessRead.getPacketBufferManager());
		try{
			mServerPort = ((InetSocketAddress) mSocketChannel.getLocalAddress()).getPort();
			mRemoteAddress = mSocketChannel.getRemoteAddress().toString();

			channel.socket().setTcpNoDelay(true);
		}
		catch(IOException e){
			mRemoteAddress = "Unknown";
		}
	}
	
	public String toString(){
		return mRemoteAddress;
	}
	
	
	public MainSocketReader getSocketReader(){
		return mSocketReader;
	}
	
	public MainSocketWriter getSocketWriter(){
		return mSocketWriter;
	}
	
	public void close() {
		try {
			if(mClosed){
				return;
			}
			mClosed = true;
			System.out.println("Fermeture du client "+mRemoteAddress);
			
			//On ferme le coté applicatif en premier
			if(mRequestHandler != null){
				mRequestHandler.close();
			}
			
			//On ferme ensuite les canaux de communication
			mSocketReader.close();
			mSocketWriter.close();
			
			//Puis on ferme le canal socket
			mSocketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SocketChannel getSocketChannel() {
		return mSocketChannel;
	}

	public void newRTSPRequest(RTSPRequest request) throws IOException {
		if(mRequestHandler == null){
			mRequestHandler = mAudioServer.createRequestHandler(this, new RequestContext(MainProtocol.RTSP, mServerPort, request.getUrl()));
			if(mRequestHandler == null){
				return;
			}
		}
		RTSPResponse response = mRequestHandler.newRTSPRequest(request);
		if(response != null){
			response.set("Date", sDateFormat.format(new Date()));
			mSocketWriter.sendRTSPResponse(response);
		}
	}
	
	public boolean isClosed(){
		return mClosed;
	}

	public void newHTTPRequest(HTTPRequest request) throws IOException {
		if(mRequestHandler == null){
			mRequestHandler = mAudioServer.createRequestHandler(this, new RequestContext(request.getVersion().startsWith("HTTP")?MainProtocol.HTTP:MainProtocol.ICE, mServerPort, request.getUrl()));
			if(mRequestHandler == null){
				//On indique que y'a surement une erreur
				mSocketWriter.sendHTTPResponse(createError(request));
				return;
			}
		}
		
		HTTPResponse response = mRequestHandler.newHTTPRequest(request);
		
		if(response != null){
			response.set("Date", sDateFormat.format(new Date()));
			mSocketWriter.sendHTTPResponse(response);
		}
	}

	private HTTPResponse createError(HTTPRequest request) {
		String body = "<html><head><title>Coucou</title></head><body><audio src='http://192.168.1.29:600/test.aac' controls='true' width='120' height='40'></audio></body></html>";
		HTTPResponse response = new HTTPResponse(404, "Not Found", request.getVersion().startsWith("HTTP")?request.getVersion():"HTTP1/1");
		response.setBody(body);
		response.set("Content-Length", String.valueOf(body.length()));
		return response;
	}

	public void newInterleavedPacket(InterleavedPacket interleavedPacket) throws IOException {
		if(mRequestHandler != null){
			mRequestHandler.newInterleavedPacket(interleavedPacket);
		}
	}

	public void newRawData(byte[] mBuffer, int len) {
		if(mRequestHandler != null){
			mRequestHandler.newRawData(mBuffer, len);
		}
	}

}
