package pt.tecnico.grpc.server;

import java.math.BigDecimal;
import java.math.RoundingMode;

import pt.tecnico.grpc.Daoliberate;

/**
 * The VoteExperimentalInteractions class represents a voting process to
 * change the value of experimental interactions.
 */
public class VoteExperimentalInteractions extends EqualVote {

    private int experimental_interactions;
    public static final double threshold = 0.5;
    
    public VoteExperimentalInteractions(ChatRoom chat_room, User initiator, int experimental_interactions) {
        super(chat_room, initiator, threshold);
        this.experimental_interactions = experimental_interactions;
    }

    public int getExperimentalInteractions() {
        return this.experimental_interactions;
    }

    @Override
    public String toString() {
        BigDecimal dbDown = new BigDecimal(super.getCollaborativeDecisionLow()).setScale(2, RoundingMode.DOWN);

        return "(Voting Process id=" + super.getId() + ") Change the \"experimental interactions\" to: " + getExperimentalInteractions() + "\n" +
                "Voting process initiated by " + super.getInitiatorName() + " on " + super.getCreationDate() + "\n" +
				"Description: " + super.getDescription() + "\n" +
                "Collaborative decision: " + dbDown.doubleValue() + "\n";
    }

    @Override
    public void doAction() {
        super.getChatRoom().setExperimentalInteractions(this.experimental_interactions);
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().changeExperimentalInteractionsByVote(this, true);
    }

    @Override
    public void ignoreAction() {
        super.getChatRoom().deleteVote(super.getId());
        super.getChatRoom().getHistoryChannel().changeExperimentalInteractionsByVote(this, false);
    }
}
