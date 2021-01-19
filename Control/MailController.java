package sample.Control;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sample.noyau.service.COS;

public class MailController {

	@FXML
	private TextField mail;

	@FXML
	private PasswordField mdp;

	public void initialize() {
		mail.setText(COS.getCosEmail());
		mdp.setText(COS.getPasswordEmail());
	}

	public void confirmer() {
		COS.setCosEmail(mail.getText());
		COS.setPasswordEmail(mdp.getText());
		Stage stg = (Stage) mail.getScene().getWindow();
		stg.close();
	}
}
