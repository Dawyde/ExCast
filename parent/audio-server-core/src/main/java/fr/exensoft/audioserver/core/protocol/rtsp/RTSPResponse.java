package fr.exensoft.audioserver.core.protocol.rtsp;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Objet contenant une réponse RTSP
 */
public class RTSPResponse {

	/**
	 * Code de la réponse RTSP
	 */
	private int mCode;

	/**
	 * Status de la réponse RTSP
	 */
	private String mStatusText;

	/**
	 * Entêtes de la réponse RTSP
	 */
	private Map<String, String> mData;

	/**
	 * Contenu de la réponse RTSP
	 */
	private String mBody;
	

	/**
	 * Création de la réponse RTSP
	 * 
	 * @param code
	 *            Code de la réponse
	 * @param status
	 *            Statis textuel de la réponse
	 */
	public RTSPResponse(int code, String status) {
		mCode = code;
		mStatusText = status;
		mData = new LinkedHashMap<String, String>();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RTSP/1.0 ").append(mCode).append(" ").append(mStatusText).append("\r\n");

		for (Entry<String, String> e : mData.entrySet()) {
			sb.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
		}
		sb.append("\r\n");
		if (mBody != null) sb.append(mBody).append("\r\n");
		return sb.toString();
	}

	/**
	 * Retourne le code de la réponse
	 * 
	 * @return Code de la réponse
	 */
	public int getCode() {
		return mCode;
	}

	/**
	 * Récupère un entête à partir de sa clé
	 * 
	 * @param key
	 *            Clé
	 * @return Valeur de l'entête ou null si l'entête n'existe pas
	 */
	public String get(String key) {
		return mData.get(key);
	}

	/**
	 * Récupère la valeur numérique d'un entête à partir de sa clé
	 * 
	 * @param key
	 *            Clé
	 * @return Valeur de l'entête ou null si l'entête n'existe pas
	 */
	public Integer getInt(String key) {
		String value = get(key);
		if (value == null) return null;
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Retourne le corps de la réponse RTSP
	 * 
	 * @return Corps de la réponse
	 */
	public String getBody() {
		return mBody;
	}

	/**
	 * Ajoute un couple clé/valeur d'entête
	 * 
	 * @param key
	 *            Clé de l'entête
	 * @param value
	 *            Valeur de l'entête
	 */
	public void set(String key, String value) {
		mData.put(key.trim(), value.trim());
	}

	/**
	 * Renseigne la valeur du corps de la réponse
	 * 
	 * @param body
	 *            Corps de la réponse
	 */
	public void setBody(String body) {
		mData.put("Content-Length", String.valueOf(body.length() + 2));
		mBody = body;
	}
}
