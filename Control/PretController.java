package sample.Control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import sample.noyau.entity.*;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;

public class PretController {
	class Action implements Comparable<Action> {
		private LocalDate date;
		private String status;
		private Report report = null;
		private Remboursement remboursement = null;
		private ImageView more, more2;

		public Action(LocalDate date, String status, Remboursement remboursement) {
			this.date = date;
			this.status = status;
			this.remboursement = remboursement;
		}

		public Action(LocalDate date, String status, Report report) {
			this.date = date;
			this.status = status;
			this.report = report;
		}

		@Override
		public int compareTo(Action action) {
			return date.compareTo(action.date);
		}

		public BorderPane getButton() {
			try {
				more = new ImageView(new Image(this.getClass().getResourceAsStream("/sample/sources/more.png")));
				more2 = new ImageView(new Image(this.getClass().getResourceAsStream("/sample/sources/more2.png")));
			} catch (Exception e) {
				(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
			}

			BorderPane borderPane = new BorderPane();
			borderPane.getStyleClass().add("action");
			Label month = new Label(date.getMonth().name());
			month.setAlignment(Pos.CENTER);
			month.getStyleClass().add("month");
			Label action = new Label(status);
			action.getStyleClass().add("status");
			action.setTextAlignment(TextAlignment.CENTER);
			Button plus = new Button("", more);
			plus.getStyleClass().add("change");
			Button plus2 = new Button("", more2);
			plus2.getStyleClass().add("change");
			borderPane.setCenter(action);
			borderPane.setTop(month);
			HBox hBox = new HBox(-10);
			hBox.getChildren().addAll(plus,plus2);
			hBox.setAlignment(Pos.CENTER_RIGHT);
			borderPane.setBottom(hBox);
			BorderPane.setAlignment(hBox, Pos.CENTER_RIGHT);
			BorderPane.setAlignment(month, Pos.CENTER);
			if (report != null) {
				borderPane.setOnMouseClicked(e -> {
					if (report.getDateFin() != null) {
						try {
							FXMLLoader loader = new FXMLLoader();
							loader.setLocation(getClass().getResource("/sample/Vue/report.fxml"));
							Parent root = loader.load();
							ReportController controller = loader.getController();
							Stage stage = new Stage();
							stage.setScene(new Scene(root));
							stage.initModality(Modality.APPLICATION_MODAL);
							stage.initStyle(StageStyle.UTILITY);
							stage.setResizable(false);
							controller.setInfo(report);
							stage.show();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					} else {
						try {
							FXMLLoader loader = new FXMLLoader();
							loader.setLocation(getClass().getResource("/sample/Vue/cloture.fxml"));
							Parent root = loader.load();
							ClotureController controller = loader.getController();
							Stage stage = new Stage();
							stage.setScene(new Scene(root));
							stage.initModality(Modality.APPLICATION_MODAL);
							stage.initStyle(StageStyle.UTILITY);
							stage.setResizable(false);
							controller.setInfo(report);
							stage.show();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});
			}
			if (remboursement != null) {
				//plus2.setVisible(false);
				borderPane.setOnMouseClicked(e -> {
					try {
						FXMLLoader loader = new FXMLLoader();
						loader.setLocation(getClass().getResource("/sample/Vue/remboursement.fxml"));
						Parent root = loader.load();
						RemboursementController controller = loader.getController();
						Stage stage = new Stage();
						stage.setScene(new Scene(root));
						stage.initStyle(StageStyle.UTILITY);
						stage.initModality(Modality.APPLICATION_MODAL);
						controller.setInfo(remboursement);
						stage.show();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				});
			}
			return borderPane;
		}

	}

	private PretRemboursable pret;
	static Stage stageAction;
	private int year;
	private Button pre = null;
	private Button next = null;
	private FlowPane flow = new FlowPane();
	private Stage stagePret;
	private HashMap<Month, HashMap<Integer, Action>> map = new HashMap<>();
	private HashMap<Month, HashMap<Integer, Remboursement>> rembExterne = new HashMap<>();
	private HashMap<Month, HashMap<Integer, Remboursement>> rembFourni = new HashMap<>();
	private TableView tableView;
	private int yearMax = 0;
	private Parent root;
	private Controller controller;

	@FXML
	private Label prochainL;
	@FXML
	private Label id;
	@FXML
	private Label motifL;
	@FXML
	private Label date;
	@FXML
	private Label prochain;
	@FXML
	private Label fourniL;
	@FXML
	private Label pv;
	@FXML
	private Label type;
	@FXML
	private Label nom;
	@FXML
	private Label somme;
	@FXML
	private Label nbMois;
	@FXML
	private Label motif;
	@FXML
	private Label fourni;
	@FXML
	private Label prenom;
	@FXML
	private Label restante;
	@FXML
	private Button more;
	@FXML
	private Button prelever;
	@FXML
	private Button reporter;
	@FXML
	private Button cloturer;
	@FXML
	private Button rAnticipe;
	@FXML
	private Button sendMail;
	@FXML
	private Button rembrFournisseur;
	@FXML
	private ScrollPane scroll;
	@FXML
	private ColumnConstraints general1;
	@FXML
	private ColumnConstraints general2;
	@FXML
	private ColumnConstraints general3;
	@FXML
	private ColumnConstraints general4;
	@FXML
	private ColumnConstraints general5;
	@FXML
	private GridPane generalGrid;

	@FXML
	void rembrFournisseur(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/rFournisseur.fxml"));
			Parent root = loader.load();
			RFournisseurController controller = loader.getController();
			Stage stage = new Stage();
			stage.setScene(new Scene(root));
			stage.initStyle(StageStyle.UTILITY);
			stage.setResizable(false);
			stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e -> {
				setcalendrier();
				printCalendrier();
				tableView.refresh();
				nbMois.setText(pret.getNbMois() + "");
				if (pret.getDateProchain() != null)
					prochain.setText(pret.getDateProchain().toString());
				else {
					prochain.setVisible(false);
					prochainL.setVisible(false);
				}
				restante.setText(pret.getSommeRestante() + " DA");
				if (!(pret instanceof PretElectromenager && pret.estReporte(LocalDate.now())) || AccueilController.droit == Droit.UTILISATEUR)
					rembrFournisseur.setVisible(false);
				else
					rembrFournisseur.setVisible(true);
			});
			stage.initModality(Modality.APPLICATION_MODAL);
			controller.setStage(stage, (PretElectromenager) pret);
			stage.show();
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void cloturer(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/cloturer.fxml"));
			Parent root = loader.load();
			CloturerController controller = loader.getController();
			Stage stage = new Stage();
			stage.setScene(new Scene(root));
			stage.initStyle(StageStyle.UTILITY);
			stage.setResizable(false);
			stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e -> {
				setcalendrier();
				printCalendrier();
				tableView.refresh();
				nbMois.setText(pret.getNbMois() + "");
				if (pret.getDateProchain() != null)
					prochain.setText(pret.getDateProchain().toString());
				else {
					prochain.setVisible(false);
					prochainL.setVisible(false);
				}
				restante.setText(pret.getSommeRestante() + " DA");
				if (!(pret instanceof PretElectromenager && pret.estReporte(LocalDate.now())) || AccueilController.droit == Droit.UTILISATEUR)
					rembrFournisseur.setVisible(false);
				else
					rembrFournisseur.setVisible(true);
			});
			stage.initModality(Modality.APPLICATION_MODAL);
			controller.setStage(stage, pret);
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
				setcalendrier();
				printCalendrier();
				tableView.refresh();
				nbMois.setText(pret.getNbMois() + "");
				if (pret.getDateProchain() != null)
					prochain.setText(pret.getDateProchain().toString());
				else {
					prochain.setVisible(false);
					prochainL.setVisible(false);
				}
				restante.setText(pret.getSommeRestante() + " DA");
				if (!(pret instanceof PretElectromenager && pret.estReporte(LocalDate.now())) || AccueilController.droit == Droit.UTILISATEUR)
					rembrFournisseur.setVisible(false);
				else
					rembrFournisseur.setVisible(true);
			});
			controller.setStage(stage, pret);
			stage.show();
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void rAnticipe(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/RAnticipe.fxml"));
			Parent root = loader.load();
			RAnticipeController controller = loader.getController();
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(new Scene(root));
			stage.initStyle(StageStyle.UTILITY);
			stage.setResizable(false);
			stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e -> {
				setcalendrier();
				printCalendrier();
				tableView.refresh();
				nbMois.setText(pret.getNbMois() + "");
				if (pret.getDateProchain() != null)
					prochain.setText(pret.getDateProchain().toString());
				else {
					prochain.setVisible(false);
					prochainL.setVisible(false);
				}
				restante.setText(pret.getSommeRestante() + " DA");
				if (!(pret instanceof PretElectromenager && pret.estReporte(LocalDate.now())) || AccueilController.droit == Droit.UTILISATEUR)
					rembrFournisseur.setVisible(false);
				else
					rembrFournisseur.setVisible(true);
			});
			controller.setStage(stage, pret);
			stage.show();
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
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
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(new Scene(root));
			stage.initStyle(StageStyle.UTILITY);
			stage.setResizable(false);
			stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e -> {
				setcalendrier();
				printCalendrier();
				tableView.refresh();
				nbMois.setText(pret.getNbMois() + "");
				if (pret.getDateProchain() != null)
					prochain.setText(pret.getDateProchain().toString());
				else {
					prochain.setVisible(false);
					prochainL.setVisible(false);
				}
				restante.setText(pret.getSommeRestante() + " DA");
				if (!(pret instanceof PretElectromenager && pret.estReporte(LocalDate.now())) || AccueilController.droit == Droit.UTILISATEUR)
					rembrFournisseur.setVisible(false);
				else
					rembrFournisseur.setVisible(true);
			});
			controller.setStage(stage, pret);
			stage.show();
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void retour(ActionEvent event) {
		if (root == null)
			stagePret.close();
		else
			stagePret.getScene().setRoot(root);
	}

	@FXML
	void sendMail(ActionEvent event) {
		try {
			pret.envoyer();
		} catch (Exception e) {
			(new Notification("Erreur de mail", "Message non envoyé", LocalDateTime.now(), false))
					.ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@FXML
	void afficherEmploye(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/employe.fxml"));
			Parent root = loader.load();
			EmployeController controller = loader.getController();

			controller.setInfo(pret.getEmploye(), stagePret, stagePret.getScene().getRoot());

			stagePret.getScene().setRoot(root);
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	public void setInfo(PretRemboursable pret, Stage stagePret, Parent root, TableView tableView) {
		this.pret = pret;
		this.stagePret = stagePret;
		this.tableView = tableView;
		this.root = root;

		id.setText(pret.getId() + "");
		pv.setText(pret.getPV() + "");
		nbMois.setText(pret.getNbMois() + "");
		type.setText(pret.getTypeProperty().getValue());
		nom.setText(pret.getEmploye().getNom());
		prenom.setText(pret.getEmploye().getPrenom());
		somme.setText(pret.getSomme() + " DA");
		restante.setText(pret.getSommeRestante() + " DA");
		motif.setText(pret.getDescription());
		date.setText(pret.getDateDemande().toString());
		if (pret.getDateProchain() != null)
			prochain.setText(pret.getDateProchain().toString());
		else {
			prochain.setVisible(false);
			prochainL.setVisible(false);
		}

		if (pret instanceof PretElectromenager) {
			rembrFournisseur.setVisible(true);
			fourni.setText(((PretElectromenager) pret).getFournisseur());
			motif.setText(((PretElectromenager) pret).getProduit());
			motifL.setText("Produit");
		}
		if (pret instanceof PretSocial) {
			motif.setText(((PretSocial) pret).getMotif());
			fourniL.setVisible(false);
			fourni.setVisible(false);
		}

		setcalendrier();

		BorderPane borderPane = new BorderPane();
		HBox hBox = new HBox(20);

		Label year2 = new Label(pret.getDateDebut().getYear() + "");
		year2.getStyleClass().add("year");

		if (pret.getDateProchain() != null)
			yearMax = pret.getDateProchain().getYear();
		else {
			for (Report report : pret.getReportsList()) {
				if (report.getDateFin() != null)
					yearMax = report.getDateDebut().getYear();
			}
		}

		try {
			pre = new Button("", new ImageView(new Image(this.getClass().getResourceAsStream("/sample/sources/prev.png"))));
			next = new Button("", new ImageView(new Image(this.getClass().getResourceAsStream("/sample/sources/next.png"))));
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
		next.getStyleClass().add("change");
		pre.getStyleClass().add("change");
		pre.setOnAction(e -> {
			year--;
			year2.setText(year + "");
			printCalendrier();
			if ((pret.getDateDebut().getYear() >= year))
				pre.setDisable(true);
			next.setDisable(false);
		});
		pre.setDisable(true);
		next.setOnAction(e -> {
			year++;
			year2.setText(year + "");
			printCalendrier();
			if (year >= yearMax)
				next.setDisable(true);
			pre.setDisable(false);
		});
		year = pret.getDateDebut().getYear();

		if (yearMax <= year)
			next.setDisable(true);

		printCalendrier();

		hBox.getChildren().addAll(pre, year2, next);

		borderPane.setTop(hBox);
		hBox.setAlignment(Pos.CENTER);
		BorderPane.setAlignment(hBox, Pos.CENTER);
		borderPane.setCenter(flow);
		flow.setAlignment(Pos.CENTER);
		BorderPane.setAlignment(flow, Pos.CENTER);

		flow.setHgap(10);
		flow.setVgap(10);
		scroll.setContent(borderPane);
		scroll.setFitToWidth(true);

		flow.setPadding(new Insets(5));
		scroll.getStyleClass().add("flow");

		scroll.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));

		general1.prefWidthProperty().bind(generalGrid.widthProperty().multiply(2.0 / 8.0));
		general2.prefWidthProperty().bind(generalGrid.widthProperty().multiply(2.0 / 8.0));
		general3.prefWidthProperty().bind(generalGrid.widthProperty().multiply(1.0 / 8.0));
		general4.prefWidthProperty().bind(generalGrid.widthProperty().multiply(2.0 / 8.0));
		general5.prefWidthProperty().bind(generalGrid.widthProperty().multiply(1.0 / 8.0));

		if (!(pret instanceof PretElectromenager && pret.estReporte(LocalDate.now())) || AccueilController.droit == Droit.UTILISATEUR)
			rembrFournisseur.setVisible(false);

		sendMail.setDisable(AccueilController.droit == Droit.UTILISATEUR);
		prelever.setDisable(AccueilController.droit == Droit.UTILISATEUR);
		reporter.setDisable(AccueilController.droit == Droit.UTILISATEUR);
		cloturer.setDisable(AccueilController.droit == Droit.UTILISATEUR);
		rAnticipe.setDisable(AccueilController.droit == Droit.UTILISATEUR);
	}

	public void printCalendrier() {

		if (pret.getDateProchain() != null)
			yearMax = pret.getDateProchain().getYear();
		else {
			for (Report report : pret.getReportsList()) {
				if (report.getDateFin() != null)
					yearMax = report.getDateDebut().getYear();
			}
		}

		if (yearMax <= year)
			next.setDisable(true);
		else
			next.setDisable(false);

		flow.getChildren().clear();
		for (Month month : Month.values()) {
			if (map.get(month).containsKey(year)) {
				BorderPane myMonth = map.get(month).get(year).getButton();
				flow.getChildren().add(myMonth);
				if (rembExterne.get(month).containsKey(year)) {
					((HBox) myMonth.getBottom()).getChildren().get(0).setVisible(true);
					((HBox) myMonth.getBottom()).getChildren().get(0).setOnMouseClicked(e -> {
						try {
							FXMLLoader loader = new FXMLLoader();
							loader.setLocation(getClass().getResource("/sample/Vue/remboursement.fxml"));
							Parent root = loader.load();
							RemboursementController controller = loader.getController();
							Stage stage = new Stage();
							stage.setScene(new Scene(root));
							stage.initModality(Modality.APPLICATION_MODAL);
							stage.setResizable(false);
							controller.setInfo(rembExterne.get(month).get(year));
							stage.show();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					});
				} else
					((HBox) myMonth.getBottom()).getChildren().get(0).setVisible(false);
				if (rembFourni.get(month).containsKey(year)) {
					((HBox) myMonth.getBottom()).getChildren().get(1).setOnMouseClicked(e -> {
						try {
							FXMLLoader loader = new FXMLLoader();
							loader.setLocation(getClass().getResource("/sample/Vue/remboursement.fxml"));
							Parent root = loader.load();
							RemboursementController controller = loader.getController();
							Stage stage = new Stage();
							stage.setScene(new Scene(root));
							stage.initModality(Modality.APPLICATION_MODAL);
							stage.setResizable(false);
							controller.setInfo(rembFourni.get(month).get(year));
							stage.show();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					});
				} else
					((HBox) myMonth.getBottom()).getChildren().get(1).setVisible(false);
			} else {
				if (rembExterne.get(month).containsKey(year)) {
					Action action = (new Action(rembExterne.get(month).get(year).getDate(),
							"Remboursement\n" + rembExterne.get(month).get(year).getSomme() + " DA",
							rembExterne.get(month).get(year)));
					BorderPane borderPane = action.getButton();
					borderPane.getBottom().setVisible(false);
					flow.getChildren().add(borderPane);
				} else {
					BorderPane vide = new BorderPane();
					vide.getStyleClass().add("action");
					Label monthL = new Label(month.name());
					monthL.setAlignment(Pos.CENTER);
					monthL.getStyleClass().add("month");
					BorderPane.setAlignment(monthL, Pos.CENTER);
					vide.setTop(monthL);
					flow.getChildren().add(vide);
				}
			}
		}
	}

	public void setcalendrier() {
		map.clear();
		rembExterne.clear();

		for (Month month : Month.values()) {
			map.put(month, new HashMap<Integer, Action>());
			rembExterne.put(month, new HashMap<Integer, Remboursement>());
			rembFourni.put(month, new HashMap<Integer, Remboursement>());
		}

		for (Remboursement remboursement : pret.getRemboursementsList()) {
			if (remboursement.getTypeRemboursement() == TypeRemboursement.PRELEVEMENT) {
				Action action = (new Action(remboursement.getDate(),
						"Remboursement\n" + remboursement.getSomme() + " DA", remboursement));
				map.get(action.date.getMonth()).put(action.date.getYear(), action);
			} else if (remboursement.getTypeRemboursement() == TypeRemboursement.APPORT_EXTERNE)
				rembExterne.get(remboursement.getDate().getMonth()).put(remboursement.getDate().getYear(),
						remboursement);
			else
				rembFourni.get(remboursement.getDate().getMonth()).put(remboursement.getDate().getYear(),
						remboursement);
		}

		for (Report report : pret.getReportsList()) {
			if (report.getDateFin() == null) {
				Action action = (new Action(report.getDateDebut(), "Clôture\n" + report.getCause(), report));
				map.get(action.date.getMonth()).put(action.date.getYear(), action);
			} else {
				LocalDate debut =  report.getDateDebut().plusMonths(1);
				LocalDate fin =  report.getDateFin().plusMonths(1);
				for (LocalDate ld = debut; !(ld.getYear() == fin.getYear() && ld.getMonth() == fin.getMonth()); ld = ld.plusMonths(1)) {
					Action action = (new Action(ld, "Report\n" + report.getCause(), report));
					map.get(action.date.getMonth()).put(action.date.getYear(), action);
				}
			}


		}
	}

	// Pour un afficher un prêt archivé
	public void disableArchive() {
		prelever.setVisible(false);
		reporter.setVisible(false);
		cloturer.setVisible(false);
		rAnticipe.setVisible(false);
		sendMail.setVisible(false);
		more.setVisible(false);
		rembrFournisseur.setVisible(false);
	}
}
