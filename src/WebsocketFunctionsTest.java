import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

class WebsocketFunctionsTest {

    @Test
    void runMethods(){

    }

    @Test
    void generateResponseKey() throws NoSuchAlgorithmException {
        String testKey = "dGhlIHNhbXBsZSBub25jZQ==";
        WebsocketFunctions testWSF = new WebsocketFunctions();
        String newKey = testWSF.generateResponseKey(testKey);
        Assertions.assertEquals(true, newKey.equals("s3pPLMBiTxaQ9kYGzzhZRbK+xOo="));
    }

    @Test
    void checkForUpdate() {
    }

    @Test
    void sendMessageOut() {
    }

    @Test
    void checkOpCode() {
    }

    @Test
    void decodeMessage() {

    }
}