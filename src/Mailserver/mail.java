package Mailserver;

import java.util.regex.Pattern;

/**
 *
 * @author Roumpoutsos Nikolaos - Sapountzis Ioannis
 */
public class mail {

    String from;
    String to;
    String smtpserver;
    String subject;
    String message;
    String status;
    int seconds;
    String Submitted;
    String ToBeSent;
    long SentTime;
    int id;

    public void mail(mail mymail) {
        this.from = mymail.getFrom();
        this.to = mymail.getTo();
        this.smtpserver = mymail.getSmtpserver();
        this.subject = mymail.getSubject();
        this.message = mymail.getMessage();
        this.status = mymail.getStatus();
        this.seconds = mymail.getSeconds();
        this.Submitted = mymail.getSubmitted();
        this.ToBeSent = mymail.getToBeSent();

        this.id = mymail.getId();

        this.SentTime = mymail.getSentTime();
    }

    public String getToBeSent() {
        return ToBeSent;
    }

    public void setToBeSent(String ToBeSent) {
        this.ToBeSent = ToBeSent;
    }

    public String getSubmitted() {
        return Submitted;
    }

    public void setSubmitted(String Submitted) {
        this.Submitted = Submitted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getSentTime() {
        return SentTime;
    }

    public void setSentTime(long SentTime) {
        this.SentTime = SentTime;
    }

    public mail(String from, String to, String smtpserver, String message, int seconds, String subject) {
        this.from = from;
        this.to = to;
        this.smtpserver = smtpserver;
        this.message = message;
        this.seconds = seconds;
        this.subject = subject;
    }

    public mail() {
        from = "";
        to = "";
        smtpserver = "";
        subject = "";
        message = "";
        seconds = 0;
        status = "";
        id = 0;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public String getSmtpserver() {
        return smtpserver;
    }

    public void setSmtpserver(String smtpserver) {
        this.smtpserver = smtpserver;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void print_mail() {
        System.out.println(getTo());
        System.out.println(getFrom());
        System.out.println(getSubject());
        System.out.println(getSmtpserver());
        System.out.println(getMessage());
    }

    public boolean checkMail() {
        Pattern p = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

        if (from.isEmpty() || to.isEmpty()) {
            return false;
        }

        if (!p.matcher(from).matches() || !p.matcher(to).matches()) {

            return false;
        }

        return true;
    }

    public boolean isEmptySMTP() {
        return smtpserver.isEmpty();
    }

    void setStatus(String Status) {
        status = Status;
    }

    String getStatus() {
        return status;
    }
}
