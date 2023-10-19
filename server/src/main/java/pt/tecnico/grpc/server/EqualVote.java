package pt.tecnico.grpc.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

/**
 * The EqualVote class represents a voting process
 * in which reputation does not come into consideration and
 * a majority of votes must be achieved.
 */
public class EqualVote {

    private int id;
	private User initiator;
    private ArrayList<User> eligible_voters;
    private HashMap<User, Boolean> already_voters;
    private String description;
    private LocalDateTime creation_date;
    private ChatRoom chat_room;
    private double threshold;
    private int participants;

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    public EqualVote(ChatRoom chat_room, User initiator, double threshold) {
        this.id = 0;
        this.eligible_voters = new ArrayList<User>(chat_room.getParticipants());
        this.participants = chat_room.getParticipants().size();
        this.initiator = initiator;
        this.already_voters = new HashMap<User, Boolean>();
        this.description = "";
        this.creation_date = LocalDateTime.now();
        this.chat_room = chat_room;
        this.threshold = threshold;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInitiatorName() {
        return this.initiator.getName();
    }

    public User getInitiator() {
        return this.initiator;
    }

    public boolean eligibleVoter(String pseudonym) {
        for(User user : this.eligible_voters) {
            if(user.getName().equals(pseudonym)) {
                return true;
            }
        }
        return false;
    }

    public void vote(String pseudonym, boolean vote) {
        User user = chat_room.getUser(pseudonym);
        this.eligible_voters.remove(user);
        this.already_voters.put(user, vote);
        user.incrInteractions();

        if(this.getCollaborativeDecisionLow() >= this.threshold) {
            this.doAction();
            initiator.setReputation(this.already_voters, this.chat_room.getNumberParticipants());
        }
        else if(this.getCollaborativeDecisionUp() < this.threshold) {
            this.ignoreAction();
            initiator.setReputation(this.already_voters, this.chat_room.getNumberParticipants());
        }
    }

    public void doAction(){};
    public void ignoreAction(){};
    public String toString(){return "";}

    public double getCollaborativeDecisionLow() {
        double collaborative_decision = 0.0;

        for (Map.Entry<User, Boolean> entry : already_voters.entrySet()) {
            collaborative_decision += (entry.getValue() ? 1 : 0);
        }

        System.out.println(collaborative_decision / this.participants);
        return  collaborative_decision / this.participants;
    }

    public double getCollaborativeDecisionUp() {
        double collaborative_decision = 0.0;

        for (Map.Entry<User, Boolean> entry : already_voters.entrySet()) {
            collaborative_decision += (entry.getValue() ? 1 : 0);
        }
        for(User user : this.eligible_voters) {
            collaborative_decision += 1;
        }

        System.out.println(collaborative_decision / this.participants);
        return  collaborative_decision / this.participants;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ChatRoom getChatRoom() {
        return this.chat_room;
    }

    public String getCreationDate() {
        return dtf.format(this.creation_date);
    }
}
