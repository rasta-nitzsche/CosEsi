package sample.Control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import sample.noyau.entity.Droit;
import sample.noyau.entity.Employe;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

public class EmployeController {
	private Employe employe;
	private Parent root;
	private Stage stage;

	@FXML
	private Button prelever;

	@FXML
	private Button reporter;

	@FXML
	private Button afficherPrets;

	@FXML
	private Button cloturer;

	@FXML
	private Label dateRecru;

	@FXML
	private Label email;

	@FXML
	private Label situation;

	@FXML
	private Label code;

	@FXML
	private Label grade;

	@FXML
	private Label id;

	@FXML
	private Label nom;

	@FXML
	private Label date;

	@FXML
	private Label service;

	@FXML
	private Label nss;

	@FXML
	private Label nbDemandes;

	@FXML
	private Label nbPrets;

	@FXML
	private Label prenom;

	@FXML
	private Label ccp;

	@FXML
	void cloturer(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/cloturer.fxml"));
			Parent root = loader.load();
			CloturerController controller = loader.getController();
			// controller.setTitle(" Êtes-vous sur de vouloir cloturer\n tous les prêts de
			// cet employé");
			Stage stage = new Stage();
			stage.setScene(new Scene(root));
			stage.initStyle(StageStyle.UTILITY);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setResizable(false);
			controller.setStage(stage, employe);
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	void reporter(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/reporter.fxml"));
			Parent root = loader.load();
			ReporterController controller = loader.getController();
			Stage stage = new Stage();
			stage.setScene(new Scene(root));
			stage.initStyle(StageStyle.UTILITY);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setResizable(false);
			controller.setStage(stage, employe);
			stage.show();
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void prelever(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/prelever.fxml"));
			Parent root = loader.load();
			PreleverController controller = loader.getController();
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(new Scene(root));
			stage.initStyle(StageStyle.UTILITY);
			stage.setResizable(false);
			stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e -> {
			});
			controller.setStage(stage, employe);
			stage.show();
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void afficherPrets(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/employePrets.fxml"));
			Parent root = loader.load();
			EmployePretsController controller = loader.getController();

			controller.setInfo(employe, stage, stage.getScene().getRoot());

			stage.getScene().setRoot(root);
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void retour(ActionEvent event) {
		if (root == null)
			stage.close();
		else
			stage.getScene().setRoot(root);
	}

	public void setInfo(Employe employe, Stage stage, Parent root) {
		this.root = root;
		this.employe = employe;
		this.stage = stage;

		id.setText(employe.getId());
		nom.setText(employe.getNom());
		prenom.setText(employe.getPrenom());
		date.setText(employe.getDateNaissance().toString());
		service.setText(employe.getService());
		nss.setText(employe.getNumeroSS());
		ccp.setText(employe.getNumeroCCP());
		email.setText(employe.getEmail());
		grade.setText(employe.getGrade());
		situation.setText(employe.getSituationFamiliale());
		code.setText(employe.getCode());
		if (employe.getDatePremEm() != null)
			dateRecru.setText(employe.getDatePremEm().toString());
		nbDemandes.setText(employe.getDemandesList().size() + " demandes");
		nbPrets.setText(employe.getPretsList().size() + " prêts");

		prelever.setDisable(AccueilController.droit == Droit.UTILISATEUR || employe.getPretsList().size() == 0);
		reporter.setDisable(AccueilController.droit == Droit.UTILISATEUR || employe.getPretsList().size() == 0);
		cloturer.setDisable(AccueilController.droit == Droit.UTILISATEUR || employe.getPretsList().size() == 0);
		afficherPrets.setDisable(employe.getPretsList().size() == 0);
	}

}
