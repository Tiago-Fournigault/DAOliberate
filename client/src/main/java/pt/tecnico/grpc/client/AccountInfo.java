package pt.tecnico.grpc.client;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * The AccountInfo class is used to store all the chat rooms the user has entered.
 */
public class AccountInfo implements Serializable {

    private ArrayList<RoomInfo> rooms;

    public AccountInfo() {
        this.rooms = new ArrayList<RoomInfo>();
    }

    public List<String> listRooms() {
        List<String> list = new ArrayList<String>();
        for(RoomInfo room : rooms) {
            list.add(room.toString());
        }
        return list;
    }

    public void addInfo(int room_id, String room_name, String pseudonym, String password) {
        RoomInfo room_info = new RoomInfo(room_id, room_name, pseudonym, password);
        this.rooms.add(room_info);
    }

    public String getPseudonym(int room_id) {
        for(RoomInfo room : rooms) {
            if(room.getId() == room_id) {
                return room.getPseudonym();
            }
        }
        return null;
    }

    public String getPassword(int room_id) {
        for(RoomInfo room : rooms) {
            if(room.getId() == room_id) {
                return room.getSecret();
            }
        }
        return null;
    }

    public boolean checkRoomAccess(int room_id) {
        boolean access = false;
        for(RoomInfo room : rooms) {
            if(room_id == room.getId()) {
                access = true;
            }
        }
        return access;
    }
}