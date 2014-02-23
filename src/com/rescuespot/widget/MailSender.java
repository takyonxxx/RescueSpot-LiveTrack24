package com.rescuespot.widget;
import org.apache.http.auth.AuthenticationException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.Provider;
import java.security.Security;
import java.util.Properties;

public class MailSender extends javax.mail.Authenticator {
    private String mailhost = "smtp.gmail.com";
    private String user;
    private String password;
    private Session session;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public MailSender(String user, String password) {
        this.user = user;
        this.password = password;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");
        session = Session.getInstance(props,this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body, String recipients)
            throws MessagingException, IOException, AuthenticationException {
        sendMail(subject, body, recipients, null);
    }	
    public synchronized void sendMail(String subject, String body, String recipients,
                                      File... files) throws MessagingException, IOException {
        try {
    	  
				
         MessageBuilder builder = new MessageBuilder(session);
         builder.setSender(user).setSubject(subject).setBody(body)
                .setFiles(files).setRecipients(recipients);
         Transport.send(builder.build());        
         Store st=session.getStore("imaps");
         st.connect("smtp.gmail.com",user, password);         
             
	       Folder inbox=st.getFolder("[Gmail]/Sent Mail");
	       inbox.open(Folder.READ_WRITE);
	       Message m[]=inbox.getMessages();
	       for(int i=0;i< m.length;i++)
           {
	        m[i].setFlag(Flags.Flag.DELETED, true);   
           }
         
        } catch (MessagingException e) {
            String error;
            if((error = e.getMessage()) != null) {
                error = error.trim();
                if(error.contains("Unknown SMTP")) {
                    error = "No Internet";
                    throw new IOException(error);
                }
            }
            throw e;
        }
    }
   
    public static final class JSSEProvider extends Provider {

        public JSSEProvider() {
            super("HarmonyJSSE", 1.0, "Harmony JSSE Provider");
            AccessController.doPrivileged(new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    put("SSLContext.TLS",
                            "org.apache.harmony.xnet.provider.jsse.SSLContextImpl");
                    put("Alg.Alias.SSLContext.TLSv1", "TLS");
                    put("KeyManagerFactory.X509",
                            "org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl");
                    put("TrustManagerFactory.X509",
                            "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl");
                    return null;
                }
            });
        }
    }
}
