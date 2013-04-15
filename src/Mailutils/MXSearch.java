package Mailutils;

import Mailserver.mail;
import java.util.Arrays;
import java.util.Comparator;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.NamingException;

/**
 *
 * @author Roumpoutsos Nikolaos - Sapountzis Ioannis
 */
public class MXSearch {

    public static String[] getMXAddress(mail mymail) throws NamingException {
        String domainName = mymail.getTo().substring(mymail.getTo().indexOf("@") + 1, mymail.getTo().length());


        // get the default initial Directory Context
        InitialDirContext iDirC = new InitialDirContext();
        // get the MX records from the default DNS
        Attributes attributes = iDirC.getAttributes("dns:/" + domainName, new String[]{"MX"}); //NamingException is thrown if no DNS record found
        // attributeMX is a 'list' of the Mail Exchange(MX) Records
        Attribute attributeMX = attributes.get("MX");

        // if there are no MX RRs then default to domainName 
        if (attributeMX == null) {
            return (new String[]{domainName});
        }

        // split MX RRs into Preference Values(records[0]) and Host Names(records[1])
        String[][] records = new String[attributeMX.size()][2];
        for (int i = 0; i < attributeMX.size(); i++) {
            records[i] = ("" + attributeMX.get(i)).split("\\s+");
        }

        // sort the MX RRs by RR value (lower is preferred) //can be omitted
        Arrays.sort(records, new Comparator<String[]>() {

            public int compare(String[] rec1, String[] rec2) {
                return (Integer.parseInt(rec1[0]) - Integer.parseInt(rec2[0]));
            }
        });

        // put sorted host names in an array, get rid of any trailing '.' 
        String[] sortedHostNames = new String[records.length];
        for (int i = 0; i < records.length; i++) {
            sortedHostNames[i] = records[i][1].endsWith(".") ? //remove [1]
                    records[i][1].substring(0, records[i][1].length() - 1) : records[i][1];
        }
        return sortedHostNames;


    }
}
