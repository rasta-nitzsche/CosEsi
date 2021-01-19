package sample.Control;

import java.time.LocalDateTime;

import org.hibernate.Session;
import org.hibernate.Transaction;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.noyau.entity.Don;
import sample.noyau.entity.Pret;
import sample.noyau.entity.PretRemboursable;
import sample.noyau.service.COS;
import sample.noyau.service.PretService;
import sample.noyau.util.HibernateUtil;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

//Controlleur de la nouvelle annee sociale
public class NAnneeController {
	
	private Stage stage;
	@FXML
	private Button confirmer;
	@FXML
	private TextField newBudget;

	@FXML
	void confirmer(ActionEvent event) {
		
		PretService.archiverPrets();
		Pret pretCloture = PretService.lirePrets().stream().filter(pret -> (
				pret instanceof PretRemboursable && ((PretRemboursable) pret).estCloture()) ||
				pret instanceof Don).findAny().orElse(null);
		
		if (pretCloture == null) {
			COS.anneesArchivees().add(COS.getAnneeSociale());
			Session session = HibernateUtil.getSessionFactory().openSession();
			Transaction transaction = session.beginTransaction();
			session.createSQLQuery("CREATE DATABASE IF NOT EXISTS 	" + HibernateUtil.getDatabaseName() + "_" + COS.getAnneeSociale()).executeUpdate();
			transaction.commit();
			session.close();
		}
		
		(new Notification("Opération effetuée", "Tous les prêts cloturés de cette année sont archivés",
				LocalDateTime.now())).ajouterTrayNotif(NotificationType.INFORMATION);
		COS.nouveauBudgetAnnuel(Double.parseDouble(newBudget.getText()));
		stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
		stage.close();
	}

	public void setInfo(Stage stage) {
		this.stage = stage;

		confirmer.disableProperty().bind(Bindings.isEmpty(newBudget.textProperty()));

		newBudget.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d{0,9}([\\.]\\d{0,4})?") || newValue.equals("0")) {
					newBudget.setText(oldValue);
				}
			}
		});

	}

}
