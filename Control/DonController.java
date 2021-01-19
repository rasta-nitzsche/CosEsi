package sample.Control;

import java.time.LocalDateTime;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import sample.noyau.entity.*;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

public class DonController {
	private Don don;
	private Stage stageDon;
	private Parent root;

	@FXML
	private Label id;

	@FXML
	private Label date;

	@FXML
	private Label pv;

	@FXML
	private Label motif;

	@FXML
	private Label nom;

	@FXML
	private Label somme;

	@FXML
	private Label prenom;

	@FXML
	private Button more;

	@FXML
	private Button sendMail;

	@FXML
	void retour(ActionEvent event) {
		if (root == null)
			stageDon.close();
		else
			stageDon.getScene().setRoot(root);
	}

	@FXML
	void afficherEmploye(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/employe.fxml"));
			Parent root = loader.load();
			EmployeController controller = loader.getController();

			controller.setInfo(don.getEmploye(), stageDon, stageDon.getScene().getRoot());

			stageDon.getScene().setRoot(root);
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void sendMail(ActionEvent event) {
		try {
			don.envoyer();
		} catch (Exception e) {
			(new Notification("Erreur de mail", "Message non envoyé", LocalDateTime.now(), false))
					.ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	public void setInfo(Don don, Stage stageDon, Parent root) {
		this.don = don;
		this.stageDon = stageDon;
		this.root = root;

		id.setText(don.getId() + "");
		pv.setText(don.getPV() + "");
		motif.setText(don.getMotif());
		nom.setText(don.getEmploye().getNom());
		prenom.setText(don.getEmploye().getPrenom());
		somme.setText(don.getSomme() + " DA");
		date.setText(don.getDateDemande().toString());

	}

	// Pour afficher un prêt archivé
	public void disableArchive() {
		sendMail.setVisible(false);
		more.setVisible(false);
	}
}
