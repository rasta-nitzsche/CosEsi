package sample.Control;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import sample.Main;
import sample.noyau.entity.Droit;
import sample.noyau.service.COS;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {
	@FXML
	protected VBox menu;
	@FXML
	protected VBox notifications;
	@FXML
	protected ImageView notifIcon;
	@FXML
	protected ImageView expand;
	protected ChangeListener<Boolean> changeListener;
	protected Timer timer = new Timer();
	
	//Added
	private static int nbNotifs = 0;

	public void notifRefresh() {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						notifications.getChildren().clear();
						for (Notification notification : COS.notifs) {
							printNotification(notification);
						}
						
						//Added
						if (menu.isVisible()) {
							if (COS.notifs.size() > nbNotifs)
								notifIcon.setImage(MenuController.existNotif);	
							else
								notifIcon.setImage(MenuController.noNotif);
						}
						
					}
				});
			}
		}, 0, 5000);
	}

	@FXML
	void showNotifications(MouseEvent event) {
		notifications.setVisible(!notifications.isVisible());
		menu.setVisible(!menu.isVisible());
		if (menu.isVisible()) {
			expand.setImage(MenuController.more);
			notifIcon.setImage(MenuController.noNotif);
		} else {
			//Added
			nbNotifs = COS.notifs.size();
			expand.setImage(MenuController.less);
			notifIcon.setImage(MenuController.moreNotif);
		}
	}

	public void printNotification(Notification notification) {
		BorderPane notif = new BorderPane();
		try {
			if (notification.isException())
				notif.setLeft(
						new ImageView(new Image(this.getClass().getResourceAsStream("/sample/sources/error.png"))));
			else
				notif.setLeft(
						new ImageView(new Image(this.getClass().getResourceAsStream("/sample/sources/info.png"))));
			((ImageView) notif.getLeft()).setFitWidth(75);
			((ImageView) notif.getLeft()).setPreserveRatio(true);
		} catch (Exception e) {
		}
		BorderPane borderPane = new BorderPane();
		Label titre = new Label(notification.getTitre());
		Label x = new Label();
		x.setText("x");
		x.setOnMouseClicked(e -> {
			COS.notifs.remove(notification);
			notifications.getChildren().remove(notif);
			//Added
			nbNotifs = COS.notifs.size();
		});
		borderPane.setLeft(titre);
		borderPane.setRight(x);
		titre.getStyleClass().add("titreNotif");
		x.getStyleClass().add("x");
		notif.setTop(borderPane);
		BorderPane.setAlignment(borderPane, Pos.CENTER_LEFT);

		Text message = new Text(notification.getDescription());
		message.setWrappingWidth(230);
		message.getStyleClass().addAll("messageNotif");
		notif.setCenter(message);
		BorderPane.setAlignment(message, Pos.CENTER_LEFT);

		Label date = new Label(notification.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		date.getStyleClass().addAll("dateNotif");
		notif.setBottom(date);
		BorderPane.setAlignment(date, Pos.CENTER_RIGHT);

		notif.getStyleClass().addAll("notification");
		notifications.getChildren().addAll(notif);
		
		//Added
		if (notification.isClotureNotif()) {
			notif.setDisable(AccueilController.droit == Droit.UTILISATEUR);
			notif.setOnMouseClicked(e -> {
				nAnneeWindow(notification);
			});
		}
	}

	@FXML
	public void afficherHome() {
		// TODO conditions sur les comptes
		timer.cancel();
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
	public void afficherDons(ActionEvent event) {
		timer.cancel();
		Controller.callPage(Main.getPrimaryStage(), getClass().getResource("/sample/Vue/dons.fxml").toString());
	}

	@FXML
	public void afficherArchives() {
		timer.cancel();
		Controller.callPage(Main.getPrimaryStage(), getClass().getResource("/sample/Vue/archivesMenu.fxml").toString());
	}

	@FXML
	void afficherDemandes(ActionEvent event) {
		timer.cancel();
		Controller.callPage(Main.getPrimaryStage(), getClass().getResource("/sample/Vue/Demandes.fxml").toString());
	}

	@FXML
	void afficherStats(ActionEvent event) {
		timer.cancel();
		Controller.callPage(Main.getPrimaryStage(), getClass().getResource("/sample/Vue/stats.fxml").toString());
	}

	@FXML
	public void afficherPrets(ActionEvent event) {
		timer.cancel();
		Controller.callPage(Main.getPrimaryStage(), getClass().getResource("/sample/Vue/prets.fxml").toString());
	}

	@FXML
	public void afficherEmployes() {
		timer.cancel();
		Controller.callPage(Main.getPrimaryStage(), getClass().getResource("/sample/Vue/employes.fxml").toString());
	}

	public static void callPage(Stage primaryStage, String XMLFile) {
		try {
			URL url = new URL(XMLFile);
			// URL url = new File(XMLFile).toURI().toURL();
			Parent root = FXMLLoader.load(url);
			primaryStage.getScene().setRoot(root);
			// primaryStage.setScene(new Scene(root));
			primaryStage.show();
			// primaryStage.setResizable(false);
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}
	
	
	//Added
	private void nAnneeWindow(Notification notification) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/nAnnee.fxml"));
			Parent root = loader.load();
			NAnneeController controller = loader.getController();
			Stage stage = new Stage();
			stage.setScene(new Scene(root));
			stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e1 -> {
				if (COS.anneesArchivees().contains(COS.getAnneeSociale()) && COS.getBudgetsAnnuels().containsKey(COS.getAnneeSociale()))
					try {
						COS.notifs.remove(notification);
						nbNotifs = COS.notifs.size();
						FXMLLoader loader1 = new FXMLLoader();
						loader1.setLocation(getClass().getResource("/sample/Vue/menu.fxml"));
						Parent root1 = loader1.load();
						MenuController controller1 = loader1.getController();
						Main.getPrimaryStage().getScene().setRoot(root1);
						controller1.setInfo();
					} catch (Exception e2) {
						(new Notification(e2)).ajouterTrayNotif(NotificationType.ERROR);
					}
			});
			stage.setResizable(false);
			stage.initOwner(Main.getPrimaryStage());
			stage.initStyle(StageStyle.UTILITY);
			stage.initModality(Modality.APPLICATION_MODAL);
			controller.setInfo(stage);
			stage.show();
		} catch (Exception e1) {
			(new Notification(e1)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

}
