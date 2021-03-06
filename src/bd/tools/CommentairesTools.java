package bd.tools;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import bd.tools.MessagesTools;
import outils.Noms;

public class CommentairesTools {
	
	public static JSONObject ajouterCommentaire(String id_auteur, String id_message, String contenu) throws UnknownHostException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		// On se connecte a la BDD puis on recupere la collection
		DBCollection messages = MessagesTools.getCollectionMessages();
		
		// On recupere l'ID du commentaire a poster
		Integer id_commentaire = getIDNouveauCommentaire(id_message);
		
		// Creation du commentaire
		String date = new Date().toString();
		BasicDBObject commentaire = new BasicDBObject();
		commentaire.put(Noms.CHAMP_CONTENU, contenu);
		commentaire.put(Noms.CHAMP_DATE, date);
		commentaire.put(Noms.CHAMP_ID_COMMENTAIRE, id_commentaire);
		// Ajout de l'auteur
		BasicDBObject auteur = new BasicDBObject();
		auteur.put(Noms.CHAMP_ID_AUTEUR, id_auteur);
		auteur.put(Noms.CHAMP_PSEUDO_AUTEUR, bd.tools.UtilisateursTools.getPseudoUtilisateur(id_auteur));
		commentaire.put(Noms.CHAMP_AUTEUR, auteur);
		
		// On ajoute le commentaire
		BasicDBObject searchQuery = new BasicDBObject(Noms.CHAMP_ID_MESSAGE, Integer.parseInt(id_message));
		BasicDBObject updateQuery = new BasicDBObject("$push", new BasicDBObject(Noms.CHAMP_COMMENTAIRES, commentaire));
		messages.update(searchQuery, updateQuery);
		
		// On retourne une reponse
		JSONObject reponse = new JSONObject();
		reponse.put("id", id_commentaire);
		reponse.put("auteur", id_auteur);
		reponse.put("texte", contenu);
		reponse.put("date", date);
		
		return reponse;
	}

	
	/**
	 * Retourne le prochain ID disponible pour un nouveau message, puis
	 * incremente le compteur de nombre de messages dans la collection
	 * "Messages"
	 * @return Un ID pour un nouveau message
	 * @throws UnknownHostException 
	 */
	public static Integer getIDNouveauCommentaire(String id_message) throws UnknownHostException {
		DBCollection collectionMessages = MessagesTools.getCollectionMessages();
		
		DBObject doc = collectionMessages.findAndModify(
	             new BasicDBObject(Noms.CHAMP_ID_MESSAGE, Integer.parseInt(id_message)), null, null, false,
	             new BasicDBObject("$inc", new BasicDBObject(Noms.CHAMP_NOMBRE_DE_COMMENTAIRES, 1)),
	             true, true);

	    return (Integer) doc.get(Noms.CHAMP_NOMBRE_DE_COMMENTAIRES);
	}
	
	
	public static JSONObject supprimerCommentaire(String id_message, String id_commentaire) throws UnknownHostException {
		// On se connecte a la BDD puis on recupere la collection
		DBCollection messages = MessagesTools.getCollectionMessages();
		
		// On supprime le commentaire
		BasicDBObject searchQuery = new BasicDBObject(Noms.CHAMP_ID_MESSAGE, Integer.parseInt(id_message));
		BasicDBObject updateQuery = new BasicDBObject(Noms.CHAMP_COMMENTAIRES, new BasicDBObject(Noms.CHAMP_ID_COMMENTAIRE, Integer.parseInt(id_commentaire)));
		messages.update(searchQuery, new BasicDBObject("$pull", updateQuery));
		
		// On retourne une reponse
		return new JSONObject();
	}
	
	
	/**
	 * Verifie si un commentaire existe dans un message
	 * @param id_message : L'ID du message contenant le commentaire
	 * @param id_commentaire : L'ID du commentaire a supprimer
	 * @return -1 si le message n'existe pas, son index dans la liste sinon.
	 * @throws UnknownHostException
	 */
	public static boolean commentaireExistant(String id_message, String id_commentaire) throws UnknownHostException {
		// On se connecte a la BDD puis on recupere les messages
		DBCollection messages = bd.tools.MessagesTools.getCollectionMessages();
		
		// Creation du message
		BasicDBObject message = new BasicDBObject();
		message.put(Noms.CHAMP_ID_MESSAGE, Integer.parseInt(id_message));

		// On verifie si le message existe
		DBCursor curseur = messages.find(message);
		if (! curseur.hasNext()) {
			// Le message n'existe pas, donc le commentaire non plus
			return false;
		}
		
		// On recupere la liste des commentaires
		JSONObject reponse_message = new JSONObject(JSON.serialize(curseur.next()));
		JSONArray commentaires = reponse_message.getJSONArray(Noms.CHAMP_COMMENTAIRES);
		
		// On itere dessus jusqu'au bout ou jusqu'a trouver le commentaire
		for (int i=0; i < commentaires.length(); i++) {
			  JSONObject commentaire = commentaires.getJSONObject(i);
			  if (commentaire.getInt(Noms.CHAMP_ID_COMMENTAIRE) == (Integer.parseInt(id_commentaire))) {
				  return true;
			  }
		}
		
		return false;
	}


	public static JSONObject listerCommentaires(String id_message) throws UnknownHostException {
		// On se connecte a la BDD puis on recupere les messages
		DBCollection messages = bd.tools.MessagesTools.getCollectionMessages();
		
		// Creation du message
		BasicDBObject message = new BasicDBObject();
		message.put(Noms.CHAMP_ID_MESSAGE, Integer.parseInt(id_message));

		// On verifie si le message existe
		DBCursor curseur = messages.find(message);
		if (! curseur.hasNext()) {
			// Le message n'existe pas, donc le commentaire non plus
			JSONObject reponseVide = new JSONObject();
			reponseVide.put(Noms.CHAMP_COMMENTAIRES, new JSONArray());
			return new JSONObject();
		}
		
		// On recupere la liste des commentaires
		JSONObject reponse_message = new JSONObject(JSON.serialize(curseur.next()));
		JSONArray commentaires = reponse_message.getJSONArray(Noms.CHAMP_COMMENTAIRES);
		
		// On retourne cette liste des commentaires
		JSONObject reponse = new JSONObject();
		reponse.put(Noms.CHAMP_COMMENTAIRES, commentaires);
		return reponse;
	}
}
