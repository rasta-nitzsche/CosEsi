package sample.Control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import sample.noyau.entity.Report;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;
import java.time.LocalDateTime;

public class ClotureController {
	private Report report;

	@FXML
	private Label date;

	@FXML
	private Label cause;

	@FXML
	void envoyer(ActionEvent event) {
		try {
			report.envoyer();
		} catch (Exception e) {
			(new Notification("Erreur de mail", "Message non envoyé", LocalDateTime.now()))
					.ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	public void setInfo(Report report) {
		this.report = report;

		date.setText(report.getDateDebut().getYear() + "  " + report.getDateDebut().getMonth());
		cause.setText(report.getCause());
	}

}
