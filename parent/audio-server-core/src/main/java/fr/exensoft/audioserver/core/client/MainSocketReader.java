package fr.exensoft.audioserver.core.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import fr.exensoft.audioserver.core.protocol.MainProtocol;
import fr.exensoft.audioserver.core.protocol.http.HTTPRequest;
import fr.exensoft.audioserver.core.protocol.rtsp.InterleavedPacket;
import fr.exensoft.audioserver.core.protocol.rtsp.RTSPRequest;
import fr.exensoft.audioserver.core.server.MainSocketProcessRead;

public class MainSocketReader {
	
	private static enum DataType{
		NONE, BODY, INTERLEAVED_PACKET, LINE, RAW
	}
	
	private MainClient mRTSPClient;
	
	private SocketChannel mSocketChannel;
	
	private MainSocketProcessRead mSocketProcessRead;

	private StringBuffer mCurrentLine = new StringBuffer(128);
	
	private RTSPRequest mRTSPRequest = null;
	
	private HTTPRequest mHTTPRequest = null;
	
	private InterleavedPacket mInterleavedPacket = null;
	
	private int mEndLine = 0;
	
	private MainProtocol mProtocol = MainProtocol.UNKNOWN;
	
	private byte[] mBuffer = null;
	
	private DataType mDataType = DataType.NONE;
	
	public MainSocketReader(MainClient rtspClient, MainSocketProcessRead rtspSocketProcessRead, SocketChannel socketChannel){
		mRTSPClient = rtspClient;
		mSocketChannel = socketChannel;
		mSocketProcessRead = rtspSocketProcessRead;
	}
	
	public void setProtocol(MainProtocol protocol){
		mProtocol = protocol;
		if(mProtocol == MainProtocol.INTERLEAVED_RTSP && mInterleavedPacket == null){
			mInterleavedPacket = new InterleavedPacket();
		}
		else if(mProtocol == MainProtocol.ICE){
			if(mBuffer == null) {
				mBuffer = new byte[4096];
			}
			mDataType = DataType.RAW;
		}
	}


	public void close() throws IOException {
		mSocketProcessRead.removeClient(mRTSPClient);
	}
	
	/**
	 * Fonction à appeler lorsque le channel est prêt pour la lecture
	 * 
	 * @param buffer
	 * @throws IOException
	 */
	public void read(SelectionKey key, ByteBuffer buffer) throws IOException {
		int len = mSocketChannel.read(buffer);
		if (len == 0) {
			buffer.clear();
			return;
		}
		if (len == -1) {// Fermeture
			mRTSPClient.close();
			return;
		}
		buffer.flip();

		try {
			// On va lire le message
			int i = 0;
			byte current;
			while (i < len) {
				current = buffer.get();
				if(mDataType == DataType.RAW){
					mBuffer[i] = current;
				}
				else if(mDataType == DataType.LINE){
					readLine(current);
				}
				else if(mDataType == DataType.INTERLEAVED_PACKET){
					readInterleavedPacket(current);
				}
				else{
					//Inconnu ?
					
					if(mProtocol == MainProtocol.INTERLEAVED_RTSP && current == '$'){//On regarde s'il s'agit d'un packet INTERLEAVED
						mDataType = DataType.INTERLEAVED_PACKET;
						mInterleavedPacket.reset();
					}
					else{//Sinon on passe en mode ligne
						mDataType = DataType.LINE;
						readLine(current);
					}
				}
				i++;
			}
			
			if(mDataType == DataType.RAW){
				mRTSPClient.newRawData(mBuffer, len);
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readInterleavedPacket(byte b) throws IOException {
		if(mInterleavedPacket.newByte(b)){
			//Fin du packet interleaved
			mRTSPClient.newInterleavedPacket(mInterleavedPacket);
			mDataType = DataType.NONE;
		}
	}

	/**
	 * Nouveau caractère recu dans la lecture d'une ligne
	 * 
	 * @param b
	 *            Nouveau caractère recu
	 * @throws ExInvalidRequestException
	 */
	private void readLine(byte b) throws Exception {
		if (b == '\r' && mEndLine == 0) mEndLine = 1;
		else if (b == '\n' && mEndLine == 1) {
			// Fin d'une ligne
			mEndLine = 0;
			newLine(mCurrentLine.toString());
			mCurrentLine.setLength(0);
		}
		else {
			if (mEndLine == 1) {
				mCurrentLine.append('\r');
				mEndLine = 0;
			}
			mCurrentLine.append((char) b);
		}
	}

	/**
	 * Nouvelle ligne lue
	 * 
	 * @param line
	 *            Ligne reçue
	 */
	private void newLine(String line) throws Exception {
		if(mRTSPRequest == null && mHTTPRequest == null){
			String[] data = line.trim().split(" ", 3);
			if (data.length != 3) return;
			
			if(data[2].equals("RTSP/1.0")){//Requête RTSP
				mRTSPRequest = new RTSPRequest(data[0], data[1]);
			}
			else if(data[2].startsWith("HTTP") || data[2].startsWith("ICE") || data[0].startsWith("SOURCE")){
				mHTTPRequest = new HTTPRequest(data[0], data[1], data[2]);
			}
			else{
				System.out.println(line);
				throw new Exception("Protocole invalide "+data[2]);
			}
		}
		else if(mRTSPRequest != null){
			if(!readRTSPRequest(mRTSPRequest, line)){
				//Requête terminée
				mDataType = DataType.NONE;
				mRTSPClient.newRTSPRequest(mRTSPRequest);
				mRTSPRequest = null;
			}
		}
		else if(mHTTPRequest != null){
			if(!readHTTPRequest(mHTTPRequest, line)){
				//Requête terminée
				mDataType = DataType.NONE;
				mRTSPClient.newHTTPRequest(mHTTPRequest);
				mHTTPRequest = null;
			}
		}
	}
	

	/**
	 * Complète une requête RTSP avec une ligne d'entête supplémentaire. La
	 * fonction retourne true si la ligne d'entête qui lui a été envoyée est
	 * valide, elle retourne false lorsque la ligne n'est pas un entête valide,
	 * ce qui indique en général que la requête est complète
	 * 
	 * @param request
	 *            L'objet RTSPRequest à compléter
	 * @param line
	 *            La ligne à ajouter dans la requête
	 * @return Retourne true si la chaine line contient un entête valide.
	 */
	public static boolean readRTSPRequest(RTSPRequest request, String line) {
		line = line.trim();
		if (line.isEmpty()) return false;

		String[] data = line.split(":", 2);
		if (data.length != 2) return false;// Erreur

		request.set(data[0], data[1]);

		return true;
	}
	

	/**
	 * Complète une requête HTTP avec une ligne d'entête supplémentaire. La
	 * fonction retourne true si la ligne d'entête qui lui a été envoyée est
	 * valide, elle retourne false lorsque la ligne n'est pas un entête valide,
	 * ce qui indique en général que la requête est complète
	 * 
	 * @param request
	 *            L'objet HTTPRequest à compléter
	 * @param line
	 *            La ligne à ajouter dans la requête
	 * @return Retourne true si la chaine line contient un entête valide.
	 */
	public static boolean readHTTPRequest(HTTPRequest request, String line) {
		line = line.trim();
		if (line.isEmpty()) return false;

		String[] data = line.split(":", 2);
		if (data.length != 2) return false;// Erreur

		request.set(data[0], data[1]);

		return true;
	}

}
