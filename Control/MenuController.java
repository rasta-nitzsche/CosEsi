package sample.Control;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.*;
import javafx.util.Callback;
import sample.Main;
import sample.noyau.entity.*;
import sample.noyau.service.COS;
import sample.noyau.service.CompteService;
import sample.noyau.service.EmployeService;
import sample.noyau.service.PretService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Remboursement au fournisseur (fixed)
 */
public class MenuController extends Controller {
	private DecimalFormat df = new DecimalFormat("#.0");
	private boolean click = true;
	private ObservableList<Employe> listEmploye = FXCollections.observableArrayList();
	static Image more;
	static Image less;
	static Image noNotif;
	static Image existNotif;
	static Image moreNotif;
	List<Pret> listPrets = PretService.lirePrets();
	ObservableList<HistoryRow> listRow = FXCollections.observableArrayList();

	class HistoryRowComparator implements Comparator<HistoryRow> {
		@Override
		public int compare(HistoryRow historyRow, HistoryRow t1) {
			if (historyRow.date.get().compareTo(t1.date.get()) != 0)
				return -historyRow.date.get().compareTo(t1.date.get());
			else
				return 1;
		}
	}

	class HistoryRow {
		private StringProperty date;
		private StringProperty type;
		private StringProperty somme;
		private double sommeD;
		private StringProperty budget;
		private double budgetD;

		public HistoryRow(LocalDate date, String type, double somme) {
			this.date = new SimpleStringProperty(date.toString());
			this.type = new SimpleStringProperty(type);
			if (somme < 0)
				this.somme = new SimpleStringProperty(df.format(somme) + " DA");
			else
				this.somme = new SimpleStringProperty("+" + df.format(somme) + " DA");
			this.sommeD = somme;
		}

		public void setBudget(double budget) {
			this.budget = new SimpleStringProperty(df.format(budget) + " DA");
			this.budgetD = budget;
		}

		public String getDate() {
			return date.get();
		}

		public StringProperty dateProperty() {
			return date;
		}

		public String getType() {
			return type.get();
		}

		public StringProperty typeProperty() {
			return type;
		}

		public String getSomme() {
			return somme.get();
		}

		public StringProperty sommeProperty() {
			return somme;
		}

		public double getSommeD() {
			return sommeD;
		}

		public String getBudget() {
			return budget.get();
		}

		public StringProperty budgetProperty() {
			return budget;
		}
	}

	@FXML
	private Label sommePrete;
	@FXML
	private Label sommeRembr;
	@FXML
	private Label donationInit;
	@FXML
	private Label budgetRestant;
	@FXML
	private Label budgetActuel;
	@FXML
	private AreaChart<String, Number> chart;
	@FXML
	private VBox content;
	@FXML
	private ImageView green;
	@FXML
	private ImageView blue;
	@FXML
	private ImageView purple;
	@FXML
	private ImageView red;
	@FXML
	private Button turn;
	@FXML
	private Button send;
	@FXML
	private StackPane stack;
	@FXML
	private BarChart preteChart;
	@FXML
	private BarChart rembrChart;
	@FXML
	private BorderPane argentPrete;
	@FXML
	private BorderPane argentRembr;
	@FXML
	private BorderPane tablePane;
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
	private TableView<HistoryRow> table2;
	@FXML
	private TableColumn<HistoryRow, String> date2;
	@FXML
	private TableColumn<HistoryRow, String> type2;
	@FXML
	private TableColumn<HistoryRow, String> montant;
	@FXML
	private TableColumn<HistoryRow, String> budget;

	public void demandes() {
		callPage(Main.getPrimaryStage(), getClass().getResource("/sample/Vue/Demandes.fxml").toString());
	}

	public void déconnexion() {
		CompteService.sortir();
		if (COS.estConnecte() && CompteService.compteActuel.getDroit() == Droit.SUPERUTILISATEUR)
			COS.serialiserHost();
		CompteService.compteActuel = null;
		callPage(Main.getPrimaryStage(), getClass().getResource("/sample/Vue/accueil.fxml").toString());
	}

	public void settings() {
		callPage(Main.getPrimaryStage(), getClass().getResource("/sample/Vue/parametres.fxml").toString());
	}

	@FXML
	void visible(MouseEvent event) {
		if (click) {
			argentPrete.setVisible(false);
			argentRembr.setVisible(false);
		}
		click = true;
	}

	@FXML
	void argentPrete(MouseEvent event) {
		argentPrete.setVisible(true);
		click = false;
	}

	@FXML
	void argentRembourse(MouseEvent event) {
		argentRembr.setVisible(true);
		click = false;
	}

	@FXML
	void enterPurple(MouseEvent event) {
		try {
			purple.setImage(new Image(this.getClass().getResourceAsStream("/sample/sources/purple2.png")));
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void enterBlue(MouseEvent event) {
		try {
			blue.setImage(new Image(this.getClass().getResourceAsStream("/sample/sources/blue2.png")));
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void enterGreen(MouseEvent event) {
		try {
			green.setImage(new Image(this.getClass().getResourceAsStream("/sample/sources/green2.png")));
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void enterRed(MouseEvent event) {
		try {
			red.setImage(new Image(this.getClass().getResourceAsStream("/sample/sources/red2.png")));
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void exitPurple(MouseEvent event) {
		try {
			purple.setImage(new Image(this.getClass().getResourceAsStream("/sample/sources/purple.png")));
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void exitBlue(MouseEvent event) {
		try {
			blue.setImage(new Image(this.getClass().getResourceAsStream("/sample/sources/blue.png")));
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void exitGreen(MouseEvent event) {
		try {
			green.setImage(new Image(this.getClass().getResourceAsStream("/sample/sources/green.png")));
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void exitRed(MouseEvent event) {
		try {
			red.setImage(new Image(this.getClass().getResourceAsStream("/sample/sources/red.png")));
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void turn(ActionEvent event) {
		tablePane.setVisible(!tablePane.isVisible());
		content.setVisible(!content.isVisible());
		if (tablePane.isVisible())
			turn.setText("Informations sur le budget");
		else
			turn.setText("Liste des employés à prélever ce mois");
	}

	@FXML
	void telecharger(ActionEvent event) {
		EmployeService.printPDF(listEmploye);
	}

	@FXML
	void sendMail(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/emailConta.fxml"));
			Parent root = loader.load();
			EmailContaController controller = loader.getController();
			EmailContaController.setInfo("Liste des employés ayant un prêt en cours",
					EmployeService.employesEnCours(EmployeService.lireEmployes()));
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

	@FXML
	void setBudget(MouseEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/budget.fxml"));
			Parent root = loader.load();
			BudgetController controller = loader.getController();
			Stage stage = new Stage();
			stage.setScene(new Scene(root));
			stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e -> {
				budgetRestant
						.setText(df.format(COS.getBudgetAnnuel(COS.getAnneeSociale().plus(Period.ofYears(-1))).get(1)) + " (DA)");
				donationInit.setText(df.format((COS.getBudgetAnnuel(COS.getAnneeSociale()).get(0)
						- COS.getBudgetAnnuel(COS.getAnneeSociale().plus(Period.ofYears(-1))).get(1))) + " (DA)");
				budgetActuel.setText(df.format(COS.getCompte()) + " DA");
			});
			stage.setResizable(false);
			stage.initOwner(Main.getPrimaryStage());
			stage.initStyle(StageStyle.UTILITY);
			stage.initModality(Modality.APPLICATION_MODAL);
			controller.setInfo(stage);
			stage.show();
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	public void setInfo() {

		send.setDisable(AccueilController.droit == Droit.UTILISATEUR);

		budgetActuel.setDisable(AccueilController.droit == Droit.UTILISATEUR);

		try {
			more = new Image(this.getClass().getResourceAsStream("/sample/sources/expand2.png"));
			less = new Image(this.getClass().getResourceAsStream("/sample/sources/expand.png"));
			noNotif = new Image(this.getClass().getResourceAsStream("/sample/sources/notif.png"));
			existNotif = new Image(this.getClass().getResourceAsStream("/sample/sources/notif+.png"));
			moreNotif = new Image(this.getClass().getResourceAsStream("/sample/sources/notifB.png"));
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
		notifRefresh();

		double budgetRel = COS.getCompte();
		budgetRestant.setText(df.format(COS.getBudgetAnnuel(COS.getAnneeSociale().plus(Period.ofYears(-1))).get(1)) + " (DA)");
		donationInit.setText(df.format((COS.getBudgetAnnuel(COS.getAnneeSociale()).get(0)
				- COS.getBudgetAnnuel(COS.getAnneeSociale().plus(Period.ofYears(-1))).get(1))) + " (DA)");
		budgetActuel.setText(df.format(COS.getCompte()) + " DA");

		/*****************************************************************************************/

		final CategoryAxis xAxis1 = new CategoryAxis();
		final NumberAxis yAxis1 = new NumberAxis();

		XYChart.Series series1 = new XYChart.Series();
		double preteSocial = 0, preteElec = 0, preteDon = 0;
		for (Pret pret : listPrets) {
			if (pret instanceof PretSocial)
				preteSocial += pret.getSomme();
			else if (pret instanceof PretElectromenager)
				preteElec += pret.getSomme();
			else if (pret instanceof Don)
				preteDon += pret.getSomme();
		}
		series1.getData().add(new XYChart.Data("Social", preteSocial));
		series1.getData().add(new XYChart.Data("Electoménager", preteElec));
		series1.getData().add(new XYChart.Data("Don", preteDon));

		preteChart.getData().add(series1);
		sommePrete.setText(df.format(preteDon + preteElec + preteSocial) + " DA");

		/*****************************************************************************************/

		final CategoryAxis xAxis2 = new CategoryAxis();
		final NumberAxis yAxis2 = new NumberAxis();

		XYChart.Series series2 = new XYChart.Series();
		double rembrSocial = 0, rembrElec = 0;
		for (Pret pret : listPrets) {
			if (pret instanceof PretSocial)
				rembrSocial += pret.getSomme() - ((PretSocial) pret).getSommeRestante();
			else if (pret instanceof PretElectromenager)
				rembrElec += pret.getSomme() - ((PretElectromenager) pret).getSommeRestante();
		}
		series2.getData().add(new XYChart.Data("Social", rembrSocial));
		series2.getData().add(new XYChart.Data("Electoménager", rembrElec));

		rembrChart.getData().add(series2);
		sommeRembr.setText(df.format(rembrElec + rembrSocial) + " DA");

		/*****************************************************************************************/

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

		listEmploye.addAll(EmployeService.employesEnCours(EmployeService.lireEmployes()));

		table.setItems(listEmploye);

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

		/**********************************************************************************************/

		for (Pret pret : listPrets) {
			// TODO : test d'annee sociale
			listRow.add(new HistoryRow(pret.getDateDemande(), pret.getTypeProperty().get(), -pret.getSomme()));

			if (pret instanceof PretRemboursable) {
				for (Remboursement remboursement : ((PretRemboursable) pret).getRemboursementsList()) {
					if (remboursement.getTypeRemboursement() == TypeRemboursement.REMBOURSEMENT_FOURNISSEUR)
						listRow.add(new HistoryRow(remboursement.getDate(),
								remboursement.getTypeRemboursement().toString(), -remboursement.getSomme()));
					else
						listRow.add(new HistoryRow(remboursement.getDate(),
								remboursement.getTypeRemboursement().toString(), remboursement.getSomme()));
				}
			}
		}

		listRow.sort(new HistoryRowComparator());

		for (HistoryRow historyRow : listRow) {
			historyRow.setBudget(budgetRel);
			budgetRel -= historyRow.sommeD;
		}

		date2.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
		type2.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
		montant.setCellValueFactory(cellData -> cellData.getValue().sommeProperty());
		budget.setCellValueFactory(cellData -> cellData.getValue().budgetProperty());

		table2.setItems(listRow);

		montant.prefWidthProperty().bind((table.widthProperty()).multiply(0.25));
		type2.prefWidthProperty().bind((table.widthProperty()).multiply(0.2));
		date2.prefWidthProperty().bind((table.widthProperty()).multiply(0.25));
		budget.prefWidthProperty().bind((table.widthProperty()).multiply(0.35));

		Callback<TableColumn<HistoryRow, String>, TableCell<HistoryRow, String>> cellFactoryColor = new Callback<TableColumn<HistoryRow, String>, TableCell<HistoryRow, String>>() {
			@Override
			public TableCell<HistoryRow, String> call(final TableColumn<HistoryRow, String> param) {
				final TableCell<HistoryRow, String> cell = new TableCell<HistoryRow, String>() {

					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (empty || item == null) {
							setText(null);
							setTextFill(null);
							return;
						}
						setText(String.valueOf(item));
						if (item.charAt(0) == '-')
							setStyle("-fx-text-fill: red");
						else
							setStyle("-fx-text-fill: green");
					}
				};
				return cell;
			}
		};

		Callback<TableColumn<HistoryRow, String>, TableCell<HistoryRow, String>> cellFactoryType = new Callback<TableColumn<HistoryRow, String>, TableCell<HistoryRow, String>>() {
			@Override
			public TableCell<HistoryRow, String> call(final TableColumn<HistoryRow, String> param) {
				final TableCell<HistoryRow, String> cell = new TableCell<HistoryRow, String>() {

					@Override
					public void updateItem(String item, boolean empty) {
						ImageView image = null;
						if (!empty) {
							try {
								if (item.equals("Prêt Electroménager")) {
									image = new ImageView(new Image(
											this.getClass().getResourceAsStream("/sample/sources/productIcon.png")));
								} else if (item.equals("Prêt social")) {
									image = new ImageView(new Image(
											this.getClass().getResourceAsStream("/sample/sources/moneyIcon.png")));
								} else if (item.equals("Don")) {
									image = new ImageView(
											new Image(this.getClass().getResourceAsStream("/sample/sources/don.png")));
								} else if (item.equals("Prélèvement")) {
									image = new ImageView(new Image(
											this.getClass().getResourceAsStream("/sample/sources/prele.png")));
								} else if (item.equals("Apport externe")) {
									image = new ImageView(new Image(
											this.getClass().getResourceAsStream("/sample/sources/apport.png")));
								} else if (item.equals("Remboursement au fournisseur")) {
									image = new ImageView(new Image(
											this.getClass().getResourceAsStream("/sample/sources/apport.png")));
								}
							} catch (Exception e) {
								(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
							}
							if (image != null) {
								image.setPreserveRatio(true);
								image.setFitWidth(35);
							}
						}

						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
						} else {
							setGraphic(image);
							setText(item);
						}
					}
				};
				return cell;
			}
		};

		montant.setCellFactory(cellFactoryColor);
		type2.setCellFactory(cellFactoryType);
		type2.getStyleClass().add("type");

		/*****************************************************************************************************/

		final CategoryAxis xAxis = new CategoryAxis();
		final NumberAxis yAxis = new NumberAxis();
		chart.setTitle("");
		chart.setLegendVisible(false);
		xAxis.setLabel("Temps");
		yAxis.setLabel("Budget");

		XYChart.Series series = new XYChart.Series();
		series.setName("2019-2022");
		for (HistoryRow historyRow : listRow) {
			series.getData().add(new XYChart.Data(historyRow.date.getValue(), historyRow.budgetD));
		}
		Collections.sort(series.getData(), new Comparator<XYChart.Data>() {

			@Override
			public int compare(XYChart.Data o1, XYChart.Data o2) {
				String xValue1 = (String) o1.getXValue();
				String xValue2 = (String) o2.getXValue();
				Number yvalue1 = (Number) o1.getYValue();
				Number yvalue2 = (Number) o2.getYValue();
				if (xValue1.compareTo(xValue2) != 0)
					return xValue1.compareTo(xValue2);
				else
					return ((Double) yvalue1).compareTo((Double) yvalue2);
			}
		});
		chart.getData().addAll(series);

		/*****************************************************************************************/

		Button one = new Button(), two = new Button();
		one.getStyleClass().addAll("which", "on");
		two.getStyleClass().addAll("which");
		one.setOnAction(e -> {
			if (!one.getStyleClass().contains("on"))
				one.getStyleClass().add("on");
			two.getStyleClass().remove("on");
			chart.setVisible(true);
			table2.setVisible(false);
		});
		two.setOnAction(e -> {
			if (!two.getStyleClass().contains("on"))
				two.getStyleClass().add("on");
			one.getStyleClass().remove("on");
			chart.setVisible(false);
			table2.setVisible(true);
		});

		HBox hBox = new HBox(10);
		hBox.setAlignment(Pos.CENTER);
		hBox.getChildren().addAll(one, two);
		content.getChildren().addAll(hBox);

		notifRefresh();
		for (Notification notification : COS.notifs) {
			printNotification(notification);
		}

	}

}
