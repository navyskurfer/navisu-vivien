package bzh.terrevirtuelle.navisu.instruments.gpstrack.polygon.impl;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class GoogleMail {
	
	static Properties mailServerProperties;
	static Session getMailSession;
	static MimeMessage generateMailMessage;
	
//	public static void main(String args[]) throws AddressException, MessagingException {
//
//	}

	public static void Send(String title, String line1, String line2, String line3, String line4, String line5) throws AddressException, MessagingException {

			 
			// Step1
			//System.out.println("\n 1st ===> setup Mail Server Properties..");
			mailServerProperties = System.getProperties();
			mailServerProperties.put("mail.smtp.port", "587");
			mailServerProperties.put("mail.smtp.auth", "true");
			mailServerProperties.put("mail.smtp.starttls.enable", "true");
			mailServerProperties.put("mail.smtp.ssl.trust", "smtp.gmail.com");


			//System.out.println("Mail Server Properties have been setup successfully..");
	 
			// Step2
			//System.out.println("\n\n 2nd ===> get Mail Session..");
			getMailSession = Session.getDefaultInstance(mailServerProperties, null);
			generateMailMessage = new MimeMessage(getMailSession);
			generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress("navyskurfer@gmail.com"));
			//generateMailMessage.addRecipient(Message.RecipientType.CC, new InternetAddress("test2@crunchify.com"));
			generateMailMessage.setSubject(title);
			String emailBody = line1 + "<br>" + line2 + "<br>" + line3 + "<br>" + line4 + "<br>" + line5;
			generateMailMessage.setContent(emailBody, "text/html");
			//System.out.println("Mail Session has been created successfully..");
	 
			// Step3
			//System.out.println("\n\n 3rd ===> Get Session and Send mail");
			Transport transport = getMailSession.getTransport("smtp");
	 
			// Enter your correct gmail UserID and Password
			// if you have 2FA enabled then provide App Specific Password
			transport.connect("smtp.gmail.com", "navisuvivien@gmail.com", "navisu56520");
			transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
			transport.close();
			
			//System.out.println("Email sent !");
		}

}