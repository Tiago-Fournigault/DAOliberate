package pt.tecnico.grpc.register;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import pt.tecnico.grpc.security.*;

/**
 * The User class is responsible for storing information of a user.
 */
public class User {

	private String name;
    private String secure_password;
    private byte[] salt;
    private ArrayList<Room> invites;
    private ArrayList<Room> pre_invites;
    private ArrayList<Room> initial_rooms;
    private ArrayList<Integer> registered_room_ids;

    public User(String name, String password) {
        this.name = name;
        this.salt = LibSecurity.getSalt();
        this.secure_password = LibSecurity.getSecurePassword(password, this.salt);
        this.invites = new ArrayList<Room>();
        this.pre_invites = new ArrayList<Room>();
        this.initial_rooms = new ArrayList<Room>();
        this.registered_room_ids = new ArrayList<Integer>();
    }

    public String getName() {
        return this.name;
    }

    public byte[] getSalt() {
        return this.salt;
    }

    public String getSecurePassword() {
        return this.secure_password;
    }

    public boolean hasInviteToRoom(int room_id) {
        boolean invite = false;
        for(Room room : this.invites) {
            if(room.getId() == room_id) {
                invite = true;
            }
        }
        return invite;
    }

    public boolean hasPreInviteToRoom(int room_id) {
        boolean invite = false;
        for(Room room : this.pre_invites) {
            if(room.getId() == room_id) {
                invite = true;
            }
        }
        return invite;
    }

    public boolean hasSecretToRoom(int room_id) {
        boolean secret = false;
        for(Room room : this.initial_rooms) {
            if(room.getId() == room_id) {
                secret = true;
            }
        }
        return secret;
    }

    public boolean hasRegisteredRoom(int room_id) {
        return this.registered_room_ids.contains(room_id);
    }

    public void addInitialSecret(Room room) {
        this.initial_rooms.add(room);
    }

    public void addPreInvite(Room room) {
        this.pre_invites.add(room);
    }

    public void addInvite(int room_id) {
        Room invite_room = null;
        for(Room room : this.pre_invites) {
            if(room.getId() == room_id) {
                invite_room = room;
            }
        }
        this.invites.add(invite_room);
    }

    public void addRegisteredRoom(int room_id) {
        this.registered_room_ids.add(room_id);
    }

    public void removeInvite(int room_id) {
        for(Room room : this.invites) {
            if(room.getId() == room_id) {
                this.invites.remove(room);
                return;
            }
        }
    }

    public List<Room> getInvites() {
        return this.invites;
    }

    public List<Room> getSecrets() {
        return this.initial_rooms;
    }

    public String getInitialSecret(int room_id) {
        String secret = null;
        Room initial_room = null;
        for(Room room : this.initial_rooms) {
            if(room.getId() == room_id) {
                secret = room.getInitialSecret();
                initial_room = room;
            }
        }
        if(initial_room != null) {
            this.initial_rooms.remove(initial_room);
        }

        return secret;
    }
}
