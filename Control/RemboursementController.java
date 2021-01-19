package sample.Control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import sample.noyau.entity.Remboursement;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;
import java.time.LocalDateTime;

public class RemboursementController {
	private Remboursement remboursement;

	@FXML
	private Label date;
	@FXML
	private Label somme;
	@FXML
	private Label type;

	@FXML
	void envoyer(ActionEvent event) {
		try {
			remboursement.envoyer();
		} catch (Exception e) {
			(new Notification("Erreur de mail", "Message non envoyé", LocalDateTime.now(), false))
					.ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	public void setInfo(Remboursement remboursement) {
		this.remboursement = remboursement;

		date.setText(remboursement.getDate().getYear() + "  " + remboursement.getDate().getMonth());
		somme.setText(remboursement.getSomme() + "");
		type.setText(remboursement.getTypeRemboursement() + "");
	}

}
