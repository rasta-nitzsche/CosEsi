package sample.Control;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import sample.Main;
import sample.noyau.entity.Droit;
import sample.noyau.service.COS;
import sample.noyau.util.Notification;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController extends Controller implements Initializable {
	@FXML
	private Button loadGuser;

	@FXML
	private Button modifierMDP;

	@FXML
	private Button fournisseur;

	@FXML
	private Button addService;

	public void loadGuser(){
		Controller.callPage(Main.getPrimaryStage(),getClass().getResource("/sample/Vue/GestionUser.fxml").toString());
	}

	public void modifierMDP(){
		Controller.callPage(Main.getPrimaryStage(),getClass().getResource("/sample/Vue/changeMDP.fxml").toString());
	}

	public void addService(){
		Controller.callPage(Main.getPrimaryStage(),getClass().getResource("/sample/Vue/services.fxml").toString());
	}

	public void fournisseur() {
		Controller.callPage(Main.getPrimaryStage(),getClass().getResource("/sample/Vue/fournisseur.fxml").toString());
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		loadGuser.setDisable(AccueilController.droit == Droit.UTILISATEUR);
		fournisseur.setDisable(AccueilController.droit == Droit.UTILISATEUR);
		addService.setDisable(AccueilController.droit == Droit.UTILISATEUR);

		notifRefresh();
		for (Notification notification : COS.notifs) {
			printNotification(notification);
		}
	}

}
