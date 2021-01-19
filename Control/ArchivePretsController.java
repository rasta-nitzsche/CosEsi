package sample.Control;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sample.Main;
import sample.noyau.entity.Don;
import sample.noyau.entity.Pret;
import sample.noyau.entity.PretRemboursable;
import sample.noyau.service.PretService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.util.List;

public class ArchivePretsController {

	@FXML
	private TableView<Pret> table;
	@FXML
	private TableColumn<Pret, String> id;
	@FXML
	private TableColumn<Pret, String> type;
	@FXML
	private TableColumn<Pret, String> date;
	@FXML
	private TableColumn<Pret, String> employe;
	@FXML
	private TableColumn<Pret, String> nom;
	@FXML
	private TableColumn<Pret, String> prenom;
	@FXML
	private TableColumn<Pret, Double> somme;

	public void initialize() {
		id.setCellValueFactory(cellData -> cellData.getValue().getIdProperty());
		type.setCellValueFactory(cellData -> cellData.getValue().getTypeProperty());
		date.setCellValueFactory(cellData -> cellData.getValue().getDateProperty());
		nom.setCellValueFactory(cellData -> cellData.getValue().getEmploye().getNomProperty());
		prenom.setCellValueFactory(cellData -> cellData.getValue().getEmploye().getPrenomProperty());
		somme.setCellValueFactory(cellData -> cellData.getValue().getSommeProperty().asObject());

		List<Pret> liste = PretService.lireArchive(ArchivesController.archive.getAnnee());
		table.setItems(FXCollections.observableArrayList(liste));

		table.setRowFactory(tv -> {
			TableRow<Pret> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					Pret pret = row.getItem();
					if (pret instanceof PretRemboursable) {
						try {
							FXMLLoader loader = new FXMLLoader();
							loader.setLocation(getClass().getResource("/sample/Vue/pret.fxml"));
							Parent root = loader.load();
							PretController controller = loader.getController();

							Stage pretStage = new Stage();
							pretStage.initOwner(Main.getPrimaryStage());
							pretStage.initStyle(StageStyle.UTILITY);
							pretStage.setScene(new Scene(root));
							controller.setInfo((PretRemboursable) pret, pretStage, null, table);
							controller.disableArchive();

							pretStage.show();
						} catch (Exception e) {
							(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
						}
					}
					if (pret instanceof Don) {
						try {
							FXMLLoader loader = new FXMLLoader();
							loader.setLocation(getClass().getResource("/sample/Vue/don.fxml"));
							Parent root = loader.load();
							DonController controller = loader.getController();

							Stage stageDon = new Stage();
							controller.setInfo((Don) pret, stageDon, null);
							controller.disableArchive();

							stageDon.setScene(new Scene(root));
							stageDon.initStyle(StageStyle.UTILITY);
							stageDon.initOwner(Main.getPrimaryStage());
							stageDon.show();
						} catch (Exception e) {
							(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
						}
					}
				}
			});
			return row;
		});
	}
}
