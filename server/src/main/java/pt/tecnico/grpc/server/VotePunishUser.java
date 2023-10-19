package pt.tecnico.grpc.server;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The VotePunishUser class represents a voting process to punish a user.
 */
public class VotePunishUser extends Vote {

    private User username_punish;
    private int days;
    private int hours;
    private int minutes;
    
    public VotePunishUser(ChatRoom chat_room, User initiator, User username_punish, int days, int hours, int minutes) {
        super(chat_room, initiator, chat_room.getThresholds().getPunishUserThreshold());
        this.username_punish = username_punish;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
    }

    public String getUsernamePunish() {
        return this.username_punish.getName();
    }

    public int getDays() {
        return this.days;
    }

    public int getHours() {
        return this.hours;
    }

    public int getMinutes() {
        return this.minutes;
    }

    @Override
    public String toString() {
        BigDecimal dbDown = new BigDecimal(super.getCollaborativeDecisionLow()).setScale(2, RoundingMode.DOWN);

        if(getDays() == 0 && getHours() == 0 && getMinutes() == 0) {
            return "(Voting Process id=" + super.getId() + ") Unpunish a user with name: " + getUsernamePunish() + "\n" +
			    "Voting process initiated by " + super.getInitiatorName() + " on " + super.getCreationDate() + "\n" +
				"Description: " + super.getDescription() + "\n" +
                "Collaborative decision: " + dbDown.doubleValue() + "\n";
        }
        else {
            return "(Voting Process id=" + super.getId() + ") Punish a user with name: " + getUsernamePunish() + "\n" +
			    "Voting process initiated by " + super.getInitiatorName() + " on " + super.getCreationDate() + "\n" +
                "Punishment period: " + getDays() + " days, " + getHours() + " hours, " + getMinutes() + " minutes\n" +
				"Description: " + super.getDescription() + "\n" +
                "Collaborative decision: " + dbDown.doubleValue() + "\n";
        }
    }

    @Override
    public void doAction() {
        super.getChatRoom().punishUser(this.username_punish, this.days, this.hours, this.minutes);
        super.getChatRoom().deleteVote(super.getId());
        if(getDays() == 0 && getHours() == 0 && getMinutes() == 0) {
            super.getChatRoom().getHistoryChannel().unpunishUserByVote(this, true);
        }
        else {
            super.getChatRoom().getHistoryChannel().punishUserByVote(this, true);
        }
    }

    @Override
    public void ignoreAction() {
        super.getChatRoom().deleteVote(super.getId());
        if(getDays() == 0 && getHours() == 0 && getMinutes() == 0) {
            super.getChatRoom().getHistoryChannel().unpunishUserByVote(this, false);
        }
        else {
            super.getChatRoom().getHistoryChannel().punishUserByVote(this, false);
        }
    }
}
