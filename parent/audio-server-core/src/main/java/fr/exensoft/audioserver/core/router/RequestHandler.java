package fr.exensoft.audioserver.core.router;

import java.io.IOException;

import fr.exensoft.audioserver.core.protocol.http.HTTPRequest;
import fr.exensoft.audioserver.core.protocol.http.HTTPResponse;
import fr.exensoft.audioserver.core.protocol.rtsp.InterleavedPacket;
import fr.exensoft.audioserver.core.protocol.rtsp.RTSPRequest;
import fr.exensoft.audioserver.core.protocol.rtsp.RTSPResponse;

public interface RequestHandler {

	default void close(){}
	
	default RTSPResponse newRTSPRequest(RTSPRequest rtspRequest) throws IOException {return null;}
	
	default HTTPResponse newHTTPRequest(HTTPRequest rtspRequest) throws IOException {return null;}

	default void newInterleavedPacket(InterleavedPacket interleavedPacket) throws IOException{}
	
	default void newRawData(byte[] buffer, int length){}
}
