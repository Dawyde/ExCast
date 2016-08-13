package fr.exensoft.audioserver.format.encoder;

public interface StreamDataListener {
	void newPacket(byte[] buffer, int length, long startByte, long endByte);
}
