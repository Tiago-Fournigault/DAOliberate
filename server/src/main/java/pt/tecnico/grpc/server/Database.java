package pt.tecnico.grpc.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Base64;
import java.io.FileInputStream;

import pt.tecnico.grpc.Daoliberate;
import pt.tecnico.grpc.security.*;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;

/**
 * The Database class is responsible for storing all information about existing chat rooms.
 */
public class Database {

    private AtomicInteger room_id_counter = new AtomicInteger(0);    //room id
    private AtomicInteger invite_id_counter = new AtomicInteger(0);    //invite id
    private ConcurrentHashMap<Integer, ChatRoom> room_map = new ConcurrentHashMap<>();
    private HashMap<Integer, ArrayList<String>> invites = new HashMap<>();

    public int getRoomIdCounter() {
        return this.room_id_counter.getAndIncrement();
    }

    public ChatRoom getRoom(int room_id){
        return this.room_map.get(room_id);
    }

    public int getInviteIdCounter() {
        return this.invite_id_counter.getAndIncrement();
    }

    public int getExperimentalInteractions(int room_id) {
        return getRoom(room_id).getExperimentalInteractions();
    }

    public int createRoom(String room_name, ArrayList<String> initial_secrets) {
        int room_id = getRoomIdCounter();
        ChatRoom chat_room = new ChatRoom(room_id, room_name, initial_secrets);
        this.room_map.put(room_id, chat_room);
        return room_id;
    }

    public boolean roomExists(int room_id) {
        return this.room_map.containsKey(room_id);
    }

    public boolean nameExists(int room_id, String name) {
        return getRoom(room_id).userExists(name);
    }

    public void addUserToRoom(String pseudonym, String password, int room_id) {
        getRoom(room_id).addParticipant(new User(pseudonym, password, getRoom(room_id).getExperimentalInteractions()));
    }

    public int numberParticipantsRoom(int room_id) {
        return getRoom(room_id).getNumberParticipants();
    }

    public User getUser(int room_id, String name) {
        return getRoom(room_id).getUser(name);
    }

    public boolean checkLogin(String name, String password, int room_id) {
        User user = getUser(room_id, name);
        if(user == null) {
            return false;
        }
        String secure_password = LibSecurity.getSecurePassword(password, user.getSalt());
        return secure_password.equals(user.getSecurePassword());
    }
    
    public void createChannel(int room_id, String channel_name) {
        getRoom(room_id).createChannel(channel_name);
    }

    public void deleteChannel(int room_id, String channel_name) {
        getRoom(room_id).deleteChannel(channel_name);
    }

    public void deleteMessage(int room_id, String channel_name, int message_id) {
        getRoom(room_id).deleteMessage(channel_name, message_id);
    }

    public void pinMessage(int room_id, String channel_name, int message_id) {
        getRoom(room_id).pinMessage(channel_name, message_id);
    }

    public void unpinMessage(int room_id, String channel_name, int message_id) {
        getRoom(room_id).unpinMessage(channel_name, message_id);
    }

    public boolean isPunishedUser(int room_id, String name) {
        return getRoom(room_id).isPunishedUser(name);
    }

    public boolean deletedUser(String name, int room_id) {
        return getRoom(room_id).deletedUser(name);
    }

    public void editMessage(int room_id, String channel_name, int message_id, String editor_name, String new_content) {
        getRoom(room_id).editMessage(channel_name, message_id, editor_name, new_content);
    }

    public Thresholds getThresholds(int room_id) {
        return getRoom(room_id).getThresholds();
    }

    public double getReputation(int room_id, String pseudonym) {
        return getRoom(room_id).getReputation(pseudonym);
    }

    public void changeThreshold(int room_id, Daoliberate.ACTION action, double threshold) {
        getRoom(room_id).changeThreshold(action, threshold);
    }

    public void changeExperimentalInteractions(int room_id, int experimental_interactions) {
        getRoom(room_id).setExperimentalInteractions(experimental_interactions);
    }

    public void changeReputation(int room_id, String pseudonym, double reputation) {
        getRoom(room_id).changeReputation(pseudonym, reputation);
    }

    public int createVote(int room_id, Vote vote) {
        return getRoom(room_id).createVote(vote);
    }

    public int createEqualVote(int room_id, EqualVote vote) {
        return getRoom(room_id).createEqualVote(vote);
    }

    public void logOnHistoryCreateChannel(int room_id, String channel_name, String pseudonym) {
        getRoom(room_id).logOnHistoryCreateChannel(channel_name, pseudonym);
    }

    public void logOnHistoryDeleteChannel(int room_id, String channel_name, String pseudonym) {
        getRoom(room_id).logOnHistoryDeleteChannel(channel_name, pseudonym);
    }

    public void logOnHistoryDeleteMessage(int room_id, String channel_name, String pseudonym, int message_id, Daoliberate.REASON reason) {
        getRoom(room_id).logOnHistoryDeleteMessage(channel_name, pseudonym, message_id, reason);
    }

    public void logOnHistoryPinMessage(int room_id, String channel_name, String pseudonym, int message_id) {
        getRoom(room_id).logOnHistoryPinMessage(channel_name, pseudonym, message_id);
    }

    public void logOnHistoryUnpinMessage(int room_id, String channel_name, String pseudonym, int message_id) {
        getRoom(room_id).logOnHistoryUnpinMessage(channel_name, pseudonym, message_id);
    }

    public void logOnHistoryEditMessage(int room_id, String channel_name, String pseudonym, int message_id, String new_content, Daoliberate.REASON reason) {
        getRoom(room_id).logOnHistoryEditMessage(channel_name, pseudonym, message_id, new_content, reason);
    }

    public void logOnHistoryInviteUser(int room_id, String invited_name, String inviter_name, String response) {
        getRoom(room_id).logOnHistoryInviteUser(invited_name, inviter_name, response);
    }

    public void logOnHistoryChangeThreshold(int room_id, Daoliberate.ACTION action_changed, double threshold) {
        getRoom(room_id).logOnHistoryChangeThreshold(action_changed, threshold);
    }

    public void logOnHistoryChangeExperimentalInteractions(int room_id, int experimental_interactions) {
        getRoom(room_id).logOnHistoryChangeExperimentalInteractions(experimental_interactions);
    }

    public void logOnHistoryChangeReputation(int room_id, String user_name, double reputation) {
        getRoom(room_id).logOnHistoryChangeReputation(user_name, reputation);
    }

    public List<String> getActionsOfRoom(int room_id) {
        return getRoom(room_id).getActions();
    }

    public boolean channelInVote(int room_id, String channel_name) {
        return getRoom(room_id).channelInVote(channel_name);
    }

    public boolean messageInVote(int room_id, String channel_name, int message_id) {
        return getRoom(room_id).messageInVote(channel_name, message_id);
    }

    public boolean userInVote(int room_id, String name) {
        return getRoom(room_id).userInVote(name);
    }

    public boolean thresholdInVote(int room_id, Daoliberate.ACTION action) {
        return getRoom(room_id).thresholdInVote(action);
    }

    public boolean experimentalInteractionsInVote(int room_id) {
        return getRoom(room_id).experimentalInteractionsInVote();
    }

    public boolean reputationInVote(int room_id, String pseudonym) {
        return getRoom(room_id).reputationInVote(pseudonym);
    }

    public List<Vote> getVotesOfRoom(int room_id) {
        return getRoom(room_id).getVotes();
    }

    public List<EqualVote> getEqualVotesOfRoom(int room_id) {
        return getRoom(room_id).getEqualVotes();
    }

    public boolean voteExists(int room_id, int vote_id) {
        return getRoom(room_id).voteExists(vote_id);
    }

    public String getVoteInitiatorName(int room_id, int vote_id) {
        return getRoom(room_id).getVoteInitiatorName(vote_id);
    }

    public void editVote(int room_id, int vote_id, String description) {
        getRoom(room_id).editVote(vote_id, description);
    }

    public void cancelVote(int room_id, int vote_id) {
        getRoom(room_id).cancelVote(vote_id);
    }

    public boolean eligibleVoter(int room_id, int vote_id, String pseudonym) {
        return getRoom(room_id).eligibleVoter(vote_id, pseudonym);
    }

    public void vote(int room_id, int vote_id, String pseudonym, boolean vote) {
        getRoom(room_id).vote(vote_id, pseudonym, vote);
    }
    
    public boolean channelExists(int room_id, String channel_name) {
        return getRoom(room_id).channelExists(channel_name);
    }

    public boolean messageExists(int room_id, String channel_name, int message_id) {
        return getRoom(room_id).messageExists(channel_name, message_id);
    }

    public boolean messagePinned(int room_id, String channel_name, int message_id) {
        return getRoom(room_id).messagePinned(channel_name, message_id);
    }

    public String getMessageOwnerName(int room_id, String channel_name, int message_id) {
        return getRoom(room_id).getMessageOwnerName(channel_name, message_id);
    }

    public List<String> getChannelsOfRoom(int room_id) {
        return getRoom(room_id).getChannels();
    }

    public List<Message> getMessagesOfRoom(int room_id, String channel_name) {
        return getRoom(room_id).getMessages(channel_name);
    }

    public List<Message> getPinnedMessagesOfChannel(int room_id, String channel_name) {
        return getRoom(room_id).getPinnedMessages(channel_name);
    }

    public void addMessageToChat(int room_id, String channel_name, String message_owner, String message_content) {
        getRoom(room_id).addMessage(channel_name, message_owner, message_content);
    }

    public boolean availableTickets(int room_id) {
        return getRoom(room_id).availableTickets();
    }

    public boolean useInvite(int room_id, String invite) {
        boolean available = false;
        if(this.invites.containsKey(room_id)) {
            ArrayList<String> new_invites = this.invites.get(room_id);
            if(new_invites.contains(invite)) {
                available = true;
                new_invites.remove(invite);
            }
        }
        return available;
    }

    public boolean verifyInvite(String invite, String signed_invite) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            PublicKey publicKey = getPublicKeyFromCert();
            sig.initVerify(publicKey);
            sig.update(invite.getBytes());
            return sig.verify(Base64.getDecoder().decode(signed_invite));
        }
        catch (java.security.NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
        catch (java.security.InvalidKeyException e) {
            System.out.println(e.getMessage());
        }
        catch (java.security.SignatureException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public PublicKey getPublicKeyFromCert() {
        try{
            FileInputStream fin = new FileInputStream("../cert/daoliberate-cert.pem");
            CertificateFactory f = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate)f.generateCertificate(fin);
            return certificate.getPublicKey();
        }
        catch (java.io.FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        catch (java.security.cert.CertificateException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean checkIngress(int room_id, String secret) {
        return getRoom(room_id).checkIngress(secret);
    }

    public boolean useIngress(int room_id, String secret, String name, String password) {
        boolean ingress = false;
        if(checkIngress(room_id, secret)) {
            User user = new User(name, password, getRoom(room_id).getExperimentalInteractions());
            ingress = getRoom(room_id).useTicket(user, secret);
        }
        return ingress;
    }

    public String getRoomNameById(int room_id) {
        return getRoom(room_id).getName();
    }

    public void saveInvite(String invite, int room_id) {
        if(this.invites.containsKey(room_id)) {
            ArrayList<String> new_invites = this.invites.get(room_id);
            new_invites.add(invite);
        }
        else {
            ArrayList<String> new_invites = new ArrayList<String>();
            new_invites.add(invite);
            this.invites.put(room_id, new_invites);
        }
        System.out.println(this.invites);
    }
}
