package Mailserver;

import Mailutils.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 *
 * @author Roumpoutsos Nikolaos - Sapountzis Ioannis
 */
public class MailHandler extends Thread {

    private String NOTIFIER = "nikosr@mail.ik2213.lab";
    List<mail> FutureMails;
    String EncodedMap = "UTF-8";

    public MailHandler() {
        FutureMails = new ArrayList<mail>();

    }

    public void addMessageToList(mail mymail) {
        if (FutureMails.size() > 0) {
            mymail.setId(FutureMails.size() - 1);
        }
        FutureMails.add(mymail);
        System.out.println("Message added in index" + mymail.getId());
    }

    public void removeMessageFromList(mail mymail) {

        FutureMails.remove(mymail.getId());
        System.out.println("Message removed");
    }

    public List<mail> getFutureMails() {
        return FutureMails;
    }

    public boolean send_mail(mail mymail) throws NamingException, InterruptedException {

        if (mymail.isEmptySMTP()) {
            System.out.println("Empty smtp server, try to MXsearch");
            System.out.println("Found:");

            for (String mailHost : MXSearch.getMXAddress(mymail)) {
                System.out.println(mailHost);
            }
            //pick the first one
            mymail.setSmtpserver(MXSearch.getMXAddress(mymail)[0]);
        }
        ////////////////////////////
        Socket smtpSocket = null;
        DataOutputStream os = null;
        BufferedReader is = null;
        int ServerPort = 25;
        int TIMEOUT = 4000;
        boolean error = false;

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

                    //System.out.println("Date: " + dateNow + "\r\n");
                    //os.writeBytes("Date: " + dateNow + "\r\n");

                    System.out.println("Subject: " + mymail.getSubject() + "\r\n");
                    
                    os.writeBytes("Subject: =?" + EncodedMap + "?Q?" + new CharEncode().encode(mymail.getSubject(), false) + "?=\r\n");

                    String mimeHeader = "MIME-Version: 1.0\r\n"
                            + "Content-Type: text/plain\r\n"
                            + " charset=" + EncodedMap + "\r\n" //default
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
            return error;
        } catch (IOException ex) {
            Logger.getLogger(MailHandler.class.getName()).log(Level.SEVERE, null, ex);
            error = true;
            return error;
        }

    }

    public void sendNotification(mail mymail, String status) throws NamingException, InterruptedException {
        String recpt = mymail.getFrom();
        mail Notmail = new mail();
        Notmail.setFrom(NOTIFIER);
        Notmail.setTo(recpt);
        Notmail.setSubject("E-mail Notification");
        Notmail.setMessage("Notification about:\nSubject=" + mymail.getSubject());
        Notmail.setSmtpserver(mymail.getSmtpserver());
        Notmail.setSeconds(mymail.getSeconds());
        send_mail(Notmail);
        mymail.setStatus(status);

    }

    public void PrintList2HTML(PrintWriter output) {
        String HTML_HEAD = "<html><body><h1>Status Page</h1><table border=\"2\">";
        String HTML_END = "</table></body></html>";

        output.print(HTML_HEAD);
        String tline;
        output.print("<tr><td>Sender</td><td>Receiver</td><td>Subject</td><td>Time submitted</td><td>Time to be sent</td><td>Status</td></tr>");

        for (mail mymail : getFutureMails()) {
            tline = "<tr><td>" + mymail.getFrom() + "</td><td>" + mymail.getTo() + "</td><td>" + mymail.getSubject() + "</td><td>" + mymail.getSubmitted() + "</td><td>" + mymail.getToBeSent() + "</td>" + "<td>" + mymail.getStatus() + "</td>" + "</tr>";
            output.print(tline);
        }

        output.print(HTML_END);
    }
}