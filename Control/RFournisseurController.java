package sample.Control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.noyau.entity.PretElectromenager;
import sample.noyau.entity.PretRemboursable;
import sample.noyau.exception.NotifException;
import sample.noyau.service.PretService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

public class RFournisseurController {
	private Stage stage;
	private PretElectromenager pret;

	@FXML
	private Button confirm;

	@FXML
	private Label somme;

	@FXML
	private Label sommeL;

	@FXML
	private Label fournisseur;

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
			PretService.rembourserFourniseur(pret);
			stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			stage.close();
		} catch (NotifException e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	public void setStage(Stage stage, PretElectromenager pret) {
		this.stage = stage;
		this.pret = pret;

		somme.setText(pret.getSomme() / PretRemboursable.getTRANCHES() + " DA");
		fournisseur.setText(pret.getFournisseur());

		/*
		 * BooleanBinding bb = new BooleanBinding() { {
		 * super.bind(date.valueProperty()); }
		 * 
		 * @Override protected boolean computeValue() { return (date.getValue() ==
		 * null); } };
		 * 
		 * confirm.disableProperty().bind(bb);
		 */

		/*
		 * confirm.disableProperty().bind( Bindings.isNull(date.valueProperty()) );
		 */
	}
}
