package pt.tecnico.grpc.server;

import java.math.BigDecimal;
import java.math.RoundingMode;

import pt.tecnico.grpc.Daoliberate;

/**
 * The VoteReputation class represents a voting process to lower
 * another user's reputation to a specific value.
 */
public class VoteReputation extends EqualVote {

    private String username;
    private double new_reputation;
    public static final double threshold = 0.5;
    
    public VoteReputation(ChatRoom chat_room, User initiator, String username, double new_reputation) {
        super(chat_room, initiator, threshold);
        this.username = username;
        this.new_reputation = new_reputation;
    }

    public String getUsername() {
        return this.username;
    }

    public double getNewReputation() {
        return this.new_reputation;
    }

    @Override
    public String toString() {
        BigDecimal dbDown = new BigDecimal(super.getCollaborativeDecisionLow()).setScale(2, RoundingMode.DOWN);

        return "(Voting Process id=" + super.getId() + ") Change the reputation of \"" + getUsername() + "\" to: " + getNewReputation() + "\n" +
                "Voting process initiated by " + super.getInitiatorName() + " on " + super.getCreationDate() + "\n" +
				"Description: " + super.getDescription() + "\n" +
                "Collaborative decision: " + dbDown.doubleValue() + "\n";
    }

    @Override
    public void doAction() {
        super.getChatRoom().changeReputation(getUsername(), getNewReputation());
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().changeReputationByVote(this, true);
    }

    @Override
    public void ignoreAction() {
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().changeReputationByVote(this, false);
    }
}
