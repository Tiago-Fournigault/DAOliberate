package pt.tecnico.grpc.register;

/**
 * The Room class is responsible for storing information of a Room.
 */
public class Room {

    private int room_id;
    private String room_name;
    private String initial_secret;

    public Room(int room_id, String room_name) {
        this.room_id = room_id;
        this.room_name = room_name;
    }

    public Room(int room_id, String room_name, String initial_secret) {
        this.room_id = room_id;
        this.room_name = room_name;
        this.initial_secret = initial_secret;
    }

    public int getId() {
        return this.room_id;
    }

    public String getName() {
        return this.room_name;
    }

    public String getInitialSecret() {
        return this.initial_secret;
    }
}