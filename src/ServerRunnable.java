import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;

public class ServerRunnable implements Runnable{

    Client client_;
    boolean websocket_ = false;

    ServerRunnable(Client client){
        client_ = client;
    }

    @Override
    public void run() {
        // HTTPRequest request = new HTTPRequest();
        try{
            // getRequest array will typically look like this ["GET", "/", "HTTP/1.1"]
            String[] getRequest = HTTPRequest.getRequest(client_);
            String requestedFile = getRequest[1];
            String fileName = getRequest[1];

            // create the key value pair with map by parsing get request
            Map<String, String> getRequestMap = HTTPRequest.parseRequestData(requestedFile, client_);

            // System.out.println("getRequest: " + getRequest[0] + " " + getRequest[1] + " " + getRequest[2]);
            String clientWSKey = getRequestMap.get("Sec-WebSocket-Key");
            String upgradeValue = getRequestMap.get("Upgrade");

            // create response header and read in and send file with corresponding response
            if (upgradeValue == null || !upgradeValue.equals("websocket")) {
                HTTPResponse.sendHeaderAndFileData(client_, fileName);
            } else if (upgradeValue.equals("websocket")) {
                clientWSKey.trim();
                HTTPResponse.sendWebsocketResponse(client_, clientWSKey);
                websocket_ = true;
                WebsocketFunctions wsFunctions = new WebsocketFunctions();
                wsFunctions.checkForUpdate(client_);
                System.out.println("responding to ws upgrade request");
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

