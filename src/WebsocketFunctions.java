import jdk.jfr.Unsigned;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class WebsocketFunctions {

    static final int unsignedShortMax = 65535;

    public String generateResponseKey(String requestKey) throws NoSuchAlgorithmException {
        // my map ends up with a preceding " " space before values, so trim that out
        requestKey = requestKey.trim();
        String provided = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        requestKey += provided;
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        byte[] hashed = md.digest( requestKey.getBytes() );
        String result = Base64.getEncoder().encodeToString( hashed );
        return result;
    }

    public synchronized void checkForUpdate(Client client) throws IOException {
        while (true) {
            // check if opcode is text, or close
            // break while loop if closed (opcode 8)
            if (checkOpCode(client)){
                client.cs.close();
                break;
            }
            // call function to check for message and decode
            char[] decoded = decodeMessage(client);
            String decodedString = new String(decoded);
            System.out.println("decoded string: " + decodedString);
            String[] splitDecoded = decodedString.split(" ", 2);
            // user can equal 'user', or "join" in the case of "join"+"roomName" message
            String user = splitDecoded[0];
            String message = null;
            // mesage can equal actual message, or room name in case of join room
            message = splitDecoded[1];
            if (user.equals("join")){
                if (Room.checkIfRoomExits(message)) {
                    // join the room from the list of rooms
                    Room.joinExistingRoom(client, message);
                } else { // doesnt exist, so create room
                    Room theRoom = new Room(message, client);
                    client.myRoom_ = theRoom; // add the room to the client
                }
            } else {    //else if message format is not "join" "roomName" send message to all room members
                client.myRoom_.sendMessageToRoomClients(decodedString);
            }
        }
    }

    public boolean checkOpCode(Client client) throws IOException {
        byte b1 = (byte) client.dataInputStream.readUnsignedByte();
        int opcode = Byte.toUnsignedInt(b1);
        ////////////////////////////
        // why does this return -1???
        ///////////////////////////////
        System.out.println("the opcode value before shift: " + opcode);
        byte tmp = (byte)(opcode >>> 7);
        System.out.println("After shift 7: " + Byte.toUnsignedInt(tmp));

        boolean fin = (opcode & 0x80) != 0;
        System.out.println("bool: " + fin);
        ///////////////////////////////
        opcode = (byte)(opcode & 0x0F);
        if (opcode == (byte)8){ // if close opcode
            return true;
        } else if (opcode == 1){
            return false;
        } else {
            //System.out.println("Error, unexpected Opcode, not 1 or 8...\n");
            return false; // winging it with unexpected opcode, should probably do something else
        }
    }

    public static void sendMessageOut(Client client, String decodedString) throws IOException {
        System.out.println("Starting send dataframe out");
        // if the socket is open
        if (!client.cs.isClosed()) {
            // write first byte (fin, res, opcode)
            client.smartOutputStream_.writeByte(0x81);
            // split the userName and the Message
            String[] userAndMsg = decodedString.split(" ", 2);
            // store the decoded message
            String messageString;
            // format a string to JSON
            System.out.println("Pre JSON format");
            messageString = "{\"user\":\""+ userAndMsg[0] + "\", \"message\":\""+ userAndMsg[1] + "\"}";
            System.out.println("JSON: " + messageString);
            // write the 2nd + bytes (payload length)
            writePayloadLength(messageString, client);
            // create a array of bytes to store each char of message (as a byte)
            byte[] message;
            // iterate through string, convert to bytes
            message = messageString.getBytes();
            //System.out.println("Starting to send message of length: " + messageString.length());
            int count = 0;
            System.out.println("message out length: " + messageString.length());
            for (int i = 0; i < message.length; i++) {
                client.smartOutputStream_.writeByte(message[i]);
                count++;
            }
            System.out.println("finished writing bytes count: " + count);
            // System.out.println("Finished writing message. Char bytes sent: " + count + "\n");
            // message finished so flush, as long as socket is not closed (socket might still exist with client in room list...
                client.smartOutputStream_.flush();
                System.out.println(".flush(), finished sending dataframe out");
                System.out.println("______________________________\n");
        }
    }

    private static void writePayloadLength(String messageString, Client client) throws IOException {
        // System.out.println("Starting sending payload length.");
        System.out.println("inside writePayloadlength, length is : " + messageString.length());
        if (messageString.length() < 126){ // if payload length rep in one byte
            client.smartOutputStream_.writeByte(messageString.length());
        } else if (messageString.length() <= unsignedShortMax){ // if payload length is > 125 && < unsignedShort.maxvalue
            // extendedPayload
            // System.out.println("Greater than 125, change to short for payload. Write first payload byte 126");
            byte extPL = (byte)126; // set to 126 for extended to short
            client.smartOutputStream_.writeByte(extPL); // setting first payload byte for extended to short
            client.smartOutputStream_.writeShort(messageString.length());
        } else { // payload length is greater than short.MAX_VALUE
            // System.out.println("Greater than short.maxvalue, change to long for payload. Write first payload byte 127");
            byte extPL = 127; // first byte is 127 need to write a long
            client.smartOutputStream_.writeByte(extPL); // setting first payload byte for extended to long
            client.smartOutputStream_.writeLong(messageString.length());
        }
        // System.out.println("Finished writing payload length : " + messageString.length() + "\n");
    }

    public char[] decodeMessage(Client client) throws IOException {
        // first byte (opcode) has already been check
        // this is maskBit and payload Length
        byte next = client.dataInputStream.readByte();
        // get rid of mask bit
        next = (byte)(next & 0x7F);
        int payloadLength = next;
        // if its an extended payload
        System.out.println("First payload byte is : " + payloadLength);
        if (payloadLength == 127){
            // System.out.println("first payload byte is 127");
            payloadLength = (int)(client.dataInputStream.readLong());
        }
        else if (payloadLength == 126){
            payloadLength = client.dataInputStream.readUnsignedShort(); // read next two bytes for extended payload
        }
        byte[] maskKey = client.dataInputStream.readNBytes(4); // read the mask key
        byte[] encoded = client.dataInputStream.readNBytes(payloadLength); // read the message
        char[] decoded = new char[payloadLength]; // store message as char array i guess
        System.out.println("Decoding message of length: " + payloadLength);
        int count = 0;
        for (int i = 0; i < payloadLength; i++) {
            char tmp;
            tmp = (char)(encoded[i] ^ maskKey[i % 4]);
            decoded[i]=tmp;
            count ++;
        }
        System.out.println("dataInputStream.available(), leftover bytes: " + client.dataInputStream.available());
        System.out.println("Finished decoding. Counted char bytes: " + count + " \n");
        //////////////////////////////
        /// Chrome doesnt like two messages longer than a short, so heres a fix for that, just deletes the extra payload
        //////////////////////////////
        int availableBytes = client.dataInputStream.available();
        if (availableBytes > 0){
            System.out.println("Chrome fix, skipping leftovers bytes of message. Skipping bytes: " + availableBytes);
            client.dataInputStream.readNBytes(availableBytes);
        }
        ////////////////////////////

        return decoded;
    }
}
