package pt.tecnico.grpc.server;

import java.math.BigDecimal;
import java.math.RoundingMode;

import pt.tecnico.grpc.Daoliberate;

/**
 * The VoteThreshold class represents a voting process for
 * changing the threshold of a voting task within a chat room.
 */
public class VoteThreshold extends EqualVote {

    private Daoliberate.ACTION action;
    private double new_threshold;
    public static final double threshold = 0.5;
    
    public VoteThreshold(ChatRoom chat_room, User initiator, Daoliberate.ACTION action, double new_threshold) {
        super(chat_room, initiator, threshold);
        this.action = action;
        this.new_threshold = new_threshold;
    }

    public Daoliberate.ACTION getDaoAction() {
        return this.action;
    }

    public String getAction() {
        String response = "";
        switch(this.action) {
            case CREATE_CHANNEL:
                response = "Create a new channel";
                break;
            case DELETE_CHANNEL:
                response = "Delete a channel";
                break;
            case DELETE_MESSAGE:
                response = "Delete a message";
                break;
            case DELETE_USER:
                response = "Delete a user";
                break;
            case EDIT_MESSAGE:
                response = "Edit a message";
                break;
            case INVITE_USER:
                response = "Invite a user";
                break;
            case PIN_MESSAGE:
                response = "Pin a message";
                break;
            case PUNISH_USER:
                response = "Punish/Unpunish a user";
                break;
            case UNPIN_MESSAGE:
                response = "Unpin a message";
                break;
        }
        return response;
    }

    public double getNewThreshold() {
        return this.new_threshold;
    }

    @Override
    public String toString() {
        BigDecimal dbDown = new BigDecimal(super.getCollaborativeDecisionLow()).setScale(2, RoundingMode.DOWN);

        return "(Voting Process id=" + super.getId() + ") Change the voting threshold for the action: " + getAction() + "\n" +
			    "New proposed threshold: " + getNewThreshold() + "\n" +
                "Voting process initiated by " + super.getInitiatorName() + " on " + super.getCreationDate() + "\n" +
				"Description: " + super.getDescription() + "\n" +
                "Collaborative decision: " + dbDown.doubleValue() + "\n";
    }

    @Override
    public void doAction() {
        super.getChatRoom().changeThreshold(this.action, this.new_threshold);
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().changeThresholdByVote(this, true);
    }

    @Override
    public void ignoreAction() {
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().changeThresholdByVote(this, false);
    }
}
