package pt.tecnico.grpc.server;

import java.math.BigDecimal;
import java.math.RoundingMode;

import pt.tecnico.grpc.Daoliberate;

/**
 * The VoteDeleteMessage class represents a voting process to delete a existent message.
 */
public class VoteDeleteMessage extends Vote {

    private String channel_name;
    private int message_id;
    private String message_content;
    private String message_owner;
    private Daoliberate.REASON reason;
    
    public VoteDeleteMessage(ChatRoom chat_room, User initiator, String channel_name, int message_id, Daoliberate.REASON reason) {
        super(chat_room, initiator, chat_room.getThresholds().getDeleteMessageThreshold());
        this.channel_name = channel_name;
        this.message_id = message_id;
        this.message_content = chat_room.getMessageContent(channel_name, message_id);
        this.message_owner = chat_room.getMessageOwnerName(channel_name, message_id);
        this.reason = reason;
    }

    public String getChannelName() {
        return this.channel_name;
    }

    public int getMessageId() {
        return this.message_id;
    }

    public String getMessageContent() {
        return this.message_content;
    }

    public String getMessageOwnerName() {
        return this.message_owner;
    }

    public String getReason() {
        switch(this.reason) {
            case OFFENSIVE:
                return "Offensive content";
            case SPAM:
                return "Spam";
            case OUT_OF_CONTEXT:
                return "Content out of context";
            default:
                return "Unknown reason";
        }
    }

    @Override
    public String toString() {
        BigDecimal dbDown = new BigDecimal(super.getCollaborativeDecisionLow()).setScale(2, RoundingMode.DOWN);

        return "(Voting Process id=" + super.getId() + ") Delete Message with id: " + getMessageId() + "\n" +
                "Message content: " + getMessageContent() + "\n" +
                "Message owner: " + getMessageOwnerName() + "\n" +
                "Message deletion due to: " + getReason() + "\n" +
			    "Voting process initiated by " + super.getInitiatorName() + " on " + super.getCreationDate() + "\n" +
				"Description: " + super.getDescription() + "\n" +
                "Collaborative decision: " + dbDown.doubleValue() + "\n";
    }

    @Override
    public void doAction() {
        super.getChatRoom().deleteMessage(this.channel_name, this.message_id);
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().deleteMessageByVote(this.channel_name, this, true);
    }

    @Override
    public void ignoreAction() {
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().deleteMessageByVote(this.channel_name, this, false);
    }
}
