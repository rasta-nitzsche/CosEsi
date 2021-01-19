package sample.Control;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import sample.Main;
import sample.noyau.entity.Droit;
import sample.noyau.service.COS;
import sample.noyau.service.CompteService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

public class AccueilController {
	private HostServices hostServices;
	public static Droit droit;
	@FXML
	Text desc;
	@FXML
	Button connexion;
	@FXML
	private TextField username;
	@FXML
	private TextField password;

	private static final String AIDE = "https://cos-esi.000webhostapp.com/";

	@FXML
	public void aide(ActionEvent event) {
		hostServices.showDocument(AIDE);
	}

	@FXML
	void propos(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/aPropos.fxml"));
			Parent root = loader.load();
			AccueilController controller = loader.getController();
			Main.getPrimaryStage().getScene().setRoot(root);
			controller.setHostServices(hostServices);
			controller.setPropos();
			controller.setUser();
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	public void connexion() {
		droit = CompteService.authentifier(username.getText(), password.getText());
		if (droit.toString().equals("REFUSE")) {
			DemandeController.showAlert("Connexion refusée", "Username et/ou mot de passe erroné(s) !");
		} else {
			if (CompteService.verifModif()) {
				CompteService.entrer();
				try {
					COS.deserialiserHost();
					FXMLLoader loader = new FXMLLoader();
					loader.setLocation(getClass().getResource("/sample/Vue/menu.fxml"));
					Parent root = loader.load();
					MenuController controller = loader.getController();
					Main.getPrimaryStage().getScene().setRoot(root);
					controller.setInfo();
				} catch (Exception e) {
					(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
				}
			} else {
				DemandeController.showAlert("Connexion refusée",
						"Un autre super utilisateur est connecté maintenant !");
			}
		}
	}

	@FXML
	void accueil(MouseEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/accueil.fxml"));
			Parent root = loader.load();
			AccueilController controller = loader.getController();
			Main.getPrimaryStage().getScene().setRoot(root);
			controller.setHostServices(hostServices);
			controller.setUser();
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	public void cnx(ActionEvent event) {
		if (COS.estConnecte()) {
			if (CompteService.verifModif()) {
				CompteService.entrer();
				try {
					FXMLLoader loader = new FXMLLoader();
					loader.setLocation(getClass().getResource("/sample/Vue/menu.fxml"));
					Parent root = loader.load();
					MenuController controller = loader.getController();
					Main.getPrimaryStage().getScene().setRoot(root);
					controller.setInfo();
				} catch (Exception e) {
					(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
				}
			} else {
				DemandeController.showAlert("Connexion refusée", "Un autre super utilisateur est connecté maintenant!");
			}
		} else
			Controller.callPage(Main.getPrimaryStage(), getClass().getResource("/sample/Vue/cnx.fxml").toString());
	}

	public void setHostServices(HostServices hostServices) {
		this.hostServices = hostServices;
	}

	public void setPropos() {
		desc.wrappingWidthProperty().bind(Main.getPrimaryStage().widthProperty().add(-150));
	}

	public void setUser() {
		if (COS.estConnecte()) {
			droit = CompteService.compteActuel.getDroit();
			connexion.setText(CompteService.compteActuel.getNom());
		}
	}

}
