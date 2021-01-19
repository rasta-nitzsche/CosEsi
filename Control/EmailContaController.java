package sample.Control;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import sample.noyau.entity.Employe;
import sample.noyau.service.COS;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class EmailContaController implements Initializable {

	@FXML
	private Button confirmer;

	@FXML
	private TextField email;

	public static List<Employe> employeList = new ArrayList<Employe>();
	public static String title = "";

	private File printPDF() {
		File file = new File("employe.pdf");

		try {
			PDDocument document = new PDDocument();

			PDPage blankPage = new PDPage();

			// Adding the blank page to the document
			document.addPage(blankPage);

			PDPageContentStream contentStream = new PDPageContentStream(document, blankPage);
			contentStream.beginText();
			contentStream.setFont(PDType1Font.HELVETICA, 24);
			contentStream.setLeading(32f);
			contentStream.newLineAtOffset(100, 725);
			contentStream.showText(title);
			contentStream.newLine();

			contentStream.setFont(PDType1Font.HELVETICA, 18);
			contentStream.setLeading(17f);
			// contentStream.newLineAtOffset(25, 550);

			int number = 0;
			for (Employe employe : employeList) {
				number++;
				contentStream.showText("  " + employe.getId() + "    " + employe.getNom() + "   " + employe.getPrenom()
						+ "   " + employe.getMontantRembourse() + "  DA");
				if (number < 35)
					contentStream.newLine();
				else {
					contentStream.endText();
					contentStream.close();

					number = 0;
					blankPage = new PDPage();
					document.addPage(blankPage);

					contentStream = new PDPageContentStream(document, blankPage);
					contentStream.beginText();
					contentStream.setFont(PDType1Font.HELVETICA, 18);
					contentStream.setLeading(17f);
					contentStream.newLineAtOffset(100, 725);
				}
			}

			contentStream.endText();
			contentStream.close();

			if (file != null) {
				try {
					document.save(file);
				} catch (Exception e) {
					(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
				}
			}

			document.close();

		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
		return file;
	}

	@FXML
	void confirmer(ActionEvent event) {
		try {
			Notification.envoyerMessage(COS.getCosEmail(), COS.getPasswordEmail(), email.getText(), title, "",
					printPDF(), "Liste des employés.pdf");
		} catch (Exception e) {
			(new Notification("Erreur de mail", "Message non envoyé", LocalDateTime.now(), false))
					.ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		confirmer.disableProperty().bind(Bindings.isEmpty(email.textProperty()));
	}

	public static void setInfo(String title, List<Employe> employeList) {
		EmailContaController.employeList = employeList;
		EmailContaController.title = title;
	}
}
