package sample.Control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import sample.Main;
import sample.noyau.entity.Employe;
import sample.noyau.entity.Pret;
import sample.noyau.entity.TypePret;
import sample.noyau.service.COS;
import sample.noyau.service.EmployeService;
import sample.noyau.service.PretService;
import sample.noyau.service.StatService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import javax.imageio.ImageIO;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class StatsController extends Controller implements Initializable {
	private PieChart typeChart;
	private AreaChart<String, Number> moisChart;
	private AreaChart<String, Number> anneeChart;
	private BarChart<String, Number> employeChart;
	private BarChart<String, Number> fournisseurChart;
	private HBox hBox1 = new HBox(10);
	private Button one = new Button();
	private Button two = new Button();
	private HBox hBox = new HBox(20);
	private int year = 2019;
	private XYChart.Series seriesMois = new XYChart.Series();
	private List<Pret> listPrets = new ArrayList<>();
	private Button prev = null;
	private Button next = null;

	@FXML
	private VBox vBox;

	@FXML
	private Button save;

	@FXML
	private MenuButton graphique;

	@FXML
	void save(ActionEvent event) {
		try {
			SnapshotParameters snapshotParameters = new SnapshotParameters();
			snapshotParameters.setTransform(new Scale(2, 2));
			VBox Box = new VBox(20);
			// Box.getChildren().addAll(vBox.getChildren().get(1),vBox.getChildren().get(2));
			vBox.getChildren().remove(0);
			vBox.getChildren().remove(2);
			WritableImage snapShot = vBox.snapshot(snapshotParameters, null);

			Node chart = vBox.getChildren().get(0);
			vBox.getChildren().clear();
			vBox.getChildren().addAll(save, chart, hBox, hBox1);

			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Sauvegarder les statistiques");
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			fileChooser.setInitialFileName("Stats.png");
			FileChooser.ExtensionFilter pdfExtensionFilter = new FileChooser.ExtensionFilter("PNG", "*.png");
			fileChooser.getExtensionFilters().add(pdfExtensionFilter);
			fileChooser.setSelectedExtensionFilter(pdfExtensionFilter);
			File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
			if (file != null) {
				try {
					ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null), "png", new File(file.getAbsolutePath()));
				} catch (Exception e) {
					(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
				}
			}
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

		listPrets.addAll(PretService.lirePrets());

		notifRefresh();

		ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
				new PieChart.Data("Prêt électromenager",
						StatService.nbPretsParType(listPrets, TypePret.ELECTROMENAGER)),
				new PieChart.Data("Prêt social", StatService.nbPretsParType(listPrets, TypePret.SOCIAL)),
				new PieChart.Data("Don", StatService.nbPretsParType(listPrets, TypePret.DON)));
		typeChart = new PieChart(pieChartData);
		typeChart.setTitle("Type de services");

		final Label caption = new Label("");
		caption.setTextFill(Color.DARKORANGE);
		caption.setStyle("-fx-font: 24 arial;");

		/*******************************************************************************/

		final CategoryAxis xAxis2 = new CategoryAxis();
		final NumberAxis yAxis2 = new NumberAxis();
		xAxis2.setLabel("Années");
		yAxis2.setLabel("Nombre de prêts");

		anneeChart = new AreaChart<String, Number>(xAxis2, yAxis2);
		anneeChart.setTitle("Nombre de prêts moyen par Année");

		XYChart.Series series2 = new XYChart.Series();
		series2.setName("2010's");

		for (int annee = 2018; annee <= LocalDate.now().getYear(); annee++)
			series2.getData().add(new XYChart.Data(annee + "",
					StatService.nbPretsParPeriode(listPrets, LocalDate.of(annee, 1, 1), LocalDate.of(annee, 12, 31))));

		anneeChart.getData().add(series2);

		/*******************************************************************************/

		final CategoryAxis xAxis = new CategoryAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel("Mois");
		yAxis.setLabel("Nombre de prêts");

		moisChart = new AreaChart<String, Number>(xAxis, yAxis);
		moisChart.setTitle("Nombre de prêts moyen par mois");

		XYChart.Series seriesMois = new XYChart.Series();
		seriesMois.setName(year + "-" + (year + 1));

		seriesMois.getData().add(new XYChart.Data("Jul",
				StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 7, 1), LocalDate.of(year, 7, 31))));
		seriesMois.getData().add(new XYChart.Data("Aou",
				StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 8, 1), LocalDate.of(year, 8, 31))));
		seriesMois.getData().add(new XYChart.Data("Sep",
				StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 9, 1), LocalDate.of(year, 9, 30))));
		seriesMois.getData().add(new XYChart.Data("Oct",
				StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 10, 1), LocalDate.of(year, 10, 31))));
		seriesMois.getData().add(new XYChart.Data("Nov",
				StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 11, 1), LocalDate.of(year, 11, 30))));
		seriesMois.getData().add(new XYChart.Data("Dec",
				StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 12, 1), LocalDate.of(year, 12, 31))));
		seriesMois.getData().add(new XYChart.Data("Jan",
				StatService.nbPretsParPeriode(listPrets, LocalDate.of(year + 1, 1, 1), LocalDate.of(year + 1, 1, 31))));
		seriesMois.getData().add(new XYChart.Data("Fev",
				StatService.nbPretsParPeriode(listPrets, LocalDate.of(year + 1, 2, 1), LocalDate.of(year + 1, 2, 28))));
		seriesMois.getData().add(new XYChart.Data("Mar",
				StatService.nbPretsParPeriode(listPrets, LocalDate.of(year + 1, 3, 1), LocalDate.of(year + 1, 3, 31))));
		seriesMois.getData().add(new XYChart.Data("Avr",
				StatService.nbPretsParPeriode(listPrets, LocalDate.of(year + 1, 4, 1), LocalDate.of(year + 1, 4, 30))));
		seriesMois.getData().add(new XYChart.Data("Mai",
				StatService.nbPretsParPeriode(listPrets, LocalDate.of(year + 1, 5, 1), LocalDate.of(year + 1, 5, 31))));
		seriesMois.getData().add(new XYChart.Data("Jun",
				StatService.nbPretsParPeriode(listPrets, LocalDate.of(year + 1, 6, 1), LocalDate.of(year + 1, 6, 30))));

		moisChart.getData().add(seriesMois);

		HBox moisBox = new HBox(10);
		moisBox.setAlignment(Pos.CENTER_LEFT);

		try {
			prev = new Button("",new ImageView(new Image(this.getClass().getResourceAsStream("/sample/sources/prevB.png"))));
			next = new Button("",new ImageView(new Image(this.getClass().getResourceAsStream("/sample/sources/nextB.png"))));
		} catch (Exception e) {
			e.printStackTrace();
		}
		next.getStyleClass().add("change");
		prev.getStyleClass().add("change");
		prev.setOnMouseClicked(e -> {
			year--;
			seriesMois.setName(year + "-" + (year + 1));
			if ((2019 >= year))
				prev.setDisable(true);
			next.setDisable(false);
			seriesMois.getData().clear();
			seriesMois.getData().add(new XYChart.Data("Jul",
					StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 7, 1), LocalDate.of(year, 7, 31))));
			seriesMois.getData().add(new XYChart.Data("Aou",
					StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 8, 1), LocalDate.of(year, 8, 31))));
			seriesMois.getData().add(new XYChart.Data("Sep",
					StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 9, 1), LocalDate.of(year, 9, 30))));
			seriesMois.getData().add(new XYChart.Data("Oct",
					StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 10, 1), LocalDate.of(year, 10, 31))));
			seriesMois.getData().add(new XYChart.Data("Nov",
					StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 11, 1), LocalDate.of(year, 11, 30))));
			seriesMois.getData().add(new XYChart.Data("Dec",
					StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 12, 1), LocalDate.of(year, 12, 31))));
			seriesMois.getData().add(new XYChart.Data("Jan", StatService.nbPretsParPeriode(listPrets,
					LocalDate.of(year + 1, 1, 1), LocalDate.of(year + 1, 1, 31))));
			seriesMois.getData().add(new XYChart.Data("Fev", StatService.nbPretsParPeriode(listPrets,
					LocalDate.of(year + 1, 2, 1), LocalDate.of(year + 1, 2, 28))));
			seriesMois.getData().add(new XYChart.Data("Mar", StatService.nbPretsParPeriode(listPrets,
					LocalDate.of(year + 1, 3, 1), LocalDate.of(year + 1, 3, 31))));
			seriesMois.getData().add(new XYChart.Data("Avr", StatService.nbPretsParPeriode(listPrets,
					LocalDate.of(year + 1, 4, 1), LocalDate.of(year + 1, 4, 30))));
			seriesMois.getData().add(new XYChart.Data("Mai", StatService.nbPretsParPeriode(listPrets,
					LocalDate.of(year + 1, 5, 1), LocalDate.of(year + 1, 5, 31))));
			seriesMois.getData().add(new XYChart.Data("Jun", StatService.nbPretsParPeriode(listPrets,
					LocalDate.of(year + 1, 6, 1), LocalDate.of(year + 1, 6, 30))));
		});
		next.setOnMouseClicked(e -> {
			year++;
			seriesMois.setName(year + "-" + (year + 1));
			if (year >= LocalDate.now().getYear())
				next.setDisable(true);
			prev.setDisable(false);
			seriesMois.getData().clear();
			seriesMois.getData().add(new XYChart.Data("Jul",
					StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 7, 1), LocalDate.of(year, 7, 31))));
			seriesMois.getData().add(new XYChart.Data("Aou",
					StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 8, 1), LocalDate.of(year, 8, 31))));
			seriesMois.getData().add(new XYChart.Data("Sep",
					StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 9, 1), LocalDate.of(year, 9, 30))));
			seriesMois.getData().add(new XYChart.Data("Oct",
					StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 10, 1), LocalDate.of(year, 10, 31))));
			seriesMois.getData().add(new XYChart.Data("Nov",
					StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 11, 1), LocalDate.of(year, 11, 30))));
			seriesMois.getData().add(new XYChart.Data("Dec",
					StatService.nbPretsParPeriode(listPrets, LocalDate.of(year, 12, 1), LocalDate.of(year, 12, 31))));
			seriesMois.getData().add(new XYChart.Data("Jan", StatService.nbPretsParPeriode(listPrets,
					LocalDate.of(year + 1, 1, 1), LocalDate.of(year + 1, 1, 31))));
			seriesMois.getData().add(new XYChart.Data("Fev", StatService.nbPretsParPeriode(listPrets,
					LocalDate.of(year + 1, 2, 1), LocalDate.of(year + 1, 2, 28))));
			seriesMois.getData().add(new XYChart.Data("Mar", StatService.nbPretsParPeriode(listPrets,
					LocalDate.of(year + 1, 3, 1), LocalDate.of(year + 1, 3, 31))));
			seriesMois.getData().add(new XYChart.Data("Avr", StatService.nbPretsParPeriode(listPrets,
					LocalDate.of(year + 1, 4, 1), LocalDate.of(year + 1, 4, 30))));
			seriesMois.getData().add(new XYChart.Data("Mai", StatService.nbPretsParPeriode(listPrets,
					LocalDate.of(year + 1, 5, 1), LocalDate.of(year + 1, 5, 31))));
			seriesMois.getData().add(new XYChart.Data("Jun", StatService.nbPretsParPeriode(listPrets,
					LocalDate.of(year + 1, 6, 1), LocalDate.of(year + 1, 6, 30))));
		});
		prev.setDisable(true);
		if (LocalDate.now().getYear() <= year)
			next.setDisable(true);
		((ImageView) prev.getGraphic()).setPreserveRatio(true);
		((ImageView) next.getGraphic()).setPreserveRatio(true);
		((ImageView) prev.getGraphic()).setFitWidth(40);
		((ImageView) next.getGraphic()).setFitWidth(40);
		moisBox.getChildren().addAll(prev, moisChart, next);
		moisBox.setPrefWidth(1100);
		moisChart.setPrefWidth(1000);

		/*******************************************************************************/

		List<Employe> listEmploye = new ArrayList<>();
		listEmploye.addAll(EmployeService.lireEmployes());

		final CategoryAxis xAxis3 = new CategoryAxis();
		final NumberAxis yAxis3 = new NumberAxis();
		employeChart = new BarChart<String, Number>(xAxis3, yAxis3);
		employeChart.setTitle("Nombre moyen de prêts par employé");
		xAxis3.setLabel("Employé");
		yAxis3.setLabel("Nombre de prêts");

		XYChart.Series series3 = new XYChart.Series();
		series3.setName("2015-2019");
		for (Employe employe : listEmploye) {
			if (StatService.nbPretParEmploye(employe) != 0)
				series3.getData().add(new XYChart.Data(employe.getNom(), StatService.nbPretParEmploye(employe)));
		}

		employeChart.getData().addAll(series3);

		/*******************************************************************************/

		final CategoryAxis xAxis4 = new CategoryAxis();
		final NumberAxis yAxis4 = new NumberAxis();
		fournisseurChart = new BarChart<String, Number>(xAxis4, yAxis4);
		fournisseurChart.setTitle("Nombre moyen de prêts par fournisseur");
		xAxis4.setLabel("fournisseur");
		yAxis4.setLabel("Nombre de prêts");

		XYChart.Series series4 = new XYChart.Series();
		series4.setName("2019-" + LocalDate.now().getYear());

		Map<String, Integer> fournisseursNbPrets = StatService.nbPretParFournisseurs(listPrets);

		for (String fournisseur : fournisseursNbPrets.keySet())
			series4.getData().add(new XYChart.Data(fournisseur, fournisseursNbPrets.get(fournisseur)));

		fournisseurChart.getData().addAll(series4);

		/*******************************************************************************/

		vBox.setSpacing(20);
		hBox.getChildren().addAll(typeChart, employeChart);
		one.getStyleClass().addAll("which", "on");
		one.setOnAction(e -> {
			vBox.getChildren().remove(1);
			vBox.getChildren().remove(1);
			vBox.getChildren().remove(1);
			vBox.getChildren().add(anneeChart);
			hBox.getChildren().remove(1);
			hBox.getChildren().add(employeChart);
			vBox.getChildren().addAll(hBox, hBox1);
			if (!one.getStyleClass().contains("on"))
				one.getStyleClass().add("on");
			two.getStyleClass().remove("on");
		});

		two.getStyleClass().add("which");
		two.setOnAction(e -> {
			vBox.getChildren().remove(1);
			vBox.getChildren().remove(1);
			vBox.getChildren().remove(1);
			vBox.getChildren().add(moisBox);
			hBox.getChildren().remove(1);
			hBox.getChildren().add(fournisseurChart);
			vBox.getChildren().addAll(hBox, hBox1);
			if (!two.getStyleClass().contains("on"))
				two.getStyleClass().add("on");
			one.getStyleClass().remove("on");
		});

		hBox.setFillHeight(true);
		hBox1.getChildren().addAll(one, two);
		hBox1.setAlignment(Pos.BOTTOM_CENTER);
		vBox.setFillWidth(true);
		vBox.getChildren().addAll(anneeChart, hBox, hBox1);

		for (Notification notification : COS.notifs) {
			printNotification(notification);
		}
	}

}
