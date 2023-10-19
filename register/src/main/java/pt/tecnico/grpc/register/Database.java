package pt.tecnico.grpc.register;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.util.Random;
import pt.tecnico.grpc.security.*;
import javax.net.ssl.SSLSession;
import java.security.PublicKey;
import java.security.Signature;

/**
 * The Database class is responsible for storing registered users
 * as well as available invitations to join chat rooms.
 */
public class Database {

    private AtomicInteger invite_id_counter = new AtomicInteger(0);    //invite counter
	private ConcurrentHashMap<String, User> user_map = new ConcurrentHashMap<>();
    private HashMap<Integer, ArrayList<Invite>> invites = new HashMap<>();

    private AuthorizationInterceptor interceptor;

    public Database(AuthorizationInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public boolean checkNameExists(String name) {
        return this.user_map.containsKey(name);
    }

    public boolean checkPasswordFormat(String password) {
        if(password.length() >= 10) {

            boolean contains_digit = false;
            boolean contains_capital = false;
            boolean contains_small = false;
            boolean invalid_char = false;
            char ch;

            for(int i = 0; i < password.length(); i++) {
                ch = password.charAt(i);

                if('a' <= ch && ch <= 'z') {
                    contains_small = true;
                }
                else if('A' <= ch && ch <= 'Z') {
                    contains_capital = true;
                }
                else if('0' <= ch && ch <= '9') {
                    contains_digit = true;
                }
                else {
                    invalid_char = true;
                }
            }

            if(invalid_char || (!contains_capital) || (!contains_digit) || (!contains_small)) {
                return false;
            }
            else {
                return true;
            }
        }

        return false;
    }

    public boolean addUser(String name, String password) {
        User user = new User(name, password);
        if(this.user_map.putIfAbsent(name, user) == null) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean checkCounterInvite(String invite) {
        boolean check = false;
        if(Integer.parseInt(invite.split(":")[0]) >= this.invite_id_counter.get()) {
            this.invite_id_counter.incrementAndGet();
            check = true;
        }
        return check;
    }

    public void addInviteToRoom(String invite, String signed_invite, int room_id) {
        if(this.invites.containsKey(room_id)) {
            ArrayList<Invite> new_invites = this.invites.get(room_id);
            new_invites.add(new Invite(invite, signed_invite));
        }
        else {
            ArrayList<Invite> new_invites = new ArrayList<Invite>();
            new_invites.add(new Invite(invite, signed_invite));
            this.invites.put(room_id, new_invites);
        }
    }

    public boolean hasInviteToRoom(String name, int room_id) {
        return this.user_map.get(name).hasInviteToRoom(room_id);
    }

    public boolean hasPreInviteToRoom(String name, int room_id) {
        return this.user_map.get(name).hasPreInviteToRoom(room_id);
    }

    public boolean hasSecretToRoom(String name, int room_id) {
        return this.user_map.get(name).hasSecretToRoom(room_id);
    }

    public boolean hasRegisteredRoom(String name, int room_id) {
        return this.user_map.get(name).hasRegisteredRoom(room_id);
    }

    public void addInviteToUser(String name, int room_id) {
        this.user_map.get(name).addInvite(room_id);
    }

    public void addPreInviteToUser(String name, int room_id, String room_name) {
        this.user_map.get(name).addPreInvite(new Room(room_id, room_name));
    }

    public void addInitialRoomSecret(String user_name, int room_id, String room_name, String initial_secret) {
        this.user_map.get(user_name).addInitialSecret(new Room(room_id, room_name, initial_secret));
    }

    public void addRegisteredRoom(String name, int room_id) {
        this.user_map.get(name).addRegisteredRoom(room_id);
    }

    public List<Room> getUserInvites(String user_name) {
        return this.user_map.get(user_name).getInvites();
    }

    public List<Room> getUserSecrets(String user_name) {
        return this.user_map.get(user_name).getSecrets();
    }

    public Invite getRandomInvite(int room_id, String name) {
        ArrayList<Invite> invites = this.invites.get(room_id);
        Random random = new Random();
        int index = random.nextInt(invites.size());
        Invite invite = invites.get(index);
        invites.remove(index);
        this.user_map.get(name).removeInvite(room_id);
        return invite;
    }

    public String getInitialSecret(int room_id, String name) {
        return this.user_map.get(name).getInitialSecret(room_id);
    }

    public boolean checkLogin(String name, String password) {
        User user = this.user_map.get(name);
        String secure_password = LibSecurity.getSecurePassword(password, user.getSalt());
        return secure_password.equals(user.getSecurePassword());
    }

    public boolean validateInvite(String invite, String signed_invite) {
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

    private static PublicKey getPublicKeyFromCert() {
        try{
            SSLSession sslSession = AuthorizationInterceptor.SSL_SESSION_CONTEXT.get();
            return sslSession.getPeerCertificates()[0].getPublicKey();
        } catch (javax.net.ssl.SSLPeerUnverifiedException e){
            System.out.println(e.getMessage());
            return null;
        }
    }
    
}
