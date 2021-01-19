package sample.Control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

public class PreleverController {
	private Stage stage;
	private Object object;
	private LocalDate datePrelev = LocalDate.now().withDayOfMonth(COS.getJourPrelevement());

	@FXML
	private Button confirm;

	@FXML
	private Label somme;

	@FXML
	private Label sommeL;

	@FXML
	private Label date;

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
				PretService.prelever((PretRemboursable) object);
			if (object instanceof Employe)
				((Employe) object).preleverPrets(datePrelev);

			stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			stage.close();
		} catch (NotifException e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	public void setStage(Stage stage, Object object) {
		this.stage = stage;
		this.object = object;

		if (object instanceof PretRemboursable) {
			if (!((PretRemboursable) object).estCloture())
				date.setText(((PretRemboursable) object).getDateProchain().getYear() + "  "
						+ ((PretRemboursable) object).getDateProchain().getMonth());
			else
				date.setText("Clôturé");
			somme.setText(((PretRemboursable) object).getSomme() / PretRemboursable.getTRANCHES() + " DA");
		}

		if (object instanceof Employe) {
			date.setText(datePrelev.getYear() + "  " + datePrelev.getMonth());
			somme.setVisible(false);
			sommeL.setVisible(false);
		}
	}
}
