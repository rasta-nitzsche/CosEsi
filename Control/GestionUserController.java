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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import sample.Main;
import sample.noyau.entity.Compte;
import sample.noyau.entity.Droit;
import sample.noyau.service.COS;
import sample.noyau.service.CompteService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.util.List;

public class GestionUserController extends Controller {

	@FXML
	private TableView<Compte> table;

	@FXML
	private TableColumn<Compte, String> nom;

	@FXML
	private TableColumn<Compte, String> droit;

	@FXML
	private TableColumn<Compte, Boolean> supprimer;

	@FXML
	private TableColumn<Compte, Boolean> modifier;

	public void settings() {
		Controller.callPage(Main.getPrimaryStage(),
				getClass().getResource("/sample/Vue/parametres.fxml").toString());

	}

	public void addUser() {
		Controller.callPage(Main.getPrimaryStage(), getClass().getResource("/sample/Vue/addUser.fxml").toString());
	}

	private class ButtonCellModif extends TableCell<Compte, Boolean> {
		Button cellButton = new Button();

		ButtonCellModif(TableView<Compte> tblView) {

			cellButton.setText("Modifier");
			cellButton.setId("modif");
			cellButton.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent t) {
					Droit droit = Droit.SUPERUTILISATEUR;
					Compte compte = table.getItems().get(getIndex());
					if (compte.getDroit() == Droit.SUPERUTILISATEUR) {
						droit = Droit.UTILISATEUR;
					} else {
						droit = Droit.SUPERUTILISATEUR;
					}
					CompteService.modifierDroit(CompteService.compteActuel.getMdp(), compte, droit);
					initialize();
				}
			});
		}

		@Override
		protected void updateItem(Boolean t, boolean empty) {
			super.updateItem(t, empty);
			if (!empty) {
				if (table.getItems().get(getIndex()).equals(COS.getCompteAdmin()))
					cellButton.setVisible(false);
				setGraphic(cellButton);
			}
		}
	}

	private class ButtonCellsupp extends TableCell<Compte, Boolean> {
		Button cellButton = new Button();

		ButtonCellsupp(TableView<Compte> tblView) {

			cellButton.setText("Supprimer");
			cellButton.setId("supp");
			cellButton.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent t) {
					Compte compte = table.getItems().get(getIndex());
					CompteService.supprimer(CompteService.compteActuel.getMdp(), compte);
					initialize();
				}
			});

		}

		@Override
		protected void updateItem(Boolean t, boolean empty) {
			super.updateItem(t, empty);
			if (!empty) {
				if (table.getItems().get(getIndex()).equals(COS.getCompteAdmin()))
					cellButton.setVisible(false);
				setGraphic(cellButton);
			}
		}
	}

	public void initialize() {
		List<Compte> liste = CompteService.lireComptes();

		nom.setCellValueFactory(cellData -> cellData.getValue().getNomProperty());
		droit.setCellValueFactory(cellData -> cellData.getValue().getDroitProperty());
		supprimer.setCellFactory(new Callback<TableColumn<Compte, Boolean>, TableCell<Compte, Boolean>>() {
			@Override
			public TableCell<Compte, Boolean> call(TableColumn<Compte, Boolean> compteBooleanTableColumn) {
				return new ButtonCellsupp(table);
			}

		});

		modifier.setCellFactory(new Callback<TableColumn<Compte, Boolean>, TableCell<Compte, Boolean>>() {
			@Override
			public TableCell<Compte, Boolean> call(TableColumn<Compte, Boolean> compteBooleanTableColumn) {
				return new GestionUserController.ButtonCellModif(table);
			}
		});

		table.setItems(FXCollections.observableArrayList(liste));

		notifRefresh();
		for (Notification notification : COS.notifs) {
			printNotification(notification);
		}
	}

	public void mail() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/sample/Vue/mail.fxml"));
			Parent root = loader.load();
			MailController controller = loader.getController();
			Stage stage = new Stage();
			stage.initOwner(Main.getPrimaryStage());
			stage.initStyle(StageStyle.UTILITY);
			stage.setScene(new Scene(root));
			controller.initialize();
			stage.show();
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}
}
