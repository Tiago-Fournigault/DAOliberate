package pt.tecnico.grpc.server;

import java.math.BigDecimal;
import java.math.RoundingMode;

import pt.tecnico.grpc.Daoliberate;

/**
 * The VotePinMessage class represents a voting process to pin a existent message.
 */
public class VotePinMessage extends Vote {

    private String channel_name;
    private int message_id;
    private String message_content;
    private String message_owner;
    
    public VotePinMessage(ChatRoom chat_room, User initiator, String channel_name, int message_id) {
        super(chat_room, initiator, chat_room.getThresholds().getPinMessageThreshold());
        this.channel_name = channel_name;
        this.message_id = message_id;
        this.message_content = chat_room.getMessageContent(channel_name, message_id);
        this.message_owner = chat_room.getMessageOwnerName(channel_name, message_id);
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

    @Override
    public String toString() {
        BigDecimal dbDown = new BigDecimal(super.getCollaborativeDecisionLow()).setScale(2, RoundingMode.DOWN);

        return "(Voting Process id=" + super.getId() + ") Pin Message with id: " + getMessageId() + "\n" +
                "Message content: " + getMessageContent() + "\n" +
                "Message owner: " + getMessageOwnerName() + "\n" +
			    "Voting process initiated by " + super.getInitiatorName() + " on " + super.getCreationDate() + "\n" +
				"Description: " + super.getDescription() + "\n" +
                "Collaborative decision: " + dbDown.doubleValue() + "\n";
    }

    @Override
    public void doAction() {
        super.getChatRoom().pinMessage(this.channel_name, this.message_id);
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().pinMessageByVote(this.channel_name, this, true);
    }

    @Override
    public void ignoreAction() {
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().pinMessageByVote(this.channel_name, this, false);
    }
}
