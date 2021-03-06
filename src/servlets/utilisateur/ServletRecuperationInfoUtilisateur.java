package servlets.utilisateur;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

public class ServletRecuperationInfoUtilisateur extends HttpServlet{
	public void doGet(HttpServletRequest requete, HttpServletResponse reponse) throws ServletException, IOException {
		String id= requete.getParameter("id");
		
		JSONObject retour = services.utilisateur.RecuperationInfoUtilisateur.recuperationInfoUtilisateur(id);
		
		reponse.setContentType("application/json");
        reponse.getWriter().print(retour);
	}
}
