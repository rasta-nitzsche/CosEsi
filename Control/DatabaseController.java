package sample.Control;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.Main;
import sample.noyau.entity.Compte;
import sample.noyau.entity.Droit;
import sample.noyau.service.COS;
import sample.noyau.service.CompteService;
import sample.noyau.util.FTPUtil;
import sample.noyau.util.HibernateUtil;

public class DatabaseController {

	private Stage stage;
	@FXML
	private Button connecter;
	@FXML
	private TextField host;
	@FXML
	private TextField port;
	@FXML
	private TextField database;
	@FXML
	private TextField username;
	@FXML
	private PasswordField password;
	@FXML
	private ChoiceBox<Integer> mySQL;

	@FXML
	private TextField hostFTP;
	@FXML
	private TextField portFTP;
	@FXML
	private TextField usernameFTP;
	@FXML
	private PasswordField passwordFTP;

	@FXML
	void connecter(ActionEvent event) {
		try {
			FTPUtil.initialize(hostFTP.getText(), Integer.valueOf(portFTP.getText()), usernameFTP.getText(),
					passwordFTP.getText());
			HibernateUtil.initialize(host.getText(), port.getText(), database.getText(), username.getText(),
					password.getText(), mySQL.getValue(), COS.getAnnotatedClasses(), "update");

			if (CompteService.lireComptes() == null || CompteService.lireComptes().isEmpty()) {
				Compte compte = new Compte(COS.Admin, COS.Admin.toLowerCase(), Droit.SUPERUTILISATEUR);
				CompteService.compteActuel = compte;
				CompteService.ajouter(CompteService.crypter("admin"), compte);
			}

			CompteService.compteActuel = null;
			COS.serialiser();
			if (FTPUtil.checkFileExists(COS.loadPath)) { // On est sure qu'on a la connexion ici
				COS.getCOSHost().createNewFile();
				COS.deserialiserHost();
			} else { // COS.estConnecte == false
				COS.nouveauBudgetAnnuel(0);
				COS.serialiserHost();
			}
			stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			stage.close();
			Main.accueil();
		} catch (Exception e) {
			FTPUtil.destroySettings();
			HibernateUtil.destroySettings();
			DemandeController.showAlert("Connexion échouée", "Veuillez vérifier les paramètres de la connexion !");
		}
	}

	public void setInfo(Stage stage) {
		this.stage = stage;
		mySQL.getItems().add(5);
		mySQL.getItems().add(8);
		mySQL.setValue(mySQL.getItems().get(1));
		port.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					port.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
		portFTP.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					portFTP.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
	}

}
