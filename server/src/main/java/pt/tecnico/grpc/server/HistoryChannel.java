package pt.tecnico.grpc.server;

import java.util.ArrayList;
import java.util.List;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import pt.tecnico.grpc.security.*;
import pt.tecnico.grpc.Daoliberate;

/**
 * The HistoryChannel class is responsible for logging
 * all moderation tasks performed in a chat room.
 */
public class HistoryChannel {

    private ArrayList<String> actions;
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public HistoryChannel() {
        this.actions = new ArrayList<String>();
    }

    public void createChannel(String channel_name, String creator_name) {
        String action = "Channel created with name: " + channel_name + "\n";
        action += "Name of creator: " + creator_name + "\n";
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void deleteChannel(String channel_name, String eraser_name) {
        String action = "Channel deleted with name: " + channel_name + "\n";
        action += "Name of eraser: " + eraser_name + "\n";
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void deleteMessage(String channel_name, String eraser_name, int message_id, String message_creator, String message_content, Daoliberate.REASON reason) {
        String action = "Message deleted (with id: " + message_id + ") in channel \"" + channel_name + "\"" + "\n";
        action += "Message creator: " + message_creator + "\n";
        action += "Message content: " + message_content + "\n";
        switch(reason) {
            case OFFENSIVE:
                action += "Message deleted due to: Offensive Content\n";
                break;
            case SPAM:
                action += "Message deleted due to: Spam\n";
                break;
            case OUT_OF_CONTEXT:
                action += "Message deleted due to: Content out of context\n";
                break;
            case OWNER:
                action += "Message deleted by message creator\n";
                break;
        }
        action += "Name of eraser: " + eraser_name + "\n";
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void pinMessage(String channel_name, String fixer_name, int message_id, String message_creator, String message_content) {
        String action = "Message pinned (with id: " + message_id + ") in channel \"" + channel_name + "\"" + "\n";
        action += "Message creator: " + message_creator + "\n";
        action += "Message content: " + message_content + "\n";
        if(fixer_name.equals(message_creator)) {
            action += "Message pinned by message creator\n";
        }
        else{
            action += "Message pinned by: " + fixer_name + "\n";
        }
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void unpinMessage(String channel_name, String unfixer_name, int message_id, String message_creator, String message_content) {
        String action = "Message unpinned (with id: " + message_id + ") in channel \"" + channel_name + "\"" + "\n";
        action += "Message creator: " + message_creator + "\n";
        action += "Message content: " + message_content + "\n";
        if(unfixer_name.equals(message_creator)) {
            action += "Message unpinned by message creator\n";
        }
        else{
            action += "Message unpinned by: " + unfixer_name + "\n";
        }
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void editMessage(String channel_name, String editor_name, int message_id, String message_creator, String message_content, String new_content, Daoliberate.REASON reason) {
        String action = "Message edited (with id: " + message_id + ") in channel \"" + channel_name + "\"" + "\n";
        action += "Message creator: " + message_creator + "\n";
        action += "Previous message content: " + message_content + "\n";
        action += "New message content: " + new_content + "\n";
        switch(reason) {
            case OFFENSIVE:
                action += "Message edited due to: Offensive Content\n";
                break;
            case CLEARER:
                action += "Message edited due to: Make content clearer\n";
                break;
            case OWNER:
                action += "Message edited by message creator\n";
                break;
        }
        action += "Name of editor: " + editor_name + "\n";
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void inviteUser(String invited_name, String inviter_name, String response) {
        String action = "User invited (with name: " + invited_name + ")\n";
        action += "Inviter name: " + inviter_name + "\n";
        action += "Result: " + response + "\n";
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void changeThreshold(Daoliberate.ACTION action_changed, double threshold) {
        String type = "";
        switch(action_changed) {
            case CREATE_CHANNEL:
                type = "Create a new channel";
                break;
            case DELETE_CHANNEL:
                type = "Delete a channel";
                break;
            case DELETE_MESSAGE:
                type = "Delete a message";
                break;
            case DELETE_USER:
                type = "Delete a user";
                break;
            case EDIT_MESSAGE:
                type = "Edit a message";
                break;
            case INVITE_USER:
                type = "Invite a user";
                break;
            case PIN_MESSAGE:
                type = "Pin a message";
                break;
            case PUNISH_USER:
                type = "Punish/Unpunish a user";
                break;
            case UNPIN_MESSAGE:
                type = "Unpin a message";
                break;
        }
        String action = "Threshold changed for action: " + type + "\n";
        action += "New defined threshold: " + threshold + "\n";
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void changeExperimentalInteractions(int experimental_interactions) {
        String action = "Experimental interactions changed to: " + experimental_interactions + "\n";
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void changeReputation(String user_name, double reputation) {
        String action = "Reputation of \"" + user_name + "\" changed to: " + reputation + "\n";
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void createChannelByVote(String channel_name, String initiator_name, String description, boolean success) {
        String action = "";
        if(success) {
            action += "Channel created with name \"" + channel_name + "\" through a voting process\n";
        }
        else {
            action += "Attempt to create a new text channel with name " + channel_name + "\" declined through a voting process\n";
        }
        action += "Voting process initiator name: " + initiator_name + "\n";
        if(!description.equals("")){
            action += "Description of voting process: " + description + "\n";
        }
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void deleteChannelByVote(String channel_name, String initiator_name, String description, boolean success) {
        String action = "";
        if(success) {
            action += "Channel deleted with name \"" + channel_name + "\" through a voting process\n";
        }
        else {
            action += "Attempt to delete a text channel with name \"" + channel_name + "\" declined through a voting process\n";
        }
        action += "Voting process initiator name: " + initiator_name + "\n";
        if(!description.equals("")){
            action += "Description of voting process: " + description + "\n";
        }
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void deleteMessageByVote(String channel_name, VoteDeleteMessage vote, boolean success) {
        String action = "";
        if(success) {
            action += "Message deleted (with id: " + vote.getMessageId() + ") in channel \"" + channel_name + "\"" + "\n";
        }
        else {
            action += "Attempt to delete a message (with id: " + vote.getMessageId() + ") in channel \"" + channel_name + "\" declined through a voting process\n";
        }
        action += "Message creator: " + vote.getMessageOwnerName() + "\n";
        action += "Message content: " + vote.getMessageContent() + "\n";
        action += "Voting process initiator name: " + vote.getInitiatorName() + "\n";
        if(success) {
            action += "Message deleted due to: " + vote.getReason() + "\n";
        }
        else {
            action += "Attempt to delete due to: " + vote.getReason() + "\n";
        }
        if(!vote.getDescription().equals("")){
            action += "Description of voting process: " + vote.getDescription() + "\n";
        }
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void pinMessageByVote(String channel_name, VotePinMessage vote, boolean success) {
        String action = "";
        if(success) {
            action += "Message pinned (with id: " + vote.getMessageId() + ") in channel \"" + channel_name + "\"" + "\n";
        }
        else {
            action += "Attempt to pin a message (with id: " + vote.getMessageId() + ") in channel \"" + channel_name + "\" declined through a voting process\n";
        }
        action += "Message creator: " + vote.getMessageOwnerName() + "\n";
        action += "Message content: " + vote.getMessageContent() + "\n";
        action += "Voting process initiator name: " + vote.getInitiatorName() + "\n";
        if(!vote.getDescription().equals("")){
            action += "Description of voting process: " + vote.getDescription() + "\n";
        }
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void unpinMessageByVote(String channel_name, VoteUnpinMessage vote, boolean success) {
        String action = "";
        if(success) {
            action += "Message unpinned (with id: " + vote.getMessageId() + ") in channel \"" + channel_name + "\"" + "\n";
        }
        else {
            action += "Attempt to unpin a message (with id: " + vote.getMessageId() + ") in channel \"" + channel_name + "\" declined through a voting process\n";
        }
        action += "Message creator: " + vote.getMessageOwnerName() + "\n";
        action += "Message content: " + vote.getMessageContent() + "\n";
        action += "Voting process initiator name: " + vote.getInitiatorName() + "\n";
        if(!vote.getDescription().equals("")){
            action += "Description of voting process: " + vote.getDescription() + "\n";
        }
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void deleteUserByVote(VoteDeleteUser vote, boolean success) {
        String action = "";
        if(success) {
            action += "User marked to be deleted (with name: " + vote.getUsernameDelete() + ") from room\n";
        }
        else {
            action += "Attempt to delete a user (with name: " + vote.getUsernameDelete() + ") from room declined through a voting process\n";
        }
        action += "Voting process initiator name: " + vote.getInitiatorName() + "\n";
        if(!vote.getDescription().equals("")){
            action += "Description of voting process: " + vote.getDescription() + "\n";
        }
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\nDeletion in 1 day.";
        this.actions.add(action);
    }

    public void editMessageByVote(String channel_name, VoteEditMessage vote, boolean success) {
        String action = "";
        if(success) {
            action += "Message edited (with id: " + vote.getMessageId() + ") in channel \"" + channel_name + "\"" + "\n";
        }
        else {
            action += "Attempt to edit a message (with id: " + vote.getMessageId() + ") in channel \"" + channel_name + "\" declined through a voting process\n";
        }
        action += "Message creator: " + vote.getMessageOwnerName() + "\n";
        action += "Message content: " + vote.getMessageContent() + "\n";
        action += "Voting process initiator name: " + vote.getInitiatorName() + "\n";
        if(success) {
            action += "New message content: " + vote.getNewContent() + "\n";
            action += "Message edited due to: " + vote.getReason() + "\n";
        }
        else {
            action += "Attempting new message content: " + vote.getNewContent() + "\n";
            action += "Attempt to edit due to: " + vote.getReason() + "\n";
        }
        if(!vote.getDescription().equals("")){
            action += "Description of voting process: " + vote.getDescription() + "\n";
        }
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void punishUserByVote(VotePunishUser vote, boolean success) {
        String action = "";
        if(success) {
            action += "User punished (with name: " + vote.getUsernamePunish() + ")\n";
        }
        else {
            action += "Attempt to punish a user (with name: " + vote.getUsernamePunish() + ") declined through a voting process\n";
        }
        action += "Duration of punishment: " + vote.getDays() + " days, " + vote.getHours() + " hours, " + vote.getMinutes() + " minutes\n";
        action += "(Punishment until: " + dtf.format(LocalDateTime.now().plusDays(vote.getDays()).plusHours(vote.getHours()).plusMinutes(vote.getMinutes())) + ")\n";
        action += "Voting process initiator name: " + vote.getInitiatorName() + "\n";
        if(!vote.getDescription().equals("")){
            action += "Description of voting process: " + vote.getDescription() + "\n";
        }
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void unpunishUserByVote(VotePunishUser vote, boolean success) {
        String action = "";
        if(success) {
            action += "User unpunished (with name: " + vote.getUsernamePunish() + ")\n";
        }
        else {
            action += "Attempt to unpunish a user (with name: " + vote.getUsernamePunish() + ") declined through a voting process\n";
        }
        action += "Voting process initiator name: " + vote.getInitiatorName() + "\n";
        if(!vote.getDescription().equals("")){
            action += "Description of voting process: " + vote.getDescription() + "\n";
        }
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void inviteUserByVote(VoteInviteUser vote, String response, boolean success) {
        String action = "";
        if(success) {
            action += "User invited (with name: " + vote.getUsernameInvite() + ")\n";
        }
        else {
            action += "Attempt to invite a user (with name: " + vote.getUsernameInvite() + ") declined through a voting process\n";
        }
        action += "Voting process initiator name: " + vote.getInitiatorName() + "\n";
        if(!vote.getDescription().equals("")){
            action += "Description of voting process: " + vote.getDescription() + "\n";
        }
        action += "Result: " + response + "\n";
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void changeThresholdByVote(VoteThreshold vote, boolean success) {
        String action = "";
        if(success) {
            action += "Threshold changed for action: " + vote.getAction() + "\n";
            action += "New defined threshold: " + vote.getNewThreshold() + "\n";
        }
        else {
            action += "Attempt to change threshold for the action: " + vote.getAction() + "\n";
            action += "Attempt to change the threshold to: " + vote.getNewThreshold() + "\n";
        }
        action += "Voting process initiator name: " + vote.getInitiatorName() + "\n";
        if(!vote.getDescription().equals("")){
            action += "Description of voting process: " + vote.getDescription() + "\n";
        }
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void changeExperimentalInteractionsByVote(VoteExperimentalInteractions vote, boolean success) {
        String action = "";
        if(success) {
            action += "Experimental interactions changed to: " + vote.getExperimentalInteractions() + "\n";
        }
        else {
            action += "Attempt to change \"experimental interactions\" to: " + vote.getExperimentalInteractions() + "\n";
        }
        action += "Voting process initiator name: " + vote.getInitiatorName() + "\n";
        if(!vote.getDescription().equals("")){
            action += "Description of voting process: " + vote.getDescription() + "\n";
        }
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void changeReputationByVote(VoteReputation vote, boolean success) {
        String action = "";
        if(success) {
            action += "Reputation of \"" + vote.getUsername() + "\" changed to: " + vote.getNewReputation() + "\n";
        }
        else {
            action += "Attempt to change reputation of \"" + vote.getUsername() + "\" to " + vote.getNewReputation() + "\n";
        }
        action += "Voting process initiator name: " + vote.getInitiatorName() + "\n";
        if(!vote.getDescription().equals("")){
            action += "Description of voting process: " + vote.getDescription() + "\n";
        }
        action += "Date: " + dtf.format(LocalDateTime.now()) + "\n";
        this.actions.add(action);
    }

    public void cancelVote(Vote vote) {
        this.actions.add(vote.toString() + "CANCELLED\n");
    }

    public void cancelVote(EqualVote vote) {
        this.actions.add(vote.toString() + "CANCELLED\n");
    }

    public String toString() {
        String response = "";
        for(String action : this.actions) {
            response += action;
            response += "\n";
        }
        return response;
    }

    public List<String> getActions() {
        return this.actions;
    }
}
