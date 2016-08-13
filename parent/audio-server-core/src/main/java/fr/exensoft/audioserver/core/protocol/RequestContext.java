package fr.exensoft.audioserver.core.protocol;

public class RequestContext {
	private MainProtocol mProtocol;
	private int mPort;
	private String mUri;
	
	public RequestContext(MainProtocol protocol, int port, String uri){
		mProtocol = protocol;
		mPort = port;
		mUri = uri;
	}
	
	public int getPort(){
		return mPort;
	}
	
	public String getUri(){
		return mUri;
	}
	
	public MainProtocol getProtocol(){
		return mProtocol;
	}
}
