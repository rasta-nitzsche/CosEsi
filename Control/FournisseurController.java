package sample.Control;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import sample.Main;
import sample.noyau.service.COS;
import sample.noyau.util.Notification;
import java.util.List;

public class FournisseurController extends Controller {

	@FXML
	public TableView<String> table;
	@FXML
	private TableColumn<String, String> fournisseur;
	@FXML
	private TableColumn<String, Boolean> supprimer;
	@FXML
	private TextField fournisseurTextField;

	public void settings() {
		callPage(Main.getPrimaryStage(), getClass().getResource("/sample/Vue/parametres.fxml").toString());
	}

	public void confirmer() {
		if (!fournisseurTextField.getText().isEmpty() || !fournisseurTextField.getText().isBlank())
			COS.addFournisseur(fournisseurTextField.getText());
		initialize();
	}

	public void initialize() {
		List<String> liste = COS.getFournisseursList();
		fournisseur.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
		table.setItems(FXCollections.observableArrayList(liste));
		supprimer.setCellFactory(new Callback<TableColumn<String, Boolean>, TableCell<String, Boolean>>() {
			@Override
			public TableCell<String, Boolean> call(TableColumn<String, Boolean> personBooleanTableColumn) {
				return new ButtonCell(table, "✘");
			}
		});

		table.setItems(FXCollections.observableArrayList(liste));

		notifRefresh();
		for (Notification notification : COS.notifs) {
			printNotification(notification);
		}
	}

	private class ButtonCell extends TableCell<String, Boolean> {
		Button cellButton = new Button();

		ButtonCell(TableView<String> tblView, String text) {

			cellButton.setText(text);
			cellButton.setId("supp");
			cellButton.setPrefWidth(supprimer.getPrefWidth());
			cellButton.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent t) {
					String fournisseur = table.getItems().get(getIndex());
					COS.removeFournisseur(fournisseur);
					initialize();
				}
			});
		}

		@Override
		protected void updateItem(Boolean t, boolean empty) {
			super.updateItem(t, empty);
			if (!empty) {
				setGraphic(cellButton);
			}
		}
	}

}
