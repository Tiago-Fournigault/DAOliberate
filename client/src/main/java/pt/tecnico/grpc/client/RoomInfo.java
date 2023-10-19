package pt.tecnico.grpc.client;

import pt.tecnico.grpc.security.*;
import java.io.Serializable;

/**
 * The RoomInfo class is used to store the pseudonym and password
 * used by the user for a room.
 */
public class RoomInfo implements Serializable {

    private int id;
    private String name;
    private String pseudonym;
    private String secret;

    public RoomInfo(int id, String name, String pseudonym, String secret) {
        this.id = id;
        this.name = name;
        this.pseudonym = pseudonym;
        this.secret = secret;
    }

    public int getId() {
        return this.id;
    }

    public String getPseudonym() {
        return this.pseudonym;
    }

    public String getSecret() {
        return this.secret;
    }

    public String toString() {
        return "" + this.name + " (room id: " + this.id + ")";
    }
}