package pt.tecnico.grpc.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

import pt.tecnico.grpc.Daoliberate;
import pt.tecnico.grpc.security.*;

/**
 * The ChatRoom class represents a chat room.
 */
public class ChatRoom {

    private int id;
    private String name;
    //private ArrayList<byte[]> salts;
    private ArrayList<String> initial_secrets;
    private AtomicInteger initial_tickets;
    private ArrayList<User> participants;
    private ConcurrentHashMap<String, ChatChannel> channels;
    private ConcurrentHashMap<Integer, Vote> votes;
    private ConcurrentHashMap<Integer, EqualVote> equal_votes;
    private AtomicInteger id_counter;      //vote id
    private HistoryChannel history_channel;
    private int experimental_interactions;
    private Thresholds thresholds;

    public ChatRoom(int id, String name, ArrayList<String> initial_secrets) {
        this.id = id;
        this.name = name;

        //byte[] salt;
        this.initial_secrets = new ArrayList<String>();
        for(int i = 0; i < initial_secrets.size(); i++) {
            //salt = LibSecurity.getSalt();
            //this.salts.add(salt);
            //this.initial_secrets.add(LibSecurity.getSecurePassword(initial_secrets.get(i), salt));
            this.initial_secrets.add(initial_secrets.get(i));
        }

        this.initial_tickets = new AtomicInteger(initial_secrets.size());
        this.participants = new ArrayList<User>();
        this.channels = new ConcurrentHashMap<String, ChatChannel>(){{put("General", new ChatChannel("General"));}};
        this.votes = new ConcurrentHashMap<Integer, Vote>();
        this.equal_votes = new ConcurrentHashMap<Integer, EqualVote>();
        this.id_counter = new AtomicInteger(0);
        this.history_channel = new HistoryChannel();
        this.experimental_interactions = 20;
        this.thresholds = new Thresholds();
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public HistoryChannel getHistoryChannel() {
        return this.history_channel;
    }

    public int getExperimentalInteractions() {
        return this.experimental_interactions;
    }

    public void setExperimentalInteractions(int experimental_interactions) {
        this.experimental_interactions = experimental_interactions;
        for(User user : this.participants) {
            user.setExperimentalInteractions(this.experimental_interactions);
        }
    }

    public void logOnHistoryCreateChannel(String channel_name, String pseudonym) {
        this.history_channel.createChannel(channel_name, pseudonym);
    }

    public void logOnHistoryDeleteChannel(String channel_name, String pseudonym) {
        this.history_channel.deleteChannel(channel_name, pseudonym);
    }

    public void logOnHistoryDeleteMessage(String channel_name, String pseudonym, int message_id, Daoliberate.REASON reason) {
        String message_creator = getMessageOwnerName(channel_name, message_id);
        String message_content = getMessageContent(channel_name, message_id);
        this.history_channel.deleteMessage(channel_name, pseudonym, message_id, message_creator, message_content, reason);
    }

    public void logOnHistoryPinMessage(String channel_name, String pseudonym, int message_id) {
        String message_creator = getMessageOwnerName(channel_name, message_id);
        String message_content = getMessageContent(channel_name, message_id);
        this.history_channel.pinMessage(channel_name, pseudonym, message_id, message_creator, message_content);
    }

    public void logOnHistoryUnpinMessage(String channel_name, String pseudonym, int message_id) {
        String message_creator = getMessageOwnerName(channel_name, message_id);
        String message_content = getMessageContent(channel_name, message_id);
        this.history_channel.unpinMessage(channel_name, pseudonym, message_id, message_creator, message_content);
    }

    public void logOnHistoryEditMessage(String channel_name, String pseudonym, int message_id, String new_content, Daoliberate.REASON reason) {
        String message_creator = getMessageOwnerName(channel_name, message_id);
        String message_content = getMessageContent(channel_name, message_id);
        this.history_channel.editMessage(channel_name, pseudonym, message_id, message_creator, message_content, new_content, reason);
    }

    public void logOnHistoryInviteUser(String invited_name, String inviter_name, String response) {
        this.history_channel.inviteUser(invited_name, inviter_name, response);
    }

    public void logOnHistoryChangeThreshold(Daoliberate.ACTION action_changed, double threshold) {
        this.history_channel.changeThreshold(action_changed, threshold);
    }

    public void logOnHistoryChangeExperimentalInteractions(int experimental_interactions) {
        this.history_channel.changeExperimentalInteractions(experimental_interactions);
    }

    public void logOnHistoryChangeReputation(String user_name, double reputation) {
        this.history_channel.changeReputation(user_name, reputation);
    }

    public String getMessageContent(String channel_name, int message_id) {
        return this.channels.get(channel_name).getMessageContent(message_id);
    }

    public String getMessageOwnerName(String channel_name, int message_id) {
        return this.channels.get(channel_name).getMessageOwnerName(message_id);
    }

    public List<String> getActions() {
        return this.history_channel.getActions();
    }

    public ArrayList<User> getParticipants() {
        return this.participants;
    }

    public int getNumberParticipants() {
        return this.participants.size();
    }

    /*public byte[] getSalt(int index) {
        return this.salts.get(index);
    }*/

    public String getInitialSecret(int index) {
        return this.initial_secrets.get(index);
    }

    public List<String> getChannels() {
        return new ArrayList<>(this.channels.keySet());
    }

    public List<Message> getMessages(String channel_name) {
        return this.channels.get(channel_name).getMessages();
    }

    public List<Message> getPinnedMessages(String channel_name) {
        return this.channels.get(channel_name).getPinnedMessages();
    }

    public void addParticipant(User user) {
        this.participants.add(user);
    }

    public boolean useTicket(User user, String secret) {
        boolean available = false;
        if(this.initial_tickets.getAndDecrement() > 0) {
            addParticipant(user);
            this.initial_secrets.remove(secret);
            available = true;
        }
        return available;
    }

    public void addMessage(String channel_name, String message_owner, String message_content) {
        this.channels.get(channel_name).addMessage(getUser(message_owner), message_content);
    }

    public boolean channelExists(String channel_name) {
        return this.channels.containsKey(channel_name);
    }

    public boolean messageExists(String channel_name, int message_id) {
        return this.channels.get(channel_name).messageExists(message_id);
    }

    public boolean messagePinned(String channel_name, int message_id) {
        return this.channels.get(channel_name).messagePinned(message_id);
    }

    public void createChannel(String channel_name) {
        this.channels.put(channel_name, new ChatChannel(channel_name));
    }

    public void deleteChannel(String channel_name) {
        this.channels.remove(channel_name);
    }

    public void deleteMessage(String channel_name, int message_id) {
        this.channels.get(channel_name).deleteMessage(message_id);
    }

    public void deleteUser(User user) {
        user.punish(1, 0, 0);
        user.markToDelete();
    }

    public void punishUser(User user, int days, int hours, int minutes) {
        user.punish(days, hours, minutes);
        if(user.isMarked() && days == 0 && hours == 0 && minutes == 0) {
            user.unmarkToDelete();
        }
    }

    public boolean isPunishedUser(String name) {
        return getUser(name).isPunished();
    }

    public boolean deletedUser(String name) {
        boolean response = false;
        if(!getUser(name).isPunished() && getUser(name).isMarked()) {
            this.participants.remove(getUser(name));
            response = true;
        }
        return response;
    }

    public void pinMessage(String channel_name, int message_id) {
        this.channels.get(channel_name).pinMessage(message_id);
    }

    public void unpinMessage(String channel_name, int message_id) {
        this.channels.get(channel_name).unpinMessage(message_id);
    }

    public void editMessage(String channel_name, int message_id, String editor_name, String new_content) {
        this.channels.get(channel_name).editMessage(message_id, getUser(editor_name), new_content);
    }

    public Thresholds getThresholds() {
        return this.thresholds;
    }

    public double getReputation(String pseudonym) {
        return getUser(pseudonym).getReputation();
    }

    public void changeThreshold(Daoliberate.ACTION action, double threshold) {
        switch(action) {
            case CREATE_CHANNEL:
                this.thresholds.setCreateChannelThreshold(threshold);
                break;
            case DELETE_CHANNEL:
                this.thresholds.setDeleteChannelThreshold(threshold);
                break;
            case DELETE_MESSAGE:
                this.thresholds.setDeleteMessageThreshold(threshold);
                break;
            case DELETE_USER:
                this.thresholds.setDeleteUserThreshold(threshold);
                break;
            case EDIT_MESSAGE:
                this.thresholds.setEditMessageThreshold(threshold);
                break;
            case INVITE_USER:
                this.thresholds.setInviteUserThreshold(threshold);
                break;
            case PIN_MESSAGE:
                this.thresholds.setPinMessageThreshold(threshold);
                break;
            case PUNISH_USER:
                this.thresholds.setPunishUserThreshold(threshold);
                break;
            case UNPIN_MESSAGE:
                this.thresholds.setUnpinMessageThreshold(threshold);
                break;
        }
    }

    public void changeReputation(String pseudonym, double reputation) {
        getUser(pseudonym).changeReputation(reputation);
    }

    public int getIdCounter() {
        return this.id_counter.getAndIncrement();
    }

    public int createVote(Vote vote) {
        int vote_id = getIdCounter();
        vote.setId(vote_id);
        this.votes.put(vote_id, vote);
        return vote_id;
    }

    public int createEqualVote(EqualVote equal_vote) {
        int vote_id = getIdCounter();
        equal_vote.setId(vote_id);
        this.equal_votes.put(vote_id, equal_vote);
        return vote_id;
    }

    public void deleteVote(int vote_id) {
        if(this.votes.containsKey(vote_id)) {
            this.votes.remove(vote_id);
        }
        else {
            this.equal_votes.remove(vote_id);
        }
    }

    public void deleteEqualVote(int vote_id) {
        this.equal_votes.remove(vote_id);
    }

    public List<Vote> getVotes(){
        return new ArrayList<Vote>(this.votes.values());
    }

    public List<EqualVote> getEqualVotes(){
        return new ArrayList<EqualVote>(this.equal_votes.values());
    }

    public boolean voteExists(int vote_id) {
        for(Vote vote : getVotes()) {
            if(vote.getId() == vote_id) {
                return true;
            }
        }
        for(EqualVote vote : getEqualVotes()) {
            if(vote.getId() == vote_id) {
                return true;
            }
        }
        return false;
    }

    public String getVoteInitiatorName(int vote_id) {
        if(this.votes.containsKey(vote_id)) {
            return this.votes.get(vote_id).getInitiatorName();
        }
        else {
            return this.equal_votes.get(vote_id).getInitiatorName();
        }
    }

    public boolean channelInVote(String channel_name) {
        for(Vote vote : votes.values()) {
            if(vote instanceof VoteCreateChannel && ((VoteCreateChannel)vote).getChannelName().equals(channel_name)) {
                return true;
            }
            else if(vote instanceof VoteDeleteChannel && ((VoteDeleteChannel)vote).getChannelName().equals(channel_name)) {
                return true;
            }
        }
        return false;
    }

    public boolean messageInVote(String channel_name, int message_id) {
        for(Vote vote : votes.values()) {
            if(vote instanceof VoteDeleteMessage 
                && ((VoteDeleteMessage)vote).getChannelName().equals(channel_name)
                && ((VoteDeleteMessage)vote).getMessageId() == message_id ) {
                return true;
            }
        }
        return false;
    }

    public boolean userInVote(String name) {
        for(Vote vote : votes.values()) {
            if(vote instanceof VoteDeleteUser && ((VoteDeleteUser)vote).getUsernameDelete().equals(name)) {
                return true;
            }
            else if(vote instanceof VotePunishUser && ((VotePunishUser)vote).getUsernamePunish().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean thresholdInVote(Daoliberate.ACTION action) {
        for(EqualVote vote : equal_votes.values()) {
            if(vote instanceof VoteThreshold && ((VoteThreshold)vote).getDaoAction() == action) {
                return true;
            }
        }
        return false;
    }

    public boolean experimentalInteractionsInVote() {
        for(EqualVote vote : equal_votes.values()) {
            if(vote instanceof VoteExperimentalInteractions) {
                return true;
            }
        }
        return false;
    }

    public boolean reputationInVote(String pseudonym) {
        for(EqualVote vote : equal_votes.values()) {
            if(vote instanceof VoteReputation && ((VoteReputation)vote).getUsername().equals(pseudonym)) {
                return true;
            }
        }
        return false;
    }

    public void editVote(int vote_id, String description) {
        if(this.votes.containsKey(vote_id)) {
            this.votes.get(vote_id).setDescription(description);
        }
        else {
            this.equal_votes.get(vote_id).setDescription(description);
        }
    }

    public void cancelVote(int vote_id) {
        if(this.votes.containsKey(vote_id)) {
            this.history_channel.cancelVote(this.votes.get(vote_id));
        }
        else {
            this.history_channel.cancelVote(this.equal_votes.get(vote_id));
        }
        deleteVote(vote_id);
    }

    public boolean eligibleVoter(int vote_id, String pseudonym) {
        if(this.votes.containsKey(vote_id)) {
            return this.votes.get(vote_id).eligibleVoter(pseudonym);
        }
        else {
            return this.equal_votes.get(vote_id).eligibleVoter(pseudonym);
        }
    }

    public void vote(int vote_id, String pseudonym, boolean vote) {
        if(this.votes.containsKey(vote_id)) {
            this.votes.get(vote_id).vote(pseudonym, vote);
        }
        else {
            this.equal_votes.get(vote_id).vote(pseudonym, vote);
        }
    }

    public boolean userExists(String name) {
        boolean exists = false;
        for(User user : this.participants) {
            if(name.equals(user.getName())) {
                exists = true;
            }
        }
        return exists;
    }

    public User getUser(String name) {
        User user = null;
        for(User aux : this.participants) {
            if(name.equals(aux.getName())) {
                user = aux;
            }
        }
        return user;
    }

    public boolean availableTickets() {
        return this.initial_tickets.get() > 0;
    }

    public boolean checkIngress(String secret) {
        return this.initial_secrets.contains(secret);
    }

}
