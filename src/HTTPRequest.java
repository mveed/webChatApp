import java.util.HashMap;

public class HTTPRequest {

    public static String [] getRequest (Client client){
        String [] getRequest = {"", "", ""};
        if (client.input.hasNext()) {
            getRequest = client.input.nextLine().split(" ");
        }
        return getRequest;
    }

    public static HashMap<String, String> parseRequestData(String requestedFile, Client client){
        HashMap<String, String> getRequestMap = new HashMap<>();
        while (client.input.hasNextLine()) {
            String[] tmp = client.input.nextLine().split(": ", 2);
            if (tmp[0].isEmpty()) {
                break;
            }
            getRequestMap.put(tmp[0], tmp[1]);
            // uncomment below to print out get request header pairs
            // System.out.println(" << " + tmp[0] + ": " + tmp[1]);
            client.out.flush();
        }
        return getRequestMap;

    }
}
