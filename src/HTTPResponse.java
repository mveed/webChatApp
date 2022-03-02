import java.io.*;
import java.net.http.WebSocket;
import java.security.NoSuchAlgorithmException;

public class HTTPResponse {

    HTTPResponse(){
    }

    public static void sendHeaderAndFileData(Client client, String fileName) throws IOException {
        // make sure called getRequest[1] (fileName) was not null
        if (!fileName.equals("")) {
            // if get request is for default "/", update to index.html
            if (fileName.equals("/")) {
                fileName = "chat.html";
            }
            // create new File to use exists() method to make sure file actually exists
            // else throw a 404 error
            File file = new File("resources/" + fileName);
            // check if file exists, and call method to create a header for response
            if (file.exists()) {
                // System.out.println("File found, creating response header");
                createHeader(client, "200", fileName);
            } else {
                System.out.println("File not found, creating response header");
                createHeader(client, "404", fileName);
                // set file to load appropriate html file for response
                fileName = "404.html";
            }
            client.out.flush();
            // read in file to client printWriter
            transferFile(fileName, client);
            // send header and file
            client.out.close();
            // System.out.println("done sending info to client");
        }

    }

    private static void transferFile(String fileName, Client client) throws IOException {
        File file = new File("resources/" + fileName);
        FileInputStream in = new FileInputStream(file);
        // transfer the whole file over
        in.transferTo(client.cs.getOutputStream());
        client.cs.getOutputStream().flush();
    }

    private static void createHeader(Client client, String code, String fileName) {
        if (code.equals("200")) {
            client.out.println("HTTP/1.1 " + code + " OK");
            client.out.print("Content-Type: ");
            // check fileName extension to generate content-type
            if (fileName.endsWith(".html")){
                client.out.print("text/html;\n");
            } else if (fileName.endsWith(".css")){
                client.out.print("text/css; \n");
            } else if (fileName.endsWith(".js")){
                client.out.print("text/javascript; \n");
            } else if (fileName.endsWith(".png")){
                client.out.print("img/png; \n");
            }
            File file = new File("resources/" + fileName);
            client.out.println("Content-length: " + file.length());
            client.out.println("");
        } else if (code.equals("404")){
            client.out.println("HTTP/1.1 " + code + "Not Found");
        }
    }

    public static void sendWebsocketResponse(Client client, String clientWSKey) throws NoSuchAlgorithmException, IOException {
        client.out.print("HTTP/1.1 101 Switching Protocols\r\n");
        client.out.print("Upgrade: websocket\r\n");
        client.out.print("Connection: Upgrade\r\n");
        // maybe make static **
        WebsocketFunctions wsFunctions = new WebsocketFunctions();
        String secKey = wsFunctions.generateResponseKey(clientWSKey);
        client.out.print("Sec-WebSocket-Accept: " + secKey +  "\r\n");
        // end header with blank line
        client.out.print("\r\n");
        client.out.flush();
    }
}
