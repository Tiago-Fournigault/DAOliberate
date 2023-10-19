package pt.tecnico.grpc.server;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The VoteDeleteUser class represents a voting process to delete a existent user.
 */
public class VoteDeleteUser extends Vote {

    private User username_delete;
    
    public VoteDeleteUser(ChatRoom chat_room, User initiator, User username_delete) {
        super(chat_room, initiator, chat_room.getThresholds().getDeleteUserThreshold());
        this.username_delete = username_delete;
    }

    public String getUsernameDelete() {
        return this.username_delete.getName();
    }

    @Override
    public String toString() {
        BigDecimal dbDown = new BigDecimal(super.getCollaborativeDecisionLow()).setScale(2, RoundingMode.DOWN);

        return "(Voting Process id=" + super.getId() + ") Delete a user with name: " + getUsernameDelete() + "\n" +
			    "Voting process initiated by " + super.getInitiatorName() + " on " + super.getCreationDate() + "\n" +
				"Description: " + super.getDescription() + "\n" +
                "Collaborative decision: " + dbDown.doubleValue() + "\n";
    }

    @Override
    public void doAction() {
        super.getChatRoom().deleteUser(this.username_delete);
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().deleteUserByVote(this, true);
    }

    @Override
    public void ignoreAction() {
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().deleteUserByVote(this, false);
    }
}
