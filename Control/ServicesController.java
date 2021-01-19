package sample.Control;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import sample.Main;
import sample.noyau.service.COS;

import java.util.List;

public class ServicesController extends Controller {

	@FXML
	public TableView<String> table;
	@FXML
	private TableColumn<String, String> srv;
	@FXML
	private TextField service;

	public void settings(){
		callPage(Main.getPrimaryStage(),getClass().getResource("/sample/Vue/parametres.fxml").toString());
	}

	public void confirmer(){
		COS.addService(service.getText());
		callPage(Main.getPrimaryStage(),getClass().getResource("/sample/Vue/parametres.fxml").toString());
	}

	public void initialize() {
		List<String> liste = COS.getServicesList();
		srv.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
		table.setItems(FXCollections.observableArrayList(liste));

	}

}
