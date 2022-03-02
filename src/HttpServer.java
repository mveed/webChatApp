import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class HttpServer {

    public ServerSocket ss;
    ArrayList<Room> rooms = new ArrayList<>();

    public HttpServer() throws IOException {
        ss = new ServerSocket(8080);
    }

    public void getClient(HttpServer ls) throws IOException {
        try {
            Client client = new Client(ls.ss.accept());
            ServerRunnable serverRunnable = new ServerRunnable(client);
            Thread thread = new Thread(serverRunnable);
            thread.start();
        }
        catch (IOException e) {
            System.out.println("IOException");
        }
    }

    public static void main(String[] args) throws IOException {
        // ls localserver
        HttpServer ls = new HttpServer();
        boolean serverRunning = true;
        // just keep it running, currently no quit conditions
        while (serverRunning) {
            try {
                ls.getClient(ls);
            } catch (IOException e){
                System.out.println("IOException.");
            }
        }
    }
}