package sample.Control;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sample.Main;
import sample.noyau.entity.*;
import sample.noyau.service.COS;
import sample.noyau.service.PretService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ArchivesController extends Controller {

	@FXML
	private TableView<Archive> table;

	@FXML
	private TableColumn<Archive, String> annee;

	@FXML
	private TableColumn<Archive, String> Psociaux;

	@FXML
	private TableColumn<Archive, String> Pelectro;

	@FXML
	private TableColumn<Archive, String> dons;

	@FXML
	private TableColumn<Archive, String> budget;

	@FXML
	private TableColumn<Archive, String> total;

	@FXML
	private Button archiverB;

	public static Archive archive;

	@FXML
	public void initialize() {
		archiverB.setDisable(AccueilController.droit == Droit.UTILISATEUR);
		annee.setCellValueFactory(cellData -> cellData.getValue().getanneeProperty().asString());
		Psociaux.setCellValueFactory(cellData -> cellData.getValue().getnbPsociauxProperty().asString());
		Pelectro.setCellValueFactory(cellData -> cellData.getValue().getnbPelectroProperty().asString());
		dons.setCellValueFactory(cellData -> cellData.getValue().getDonsProperty().asString());
		total.setCellValueFactory(cellData -> cellData.getValue().getTotalProperty().asString());
		budget.setCellValueFactory(cellData -> cellData.getValue().getBudgetProperty().asString());

		Set<Year> anneesArchivees = COS.anneesArchivees();
		List<Archive> liste = new ArrayList<Archive>();
		if (!anneesArchivees.contains(COS.getAnneeSociale()))
			liste.add(new Archive(COS.getAnneeSociale().getValue(), 0, 0, 0, 0, COS.getBudgetInit()));
		for (Year year : anneesArchivees) {
			List<Pret> db = PretService.lireArchive(year.getValue());
			int nbPsociaux = 0, nbPelectro = 0, nbDons = 0, ttl = 0;
			double budgetInit = 0;
			ttl = db.size();
			for (Pret pret : db) {
				if (pret.getClassName().equals("PretSocial")) {
					nbPsociaux++;
				} else if (pret.getClassName().equals("Don")) {
					nbDons++;
				} else {
					nbPelectro++;
				}
			}
			budgetInit = COS.getBudgetAnnuel(year).get(0);
			liste.add(new Archive(year.getValue(), nbPsociaux, nbPelectro, nbDons, ttl, budgetInit));
		}
		table.setItems(FXCollections.observableArrayList(liste));

		table.setRowFactory(tv -> {
			TableRow<Archive> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					archive = row.getItem();

					try {
						FXMLLoader loader = new FXMLLoader();
						loader.setLocation(getClass().getResource("/sample/Vue/archivePrets.fxml"));
						Parent root = loader.load();
						Stage pretStage = new Stage();
						pretStage.initOwner(Main.getPrimaryStage());
						pretStage.initStyle(StageStyle.UTILITY);
						pretStage.setScene(new Scene(root));
						pretStage.show();
					} catch (Exception e) {
						(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
					}
				}
			});
			return row;
		});

		notifRefresh();
		for (Notification notification : COS.notifs) {
			printNotification(notification);
		}

	}

	@FXML
	public void archiver() {
		PretService.archiverPrets();
		(new Notification("Opération effetuée", "Tous les prêts cloturés de cette année sont archivés",
				LocalDateTime.now())).ajouterTrayNotif(NotificationType.INFORMATION);
		initialize();
	}
}
