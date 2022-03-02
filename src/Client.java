import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public Socket cs;
    public Scanner input;
    public PrintWriter out;
    public DataInputStream dataInputStream;
    public Room myRoom_;
    public DataOutputStream smartOutputStream_;

    public Client(Socket socket) throws IOException {
        cs = socket;
        input = new Scanner(cs.getInputStream());
        out = new PrintWriter(cs.getOutputStream());
        dataInputStream = new DataInputStream(cs.getInputStream());
        smartOutputStream_ = new DataOutputStream(cs.getOutputStream());
    }
}
