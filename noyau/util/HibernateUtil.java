package sample.noyau.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

// A set of methods that help with the configuration of the Hibernate framework

public class HibernateUtil {
	private static String host;
	private static String databaseName;
	private static String port;
	private static Properties settings = new Properties();;
	private static SessionFactory sessionFactory;
	private static Map<Integer, SessionFactory> SessionFactoriesArchive = new HashMap<Integer, SessionFactory>();
	private static List<ServiceRegistry> serviceRegistries = new ArrayList<ServiceRegistry>();

	// Default initialization; Connect to an external database
	public static void initialize() {
		List<Class> annotatedClasses = new ArrayList<Class>();
		annotatedClasses.add(sample.noyau.entity.Demande.class);
		annotatedClasses.add(sample.noyau.entity.Pret.class);
		annotatedClasses.add(sample.noyau.entity.Employe.class);
		annotatedClasses.add(sample.noyau.entity.Report.class);
		annotatedClasses.add(sample.noyau.entity.Remboursement.class);
		annotatedClasses.add(sample.noyau.entity.PretRemboursable.class);
		annotatedClasses.add(sample.noyau.entity.PretSocial.class);
		annotatedClasses.add(sample.noyau.entity.PretElectromenager.class);
		annotatedClasses.add(sample.noyau.entity.Don.class);
		annotatedClasses.add(sample.noyau.entity.Compte.class);
		initialize("remotemysql.com", "3306", "nULoWfxvr4", "nULoWfxvr4", "tiryTnTtk3", 5, annotatedClasses, "update");
	}

	public static void initialize(List<Class> annotatedClasses, String mode) {
		Configuration configuration = new Configuration();
		settings.put(AvailableSettings.URL, "jdbc:mysql://" + host + ":" + port + "/" + databaseName
				+ "?useLegacyDatetimeCode=false&serverTimezone=Africa/Algiers");
		settings.put(AvailableSettings.HBM2DDL_AUTO, mode);
		configuration.setProperties(settings);
		for (Class annotatedClass : annotatedClasses)
			configuration.addAnnotatedClass(annotatedClass);
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties()).build();
		serviceRegistries.add(serviceRegistry);
		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
	}

	public static void initialize(String host, String port, String databaseName, String username, String password,
			int mySQLVersion, List<Class> annotatedClasses, String mode) {
		try {
			HibernateUtil.host = host;
			HibernateUtil.databaseName = databaseName;
			HibernateUtil.port = port;
			Configuration configuration = new Configuration();
			settings.put(AvailableSettings.DRIVER, "com.mysql.jdbc.Driver");
			settings.put(AvailableSettings.URL, "jdbc:mysql://" + host + ":" + port + "/" + databaseName
					+ "?useLegacyDatetimeCode=false&serverTimezone=Africa/Algiers");
			settings.put(AvailableSettings.USER, username);
			settings.put(AvailableSettings.PASS, password);
			settings.put(AvailableSettings.DIALECT,
					"org.hibernate.dialect.MySQL" + Integer.toString(mySQLVersion) + "Dialect");
			settings.put(AvailableSettings.SHOW_SQL, "true");
			settings.put(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread");
			settings.put(AvailableSettings.HBM2DDL_AUTO, mode);
			configuration.setProperties(settings);
			for (Class annotatedClass : annotatedClasses)
				configuration.addAnnotatedClass(annotatedClass);
			ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.applySettings(configuration.getProperties()).build();
			serviceRegistries.add(serviceRegistry);
			sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void initializeArchive(int anneeArchive, List<Class> annotatedClasses) {
		try {
			Configuration configuration = new Configuration();
			settings.put(AvailableSettings.URL, "jdbc:mysql://" + host + ":" + port + "/" + databaseName + "_"
					+ anneeArchive + "?useLegacyDatetimeCode=false&serverTimezone=Africa/Algiers");
			configuration.setProperties(settings);
			for (Class annotatedClass : annotatedClasses)
				configuration.addAnnotatedClass(annotatedClass);
			ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.applySettings(configuration.getProperties()).build();
			serviceRegistries.add(serviceRegistry);
			SessionFactoriesArchive.put(anneeArchive, configuration.buildSessionFactory(serviceRegistry));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static SessionFactory getSessionFactoryArchive(int anneeArchive, List<Class> annotatedClasses) {
		if (SessionFactoriesArchive.get(anneeArchive) == null) {
			initializeArchive(anneeArchive, annotatedClasses);
		}
		return SessionFactoriesArchive.get(anneeArchive);
	}

	public static SessionFactory getSessionFactory() {
		/*
		 * if (sessionFactory == null) { try { initialize(); } catch (Exception e) {
		 * 
		 * } }
		 */
		return sessionFactory;
	}

	public static String getHost() {
		return host;
	}

	public static void setHost(String host) {
		HibernateUtil.host = host;
	}

	public static String getPort() {
		return port;
	}

	public static void setPort(String port) {
		HibernateUtil.port = port;
	}

	public static void setDatabaseName(String databaseName) {
		HibernateUtil.databaseName = databaseName;
	}

	public static void setSettings(Properties settings) {
		HibernateUtil.settings = settings;
	}

	public static String getDatabaseName() {
		return databaseName;
	}

	public static Properties getSettings() {
		return settings;
	}

	public static Boolean estConnecte() {
		if (host == null || host.isEmpty() || host.isBlank())
			return false;
		else
			return true;
	}

	public static void destroySettings() {
		host = null;
		databaseName = null;
		port = null;
		settings = new Properties();
	}

	public static void closeSessionFactories() {
		for (ServiceRegistry serviceRegistry : serviceRegistries) {
			StandardServiceRegistryBuilder.destroy(serviceRegistry);
		}
	}

}
