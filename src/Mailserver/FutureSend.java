
package Mailserver;

import java.util.TimerTask;
import Mailutils.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 *
 * @author Roumpoutsos Nikolaos - Sapountzis Ioannis
 */
public class FutureSend extends TimerTask {

    mail mymail;
    String NOTIFIER = "nikosr@mail.ik2213.lab";
    MailHandler mailhandler;
    String EncodedMap="UTF-8";
    
    public FutureSend(mail mymail,MailHandler mailhandler) {
        this.mymail = mymail;
        this.mailhandler=mailhandler;
    }

    @Override
    public void run() {
        boolean error = false;
        if (mymail.isEmptySMTP()) {
            try {
                System.out.println("Empty smtp server, try to MXsearch");
                System.out.println("Found:");

                for (String mailHost : MXSearch.getMXAddress(mymail)) {
                    System.out.println(mailHost);
                }
                //pick the first one
                mymail.setSmtpserver(MXSearch.getMXAddress(mymail)[0]);
            } catch (NamingException ex) {
                Logger.getLogger(FutureSend.class.getName()).log(Level.SEVERE, null, ex);
                error = true;
            }
        }
        ////////////////////////////
        Socket smtpSocket = null;
        DataOutputStream os = null;
        BufferedReader is = null;
        int ServerPort = 25;
        int TIMEOUT = 4000;


        try {
            //create the date
            Calendar currentDate = Calendar.getInstance();
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");
                            String dateNow = formatter.format(currentDate.getTime());

            // Open port to smtp server
            smtpSocket = new Socket(mymail.getSmtpserver(), ServerPort);
            smtpSocket.setSoTimeout(TIMEOUT);
            //Open Streams
            os = new DataOutputStream(smtpSocket.getOutputStream());
            is = new BufferedReader(new InputStreamReader(smtpSocket.getInputStream()));

            if (smtpSocket != null && is != null && os != null) { //Socket is ready for use.
                //sending mail
                System.out.println("Sending email...");
                String response = "";
                try {

                    response = is.readLine();
                    System.out.println(response);
                    System.out.println("HELO " + mymail.getSmtpserver() + "\r\n");
                    os.writeBytes("HELO " + mymail.getSmtpserver() + "\r\n");
                    response = is.readLine();
                    System.out.println(response);
                    if (!response.startsWith("250")) {
                        error = true;
                    }

                    System.out.println("MAIL FROM: <" + mymail.getFrom() + ">\r\n");
                    os.writeBytes("MAIL FROM: <" + mymail.getFrom() + ">\r\n");
                    response = is.readLine();
                    System.out.println(response);
                    if (!response.startsWith("250")) {
                        error = true;
                    }


                    System.out.println("RCPT TO: <" + mymail.getTo() + ">\r\n");
                    os.writeBytes("RCPT TO:<" + mymail.getTo() + ">\r\n");

                    response = is.readLine();
                    System.out.println(response);
                    if (!response.startsWith("250")) {
                        error = true;
                    }

                    System.out.println("DATA\r\n");
                    os.writeBytes("DATA\r\n");

                    response = is.readLine();
                    System.out.println(response);
                    if (!response.startsWith("354")) {
                        error = true;
                    }
                  

                    System.out.println("From: " + mymail.getFrom() + "\r\n");
                    os.writeBytes("From: " + mymail.getFrom() + "\r\n");

                    System.out.println("To:  " + mymail.getTo() + "\r\n");
                    os.writeBytes("To:  " + mymail.getTo() + "\r\n");

                   // System.out.println("Date: " + dateNow + "\r\n");
                  //  os.writeBytes("Date: " + dateNow + "\r\n");

                    System.out.println("Subject: " + mymail.getSubject() + "\r\n");
                   
                    os.writeBytes("Subject: =?"+EncodedMap+"?Q?" + new CharEncode().encode(mymail.getSubject(), false) + "?=\r\n");

                      String mimeHeader = "MIME-Version: 1.1\n"
                            + "Content-Type: text/plain;"
                            + " charset="+EncodedMap+"\n"
                            + "Content-Transfer-Encoding: quoted-printable\r\n\r\n";

                    System.out.println(mimeHeader);
                    os.writeBytes(mimeHeader);
                    
                    System.out.println(new CharEncode().encode(mymail.getMessage(), true) + "\r\n");
                    os.writeBytes(new CharEncode().encode(mymail.getMessage(), true) + "\r\n");


                    System.out.println("\r\n.\r\n");
                    os.writeBytes("\r\n.\r\n");
                    response = is.readLine();
                    System.out.println(response);
                    if (!response.startsWith("250")) {
                        error = true;
                    }

                    System.out.println("QUIT\r\n");
                    os.writeBytes("QUIT\r\n");
                    response = is.readLine();
                    System.out.println(response);
                    if (!response.startsWith("221")) {
                        error = true;
                    }

                    is.close();
                    os.close();

                } catch (Exception e) {
                    System.out.println(e.toString());
                    System.out.println("Cannot send email as an error occurred.");
                    error = true;
                }



            }
            //        return false;
        } catch (IOException ex) {
            Logger.getLogger(MailHandler.class.getName()).log(Level.SEVERE, null, ex);
            error = true;
        }
        try {
            if (!error) {
                //mailhandler.removeMessageFromList(mymail);
                sendNotification(mymail, "Your message was sent succesfully!");
                mymail.setStatus("SENT");
         
            } else {
                sendNotification(mymail, "Sorry, an error occured with your message delivery!");
                mymail.setStatus("ERROR");
            }
        } catch (NamingException ex) {
            Logger.getLogger(FutureSend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(FutureSend.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void sendNotification(mail mymail, String notification) throws NamingException, InterruptedException {
        mail Notmail=new mail();
        String recpt = mymail.getFrom();
        Notmail.setFrom(NOTIFIER);
        Notmail.setTo(recpt);
        Notmail.setMessage(notification+"\r\nSubject:"+mymail.getSubject());
        Notmail.setSubject("E-mail Notification");
        mailhandler.send_mail(Notmail);
    }
}
