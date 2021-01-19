package sample.Control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.hibernate.NonUniqueObjectException;

import sample.Main;
import sample.noyau.entity.Droit;
import sample.noyau.entity.Employe;
import sample.noyau.entity.TypeCritere;
import sample.noyau.service.COS;
import sample.noyau.service.EmployeService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.ResourceBundle;

public class EmployesController extends Controller implements Initializable {
	private ObservableList<Employe> list = FXCollections.observableArrayList();

	@FXML
	private HBox body;
	@FXML
	private Button ajouterEmployes;
	@FXML
	private Button sendMail;
	@FXML
	private TableView<Employe> table;
	@FXML
	private TableColumn<Employe, String> id;
	@FXML
	private TableColumn<Employe, String> nom;
	@FXML
	private TableColumn<Employe, String> prenom;
	@FXML
	private TableColumn<Employe, String> service;
	@FXML
	private TableColumn<Employe, String> date;
	@FXML
	private TextField nomEmpl;

	@FXML
	public void retour(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/menu.fxml"));
			Parent root = loader.load();
			MenuController controller = loader.getController();
			Main.getPrimaryStage().getScene().setRoot(root);
			controller.setInfo();
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void rechercher(ActionEvent event) {
		if (!nomEmpl.getText().equals("")) {
			HashMap<TypeCritere, Object> map = new HashMap<>();
			map.put(TypeCritere.CRITERE2, nomEmpl.getText());
			list.clear();
			list.addAll(EmployeService.filtrerEmployes(map));
		}
	}

	@FXML
	void ajouterEmployes(ActionEvent event) {
		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Importer la liste des employés");
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			FileChooser.ExtensionFilter pdfExtensionFilter = new FileChooser.ExtensionFilter("XLSX", "*.xlsx");
			fileChooser.getExtensionFilters().add(pdfExtensionFilter);
			fileChooser.setSelectedExtensionFilter(pdfExtensionFilter);
			File file = fileChooser.showOpenDialog(Main.getPrimaryStage());
			if (file != null) {
				try {
					EmployeService.lireEmployesExcel(file.getAbsolutePath());
					list.clear();
					list.addAll(EmployeService.lireEmployes());
				} catch (NonUniqueObjectException e) {
					(new Notification("Erreur d'employé", "Vous avez ajouter un employé (ID) déjà existant",
							LocalDateTime.now(), true)).ajouterTrayNotif(NotificationType.ERROR);
				} catch (Exception e) {
					(new Notification("Erreur du fichier", "Le fichier n'est pas compatible", LocalDateTime.now(),
							true)).ajouterTrayNotif(NotificationType.ERROR);
				}
			}

		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void telecharger(ActionEvent event) {
		EmployeService.printPDF(EmployeService.employesEnCours(list));
	}

	@FXML
	void sendMail(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/emailConta.fxml"));
			Parent root = loader.load();
			EmailContaController controller = loader.getController();
			EmailContaController.setInfo("Liste des employés", EmployeService.lireEmployes());
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(new Scene(root));
			stage.initStyle(StageStyle.UTILITY);
			stage.setResizable(false);
			stage.show();
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		notifRefresh();

		id.setCellValueFactory(cellData -> cellData.getValue().getIdProperty());
		nom.setCellValueFactory(cellData -> cellData.getValue().getNomProperty());
		prenom.setCellValueFactory(cellData -> cellData.getValue().getPrenomProperty());
		date.setCellValueFactory(cellData -> cellData.getValue().getDateProperty());
		service.setCellValueFactory(cellData -> cellData.getValue().getServiceProperty());

		id.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
		date.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
		nom.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
		prenom.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
		service.prefWidthProperty().bind(table.widthProperty().multiply(0.2));

		list.addAll(EmployeService.lireEmployes());
		table.setItems(list);

		table.setRowFactory(tv -> {
			TableRow<Employe> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					Employe rowData = row.getItem();

					try {
						FXMLLoader loader = new FXMLLoader();
						loader.setLocation(getClass().getResource("/sample/Vue/employe.fxml"));
						Parent root = loader.load();
						EmployeController controller = loader.getController();

						Stage stageEmploye = new Stage();
						stageEmploye.initOwner(Main.getPrimaryStage());
						stageEmploye.initStyle(StageStyle.UTILITY);
						controller.setInfo(rowData, stageEmploye, null);

						stageEmploye.setScene(new Scene(root));
						stageEmploye.show();
					} catch (Exception e) {
						(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
					}
				}
			});
			return row;
		});

		for (Notification notification : COS.notifs) {
			printNotification(notification);
		}

		sendMail.setDisable(AccueilController.droit == Droit.UTILISATEUR);
		ajouterEmployes.setDisable(AccueilController.droit == Droit.UTILISATEUR);

	}
}
