package pt.tecnico.grpc.server;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The VoteCreateChannel class represents a voting process to create a new text channel.
 */
public class VoteCreateChannel extends Vote {

    private String channel_name;
    
    public VoteCreateChannel(ChatRoom chat_room, User initiator, String channel_name) {
        super(chat_room, initiator, chat_room.getThresholds().getCreateChannelThreshold());
        this.channel_name = channel_name;
    }

    public String getChannelName() {
        return this.channel_name;
    }

    @Override
    public String toString() {
        BigDecimal dbDown = new BigDecimal(super.getCollaborativeDecisionLow()).setScale(2, RoundingMode.DOWN);

        return "(Voting Process id=" + super.getId() + ") Create a new channel with name: " + getChannelName() + "\n" +
			    "Voting process initiated by " + super.getInitiatorName() + " on " + super.getCreationDate() + "\n" +
				"Description: " + super.getDescription() + "\n" +
                "Collaborative decision: " + dbDown.doubleValue() + "\n";
    }

    @Override
    public void doAction() {
        super.getChatRoom().createChannel(this.channel_name);
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().createChannelByVote(this.channel_name, super.getInitiatorName(), super.getDescription(), true);
    }

    @Override
    public void ignoreAction() {
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().createChannelByVote(this.channel_name, super.getInitiatorName(), super.getDescription(), false);
    }
}
