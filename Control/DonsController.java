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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sample.noyau.entity.*;
import sample.Main;
import sample.noyau.exception.NotifException;
import sample.noyau.service.COS;
import sample.noyau.service.EmployeService;
import sample.noyau.service.PretService;
import sample.noyau.util.ExcelUtil;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DonsController extends Controller implements Initializable {
	ObservableList<Don> list = FXCollections.observableArrayList();

	@FXML
	private DatePicker dateDebut;

	@FXML
	private DatePicker dateFin;

	@FXML
	private TextField sommeMin;

	@FXML
	private TextField sommeFin;

	@FXML
	private TextField pv;

	@FXML
	private TextField nomEmpl;

	@FXML
	private TableView<Don> table;

	@FXML
	private TableColumn<Don, String> id;

	@FXML
	private TableColumn<Don, String> date;

	@FXML
	private TableColumn<Don, Double> somme;

	@FXML
	private TableColumn<Don, String> employe;

	@FXML
	private TableColumn<Don, String> nom;

	@FXML
	private TableColumn<Don, String> prenom;

	@FXML
	private Button importer;
	@FXML
	private Button exporter;

	@FXML
	void exporter(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Exporter la liste des dons");
		FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("XLSX", "*.xlsx");
		FileChooser.ExtensionFilter extensionFilter2 = new FileChooser.ExtensionFilter("XLS", "*.xls");
		fileChooser.setInitialFileName("donsListe");
		fileChooser.getExtensionFilters().addAll(extensionFilter, extensionFilter2);
		fileChooser.setSelectedExtensionFilter(extensionFilter);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
		if (file != null) {
			try {
				ExcelUtil.exporterPrets(new ArrayList<>(list), file.getAbsolutePath());
			} catch (Exception e2) {
				(new Notification("Erreur du fichier", "Le fichier n'est pas compatible", LocalDateTime.now(), true))
						.ajouterTrayNotif(NotificationType.ERROR);
			}
		}
	}

	@FXML
	public void importer(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Importer la liste des dons");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		FileChooser.ExtensionFilter pdfExtensionFilter = new FileChooser.ExtensionFilter("XLSX", "*.xlsx");
		FileChooser.ExtensionFilter pdfExtensionFilter2 = new FileChooser.ExtensionFilter("XLS", "*.xls");
		fileChooser.getExtensionFilters().addAll(pdfExtensionFilter, pdfExtensionFilter2);
		fileChooser.setSelectedExtensionFilter(pdfExtensionFilter);
		File file = fileChooser.showOpenDialog(Main.getPrimaryStage());
		if (file != null) {
			try {
				List<Pret> listeImporte = ExcelUtil.importerPretsNouveauFormat(file.getAbsolutePath(),
						EmployeService.lireEmployes(), PretService.lirePrets());
				for (Pret pret : listeImporte)
					PretService.ajouter(pret);
				list.clear();
				list.addAll(PretService.lireDons());
			} catch (NotifException e) {
				(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
			} catch (Exception e2) {
				(new Notification("Erreur du fichier", "Le fichier n'est pas compatible", LocalDateTime.now(), true))
						.ajouterTrayNotif(NotificationType.ERROR);
			}
		}
	}

	@FXML
	void goBack(ActionEvent event) {
		list.clear();
		list.addAll(PretService.lireDonsAnnee());
	}

	@FXML
	void rechercher(ActionEvent event) {
		HashMap<TypeCritere, Object> mapCriteres = new HashMap<TypeCritere, Object>();
		if (dateFin.getValue() != null) {
			Pile pile = new Pile();
			pile.empiler(dateFin.getValue());
			if (dateDebut.getValue() != null)
				pile.empiler(dateDebut);
			else
				pile.empiler(LocalDate.MIN);
			mapCriteres.put(TypeCritere.CRITERE3, pile);
		} else {
			if (dateDebut.getValue() != null) {
				Pile pile = new Pile();
				pile.empiler(LocalDate.MAX);
				pile.empiler(dateDebut);
				mapCriteres.put(TypeCritere.CRITERE3, pile);
			}
		}

		if (!sommeFin.getText().equals("")) {
			Pile pile = new Pile();
			pile.empiler(Double.parseDouble(sommeFin.getText()));
			if (!sommeMin.getText().equals(""))
				pile.empiler(Double.parseDouble(sommeMin.getText()));
			else
				pile.empiler(0);
			mapCriteres.put(TypeCritere.CRITERE2, pile);
		} else {
			if (!sommeMin.getText().equals("")) {
				Pile pile = new Pile();
				pile.empiler(Double.MAX_VALUE);
				pile.empiler(Double.parseDouble(sommeMin.getText()));
				mapCriteres.put(TypeCritere.CRITERE2, pile);
			}
		}

		if (!nomEmpl.getText().equals(""))
			mapCriteres.put(TypeCritere.CRITERE7, nomEmpl.getText());
		if (!pv.getText().equals(""))
			mapCriteres.put(TypeCritere.CRITERE4, Integer.parseInt(pv.getText()));

		try {
			list.clear();
			List<Pret> listPrets = PretService.filtrerPrets(mapCriteres);
			// Pour ne pas avoir une erreur de Cast
			listPrets = listPrets.stream().filter(pret -> pret instanceof Don).collect(Collectors.toList());

			for (Pret pret : listPrets) {
				list.add((Don) pret);
			}
			// table.setItems(list);
			// table.refresh();
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		notifRefresh();

		id.setCellValueFactory(cellData -> cellData.getValue().getIdProperty());
		date.setCellValueFactory(cellData -> cellData.getValue().getDateProperty());
		nom.setCellValueFactory(cellData -> cellData.getValue().getEmploye().getNomProperty());
		prenom.setCellValueFactory(cellData -> cellData.getValue().getEmploye().getPrenomProperty());
		somme.setCellValueFactory(cellData -> cellData.getValue().getSommeProperty().asObject());

		id.prefWidthProperty().bind(table.widthProperty().multiply(2.0 / 12.0));
		date.prefWidthProperty().bind(table.widthProperty().multiply(2.0 / 12.0));
		nom.prefWidthProperty().bind(table.widthProperty().multiply(2.5 / 12.0));
		prenom.prefWidthProperty().bind(table.widthProperty().multiply(2.5 / 12.0));
		somme.prefWidthProperty().bind(table.widthProperty().multiply(3.0 / 12.0));

		somme.getStyleClass().add("somme");

		list.addAll(PretService.lireDonsAnnee());

		table.setItems(list);

		table.setRowFactory(tv -> {
			TableRow<Don> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					Don rowData = row.getItem();

					try {
						FXMLLoader loader = new FXMLLoader();
						loader.setLocation(getClass().getResource("/sample/Vue/don.fxml"));
						Parent root = loader.load();
						DonController controller = loader.getController();

						Stage stageDon = new Stage();
						controller.setInfo(rowData, stageDon, null);

						stageDon.setScene(new Scene(root));
						stageDon.initStyle(StageStyle.UTILITY);
						stageDon.initOwner(Main.getPrimaryStage());
						stageDon.show();
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

		importer.setDisable(AccueilController.droit == Droit.UTILISATEUR);

	}
}
