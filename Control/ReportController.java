package sample.Control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import sample.noyau.entity.Report;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;
import java.time.LocalDateTime;

public class ReportController {
	private Report report;

	@FXML
	private Label dDEbut;

	@FXML
	private Label dFin;

	@FXML
	private Label cause;

	@FXML
	void envoyer(ActionEvent event) {
		try {
			report.envoyer();
		} catch (Exception e) {
			(new Notification("Erreur de mail", "Message non envoyé", LocalDateTime.now(), false))
					.ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	public void setInfo(Report report) {
		this.report = report;

		dDEbut.setText(report.getDateDebut().plusMonths(1).getYear() + "  " + report.getDateDebut().plusMonths(1).getMonth());
		dFin.setText(report.getDateFin().getYear() + "  " + report.getDateFin().getMonth());
		cause.setText(report.getCause());
	}

}
