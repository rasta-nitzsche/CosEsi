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
import sample.noyau.service.COS;
import sample.noyau.service.PretService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.text.DecimalFormat;
import java.time.LocalDateTime;

public class BudgetController {

	private Stage stage;
	@FXML
	private Button confirmer;
	@FXML
	private TextField newBudget;
	@FXML
	private Label oldBudget;

	@FXML
	void confirmer(ActionEvent event) {
		COS.nouveauBudgetAnnuel(Double.parseDouble(newBudget.getText()));
		PretService.archiverPrets();
		(new Notification("Opération effetuée", "Tous les prêts cloturés de cette année sont archivés",
				LocalDateTime.now())).ajouterTrayNotif(NotificationType.INFORMATION);
		stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
		stage.close();
	}

	public void setInfo(Stage stage) {
		DecimalFormat df = new DecimalFormat("#.0");
		this.oldBudget.setText(df.format(COS.getCompte()) + " DA");
		this.stage = stage;

		confirmer.disableProperty().bind(Bindings.isEmpty(newBudget.textProperty()));

		newBudget.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d{0,9}([\\.]\\d{0,4})?") || newValue.equals("0")) {
					newBudget.setText(oldValue);
				}
			}
		});

	}

}
