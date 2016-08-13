package fr.exensoft.audioserver.core.buffermanager;

import java.util.ArrayList;
import java.util.LinkedList;

import fr.exensoft.audioserver.core.buffers.BaseBuffer;
import fr.exensoft.audioserver.core.buffers.BaseBufferFactory;


public class BaseBufferManager<T extends BaseBuffer>{
	/**
	 * Cette classe a pour but de garder une réserve de paquets RTP et de les
	 * recycler afin d'éviter de manger toute la mémoire
	 */

	private BaseBufferFactory<T> mBaseBufferFactory;
	
	// Compteur pour le nombre de buffer créés
	private int mCreatedPackets = 0;

	// Liste des buffers actuellement utilisés
	private ArrayList<T> mUsedPackets = new ArrayList<T>();

	// Liste des buffer disponibles
	private LinkedList<T> mAvailablePackets = new LinkedList<T>();
	
	public BaseBufferManager(BaseBufferFactory<T> bufferFactory){
		mBaseBufferFactory = bufferFactory;
	}

	/**
	 * Permet de libérer un buffer. Le buffer pourra être réatribué par la suite
	 * 
	 * @param packet
	 *            Buffer à libérer
	 */
	public synchronized void releasePacket(T packet) {
		if(mUsedPackets.remove(packet)){
			mAvailablePackets.add(packet);
		}
	}

	/**
	 * Récupere un buffer disponible. Il faudra appeler la fonction
	 * releasePacket lorsque ce buffer ne sera plus utilisé.
	 * 
	 * @return Retourne un buffer qui pourra être utilisé par la suite
	 */
	public synchronized T getPacket() {
		if (mAvailablePackets.size() > 0) {// On commence par regarder si on a
											// des buffer de disponibles.
			T retour = mAvailablePackets.poll();
			mUsedPackets.add(retour);
			return retour;
		}
		else {// Sinon on va en créer un nouveau
			T retour = mBaseBufferFactory.create();
			mUsedPackets.add(retour);
			mCreatedPackets++;
			return retour;
		}
	}

	/**
	 * Indique le nombre de buffer qui sont actuellement instanciés.
	 * 
	 * @return Le nombre de buffer instanciés
	 */
	public int size() {
		return mCreatedPackets;
	}
}
