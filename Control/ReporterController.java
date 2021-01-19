package sample.Control;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.noyau.entity.Employe;
import sample.noyau.entity.PretRemboursable;
import sample.noyau.exception.NotifException;
import sample.noyau.service.COS;
import sample.noyau.service.PretService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;
import java.time.LocalDate;

public class ReporterController {
	class NewDate {
		private LocalDate date;

		public NewDate(LocalDate date) {
			this.date = date;
		}

		@Override
		public String toString() {
			return date.getYear() + " " + date.getMonth();
		}
	}

	private Stage stage;
	private Object object;
	private LocalDate debut;

	@FXML
	private Button confirm;

	@FXML
	private Label dateDebut;

	@FXML
	private ChoiceBox<NewDate> date;

	@FXML
	private TextField cause;

	@FXML
	void annuler(ActionEvent event) {
		stage.close();
	}

	@FXML
	void close(ActionEvent event) {
		stage.close();
	}

	@FXML
	void confirmer(ActionEvent event) {
		try {
			if (object instanceof PretRemboursable)
				PretService.reporter((PretRemboursable)object,((PretRemboursable)object).getDateProchain().minusMonths(1),date.getValue().date,cause.getText());
			if (object instanceof Employe) {
				((Employe) object).reporterPrets(debut, date.getValue().date, cause.getText());
			}
			stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			stage.close();
		} catch (NotifException e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	public void setStage(Stage stage, Object object) {
		this.stage = stage;
		this.object = object;

		LocalDate localDate = LocalDate.now().withDayOfMonth(COS.getJourPrelevement());
		if (object instanceof PretRemboursable)
			localDate = ((PretRemboursable) object).getDateDebut();
		localDate = localDate.minusMonths(1);

		for (int i = 0; i < 20; i++) {
			date.getItems().add(new NewDate(localDate));
			localDate = localDate.plusMonths(1);
		}
		date.setValue(date.getItems().get(1));

		debut = LocalDate.now().withDayOfMonth(COS.getJourPrelevement());

		if (object instanceof PretRemboursable)
			if (!((PretRemboursable) object).estCloture())
				dateDebut.setText(((PretRemboursable) object).getDateProchain().getYear() + "  "
						+ ((PretRemboursable) object).getDateProchain().getMonth());
			else
				dateDebut.setText("Clôturé");
		else
			dateDebut.setText(debut.getYear() + "  " + debut.getMonth());

		confirm.disableProperty().bind(Bindings.isEmpty(cause.textProperty()));
	}

}
