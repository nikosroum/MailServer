package Mailserver;

import java.net.*;
import java.io.*;
import java.text.*;

import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 *
 * @author Roumpoutsos Nikolaos - Sapountzis Ioannis
 */
public class Handler extends Thread {

    Socket mySocket;
    BufferedReader input;
    PrintWriter output;
    mail mymail;
    MailHandler mailhandler;
    String EncodeMap = "UTF-8";//the encoded charset map

    public Handler(Socket mySocket, MailHandler mailhandler) {
        this.mySocket = mySocket;

        this.mymail = null;
        try {
            input = new BufferedReader(new InputStreamReader(mySocket.getInputStream(), EncodeMap));//ISO-8859-15
            output = new PrintWriter(mySocket.getOutputStream());

        } catch (Exception e) {
        }
        this.mailhandler = mailhandler;
    }
    
//send a file to out
    public void send_file(String filename, PrintWriter out) throws FileNotFoundException, IOException {
        FileReader file = new FileReader(filename);
        BufferedReader in = new BufferedReader(file);
        String line = "";
        while ((line = in.readLine()) != null) {
            out.println(line);
        }
        in.close();
        out.flush();
        out.close();

    }
//create an http header with a specific code

    private String http_header(int return_code) {
        String s = "HTTP/1.1 ";

        switch (return_code) {
            case 200:
                s = s + "200 OK";
                break;
            case 400:
                s = s + "400 Bad Request";
                break;
            case 404:
                s = s + "404 Not Found";
                break;
            case 500:
                s = s + "500 Internal Server Error";
                break;
            case 501:
                s = s + "501 Not Implemented";
                break;
        }

        s = s + "\r\n";
        s = s + "Connection: close\r\n"; //we can't handle persistent connections
        s = s + "Server: MyServer \r\n"; //server name

        s = s + "Content-Type: text/html\r\n"; //the only filetype our server supports

        s = s + "\r\n"; //this marks the end of the httpheader


        return s;
    }

    @Override
    public void run() {
        try {
            int method = 0; //1 get,2 post 0 not supported

            System.out.println("New client connected.");
            System.out.println("Socket:" + mySocket.getRemoteSocketAddress().toString());
            System.out.println("IP:" + mySocket.getInetAddress().getHostAddress());

            //This is the two types of request we can handle
            //GET /index.html HTTP/1.0
            //POST HTTP/1.0

            System.out.println("Connection, sending data.");

            // read the data sent. 

            String tmp = input.readLine(); // read the data sent. 
            while(tmp==null)//wait until client sends something
                tmp = input.readLine();
            String tmp2 = new String(tmp);
            int start = 0;
            tmp.toUpperCase(); //convert it to uppercase
            if (tmp.startsWith("GET")) { //compare if is it GET
                method = 1;
                start = 5; //skip "GET /"
            }
            if (tmp.startsWith("POST")) { //compare if is it GET
                method = 2;
                start = 6; //skip "POST /"
            }
            if (method == 0) { // not supported
                try {
                    output.print(http_header(501));// send a 501 error
                    output.close();
                    return;
                } catch (Exception e3) { //if some error happened catch it
                    System.out.println("error:" + e3.getMessage());
                } //and display error
            }

            if (method == 1) {
                String webpage;
                webpage = tmp2.substring(start, tmp2.lastIndexOf("HTTP") - 1);
                System.out.println("Client request for: " + webpage);

                if (webpage.equals("")) {
                    webpage = "index.html";//default webpage
                }
                if (webpage.equals("status.html")) {
                    output.print(http_header(200));
                    mailhandler.PrintList2HTML(output);
                    output.close();
                    input.close();
                    mySocket.close();
                    return;
                } else {
                    
                    try {
                        if (new File(webpage).isFile()) { //send the file
                            output.print(http_header(200));
                            send_file(webpage, output);
                        } else {// file not found
                            output.print(http_header(404));
                            send_file("404.html", output);
                            input.close();
                            output.close();
                            mySocket.close();
                            return;
                        }
                        input.close();
                        output.close();
                        mySocket.close();
                        return;
                    } catch (IOException ex) {
                        Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }//end of index or status webpage service
            } else { //method=2 POST

                String line;
                int length = 0;
                mymail = new mail();
                Timer timer = new Timer(); //thread for the scheduled mail delivery

                //Parse Header
                while ((line = input.readLine()) != null && !line.isEmpty()) {
                    //first while loop to skip header
                    //  System.out.println(line);
                    if (line.contains("Content-Length:")) {// determine how many bytes we are going to receive
                        length = Integer.parseInt(line.substring(("Content-Length:").length() + 1, line.length()));
                    }
                }

                char[] buff = new char[length];
                input.read(buff, 0, length);
                System.out.println(buff);// for debug
                StringTokenizer st2 = new StringTokenizer(String.valueOf(buff));
                //Parse parameters
                while (st2.hasMoreTokens()) { //iterate through tokens
                    line = (st2.nextToken("\r\n"));
                    if (line.startsWith("sender=")) {
                        mymail.setFrom(line.substring(("sender=").length(), line.length()));

                    }
                    if (line.startsWith("receiver=")) {
                        mymail.setTo(line.substring(("receiver=").length(), line.length()));
                    }
                    if (line.startsWith("smtpServer=")) {
                        mymail.setSmtpserver(line.substring(("smtpServer=").length(), line.length()));
                    }
                    if (line.startsWith("subject=")) {
                        mymail.setSubject(line.substring(("subject=").length(), line.length()));
                    }
                    if (line.startsWith("message=")) {
                        String message = "";
                        line = line.substring(("message=").length(), line.length());
                        while ((st2.hasMoreTokens()) && !line.startsWith("seconds=")) { //read all message lines till the next variable
                            message = message + "\r\n" + line;
                            line = st2.nextToken("\r\n");
                            if (line.isEmpty()) {//\r\n was read
                                message = message + "\r\n";
                            }
                            
                        }
                        mymail.setMessage(message);
                        System.out.println("message: " + mymail.getMessage());
                    }
                    if (line.startsWith("seconds=")) {
                        if (!line.substring(("seconds=").length(), line.length()).isEmpty()) {
                            
                            mymail.setSeconds(Integer.parseInt(line.substring(("seconds=").length(), line.length())));
                        }
                    }
                }

                if (!mymail.checkMail()) {//if mail is not in the right format
                    output.print(http_header(400));
                    send_file("badmail.html", output);
                    input.close();
                    output.close();
                    mySocket.close();
                } else {

                    try {

                        if (mymail.getSeconds() > 0) { //mail will be sent in the future
                            System.out.println("with Delay");
                            mymail.setStatus("PENDING");

                            Calendar currentDate = Calendar.getInstance();
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");
                            String dateNow = formatter.format(currentDate.getTime());
                            currentDate.add(Calendar.SECOND, mymail.getSeconds());	//Adding seconds to current date time
                            String Sentdate = formatter.format(currentDate.getTime());

                            mymail.setSubmitted(dateNow);
                            mymail.setToBeSent(Sentdate);
                            mailhandler.addMessageToList(mymail);
                            //schedule mail sending in defined seconds
                            timer.schedule(new FutureSend(mymail, mailhandler), mymail.getSeconds() * 1000);
                            System.out.println("Message added-to be sent in :" + mymail.getSeconds());

                        } else {
                            try {
                                System.out.println("Sent without delay");
                                if (mailhandler.send_mail(mymail)) {//error while sending
                                    send_file("errormail.html", output);
                                    input.close();
                                    output.close();
                                    mySocket.close();
                                    return;
                                }


                            } catch (InterruptedException ex) {
                                Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } catch (NamingException ex) {
                        Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    send_file("successmail.html", output);
                    input.close();
                    output.close();
                    mySocket.close();
                    return;
                }


            }

        } catch (NumberFormatException nfe) {
            try {
                Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, nfe);
                send_file("errormail.html", output);
                input.close();
                output.close();
                mySocket.close();
                return;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
