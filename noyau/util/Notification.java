package sample.noyau.util;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import sample.noyau.entity.Droit;
import sample.noyau.exception.NotifException;
import sample.noyau.service.COS;
import sample.noyau.service.CompteService;
import tray.animations.AnimationType;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;

public class Notification implements Comparable<Notification>, Serializable {

	private String titre;
	private String description;
	private LocalDateTime date;
	private boolean exception;

	private static List<ScheduledExecutorService> tasks = new ArrayList<ScheduledExecutorService>();

	/** Général */
	public Notification(Exception e) {
		e.printStackTrace();
		titre = "Oops";
		description = "Une erreur est survenue";
		date = LocalDateTime.now();
		this.exception = true;
		COS.notifs.add(this);
	}

	/** Pour les exceptions */
	public Notification(NotifException exception) {
		titre = NotifException.msgParDefaut;
		description = exception.getMessage();
		date = LocalDateTime.now();
		this.exception = true;
		COS.notifs.add(this);
	}

	/** Pour les taches */
	public Notification(String titre, String description, LocalDateTime date) {
		this.titre = titre;
		this.description = description;
		this.date = date;
		exception = false;
		COS.notifs.add(this);
	}

	/** Pour les exceptions d'un message personalisé, en général */
	public Notification(String titre, String description, LocalDateTime date, boolean exception) {
		this.titre = titre;
		this.description = description;
		this.date = date;
		this.exception = exception;
		COS.notifs.add(this);
	}

	/** L'initialisation des taches **/
	public static void initialize() {
		preleverNotifTask();
		ramadhanNotifTask();
		clotureAnneeNotifTask();
		syncHostTask();
	}

	/** Creation des taches **/
	public static void tache(LocalDate date, String titre, String description) {
		long initialDelay = LocalDate.now().until(date, ChronoUnit.DAYS);
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				new Notification(titre, description, LocalDateTime.now());
			}
		}, initialDelay, LocalDate.now().lengthOfMonth(), TimeUnit.DAYS);
		tasks.add(scheduler);
	}

	public static void tache(HijrahDate date, String titre, String description) {
		long initialDelay = HijrahDate.now().until(date, ChronoUnit.DAYS);
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				new Notification(titre, description, LocalDateTime.now());
			}
		}, initialDelay, HijrahDate.now().lengthOfMonth(), TimeUnit.DAYS);
		tasks.add(scheduler);
	}
	
	public static void tache(LocalDate date, Boolean cause, String titre, String description) {
		long initialDelay = LocalDate.now().until(date, ChronoUnit.DAYS);
		if (initialDelay < 0) initialDelay = 0;
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (cause)
					new Notification(titre, description, LocalDateTime.now());
			}
		}, initialDelay, 20, TimeUnit.SECONDS);
		tasks.add(scheduler);
	}
	
	public static void tache(Boolean cause, String titre, String description) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (cause)
					new Notification(titre, description, LocalDateTime.now());
			}
		}, 0, 20, TimeUnit.SECONDS);
		tasks.add(scheduler);
	}

	/** Les taches **/
	public static void syncHostTask() {
		ScheduledExecutorService scheduler1 = Executors.newScheduledThreadPool(1);
		scheduler1.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (COS.estConnecte()) {
					if (CompteService.compteActuel.getDroit() == Droit.SUPERUTILISATEUR)
						COS.serialiserHost();
					else
						COS.deserialiserHost();
				}
			}
		}, 0, 120, TimeUnit.SECONDS);
		tasks.add(scheduler1);
	}

	public static void preleverNotifTask() {
		LocalDate datePrelevement = LocalDate.now().withDayOfMonth(COS.getJourPrelevement());
		LocalDate now = LocalDate.now();
		if (now.isAfter(datePrelevement))
			datePrelevement = datePrelevement.plusMonths(1);
		tache(datePrelevement, "Prélèvement Mensuel", "Il faut prélever les\nprêts pour la date: " + datePrelevement);
	}

	public static void clotureAnneeNotifTask() {
		LocalDate dateCloture = LocalDate.of(COS.getAnneeSociale().getValue(), COS.getJourCloture().getMonth(), COS.getJourCloture().getDayOfMonth());
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (!(COS.anneesArchivees().contains(COS.getAnneeSociale()) && COS.getBudgetsAnnuels().containsKey(COS.getAnneeSociale())))
					new Notification("Nouvelle année sociale", "Archiver les prêts de \ncette année: " + COS.getAnneeSociale()
							+ "\n réinstaliser le budget de\nl'année suivante.", LocalDateTime.now());
				else {
					COS.notifs.removeAll(COS.notifs.stream().filter(notif -> notif.isClotureNotif()).collect(Collectors.toList()));
				}
			}
		}, 0, 5, TimeUnit.SECONDS);
		tasks.add(scheduler);
	}

	public static void ramadhanNotifTask() {
		HijrahDate dateRamdhan = HijrahDate.now().with(ChronoField.DAY_OF_MONTH, 1).with(ChronoField.MONTH_OF_YEAR, 9);
		HijrahDate now = HijrahDate.now();
		if (now.isAfter(dateRamdhan))
			dateRamdhan = dateRamdhan.plus(1, ChronoUnit.YEARS);
		tache(dateRamdhan, "Mois de Ramadhan", "Reporter tous les prêts\npour ce mois: " + dateRamdhan);
	}

	/** Les emails **/
	public static void envoyerMessage(final String userName, final String password, String toAddress, String subject,
			String message) throws AddressException, MessagingException {
		final String SOCKET_FACTORY = "javax.net.ssl.SSLSocketFactory";
		Properties properties = new Properties();
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.user", userName);
		properties.put("mail.smtp.debug", "false");
		properties.put("mail.smtp.socketFactory.port", "465");
		properties.put("mail.smtp.socketFactory.class", SOCKET_FACTORY);
		properties.put("mail.smtp.socketFactory.fallback", "false");

		Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, password);
			}
		});

		Message msg = new MimeMessage(session);

		msg.setFrom(new InternetAddress(userName));
		InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
		msg.setRecipients(Message.RecipientType.TO, toAddresses);
		msg.setSubject(subject);
		msg.setSentDate(new Date());
		msg.setText(message);

		Transport t = session.getTransport("smtp");
		t.connect(userName, password);
		t.sendMessage(msg, msg.getAllRecipients());
		t.close();
		(new Notification("Succés", "Message a été envoyé avec succés", LocalDateTime.now(), true))
				.ajouterTrayNotif(NotificationType.INFORMATION);
	}

	public static void envoyerMessage(final String userName, final String password, String toAddress, String subject,
			String message, File file, String fileName) throws AddressException, MessagingException {
		final String SOCKET_FACTORY = "javax.net.ssl.SSLSocketFactory";
		Properties properties = new Properties();
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.user", userName);
		properties.put("mail.smtp.debug", "false");
		properties.put("mail.smtp.socketFactory.port", "465");
		properties.put("mail.smtp.socketFactory.class", SOCKET_FACTORY);
		properties.put("mail.smtp.socketFactory.fallback", "false");

		Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, password);
			}
		});

		Message msg = new MimeMessage(session);

		msg.setFrom(new InternetAddress(userName));
		InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
		msg.setRecipients(Message.RecipientType.TO, toAddresses);
		msg.setSubject(subject);
		msg.setSentDate(new Date());
		msg.setText(message);

		MimeBodyPart messageBodyPart = new MimeBodyPart();

		Multipart multipart = new MimeMultipart();

		messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(file);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(fileName);
		multipart.addBodyPart(messageBodyPart);

		msg.setContent(multipart);

		Transport t = session.getTransport("smtp");
		t.connect(userName, password);
		t.sendMessage(msg, msg.getAllRecipients());
		t.close();

		(new Notification("Succés", "Message a été envoyé avec succés", LocalDateTime.now(), true))
				.ajouterTrayNotif(NotificationType.INFORMATION);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (exception ? 1231 : 1237);
		result = prime * result + ((titre == null) ? 0 : titre.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Notification other = (Notification) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (exception != other.exception)
			return false;
		if (titre == null) {
			if (other.titre != null)
				return false;
		} else if (!titre.equals(other.titre))
			return false;
		return true;
	}

	public void ajouterTrayNotif(NotificationType type) {
		TrayNotification tray = new TrayNotification(titre, description, type);
		tray.setRectangleFill(Paint.valueOf("#ffc107"));
		try {
			switch (type) {
			case ERROR:
				tray.setImage(new Image(this.getClass().getResourceAsStream("/sample/sources/error.png")));
			default:
				tray.setImage(new Image(this.getClass().getResourceAsStream("/sample/sources/info.png")));
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		tray.setAnimationType(AnimationType.POPUP);
		tray.showAndWait();
	}
	
	public boolean isClotureNotif()  {
		return (titre.equalsIgnoreCase("Nouvelle année sociale"));
	}

	@Override
	public String toString() {
		return "Notification [titre=" + titre + ", description=" + description + ", date=" + date + ", exception="
				+ exception + "]";
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public boolean isException() {
		return exception;
	}

	public void setException(boolean exception) {
		this.exception = exception;
	}

	@Override
	public int compareTo(Notification notif) {
		if (titre.equals(notif.titre) && description.equals(notif.description) && !exception)
			return 0; // les mêmes tâches
		else
			return (notif.date).compareTo(date);
	}

	public static List<ScheduledExecutorService> getTasks() {
		return tasks;
	}

	public static void shutdownTasks() {
		for (ScheduledExecutorService task : tasks) {
			task.shutdown();
		}
	}

}
