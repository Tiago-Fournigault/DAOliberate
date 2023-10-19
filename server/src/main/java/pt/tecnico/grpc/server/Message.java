package pt.tecnico.grpc.server;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Message class is responsible for storing information of a message.
 */
public class Message {

    private int id;
    private User owner;
    private User editor;
    private String content;

    public Message(int id, User owner, String content) {
        this.id = id;
        this.owner = owner;
        this.editor = null;
        this.content = content;
    }

    public int getId() {
        return this.id;
    }

    public User getOwner() {
        return this.owner;
    }

    public User getEditor() {
        return this.editor;
    }

    public String getContent() {
        return this.content;
    }

    public void editMessageContent(User editor, String new_content) {
        this.editor = editor;
        this.content = new_content;
    }
}
