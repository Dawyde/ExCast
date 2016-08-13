package fr.exensoft.audioserver.codec.format.mp3;


/**
 * Composant qui permet la lecture d'un buffer d'octets bit à bit.
 */
public class BitStreamReader {
	private int mIndex = 0;
	private int mBitIndex = 0;

	private byte[] mBuffer;

	/**
	 * Création du composant
	 * 
	 * @param buffer
	 *            Buffer d'octet à lire
	 * @param startIndex
	 *            Index de l'octet sur lequel commencer notre lecture
	 */
	public BitStreamReader(byte[] buffer, int startIndex) {
		mBuffer = buffer;
		mIndex = startIndex;
	}
	

	/**
	 * Création du composant
	 * 
	 * @param buffer
	 *            Buffer d'octet à lire
	 */
	public BitStreamReader(byte[] buffer) {
		this(buffer, 0);
	}
	
	public void setPosition(int position){
		mBitIndex = 0;
		mIndex = position;
	}

	/**
	 * Lecture d'un bit puis passage au bit suivant
	 * 
	 * @return
	 */
	public byte readBit() {

		byte value = (byte) (((mBuffer[mIndex] >> (7 - mBitIndex) & 1) != 0) ? 1 : 0);
		if (mBitIndex == 7) {
			mBitIndex = 0;
			mIndex++;
		}
		else mBitIndex++;
		return value;
	}
	

	/**
	 * Lecture du nombre de bit précisé en paramètre
	 * 
	 * @param count
	 *            Nombre de bit à lire
	 * @return
	 */
	public int readBits(int count) {
		if (count == 8 && mBitIndex == 0) {
			return mBuffer[mIndex++] & 255;
		}
		int value = 0;
		for (int i = 0; i < count; i++) {
			value = (value << 1) | readBit();
		}
		return value;
	}

	/**
	 * Avance d'un certain nombre de bits sans les lire
	 * 
	 * @param n
	 */
	public void skip(int n) {
		for(int i=0;i<n;i++){
			readBit();
		}
	}

	/**
	 * 
	 * @param nb_bits
	 * @return
	 */
	public int nextBits(int nb_bits) {
		return 0;
	}

	/**
	 * Lecture d'une valeur en utilisant un code exponentiel-Golomb
	 * 
	 * @return
	 */
	public int readExpGolomb() {
		int count = 0;
		while (readBit() != 1)
			count++;
		if (count == 0) return 0;
		return (1 << count) - 1 + readBits(count);
	}

	/**
	 * Lecture d'une valeur non signée en utilisant un code exponentiel-Golomb
	 * 
	 * @return
	 */
	public int readUe() {
		return readExpGolomb();
	}

	/**
	 * Lecture d'une valeur signée en utilisant un code exponentiel-Golomb
	 * 
	 * @return
	 */
	public int readSe() {
		int value = readExpGolomb();
		if (value % 2 == 0) return (int) -Math.ceil((value + 1) / 2);
		else return (int) Math.ceil((value + 1) / 2);
	}

	public void printState() {
		System.out.println(mIndex+"."+mBitIndex);
	}

}
