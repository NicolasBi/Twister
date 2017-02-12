package services.authentification;

import java.sql.SQLException;

import org.json.JSONObject;

import bd.tools.SessionsTools;
import bd.tools.UtilisateursTools;
import services.CodesErreur;
import services.ErrorJSON;


public class Logout {
	public static JSONObject logout(String cle) {
		if (! verificationParametres(cle)) {
			return ErrorJSON.serviceRefused("L'un des parametres est null", CodesErreur.ERREUR_ARGUMENTS);
		}

		try {
			
			// On verifie que l'utilisateur est bien connecte
			//TODO:
			
			// On supprime la cle de connexion
			boolean estSupprime = SessionsTools.suppressionCle(cle);
			if (! estSupprime){
				return ErrorJSON.serviceRefused("Erreur lors de la deconnexion", CodesErreur.ERREUR_DECONNEXION);

			}
			
			// On genere une reponse
			JSONObject retour = new JSONObject();
			retour.put("Deconnexion realisee", cle);
			return retour;
		} catch (SQLException e) {
			return ErrorJSON.serviceRefused("Erreur de SQL", CodesErreur.ERREUR_SQL);			
		} catch (Exception e) {
			return ErrorJSON.serviceRefused("Erreur Inconnue", CodesErreur.ERREUR_INCONNUE);				
		}
	}

	
	private static boolean verificationParametres(String cle) {
		return (cle != null);
	}
}
	
	
	