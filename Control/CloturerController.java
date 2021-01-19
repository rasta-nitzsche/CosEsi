package sample.Control;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.noyau.entity.Employe;
import sample.noyau.entity.PretRemboursable;
import sample.noyau.exception.NotifException;
import sample.noyau.service.PretService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

public class CloturerController {
	private Stage stage;
	private Object object;

	@FXML
	private TextField cause;

	@FXML
	private Label title;

	@FXML
	private Button confirm;

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
				PretService.cloturer((PretRemboursable) object, cause.getText());
			if (object instanceof Employe)
				((Employe) object).cloturerPrets(cause.getText());
			stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			stage.close();
		} catch (NotifException e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}

	}

	public void setStage(Stage stage, Object object) {
		this.stage = stage;
		this.object = object;

		confirm.disableProperty().bind(Bindings.isEmpty(cause.textProperty()));
	}

	public void setTitle(String title) {
		this.title.setText(title);
	}
}
