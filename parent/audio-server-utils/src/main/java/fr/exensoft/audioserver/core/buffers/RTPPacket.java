package fr.exensoft.audioserver.core.buffers;

public class RTPPacket implements BaseBuffer{

	public static class RTPPacketFactory implements BaseBufferFactory<RTPPacket>{
		@Override
		public RTPPacket create() {
			return new RTPPacket();
		}
	}
	
	// Octet 1
	protected byte mVersion = 2;
	protected boolean mPadding = false;
	protected boolean mExtension = false;
	protected byte mContributingSourceCount = 0;
	
	protected long mStartByte = 0;
	protected long mEndByte = 0;

	protected long mTimeMillis = 0;

	// Octet 2
	protected boolean mMarked = false;
	protected int mPayloadType = 0;

	// Sequence number (octet 3-4)
	protected int mSequenceNumber = 0;

	// Timestamp (octet 5-8)
	protected long mTimestamp = 0;

	// Source identifier
	protected int mSourceIdentifier = 0;

	// Payload
	protected byte[] mPayload = new byte[2048];
	protected int mPayloadLength = 0;

	public RTPPacket() {

	}

	public int getPayloadType() {
		return mPayloadType;
	}

	public int getSequenceNumber() {
		return mSequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		mSequenceNumber = sequenceNumber;
	}

	public int writePacket(byte[] out, int offset) {

		out[offset] = (byte) (mVersion << 6 | (mPadding ? 32 : 0) | (mPadding ? 16 : 0) | mContributingSourceCount);

		out[offset + 1] = (byte) (mPayloadType);

		out[offset + 2] = (byte) (mSequenceNumber >> 8);
		out[offset + 3] = (byte) (mSequenceNumber & 255);

		out[offset + 4] = (byte) (mTimestamp >> 24);
		out[offset + 5] = (byte) ((mTimestamp >> 16) & 255);
		out[offset + 6] = (byte) ((mTimestamp >> 8) & 255);
		out[offset + 7] = (byte) (mTimestamp & 255);

		out[offset + 8] = (byte) (mSourceIdentifier >> 24);
		out[offset + 9] = (byte) ((mSourceIdentifier >> 16) & 255);
		out[offset + 10] = (byte) ((mSourceIdentifier >> 8) & 255);
		out[offset + 11] = (byte) (mSourceIdentifier & 255);
		

		System.arraycopy(mPayload, 0, out, offset + 12, mPayloadLength);
		return mPayloadLength+12;
	}

	public void parsePacket(byte[] bytes, int length) {
		int tmp = bytes[0] & 255;
		mVersion = (byte) (tmp >> 6);
		mPadding = (tmp & 32) != 0;
		mExtension = (tmp & 16) != 0;
		mContributingSourceCount = (byte) (tmp & 15);

		tmp = bytes[1] & 255;
		mPayloadType = tmp & 127;
		mMarked = (tmp & 128) != 0;

		mSequenceNumber = ((bytes[2] & 255) << 8) | (bytes[3] & 255);

		mTimestamp = ((bytes[4] & 255) << 24) | ((bytes[5] & 255) << 16) | ((bytes[6] & 255) << 8) | (bytes[7] & 255);

		mSourceIdentifier = ((bytes[8] & 255) << 24) | ((bytes[9] & 255) << 16) | ((bytes[10] & 255) << 8) | (bytes[11] & 255);

		mPayloadLength = length - 12;

		mTimeMillis = System.currentTimeMillis();

		System.arraycopy(bytes, 12, mPayload, 0, mPayloadLength);

	}

	public long getTimestamp() {

		return mTimestamp;
	}

	public long getTimeMillis() {
		return mTimeMillis;
	}

	public byte[] getPayload() {
		return mPayload;
	}

	public void setPayload(byte[] payload) {
		mPayloadLength = payload.length;
		System.arraycopy(payload, 0, mPayload, 0, mPayloadLength);
	}
	
	public void setPayload(byte[] payload, int length) {
		mPayloadLength = length;
		System.arraycopy(payload, 0, mPayload, 0, mPayloadLength);
	}

	public int getLength() {
		return 12 + mPayloadLength;
	}

	public void setPayloadLength(int length) {
		mPayloadLength = length;
	}

	public void setPayloadType(int payloadType) {
		mPayloadType = payloadType;
	}

	public void setTimestamp(long mTimestamp2) {
		mTimestamp = mTimestamp2;
	}
	
	public long getStartByte(){
		return mStartByte;
	}
	
	public long getEndByte(){
		return mEndByte;
	}
	
	public void setStartByte(long startByte){
		mStartByte = startByte;
	}
	
	public void setEndByte(long endByte){
		mEndByte = endByte;
	}
}
