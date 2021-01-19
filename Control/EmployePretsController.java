package sample.Control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;
import sample.noyau.entity.*;
import sample.noyau.service.PretService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.io.FileInputStream;

public class EmployePretsController {
	private ObservableList<Pret> list = FXCollections.observableArrayList();
	private Employe employe;
	private Parent root;
	private Stage stage;

	@FXML
	private TableView<Pret> table;
	@FXML
	private TableColumn<Pret, String> id;
	@FXML
	private TableColumn<Pret, String> type;
	@FXML
	private TableColumn<Pret, String> date;
	@FXML
	private TableColumn<Pret, Double> somme;
	@FXML
	private TableColumn<Pret, String> sommeRestante;
	@FXML
	private TableColumn<Pret, Void> icon;
	@FXML
	private Label title;
	@FXML
	private Button pretsEnCours;

	@FXML
	void retour(ActionEvent event) {
		if (root == null)
			stage.close();
		else
			stage.getScene().setRoot(root);
	}

	@FXML
	void pretsEnCours(ActionEvent event) {
		if (pretsEnCours.getText().equals("Prêts en cours")) {
			pretsEnCours.setText("Tous les prêts");
			list.clear();
			list.addAll(PretService.pretsEnCours(employe.getPretsRemboursables()));
		} else {
			pretsEnCours.setText("Prêts en cours");
			list.clear();
			list.addAll(employe.getPretsList());
		}
	}

	public void setInfo(Employe employe, Stage stage, Parent root) {
		this.root = root;
		this.employe = employe;
		this.stage = stage;

		title.setText("Liste des prêts de " + employe.getNom() + " " + employe.getPrenom());

		id.setCellValueFactory(cellData -> cellData.getValue().getIdProperty());
		type.setCellValueFactory(cellData -> cellData.getValue().getTypeProperty());
		date.setCellValueFactory(cellData -> cellData.getValue().getDateProperty());
		somme.setCellValueFactory(cellData -> cellData.getValue().getSommeProperty().asObject());
		sommeRestante.setCellValueFactory(cellData -> cellData.getValue().getSommeRStringProperty());

		Callback<TableColumn<Pret, Void>, TableCell<Pret, Void>> cellFactory = new Callback<TableColumn<Pret, Void>, TableCell<Pret, Void>>() {
			@Override
			public TableCell<Pret, Void> call(final TableColumn<Pret, Void> param) {
				final TableCell<Pret, Void> cell = new TableCell<Pret, Void>() {
					Image image;

					@Override
					public void updateItem(Void item, boolean empty) {
						if (!empty) {
							try {
								if (getTableView().getItems().get(getIndex()) instanceof PretElectromenager){
									image = new Image(this.getClass().getResourceAsStream("/sample/sources/productIcon.png"));
								}
								else if (getTableView().getItems().get(getIndex()) instanceof PretSocial){
									image = new Image(this.getClass().getResourceAsStream("/sample/sources/moneyIcon.png"));
								}else{
									image = new Image(this.getClass().getResourceAsStream("/sample/sources/don.png"));
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

		id.prefWidthProperty().bind((table.widthProperty().add(-icon.getWidth())).multiply(2.0 / 12.5));
		type.prefWidthProperty().bind((table.widthProperty().add(-icon.getWidth())).multiply(3.0 / 12.5));
		date.prefWidthProperty().bind((table.widthProperty().add(-icon.getWidth())).multiply(2.0 / 12.5));
		somme.prefWidthProperty().bind((table.widthProperty().add(-icon.getWidth())).multiply(2.5 / 12.5));
		sommeRestante.prefWidthProperty().bind((table.widthProperty().add(-icon.getWidth())).multiply(3.0 / 12.5));

		somme.getStyleClass().add("somme");
		sommeRestante.getStyleClass().add("restante");

		list.addAll(employe.getPretsList());

		table.setItems(list);

		table.setRowFactory(tv -> {
			TableRow<Pret> row = new TableRow<>() {
				@Override
				public void updateItem(Pret item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null) {
						setStyle("");
					} else if (item instanceof PretRemboursable && ((PretRemboursable) item).estCloture()) {
						setStyle("-fx-background-color: rgba(217, 217, 217,0.6);");
					} else {
						setStyle("-fx-background-color: white;");
					}
				}
			};
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					Pret rowData = row.getItem();

					try {
						if (rowData instanceof Don) {
							FXMLLoader loader = new FXMLLoader();
							loader.setLocation(getClass().getResource("/sample/Vue/don.fxml"));
							Parent rootDon = loader.load();
							DonController controller = loader.getController();

							controller.setInfo((Don) rowData, stage, stage.getScene().getRoot());
							stage.getScene().setRoot(rootDon);
						} else {
							FXMLLoader loader = new FXMLLoader();
							loader.setLocation(getClass().getResource("/sample/Vue/pret.fxml"));
							Parent rootPret = loader.load();
							PretController controller = loader.getController();

							controller.setInfo((PretRemboursable) rowData, stage, stage.getScene().getRoot(), table);
							stage.getScene().setRoot(rootPret);
						}
					} catch (Exception e) {
						(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
					}
				}
			});
			return row;
		});

	}

}
