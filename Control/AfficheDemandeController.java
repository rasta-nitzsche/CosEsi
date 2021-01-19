package sample.Control;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import sample.Main;
import sample.noyau.entity.*;
import sample.noyau.exception.NotifException;
import sample.noyau.service.COS;
import sample.noyau.service.DemandeService;
import sample.noyau.util.ExcelUtil;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class AfficheDemandeController extends Controller {

	@FXML
	private TableView<Demande> table;

	@FXML
	private TableColumn<Demande, String> description;

	@FXML
	private TableColumn<Demande, String> type;

	@FXML
	private TableColumn<Demande, String> date;

	@FXML
	private TableColumn<Demande, String> employe;

	@FXML
	private TableColumn<Demande, String> nom;

	@FXML
	private TableColumn<Demande, String> prenom;

	@FXML
	private TableColumn<Demande, Double> somme;

	@FXML
	private TableColumn<Demande, String> fournisseur;

	@FXML
	private TableColumn<Demande, Boolean> accepter;

	@FXML
	private TableColumn<Demande, Boolean> refuser;

	@FXML
	private Button ajouterB;

	@FXML
	private Button importerB;

	@FXML
	private Button exporterB;

	public void ajoutdemande() {
		Controller.callPage(Main.getPrimaryStage(),
				getClass().getResource("/sample/Vue/demandemanuelle.fxml").toString());
	}

	@FXML
	public void initialize() {

		ajouterB.setDisable(AccueilController.droit == Droit.UTILISATEUR);
		importerB.setDisable(AccueilController.droit == Droit.UTILISATEUR);

		List<Demande> liste = DemandeService.lireDemandes();
		List<Demande> toremove = new ArrayList<>();
		for (Demande demande : liste) {
			if (demande.getStatus().toString().equals("refusé")) {
				toremove.add(demande);
			}
		}
		liste.removeAll(toremove);

		description.setCellValueFactory(cellData -> cellData.getValue().getDescriptionProperty());
		type.setCellValueFactory(cellData -> cellData.getValue().getTypeProperty());
		date.setCellValueFactory(cellData -> cellData.getValue().getDateProperty());
		nom.setCellValueFactory(cellData -> cellData.getValue().getEmploye().getNomProperty());
		prenom.setCellValueFactory(cellData -> cellData.getValue().getEmploye().getPrenomProperty());
		somme.setCellValueFactory(cellData -> cellData.getValue().getSommeProperty().asObject());
		fournisseur.setCellValueFactory(cellData -> cellData.getValue().getFournisseurProperty());
		accepter.setCellFactory(new Callback<TableColumn<Demande, Boolean>, TableCell<Demande, Boolean>>() {
			@Override
			public TableCell<Demande, Boolean> call(TableColumn<Demande, Boolean> personBooleanTableColumn) {
				return new ButtonCell(table, "✓");
			}
		});
		refuser.setCellFactory(new Callback<TableColumn<Demande, Boolean>, TableCell<Demande, Boolean>>() {
			@Override
			public TableCell<Demande, Boolean> call(TableColumn<Demande, Boolean> personBooleanTableColumn) {
				return new ButtonCell(table, "✘");
			}
		});

		table.setItems(FXCollections.observableArrayList(liste));

		notifRefresh();
		for (Notification notification : COS.notifs) {
			printNotification(notification);
		}
	}

	private class ButtonCell extends TableCell<Demande, Boolean> {
		Button cellButton = new Button();

		ButtonCell(TableView<Demande> tblView, String text) {

			cellButton.setText(text);
			cellButton.setDisable(AccueilController.droit == Droit.UTILISATEUR);
			if (text.equals("✓")) {
				cellButton.setId("acc");
			} else {
				cellButton.setId("ref");
			}
			cellButton.setPrefWidth(accepter.getPrefWidth());
			cellButton.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent t) {
					Demande demande = table.getItems().get(getIndex());
					Stage stg = new Stage();
					stg.initOwner(Main.getPrimaryStage());
					stg.initStyle(StageStyle.UTILITY);
					AfficheDemandeController.callPage(stg, getClass().getResource("/sample/Vue/PV.fxml").toString());
					stg.setResizable(false);

					if (text.equals("✓")) {
						try {
							if (PvController.getNumpv() != -1) {
								LocalDate now = LocalDate.now();
								LocalDate dateDebut = now.withDayOfMonth(COS.getJourPrelevement());
								if (now.isAfter(dateDebut))
									dateDebut = dateDebut.plusMonths(1);
								switch (demande.getTypePret()) {
								case SOCIAL:
									DemandeService.accepter(demande, PvController.getNumpv(), dateDebut);
									break;
								case ELECTROMENAGER:
									DemandeService.accepter(demande, PvController.getNumpv(), dateDebut);
									break;
								case DON:
									DemandeService.accepter(demande, PvController.getNumpv());
									break;
								}
							} else {
								return;
							}
						} catch (NotifException e) {
							(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
						}
					} else {
						if (PvController.getNumpv() != -1) {
							DemandeService.refuser(demande, PvController.getNumpv());
						} else {
							return;
						}
					}
					initialize();
				}
			});
		}

		@Override
		protected void updateItem(Boolean t, boolean empty) {
			super.updateItem(t, empty);
			if (!empty) {
				setGraphic(cellButton);
			}
		}
	}

	@FXML
	public void exporter(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Exporter la liste des demandes");
		FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("CSV", "*.csv");
		FileChooser.ExtensionFilter extensionFilter2 = new FileChooser.ExtensionFilter("XLSX", "*.xlsx");
		fileChooser.setInitialFileName("demandesListe");
		fileChooser.getExtensionFilters().add(extensionFilter);
		fileChooser.getExtensionFilters().add(extensionFilter2);
		fileChooser.setSelectedExtensionFilter(extensionFilter2);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
		if (file != null) {
			try {
				if (ExcelUtil.getExcelExtension(file.getName()) == "csv")
					DemandeService.exporterDemandesCSV(file.getPath());
				else 
					DemandeService.exporterDemandesExcel(file.getPath());
			} catch (Exception e) {
				(new Notification("Erreur du fichier", "Le fichier n'est pas compatible", LocalDateTime.now(), true))
						.ajouterTrayNotif(NotificationType.ERROR);
			}
		}
	}

	@FXML
	public void importer(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Importer la liste des demandes");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("CSV", "*.csv");
		FileChooser.ExtensionFilter extensionFilter2 = new FileChooser.ExtensionFilter("XLSX", "*.xlsx");
		fileChooser.getExtensionFilters().add(extensionFilter);
		fileChooser.getExtensionFilters().add(extensionFilter2);
		fileChooser.setSelectedExtensionFilter(extensionFilter2);
		File file = fileChooser.showOpenDialog(Main.getPrimaryStage());
		if (file != null) {
			try {
				if (ExcelUtil.getExcelExtension(file.getName()) == "csv")
					DemandeService.importerDemandesCSV(file.getPath());
				else 
					DemandeService.importerDemandesExcel(file.getPath());
				initialize();
			} catch (Exception e2) {
				e2.printStackTrace();
				(new Notification("Erreur du fichier", "Le fichier n'est pas compatible", LocalDateTime.now(), true))
						.ajouterTrayNotif(NotificationType.ERROR);
			}
		}
	}

	public static void callPage(Stage primaryStage, String XMLFile) {
		try {
			URL url = new URL(XMLFile);
			// URL url = new File(XMLFile).toURI().toURL();
			Parent root = FXMLLoader.load(url);
			primaryStage.setScene(new Scene(root));
			primaryStage.showAndWait();
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}
}
