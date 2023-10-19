package pt.tecnico.grpc.server;

import java.util.Map;


/**
 * The Reputation class stores the reputation of a user
 * and contains methods to manipulate the reputation.
 */
public class Reputation {

	private double reputation;
    private int interactions;
    private int experimental_interactions;

    public Reputation(int experimental_interactions) {
        this.reputation = 1;
        this.interactions = 0;
        this.experimental_interactions = experimental_interactions;
    }

    public void setExperimentalInteractions(int experimental_interactions) {
        this.experimental_interactions = experimental_interactions;
    }

    public double getReputation() {
        return this.reputation;
    }

    public void changeReputation(double reputation) {
        this.reputation = reputation;
    }

    public void setReputation(Map<User, Boolean> votes, int n_participants) {
        double acceptance_degree = 0.0;
        double reputation_total = 0.0;
        double reputation_gained = 0.0;

        if(this.reputation >= n_participants - 2) {
            return;
        }

        for(Map.Entry<User, Boolean> entry : votes.entrySet()) {
            acceptance_degree += entry.getKey().getReputation() * (entry.getValue() ? 1 : -1);
            reputation_total += entry.getKey().getReputation();
        }

        reputation_gained = getCoefficient() * (acceptance_degree / reputation_total);
        if(this.reputation + reputation_gained >= n_participants - 2) {
            this.reputation = n_participants - 2;
        }
        else {
            this.reputation += reputation_gained;
        }
    }

    public double getCoefficient() {
        double coefficient = 0.0;
        if(this.interactions >= this.experimental_interactions) {
            coefficient = 1.0;
        }
        else {
            coefficient = this.interactions / (double) this.experimental_interactions;
        }
        System.out.println("Coefficient: " + coefficient);
        return coefficient;
    }

    public void incrInteractions() {
        this.interactions++;
    }
}
