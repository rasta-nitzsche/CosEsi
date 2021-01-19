package sample.Control;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import sample.Main;
import sample.noyau.service.COS;
import sample.noyau.service.CompteService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class ChangeMdpController extends Controller implements Initializable {

	@FXML
	private PasswordField mdp;

	@FXML
	private PasswordField confirm;

	@FXML
	private PasswordField old;

	public void settings() {
		MenuController.callPage(Main.getPrimaryStage(),getClass().getResource("/sample/Vue/parametres.fxml").toString());
	}

	@FXML
	public void changer() {
		if (mdp.getText().isEmpty() || old.getText().isEmpty() || confirm.getText().isEmpty()) {
			(new Notification("Informations invalides", "Mot de passe non spécifié !", LocalDateTime.now()))
					.ajouterTrayNotif(NotificationType.ERROR);
		} else if (!CompteService.compteActuel.getMdp().equals(CompteService.crypter(old.getText()))) {
			(new Notification("Informations invalides", "Mot de passe de la session courante erroné !",
					LocalDateTime.now())).ajouterTrayNotif(NotificationType.ERROR);
		} else if (!mdp.getText().equals(confirm.getText())) {
			(new Notification("Informations invalides", "Les nouveaux mots de passes ne sont pas identiques !",
					LocalDateTime.now())).ajouterTrayNotif(NotificationType.ERROR);
		} else {
			CompteService.modifierMdp(CompteService.compteActuel.getMdp(), mdp.getText());
			(new Notification("Mot de passe modifié", "Le mot de passe est bien modifié. ", LocalDateTime.now()))
					.ajouterTrayNotif(NotificationType.ERROR);
			MenuController.callPage(Main.getPrimaryStage(),getClass().getResource("/sample/Vue/parametres.fxml").toString());

		}

	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		notifRefresh();
		for (Notification notification : COS.notifs) {
			printNotification(notification);
		}
	}
}
