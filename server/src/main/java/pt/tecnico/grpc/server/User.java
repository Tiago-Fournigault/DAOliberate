package pt.tecnico.grpc.server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.time.LocalDateTime;

import pt.tecnico.grpc.security.*;

/**
 * The User class is responsible for storing information of a user.
 */
public class User {

	private String name;
    private String secure_password;
    private byte[] salt;
    private Reputation reputation;
    private LocalDateTime punish_time;
    private boolean marked;

    public User(String name, String password, int experimental_interactions) {

        this.name = name;
        this.salt = LibSecurity.getSalt();
        this.secure_password = LibSecurity.getSecurePassword(password, this.salt);
        this.reputation = new Reputation(experimental_interactions);
        this.punish_time = null;
        this.marked = false;
    }

    public String getName() {
        return this.name;
    }

    public byte[] getSalt() {
        return this.salt;
    }

    public String getSecurePassword() {
        return this.secure_password;
    }

    public double getReputation() {
        return this.reputation.getReputation();
    }

    public void setExperimentalInteractions(int experimental_interactions) {
        this.reputation.setExperimentalInteractions(experimental_interactions);
    }

    public void incrInteractions() {
        this.reputation.incrInteractions();
    }

    public void setReputation(Map<User, Boolean> votes, int n_participants) {
        this.reputation.setReputation(votes, n_participants);
    }

    public void changeReputation(double reputation) {
        this.reputation.changeReputation(reputation);
    }

    public void punish(int days, int hours, int minutes) {
        this.punish_time = LocalDateTime.now().plusDays(days).plusHours(hours).plusMinutes(minutes);
    }

    public boolean isPunished() {
        boolean response = false;
        if(this.punish_time != null) {
            response = LocalDateTime.now().isBefore(this.punish_time);
        }
        return response;
    }

    public void markToDelete() {
        this.marked = true;
    }

    public void unmarkToDelete() {
        this.marked = false;
    }

    public boolean isMarked() {
        return this.marked;
    }
}
