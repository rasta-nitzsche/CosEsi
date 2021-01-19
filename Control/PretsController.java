package sample.Control;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
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
import java.util.*;
import java.util.stream.Collectors;

public class PretsController extends Controller implements Initializable {
	private ObservableList<PretRemboursable> list = FXCollections.observableArrayList();

	@FXML
	private ToggleGroup typeGroupe;
	@FXML
	private TextField rId;
	@FXML
	private RadioButton rElec;
	@FXML
	private RadioButton rSocial;
	@FXML
	private DatePicker rDebut;
	@FXML
	private DatePicker rFin;
	@FXML
	private TextField rMin;
	@FXML
	private TextField rMax;
	@FXML
	private CheckBox rRembourse;
	@FXML
	private CheckBox rCloture;
	@FXML
	private TextField rEmploye;
	@FXML
	private TextField rPv;
	@FXML
	private Button rechercher;
	@FXML
	private TableView<PretRemboursable> table;
	@FXML
	private TableColumn<PretRemboursable, String> id;
	@FXML
	private TableColumn<PretRemboursable, String> type;
	@FXML
	private TableColumn<PretRemboursable, String> date;
	@FXML
	private TableColumn<PretRemboursable, String> employe;
	@FXML
	private TableColumn<PretRemboursable, String> nom;
	@FXML
	private TableColumn<PretRemboursable, String> prenom;
	@FXML
	private TableColumn<PretRemboursable, Double> somme;
	@FXML
	private TableColumn<PretRemboursable, Double> sommeRestante;
	@FXML
	private TableColumn<PretRemboursable, Void> icon;
	@FXML
	private Button importer;
	@FXML
	private Button exporter;

	@FXML
	public void pretsEnCours(ActionEvent event) {
		List<PretRemboursable> pretRemboursableList = PretService.pretsEnCours(PretService.lirePretsAnnee());
		list.clear();
		list.addAll(pretRemboursableList);
	}

	@FXML
	public void goBack(ActionEvent event) {
		list.clear();
		list.addAll(PretService.lirePretsAnnee());
		rSocial.setSelected(false);
		rElec.setSelected(false);
	}

	@FXML
	public void retour(ActionEvent event) {
		try{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/menu.fxml"));
			Parent root = loader.load();
			MenuController controller = loader.getController();
			Main.getPrimaryStage().getScene().setRoot(root);
			controller.setInfo();
		}catch (Exception e){
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	public void exporter(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Exporter la liste des prêts");
		FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("XLSX", "*.xlsx");
		FileChooser.ExtensionFilter extensionFilter2 = new FileChooser.ExtensionFilter("XLS", "*.xls");
		FileChooser.ExtensionFilter extensionFilter3 = new FileChooser.ExtensionFilter("CSV", "*.csv");
		fileChooser.setInitialFileName("pretsListe");
		fileChooser.getExtensionFilters().addAll(extensionFilter, extensionFilter2, extensionFilter3);
		fileChooser.setSelectedExtensionFilter(extensionFilter);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
		if (file != null) {
			try {
				if (ExcelUtil.getExcelExtension(file.getName()) == "csv")
					PretService.exporterPretsCSV(new ArrayList<>(list), file.getPath());
				else 
					ExcelUtil.exporterPrets(new ArrayList<>(list), file.getPath());
			} catch (Exception e2) {
				e2.printStackTrace();
				(new Notification("Erreur du fichier", "Le fichier n'est pas compatible", LocalDateTime.now(), true))
						.ajouterTrayNotif(NotificationType.ERROR);
			}
		}
	}

	@FXML
	public void importer(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Importer la liste des prêts");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		FileChooser.ExtensionFilter pdfExtensionFilter = new FileChooser.ExtensionFilter("XLSX", "*.xlsx");
		FileChooser.ExtensionFilter pdfExtensionFilter2 = new FileChooser.ExtensionFilter("XLS", "*.xls");
		fileChooser.getExtensionFilters().addAll(pdfExtensionFilter, pdfExtensionFilter2);
		fileChooser.setSelectedExtensionFilter(pdfExtensionFilter);
		File file = fileChooser.showOpenDialog(Main.getPrimaryStage());
		List<Pret> listeImporte = null;
		if (file != null) {
			try {
				listeImporte = ExcelUtil.importerPretsNouveauFormat(file.getAbsolutePath(),
						EmployeService.lireEmployes(), PretService.lirePrets());
				for (Pret pret : listeImporte)
					PretService.ajouter(pret);

				list.clear();
				list.addAll(PretService.lirePretsAnnee());
			} catch (NotifException e) {
				(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
			} catch (Exception e2) {
				(new Notification("Erreur du fichier", "Le fichier n'est pas compatible", LocalDateTime.now(), true))
						.ajouterTrayNotif(NotificationType.ERROR);
			}
		}
	}

	@FXML
	void rechercher(ActionEvent event) {
		HashMap<TypeCritere, Object> mapCriteres = new HashMap<TypeCritere, Object>();

		// if (! rId.getText().equals("")) mapCriteres.put(TypeCritere.CRITERE2,
		// rId.getText());
		if (rElec.isSelected())
			mapCriteres.put(TypeCritere.CRITERE6, "PretElectromenager");
		if (rSocial.isSelected())
			mapCriteres.put(TypeCritere.CRITERE6, "PretSocial");
		if (rRembourse.isSelected())
			mapCriteres.put(TypeCritere.CRITERE5, null);
		if (rCloture.isSelected())
			mapCriteres.put(TypeCritere.CRITERE8, null);
		if (rFin.getValue() != null) {
			Pile pile = new Pile();
			pile.empiler(rFin.getValue());
			if (rDebut.getValue() != null)
				pile.empiler(rDebut);
			else
				pile.empiler(LocalDate.MIN);
			mapCriteres.put(TypeCritere.CRITERE3, pile);
		} else {
			if (rDebut.getValue() != null) {
				Pile pile = new Pile();
				pile.empiler(LocalDate.MAX);
				pile.empiler(rDebut);
				mapCriteres.put(TypeCritere.CRITERE3, pile);
			}
		}

		if (!rMax.getText().equals("")) {
			Pile pile = new Pile();
			pile.empiler(Double.parseDouble(rMax.getText()));
			if (!rMin.getText().equals(""))
				pile.empiler(Double.parseDouble(rMin.getText()));
			else
				pile.empiler(0);
			mapCriteres.put(TypeCritere.CRITERE2, pile);
		} else {
			if (!rMin.getText().equals("")) {
				Pile pile = new Pile();
				pile.empiler(Double.MAX_VALUE);
				pile.empiler(Double.parseDouble(rMin.getText()));
				mapCriteres.put(TypeCritere.CRITERE2, pile);
			}
		}

		if (!rEmploye.getText().equals(""))
			mapCriteres.put(TypeCritere.CRITERE7, rEmploye.getText());
		if (!rPv.getText().equals(""))
			mapCriteres.put(TypeCritere.CRITERE4, Integer.parseInt(rPv.getText()));
		try {
			list.clear();
			List<Pret> listPrets = PretService.filtrerPrets(mapCriteres);

			// Pour ne pas avoir une erreur de Cast
			listPrets = listPrets.stream().filter(pret -> pret instanceof PretRemboursable)
					.collect(Collectors.toList());

			for (Pret pret : listPrets) {
				list.add((PretRemboursable) pret);
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
		type.setCellValueFactory(cellData -> cellData.getValue().getTypeProperty());
		date.setCellValueFactory(cellData -> cellData.getValue().getDateProperty());
		nom.setCellValueFactory(cellData -> cellData.getValue().getEmploye().getNomProperty());
		prenom.setCellValueFactory(cellData -> cellData.getValue().getEmploye().getPrenomProperty());
		somme.setCellValueFactory(cellData -> cellData.getValue().getSommeProperty().asObject());
		sommeRestante.setCellValueFactory(cellData -> cellData.getValue().getSommeRProperty().asObject());

		Callback<TableColumn<PretRemboursable, Void>, TableCell<PretRemboursable, Void>> cellFactory = new Callback<TableColumn<PretRemboursable, Void>, TableCell<PretRemboursable, Void>>() {
			@Override
			public TableCell<PretRemboursable, Void> call(final TableColumn<PretRemboursable, Void> param) {
				final TableCell<PretRemboursable, Void> cell = new TableCell<PretRemboursable, Void>() {
					Image image;

					@Override
					public void updateItem(Void item, boolean empty) {
						if (!empty) {
							try {
								if (getTableView().getItems().get(getIndex()) instanceof PretElectromenager){
									image = new Image(this.getClass().getResourceAsStream("/sample/sources/productIcon.png"));
								}
								else{
									image = new Image(this.getClass().getResourceAsStream("/sample/sources/moneyIcon.png"));
								}
							} catch (Exception e) {
								(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
							}
						}

						super.updateItem(item, empty);
						ImageView imageView = new ImageView(image);
						imageView.setPreserveRatio(true);
						imageView.setFitWidth(35);
						if (empty) {
							setGraphic(null);
						} else {
							setGraphic(imageView);
						}
					}
				};
				return cell;
			}
		};

		icon.setCellFactory(cellFactory);
		icon.getStyleClass().add("icon");

		id.prefWidthProperty().bind((table.widthProperty().add(-icon.getWidth())).multiply(2.0 / 17.0));
		type.prefWidthProperty().bind((table.widthProperty().add(-icon.getWidth())).multiply(3.0 / 17.0));
		date.prefWidthProperty().bind((table.widthProperty().add(-icon.getWidth())).multiply(2.0 / 17.0));
		nom.prefWidthProperty().bind((table.widthProperty().add(-icon.getWidth())).multiply(2.5 / 17.0));
		prenom.prefWidthProperty().bind((table.widthProperty().add(-icon.getWidth())).multiply(2.0 / 17.0));
		somme.prefWidthProperty().bind((table.widthProperty().add(-icon.getWidth())).multiply(2.5 / 17.0));
		sommeRestante.prefWidthProperty().bind((table.widthProperty().add(-icon.getWidth())).multiply(3.0 / 17.0));

		somme.getStyleClass().add("somme");
		sommeRestante.getStyleClass().add("restante");

		list.addAll(PretService.lirePretsAnnee());

		table.setItems(list);

		table.setRowFactory(tv -> {
			TableRow<PretRemboursable> row = new TableRow<>() {
				@Override
				public void updateItem(PretRemboursable item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null) {
						setStyle("");
					} else if (item.estCloture()) {
						setStyle("-fx-background-color: rgba(217, 217, 217,0.6);");
					} else {
						setStyle("-fx-background-color: white;");
					}
				}
			};

			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					PretRemboursable rowData = row.getItem();

					try {
						FXMLLoader loader = new FXMLLoader();
						loader.setLocation(getClass().getResource("/sample/Vue/pret.fxml"));
						Parent root = loader.load();
						PretController controller = loader.getController();

						Stage pretStage = new Stage();
						pretStage.initOwner(Main.getPrimaryStage());
						pretStage.initStyle(StageStyle.UTILITY);
						pretStage.setScene(new Scene(root));

						controller.setInfo(rowData, pretStage, null, table);

						pretStage.show();
					} catch (Exception e) {
						(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
					}
				}
			});
			return row;
		});

		rPv.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d{0,9}?") || newValue.equals("0")) {
					somme.setText(oldValue);
				}
			}
		});

		rMin.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d{0,9}([\\.]\\d{0,4})?") || newValue.equals("0")) {
					somme.setText(oldValue);
				}
			}
		});

		rMax.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d{0,9}([\\.]\\d{0,4})?") || newValue.equals("0")) {
					somme.setText(oldValue);
				}
			}
		});

		notifRefresh();
		for (Notification notification : COS.notifs) {
			printNotification(notification);
		}

		importer.setDisable(AccueilController.droit == Droit.UTILISATEUR);

	}
}
