package sample.Control;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.time.LocalDateTime;

public class PvController {

	@FXML
	private Button conf;
	@FXML
	private TextField pv;

	protected static int numpv = -1;

	@FXML
	public void initialize() {

		pv.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					pv.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
	}

	public void confirmer() {
		if (pv.getText().isEmpty()) {
			(new Notification("Erreur", "Cause : Pv non spécifié", LocalDateTime.now(), true))
					.ajouterTrayNotif(NotificationType.ERROR);
		} else {
			numpv = Integer.parseInt(pv.getText());
			Stage stage = (Stage) conf.getScene().getWindow();
			stage.close();
		}
	}

	public static int getNumpv() {
		return numpv;
	}
}
