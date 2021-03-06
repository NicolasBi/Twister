package services.authentification;

import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bd.tools.AmitiesTools;
import bd.tools.SessionsTools;
import bd.tools.UtilisateursTools;
import exceptions.ClefInexistanteException;
import exceptions.tailles.MotDePasseTropGrandException;
import exceptions.tailles.MotDePasseTropPetitException;
import exceptions.tailles.PseudoTropGrandException;
import exceptions.tailles.PseudoTropPetitException;
import outils.PseudosAdmins;
import outils.Tailles;
import services.CodesErreur;
import services.ErrorJSON;

public class Login {
	public static JSONObject login(String pseudo, String motDePasse) {
		try {
			// On verifie qu'un des parametres obligatoire n'est pas null
			if (! verificationParametres(pseudo, motDePasse)) {
				return ErrorJSON.serviceRefused("L'un des parametres est null", CodesErreur.ERREUR_ARGUMENTS);
			}
			
			// On verifie que les parametres entres respectent nos criteres de taille
			verificationTailleInput(pseudo, motDePasse);
			
			// On verifie que l'utilisateur existe
			boolean isUser = UtilisateursTools.checkExistencePseudo(pseudo);
			if (! isUser) {
				return ErrorJSON.serviceRefused("L'utilisateur n'existe pas", CodesErreur.ERREUR_UTILISATEUR_INEXISTANT);
			}
			
			// On recupere l'identifiant de l'utilisateur
			String identifiant = UtilisateursTools.getIDByPseudo(pseudo);
			
			// On hash le mot de passe
			motDePasse = outils.MesMethodes.hasherMotDePasse(motDePasse);
			
			// On verifie que le couple (login, mot de passe) fonctionne
			boolean passwordOk = UtilisateursTools.checkMotDePasseAvecPseudo(pseudo, motDePasse);
			if (! passwordOk) {
				return ErrorJSON.serviceRefused("Erreur, mot de passe incorrect", CodesErreur.ERREUR_MDP_INCORRECT);
			}
			
			// On verifie que l'utilisateur n'est pas deja connecte
			String key;
			boolean estConnecte = SessionsTools.estConnecte(identifiant);
			if (! estConnecte) {
				// Si il ne l'est pas deja, on doit inserer sa clef de session dans la BDD
				boolean estAdministrateur = estAdministrateur(pseudo);
				key = SessionsTools.insertSession(identifiant, estAdministrateur);
			} else {
				// Sinon, on recupere sa clef puis on met a jour son timestamp
				//TODO: Supprimer l'ancienne clef
				key = SessionsTools.getClefById(identifiant);
				SessionsTools.updateTempsCle(key);
			}
			
			// On recupere la liste des id des gens suivis par l'utilisateur
			JSONObject jsonIDSuivis = AmitiesTools.listerTousLesAmis(identifiant);
			
			// On transforme le JSONObject en JSONArray
			JSONArray listeIDSuivis = jsonIDSuivis.getJSONArray("Amis");
			
			// On recupere les informations restantes
			JSONObject informations = bd.tools.UtilisateursTools.getInformationsUtilisateur(pseudo);
			String prenom = informations.getString("prenom");
			String nom = informations.getString("nom");
			String anniversaire = informations.getString("anniversaire");
			
			// On genere une reponse
			JSONObject retour = new JSONObject();
			retour.put("id", identifiant);
			retour.put("pseudo", pseudo);
			retour.put("clef", key);
			retour.put("suivis", listeIDSuivis);
			retour.put("prenom", prenom);
			retour.put("nom", nom);
			retour.put("anniversaire", anniversaire);
			return retour;

		} catch (SQLException e) {
			return ErrorJSON.serviceRefused("Erreur, requete SQL incorrecte", CodesErreur.ERREUR_SQL);
		} catch (InstantiationException e) {
			return ErrorJSON.serviceRefused("Erreur lors de la connexion a la base de donnees MySQL (InstantiationException)", CodesErreur.ERREUR_CONNEXION_BD_MYSQL);
		} catch (IllegalAccessException e) {
			return ErrorJSON.serviceRefused("Erreur lors de la connexion a la base de donnees MySQL (IllegalAccessException)", CodesErreur.ERREUR_CONNEXION_BD_MYSQL);
		} catch (ClassNotFoundException e) {
			return ErrorJSON.serviceRefused("Erreur lors de la connexion a la base de donnees MySQL (ClassNotFoundException)", CodesErreur.ERREUR_CONNEXION_BD_MYSQL);
		} catch (NoSuchAlgorithmException e) {
			return ErrorJSON.serviceRefused("Erreur lors du hashage du mot de passe", CodesErreur.ERREUR_HASHAGE);
		} catch (PseudoTropPetitException e) {
			return ErrorJSON.serviceRefused("Erreur, pseudo trop petit", CodesErreur.ERREUR_PSEUDO_TROP_COURT);
		} catch (PseudoTropGrandException e) {
			return ErrorJSON.serviceRefused("Erreur, pseudo trop grand", CodesErreur.ERREUR_PSEUDO_TROP_GRAND);
		} catch (MotDePasseTropPetitException e) {
			return ErrorJSON.serviceRefused("Erreur, mot de passe trop petit", CodesErreur.ERREUR_MDP_TROP_COURT);
		} catch (MotDePasseTropGrandException e) {
			return ErrorJSON.serviceRefused("Erreur, mot de passe trop grand", CodesErreur.ERREUR_MDP_TROP_LONG);
		} catch (ClefInexistanteException e) {
			return ErrorJSON.serviceRefused("Erreur, clef inexistante", CodesErreur.ERREUR_CLEF_INEXISTANTE);
		} catch (JSONException e) {
			return ErrorJSON.serviceRefused("Erreur de JSON", CodesErreur.ERREUR_JSON);
		} catch (UnknownHostException e) {
			return ErrorJSON.serviceRefused("Hote inconnu", CodesErreur.ERREUR_HOTE_INCONNU);
		} 
	}
	
	
	/**
	 * Verifie que les parametres entres respectent nos criteres de taille.
	 * Ces tailles sont situees dans le fichier services.Tailles.java
	 * Cette fonction lance une exception si un des parametres ne respecte
	 * pas ces criteres
	 * @param pseudo
	 * @param motDePasse
	 * @throws PseudoTropPetitException 
	 * @throws PseudoTropGrandException 
	 * @throws MotDePasseTropPetitException 
	 * @throws MotDePasseTropGrandException 
	 */
	private static void verificationTailleInput(String pseudo, String motDePasse) throws PseudoTropPetitException, PseudoTropGrandException, MotDePasseTropPetitException, MotDePasseTropGrandException {
		if (pseudo.length() < Tailles.MIN_PSEUDO) {
			throw new PseudoTropPetitException();
		} else if (pseudo.length() > Tailles.MAX_PSEUDO) {
			throw new PseudoTropGrandException();
		} else if (motDePasse.length() < Tailles.MIN_MOT_DE_PASSE) {
			throw new MotDePasseTropPetitException();
		} else if (motDePasse.length() > Tailles.MAX_MOT_DE_PASSE) {
			throw new MotDePasseTropGrandException();
		}
	}

	
   /**
    * Verification de la validite des parametres
    * @return : Un booleen a true si les paramatres sont valides.
    */
	private static boolean verificationParametres(String login, String motDePasse) {
		return (login != null && motDePasse != null);
	}
	
	
	private static boolean estAdministrateur(String login) {
		return PseudosAdmins.pseudoAdmins.contains(login);
	}
}
