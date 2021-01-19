package sample.Control;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import sample.Main;

import sample.noyau.entity.Demande;
import sample.noyau.entity.Employe;
import sample.noyau.entity.Status;
import sample.noyau.entity.TypePret;
import sample.noyau.service.COS;
import sample.noyau.service.DemandeService;
import sample.noyau.util.ID;
import sample.noyau.service.EmployeService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DemandeController extends Controller {

	@FXML
	private ChoiceBox comboBox;

	@FXML
	private TextField nom;

	@FXML
	private TextField somme;

	@FXML
	private ChoiceBox fournisseurs;

	@FXML
	private Label frn;

	@FXML
	private DatePicker date;

	@FXML
	private TextField description;

	@FXML
	public void initialize() {
		notifRefresh();
		for (Notification notification : COS.notifs) {
			printNotification(notification);
		}

		ObservableList<String> items = FXCollections.observableArrayList("Social", "Electroménager", "Don");
		comboBox.setValue("Social");
		comboBox.setItems(items);

		ObservableList<String> frns = FXCollections.observableArrayList(COS.getFournisseursList());
		fournisseurs.setValue("");
		fournisseurs.setItems(frns);

		somme.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					somme.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});

		frn.setVisible(false);
		fournisseurs.setVisible(false);

		comboBox.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				if (comboBox.getValue().equals("Electroménager")) {
					frn.setVisible(true);
					fournisseurs.setVisible(true);
				} else {
					frn.setVisible(false);
					fournisseurs.setVisible(false);
				}
			}
		});

		TextFields.bindAutoCompletion(nom, EmployeService.lireEmploye());

	}

	@FXML
	public void demande() {
		Controller.callPage(Main.getPrimaryStage(), getClass().getResource("/sample/Vue/Demandes.fxml").toString());
	}

	public void comfirmeDemande() {
		if (nom.getText().isEmpty()) {
			(new Notification("Demande échouée", "Cause: Employé non spécifié", LocalDateTime.now(), true))
					.ajouterTrayNotif(NotificationType.ERROR);
		} else {
			if (comboBox.getSelectionModel().isEmpty()) {
				(new Notification("Demande échouée", "Cause: Type de prêt non spécifié", LocalDateTime.now(), true))
						.ajouterTrayNotif(NotificationType.ERROR);
			} else {
				if (date.getValue() == null) {
					(new Notification("Demande échouée", "Cause: Date de la demande non spécifiée", LocalDateTime.now(),
							true)).ajouterTrayNotif(NotificationType.ERROR);
				} else {
					if (date.getValue().isAfter(LocalDate.now())) {
						(new Notification("Demande échouée",
								"Cause: Date de la demande est érronée (Supèrieure à la date d'aujourd'hui)",
								LocalDateTime.now(), true)).ajouterTrayNotif(NotificationType.ERROR);
					} else {
						if (somme.getText().isEmpty()) {
							(new Notification("Demande échouée", "Cause: Somme non spécifié", LocalDateTime.now(),
									true)).ajouterTrayNotif(NotificationType.ERROR);
						} else {
							if (description.getText().isEmpty()) {
								(new Notification("Demande échouée", "Cause: description non spécifiée",
										LocalDateTime.now(), true)).ajouterTrayNotif(NotificationType.ERROR);
							} else {
								if (comboBox.getSelectionModel().isSelected(1)
										&& fournisseurs.getSelectionModel().isEmpty()) {
									(new Notification("Demande échouée", "Cause: fournisseur non spécifié",
											LocalDateTime.now(), true)).ajouterTrayNotif(NotificationType.ERROR);
								} else {
									try {
										TypePret typePret;
										if (comboBox.getSelectionModel().isSelected(1)) {
											typePret = TypePret.ELECTROMENAGER;
										} else if (comboBox.getSelectionModel().isSelected(0)) {
											typePret = TypePret.SOCIAL;
										} else {
											typePret = TypePret.DON;
										}

										String[] string = nom.getText().split("-");
										String id = string[0];
										Employe employe = EmployeService.trouverID(id);

										Status status = Status.EN_COURS;
										Demande demande = new Demande(new ID(), typePret, employe, date.getValue(), status,
												Integer.parseInt(somme.getText()), description.getText(),
												fournisseurs.getValue().toString());
										DemandeService.ajouter(demande);

										(new Notification("Demande Ajoutée",
												"Votre demande a bien été ajoutée et est disponible dans le menu des demandes à traiter",
												LocalDateTime.now(), true)).ajouterTrayNotif(NotificationType.ERROR);

										Controller.callPage(Main.getPrimaryStage(),
												getClass().getResource("/sample/Vue/Demandes.fxml").toString());
									}
									catch (Exception e) {
										(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
									}

								}
							}
						}
					}
				}
			}
		}
	}

	public static Alert showAlert(String title, String Content) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(Content);
		alert.showAndWait();
		return alert;
	}

}
