package pt.tecnico.grpc.register;

/**
 * The Invite class is responsible for storing information of an invite.
 */
public class Invite {

	private String invite;
    private String signed_invite;

    public Invite(String invite, String signed_invite) {
        this.invite = invite;
        this.signed_invite = signed_invite;
    }
    
    public String getInvite() {
        return this.invite;
    }

    public String getSignedInvite() {
        return this.signed_invite;
    }
}
