import java.io.IOException;
import java.util.ArrayList;

public class Room {
    // a list of clients for each room
    ArrayList<Client> clients_ = new ArrayList<>();
    ArrayList<String> storedMessages_ = new ArrayList<>();
    String name_;
    // static list of rooms to check before creating new room
    static ArrayList<Room> rooms = new ArrayList<>();

    Room(String name, Client client){
        name_ = name;
        rooms.add(this);
        clients_.add(client);
    }
    public synchronized static boolean checkIfRoomExits(String name){
        // if the room list is empty, create a room
        if (rooms == null){
            System.out.println("roomList empty, create first room");
            return false;
        }
        for (int i = 0; i < rooms.size(); i++){
            if (rooms.get(i).name_.equals(name)){
                return true;
            }
        }
        return false;
    }

    public synchronized static void joinExistingRoom(Client client, String name) throws IOException {
        // iterate through rooms list, add client to room list when matching room found
        for (int i = 0; i < Room.rooms.size(); i++){
            // check if the name == join 'roomName'
            if (Room.rooms.get(i).name_.equals(name)){
                // when found, add current client with socket to the rooms clients list
                Room.rooms.get(i).clients_.add(client);
                // add the room to the client
                client.myRoom_ = Room.rooms.get(i);
                //////////////////////////////////////////////////
                // send all the message history to the new user that joined
                //////////////////////////////////////////////////
                Room currentRoom = client.myRoom_;
                System.out.println("____________");
                for (int j = 0; j < currentRoom.storedMessages_.size(); j++){
                    WebsocketFunctions.sendMessageOut(client, currentRoom.storedMessages_.get(j));
                }
            }
        }
    }


    public synchronized void sendMessageToRoomClients(String decodedString) throws IOException {
        for (int i = 0; i < clients_.size(); i++){
            WebsocketFunctions.sendMessageOut(clients_.get(i), decodedString);
        }
        storedMessages_.add(decodedString);
    }
}
