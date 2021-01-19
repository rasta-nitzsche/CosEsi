package sample.noyau.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class FTPUtil {
	private static String server;
	private static Integer port;
	private static String user;
	private static String pass;
	private static FTPClient ftpClient = new FTPClient();

	public static void initialize(String server, Integer port, String user, String pass) throws Exception {
		FTPUtil.server = server;
		FTPUtil.port = port;
		FTPUtil.user = user;
		FTPUtil.pass = pass;
		ftpClient = new FTPClient();
		ftpClient.connect(server, port);
		ftpClient.login(user, pass);
		int replyCode = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(replyCode))
			throw new Exception();
	}

	public static void initialize() throws Exception {
		ftpClient = new FTPClient();
		ftpClient.connect(server, port);
		ftpClient.login(user, pass);
		int replyCode = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(replyCode))
			throw new Exception();
	}

	public static void upload(File file, String path) {
		ftpClient = new FTPClient();
		try {
			ftpClient.connect(server, port);
			ftpClient.login(user, pass);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileTransferMode(FTP.BLOCK_TRANSFER_MODE);
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			InputStream inputStream = new FileInputStream(file);
			System.out.println("upload: " + file.getName());
			boolean done = ftpClient.storeFile(path, inputStream);
			inputStream.close();
			if (done) {
				System.out.println(file.getName() + " uploaded successfully. (" + LocalDateTime.now() + ")");
			} else {
				new Notification("Erreur de synchronisation", "Erreur de connexion avec le serveur FTP du COSEsi",
						LocalDateTime.now());
				System.out.println(file.getName() + "upload failed. (" + LocalDateTime.now() + ")");
			}
		} catch (IOException e) {
			new Notification("Erreur de synchronisation", "Erreur de connexion avec le serveur FTP du COSEsi",
					LocalDateTime.now());
		} finally {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.logout();
					ftpClient.disconnect();
				}
			} catch (IOException ex) {
				new Notification("Erreur de synchronisation", "Erreur de connexion avec le serveur FTP du COSEsi",
						LocalDateTime.now());
			}
		}
	}

	public static void download(String pathSource, String pathDestination) {
		File file = new File(pathDestination);
		try {
			ftpClient.connect(server, port);
			ftpClient.login(user, pass);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileTransferMode(FTP.BLOCK_TRANSFER_MODE);
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(file));
			System.out.println("download: " + file.getName());
			boolean success = ftpClient.retrieveFile(pathSource, outputStream1);
			outputStream1.close();
			if (success) {
				System.out.println(file.getName() + " downloaded successfully. (" + LocalDateTime.now() + ")");
			} else {
				System.out.println(file.getName() + " download failed. (" + LocalDateTime.now() + ")");
				new Notification("Erreur de synchronisation", "Erreur de connexion avec le serveur FTP du COSEsi",
						LocalDateTime.now());
			}
		} catch (IOException e) {
			new Notification("Erreur de synchronisation", "Erreur de connexion avec le serveur FTP du COSEsi",
					LocalDateTime.now());

		} finally {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.logout();
					ftpClient.disconnect();
				}
			} catch (IOException ex) {
				new Notification("Erreur de synchronisation", "Erreur de connexion avec le serveur FTP du COSEsi",
						LocalDateTime.now());
			}
		}
	}

	public static boolean checkFileExists(String filePath) throws IOException {
		ftpClient = new FTPClient();
		ftpClient.connect(server, port);
		ftpClient.login(user, pass);
		InputStream inputStream = ftpClient.retrieveFileStream(filePath);
		int returnCode = ftpClient.getReplyCode();
		if (inputStream == null || returnCode == 550) {
			return false;
		}
		return true;
	}

	public static String getServer() {
		return server;
	}

	public static void setServer(String server) {
		FTPUtil.server = server;
	}

	public static int getPort() {
		return port;
	}

	public static void setPort(int port) {
		FTPUtil.port = port;
	}

	public static String getUser() {
		return user;
	}

	public static void setUser(String user) {
		FTPUtil.user = user;
	}

	public static String getPass() {
		return pass;
	}

	public static void setPass(String pass) {
		FTPUtil.pass = pass;
	}

	public static Boolean estConnecte() {
		return (server != null);
	}

	public static void destroySettings() {
		server = null;
		port = null;
		user = null;
		pass = null;
		ftpClient = new FTPClient();
	}

}
