package fr.exensoft.audioserver.core.protocol.http;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Objet contenant une réponse HTTP
 */
public class HTTPResponse {

	/**
	 * Code de la réponse HTTP
	 */
	private int mCode;

	/**
	 * Status de la réponse HTTP
	 */
	private String mStatusText;

	/**
	 * Entêtes de la réponse HTTP
	 */
	private Map<String, String> mData;

	/**
	 * Contenu de la réponse HTTP
	 */
	private String mBody;
	
	private String mVersion;
	

	/**
	 * Création de la réponse HTTP
	 * 
	 * @param code
	 *            Code de la réponse
	 * @param status
	 *            Statis textuel de la réponse
	 */
	public HTTPResponse(int code, String status, String version) {
		mCode = code;
		mStatusText = status;
		mVersion = version;
		mData = new LinkedHashMap<String, String>();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mVersion).append(" ").append(mCode).append(" ").append(mStatusText).append("\r\n");

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
	 * Retourne le corps de la réponse HTTP
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
