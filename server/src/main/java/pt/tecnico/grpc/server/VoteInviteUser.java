package pt.tecnico.grpc.server;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The VoteInviteUser class represents a voting process to invite a new user to a chat room.
 */
public class VoteInviteUser extends EqualVote {

    private String username_invite;
    private DaoliberateLibrary library;
    public static final double threshold = 0.5;
    
    public VoteInviteUser(ChatRoom chat_room, DaoliberateLibrary library, User initiator, String username_invite) {
        super(chat_room, initiator, threshold);
        this.library = library;
        this.username_invite = username_invite;
    }

    public String getUsernameInvite() {
        return this.username_invite;
    }

    @Override
    public String toString() {
        BigDecimal dbDown = new BigDecimal(super.getCollaborativeDecisionLow()).setScale(2, RoundingMode.DOWN);

        return "(Voting Process id=" + super.getId() + ") Invite a user with name: " + getUsernameInvite() + "\n" +
			    "Voting process initiated by " + super.getInitiatorName() + " on " + super.getCreationDate() + "\n" +
				"Description: " + super.getDescription() + "\n" +
                "Collaborative decision: " + dbDown.doubleValue() + "\n";
    }

    @Override
    public void doAction() {
        String response = this.library.inviteUser(super.getChatRoom().getId(), username_invite);
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().inviteUserByVote(this, response, true);
    }

    @Override
    public void ignoreAction() {
        String response = "";
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().inviteUserByVote(this, response, false);
    }
}
