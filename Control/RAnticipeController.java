package sample.Control;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.noyau.entity.PretRemboursable;
import sample.noyau.exception.NotifException;
import sample.noyau.service.PretService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.time.LocalDate;

public class RAnticipeController {
	private Stage stage;
	private PretRemboursable pret;

	@FXML
	private Button confirm;

	@FXML
	private Button rTotale;

	@FXML
	private TextField somme;

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
			PretService.remboursementAnticipe(pret, LocalDate.now(),Double.parseDouble(somme.getText()));
			stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			stage.close();
		} catch (NotifException e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}

	}

	@FXML
	void rTotale(ActionEvent event) {
		try {
			PretService.remboursementAnticipe(pret,LocalDate.now());
			stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			stage.close();
		} catch (NotifException e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	public void setStage(Stage stage, PretRemboursable pret) {
		this.stage = stage;
		this.pret = pret;

		confirm.disableProperty().bind(Bindings.isEmpty(somme.textProperty()));

		somme.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d{0,9}([\\.]\\d{0,4})?") || newValue.equals("0")) {
					somme.setText(oldValue);
				}
			}
		});
	}
}
