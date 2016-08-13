package fr.exensoft.audioserver.codec.libfdk;

import java.io.IOException;

import fr.exensoft.audioserver.NativeUtils;

public class LibFDKWrapper {
	static {
	    try{
	    	System.loadLibrary("fdkwrapper");
	    }
	    catch(UnsatisfiedLinkError e){
	    	try {
				NativeUtils.loadLibraryFromJar("/fdkwrapper.dll");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    }
	    load();
	}
	private long mWrapperPtr = -1;
	
	private native static void load();
	
	public native void init() throws NativeFDKException;
	
	public native void open() throws NativeFDKException;
	
	public native void close() throws NativeFDKException;
	
	/**
	 * Détermine le profile aot à utiliser
	 * @param aot Profile à utiliser 
	 * @return True en cas de succès 
	 * @throws NativeFDKException Une exception si l'objet n'est pas ouvert et non initilalisé
	 */
	public native boolean setAOT(int aot) throws NativeFDKException;
	
	public native boolean setChannelMode(int channelMode) throws NativeFDKException;
	
	public native boolean setBitrate(int bitrate) throws NativeFDKException;
	
	public native boolean setSampleRate(int bitrate) throws NativeFDKException;

	public native boolean setBitrateMode(int value) throws NativeFDKException;

	public native boolean setTransmux(int value) throws NativeFDKException;

	public native boolean setChannelOrder(int value) throws NativeFDKException;
	
	public native boolean setSbrMode(int value) throws NativeFDKException;
	
	public native boolean setAfterBurner(boolean enable) throws NativeFDKException;

	// LES GETTERS 

	public native int getAOT() throws NativeFDKException;
	
	public native int getChannelMode() throws NativeFDKException;
	
	public native int getBitrate() throws NativeFDKException;
	
	public native int getSampleRate() throws NativeFDKException;

	public native int getBitrateMode() throws NativeFDKException;

	public native int getTransmux() throws NativeFDKException;

	public native int getChannelOrder() throws NativeFDKException;
	
	public native int getSbrMode() throws NativeFDKException;
	
	public native int getAfterBurner() throws NativeFDKException;
	
	
	public native int encodeAudio(byte[] outputBuffer, byte[] inputBuffer, int intputLength);
}
