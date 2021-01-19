package sample.Control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import sample.Main;
import sample.noyau.entity.Compte;
import sample.noyau.entity.Droit;
import sample.noyau.service.COS;
import sample.noyau.service.CompteService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.time.LocalDateTime;

public class AddUserController extends Controller {

	@FXML
	private TextField username;

	@FXML
	private PasswordField mdp;

	@FXML
	private PasswordField confirm;

	@FXML
	private PasswordField old;

	@FXML
	private ChoiceBox droit;

	public void settings(){
		callPage(Main.getPrimaryStage(),getClass().getResource("/sample/Vue/parametres.fxml").toString());
	}
	public void initialize() {
		ObservableList<String> items = FXCollections.observableArrayList("Super Utilisateur", "Utilisateur");
		droit.setValue("Utilisateur");
		droit.setItems(items);
		notifRefresh();
		for (Notification notification : COS.notifs) {
			printNotification(notification);
		}
	}

	@FXML
	public void confirmer() {
		if (username.getText().isEmpty()) {
			(new Notification("Informations invalides", "Username non spécifié !", LocalDateTime.now(), true))
					.ajouterTrayNotif(NotificationType.ERROR);
		} else if (mdp.getText().isEmpty() || confirm.getText().isEmpty()) {
			(new Notification("Informations invalides", "Mot de passe non spécifié !", LocalDateTime.now(), true))
					.ajouterTrayNotif(NotificationType.ERROR);
		} else if (!mdp.getText().equals(confirm.getText())) {
			(new Notification("Informations invalides", "Les mots de passes ne sont pas identiques !",
					LocalDateTime.now(), true)).ajouterTrayNotif(NotificationType.ERROR);
		} else {
			Droit dt;
			if (droit.getValue().equals("Utilisateur")) {
				dt = Droit.UTILISATEUR;
			} else {
				dt = Droit.SUPERUTILISATEUR;
			}
			Compte compte = new Compte(username.getText(), mdp.getText(), dt);
			try {
				CompteService.ajouter(CompteService.compteActuel.getMdp(), compte);
				(new Notification("Compte ajouté", "Le compte est crée. \n Vous pouvez vous connecter avec. ",
						LocalDateTime.now(), true)).ajouterTrayNotif(NotificationType.INFORMATION);
				MenuController.callPage(Main.getPrimaryStage(),getClass().getResource("/sample/Vue/parametres.fxml").toString());

			} catch (Exception e) {
				(new Notification("Compte déjà existante", "Veuillez choisir un autre Username", LocalDateTime.now(),
						true)).ajouterTrayNotif(NotificationType.ERROR);
			}
		}
	}

}
