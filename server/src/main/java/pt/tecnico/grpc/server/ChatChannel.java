package pt.tecnico.grpc.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;

import pt.tecnico.grpc.security.*;

/**
 * The ChatChannel class represents a text channel in a chat room.
 */
public class ChatChannel {

    private String name;
    private ConcurrentHashMap<Integer, Message> messages;
    private AtomicInteger id_counter;      //message id
    private ArrayList<Integer> pinned_messages;

    public ChatChannel(String name) {
        this.name = name;
        this.messages = new ConcurrentHashMap<Integer, Message>();
        this.id_counter = new AtomicInteger(0);
        this.pinned_messages = new ArrayList<Integer>();
    }

    public String getName() {
        return this.name;
    }

    public boolean messageExists(int message_id) {
        return this.messages.containsKey(message_id);
    }

    public boolean messagePinned(int message_id) {
        return this.pinned_messages.contains(message_id);
    }

    public void deleteMessage(int message_id) {
        this.messages.remove(message_id);
    }

    public void pinMessage(int message_id) {
        this.pinned_messages.add(message_id);
    }

    public void unpinMessage(int message_id) {
        this.pinned_messages.remove(message_id);
    }

    public void editMessage(int message_id, User editor, String new_content) {
        this.messages.get(message_id).editMessageContent(editor, new_content);
    }

    public String getMessageContent(int message_id) {
        return this.messages.get(message_id).getContent();
    }

    public String getMessageOwnerName(int message_id) {
        return this.messages.get(message_id).getOwner().getName();
    }

    public List<Message> getMessages() {
        Collection<Message> values = this.messages.values();
        return new ArrayList<Message>(values);
    }

    public List<Message> getPinnedMessages() {
        ArrayList<Message> pinned_msgs = new ArrayList<Message>();
        for(Integer i : this.pinned_messages) {
            pinned_msgs.add(this.messages.get(i));
        }
        return pinned_msgs;
    }

    public int getIdCounter() {
        return this.id_counter.getAndIncrement();
    }

    public void addMessage(User message_owner, String message_content) {
        int message_id = getIdCounter();
        Message message = new Message(message_id, message_owner, message_content);
        this.messages.put(message_id, message);
    }

}
