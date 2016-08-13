package fr.exensoft.audioserver;

import fr.exensoft.audioserver.codec.libfdk.LibFDKWrapper;
import fr.exensoft.audioserver.codec.libfdk.NativeFDKException;

public class MainTest {

	public static void main(String[] args) {
		System.out.println("Bonjour");
		LibFDKWrapper wrapper = new LibFDKWrapper();
		try {
			wrapper.open();
		} catch (NativeFDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
