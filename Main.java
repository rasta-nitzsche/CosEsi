package sample;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import sample.Control.AccueilController;
import sample.Control.DatabaseController;
import sample.Control.DemandeController;
import sample.noyau.entity.*;
import sample.noyau.service.*;
import sample.noyau.util.FTPUtil;
import sample.noyau.util.HibernateUtil;
import sample.noyau.util.Notification;

public class Main extends Application {
	static private Stage primaryStage;
	static private HostServices hostService;

	@Override
	public void start(Stage primaryStage) throws Exception {

		Main.hostService = getHostServices();
		Main.primaryStage = primaryStage;

		Image icon = new Image(getClass().getResourceAsStream("/sample/sources/icon.png"));
		primaryStage.getIcons().add(icon);

		if (!COS.externConnection()) {
			try {
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("Vue/databaseCnx.fxml"));
				Parent root = loader.load();
				Stage stage = new Stage();
				stage.setScene(new Scene(root));
				DatabaseController controller = loader.getController();
				controller.setInfo(stage);
				stage.setResizable(false);
				stage.show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				HibernateUtil.initialize(COS.getAnnotatedClasses(), "update");
				FTPUtil.initialize();
				if (!COS.getCOSHost().exists()) {
					COS.getCOSHost().createNewFile();
					COS.deserialiserHost();
				} else if (!FTPUtil.checkFileExists(COS.loadPath))
					COS.serialiserHost();
				accueil();
			} catch (Exception e) {
				DemandeController.showAlert("Connexion échouée", "Veuillez vérifier les paramètres de la connexion !");
			}
		}
	}

	public static void main(String[] args) {
		/** Don't Change this please **/
		COS.addAnnotatedClasses();
		COS.deserialiser();
		COS.deserialiserHost();
		launch(args);
		COS.serialiser();
		if (COS.estConnecte() && CompteService.compteActuel.getDroit() == Droit.SUPERUTILISATEUR)
			COS.serialiserHost();
		Notification.shutdownTasks();
		CompteService.sortir();
		System.exit(0);
	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static HostServices getHostService() {
		return hostService;
	}

	public static void accueil() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("Vue/accueil.fxml"));
		Parent root = loader.load();
		AccueilController controller = loader.getController();
		controller.setHostServices(hostService);
		controller.setUser();
		primaryStage.setTitle("COSEsi");
		primaryStage.setScene(new Scene(root));
		primaryStage.setWidth(1200);
		primaryStage.setHeight(700);
		primaryStage.show();
		Notification.initialize();
	}

}