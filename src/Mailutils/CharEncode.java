
package Mailutils;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author Roumpoutsos Nikolaos - Sapountzis Ioannis
 */
public class CharEncode {

    public String encode(String message,boolean LineBreak) {//LineBreak=true when 75 chars limit is required
        String EncodedMap="UTF-8";
        byte[] bytes = null;
        
        if (message.isEmpty()) return "";
        
        try {
            
            bytes = message.getBytes(EncodedMap);//Charset 
           
        } catch (UnsupportedEncodingException ex) {
            return "";
        }

        String encodedMessage = "";//store the encoded message

        int total_chars = 0; 

        for (int i = 0; i < bytes.length; i++) {
            
            if (total_chars >= 72 && LineBreak) {//limit reached (less than 75 to be sure) create new line,not for Subject to the begging
                encodedMessage = encodedMessage.concat("=\n");
                total_chars = 0;
            }
            
            if (bytes[i] == 13 ) {//it is a CR
                encodedMessage = encodedMessage.concat("\n");
                total_chars = 0;
                continue;
            } else if (bytes[i] == 10) {//it is a LF, CR was previously
                continue;
            }

            encodedMessage = encodedMessage.concat("=" + Integer.toHexString(bytes[i] & 255).toUpperCase());
            System.out.println("en:"+encodedMessage);
            total_chars += 3;//+3 for the encoded char

            

        }

        return encodedMessage;

    }
}
