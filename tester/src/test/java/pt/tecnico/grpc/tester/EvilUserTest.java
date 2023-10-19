package pt.tecnico.grpc.tester;

import pt.tecnico.grpc.client.ClientLibrary;
import pt.tecnico.grpc.client.Invite;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.*;
import java.util.List;

import pt.tecnico.grpc.Daoliberate;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EvilUserTest {

    class User {
        public String name;
        public String password;
        public String pseudonym;

        public User(String name, String password, String pseudonym) {
            this.name = name;
            this.password = password;
            this.pseudonym = pseudonym;
        }
    }

    private ClientLibrary frontend;
    private int MAX_USERS = 10;
    private User[] users = new User[MAX_USERS];

    private long signup_time = 0;
    private long signup_iter = 0;
    private long signup_start_time = 0;
    private long signup_end_time = 0;

    private long createroom_time = 0;
    private long createroom_iter = 0;
    private long createroom_start_time = 0;
    private long createroom_end_time = 0;

    private long ingressroom_time = 0;
    private long ingressroom_iter = 0;
    private long ingressroom_start_time = 0;
    private long ingressroom_end_time = 0;

    private long vote_time = 0;
    private long vote_iter = 0;
    private long vote_start_time = 0;
    private long vote_end_time = 0;

    private long initiate_voting_time = 0;
    private long initiate_voting_iter = 0;
    private long initiate_voting_start_time = 0;
    private long initiate_voting_end_time = 0;

    private long consult_history_time = 0;
    private long consult_history_iter = 0;
    private long consult_history_start_time = 0;
    private long consult_history_end_time = 0;

    private long create_message_time = 0;
    private long create_message_iter = 0;
    private long create_message_start_time = 0;
    private long create_message_end_time = 0;

    private long invite_time = 0;
    private long invite_iter = 0;
    private long invite_start_time = 0;
    private long invite_end_time = 0;

    private long enter_room_time = 0;
    private long enter_room_iter = 0;
    private long enter_room_start_time = 0;
    private long enter_room_end_time = 0;

    private long enter_channel_time = 0;
    private long enter_channel_iter = 0;
    private long enter_channel_start_time = 0;
    private long enter_channel_end_time = 0;

    private long ingress_via_invite_time = 0;
    private long ingress_via_invite_iter = 0;
    private long ingress_via_invite_start_time = 0;
    private long ingress_via_invite_end_time = 0;

    private long check_invites_time = 0;
    private long check_invites_iter = 0;
    private long check_invites_start_time = 0;
    private long check_invites_end_time = 0;

    private long check_secrets_time = 0;
    private long check_secrets_iter = 0;
    private long check_secrets_start_time = 0;
    private long check_secrets_end_time = 0;

    public void startTime(String action) {
        switch(action) {
            case "signup":
                this.signup_start_time = System.currentTimeMillis();
                break;
            case "create_room":
                this.createroom_start_time = System.currentTimeMillis();
                break;
            case "ingress_room":
                this.ingressroom_start_time = System.currentTimeMillis();
                break;
            case "vote":
                this.vote_start_time = System.currentTimeMillis();
                break;
            case "initiate_voting":
                this.initiate_voting_start_time = System.currentTimeMillis();
                break;
            case "consult_history":
                this.consult_history_start_time = System.currentTimeMillis();
                break;
            case "create_message":
                this.create_message_start_time = System.currentTimeMillis();
                break;
            case "invite":
                this.invite_start_time = System.currentTimeMillis();
                break;
            case "enter_room":
                this.enter_room_start_time = System.currentTimeMillis();
                break;
            case "enter_channel":
                this.enter_channel_start_time = System.currentTimeMillis();
                break;
            case "ingress_via_invite":
                this.ingress_via_invite_start_time = System.currentTimeMillis();
                break;
            case "check_invites":
                this.check_invites_start_time = System.currentTimeMillis();
                break;
            case "check_secrets":
                this.check_secrets_start_time = System.currentTimeMillis();
                break;
        }
    }

    public void endTime(String action) {
        switch(action) {
            case "signup":
                this.signup_end_time = System.currentTimeMillis();
                //System.err.println("Time to signup: " + (this.signup_end_time - this.signup_start_time));
                this.signup_time += this.signup_end_time - this.signup_start_time;
                this.signup_iter++;
                break;
            case "create_room":
                this.createroom_end_time = System.currentTimeMillis();
                //System.err.println("Time to create room: " + (this.createroom_end_time - this.createroom_start_time));
                this.createroom_time += this.createroom_end_time - this.createroom_start_time;
                this.createroom_iter++;
                break;
            case "ingress_room":
                this.ingressroom_end_time = System.currentTimeMillis();
                //System.err.println("Time to ingress in room: " + (this.ingressroom_end_time - this.ingressroom_start_time));
                this.ingressroom_time += this.ingressroom_end_time - this.ingressroom_start_time;
                this.ingressroom_iter++;
                break;
            case "vote":
                this.vote_end_time = System.currentTimeMillis();
                //System.err.println("Time to vote: " + (this.vote_end_time - this.vote_start_time));
                this.vote_time += this.vote_end_time - this.vote_start_time;
                this.vote_iter++;
                break;
            case "initiate_voting":
                this.initiate_voting_end_time = System.currentTimeMillis();
                //System.err.println("Time to initiate voting: " + (this.initiate_voting_end_time - this.initiate_voting_start_time));
                this.initiate_voting_time += this.initiate_voting_end_time - this.initiate_voting_start_time;
                this.initiate_voting_iter++;
                break;
            case "consult_history":
                this.consult_history_end_time = System.currentTimeMillis();
                //System.err.println("Time to consult history: " + (this.consult_history_end_time - this.consult_history_start_time));
                this.consult_history_time += this.consult_history_end_time - this.consult_history_start_time;
                this.consult_history_iter++;
                break;
            case "create_message":
                this.create_message_end_time = System.currentTimeMillis();
                //System.err.println("Time to create message: " + (this.create_message_end_time - this.create_message_start_time));
                this.create_message_time += this.create_message_end_time - this.create_message_start_time;
                this.create_message_iter++;
                break;
            case "invite":
                this.invite_end_time = System.currentTimeMillis();
                //System.err.println("Time to invite: " + (this.invite_end_time - this.invite_start_time));
                this.invite_time += this.invite_end_time - this.invite_start_time;
                this.invite_iter++;
                break;
            case "enter_room":
                this.enter_room_end_time = System.currentTimeMillis();
                //System.err.println("Time to enter room: " + (this.enter_room_end_time - this.enter_room_start_time));
                this.enter_room_time += this.enter_room_end_time - this.enter_room_start_time;
                this.enter_room_iter++;
                break;
            case "enter_channel":
                this.enter_channel_end_time = System.currentTimeMillis();
                //System.err.println("Time to enter channel: " + (this.enter_channel_end_time - this.enter_channel_start_time));
                this.enter_channel_time += this.enter_channel_end_time - this.enter_channel_start_time;
                this.enter_channel_iter++;
                break;
            case "ingress_via_invite":
                this.ingress_via_invite_end_time = System.currentTimeMillis();
                //System.err.println("Time to enter channel: " + (this.ingress_via_invite_end_time - this.ingress_via_invite_start_time));
                this.ingress_via_invite_time += this.ingress_via_invite_end_time - this.ingress_via_invite_start_time;
                this.ingress_via_invite_iter++;
                break;
            case "check_invites":
                this.check_invites_end_time = System.currentTimeMillis();
                //System.err.println("Time to check invites: " + (this.check_invites_end_time - this.check_invites_start_time));
                this.check_invites_time += this.check_invites_end_time - this.check_invites_start_time;
                this.check_invites_iter++;
                break;
            case "check_secrets":
                this.check_secrets_end_time = System.currentTimeMillis();
                //System.err.println("Time to check secrets: " + (this.check_secrets_end_time - this.check_secrets_start_time));
                this.check_secrets_time += this.check_secrets_end_time - this.check_secrets_start_time;
                this.check_secrets_iter++;
                break;
        }
    }

    public void printTime(String action) {
        switch(action) {
            case "signup":
                System.err.println("Avg time to signup: " + (this.signup_time/this.signup_iter));
                break;
            case "create_room":
                System.err.println("Avg time to createroom: " + (this.createroom_time/this.createroom_iter) + " (nº of rooms created: " + this.createroom_iter + ")");
                break;
            case "ingress_room":
                System.err.println("Avg time to ingress in room: " + (this.ingressroom_time/this.ingressroom_iter) + " (nº of ingresses: " + this.ingressroom_iter + ")");
                break;
            case "vote":
                System.err.println("Avg time to vote: " + (this.vote_time/this.vote_iter) + " (nº of votes: " + this.vote_iter + ")");
                break;
            case "initiate_voting":
                System.err.println("Avg time to initiate voting: " + (this.initiate_voting_time/this.initiate_voting_iter) + " (nº of votings initiated: " + this.initiate_voting_iter + ")");
                break;
            case "consult_history":
                System.err.println("Avg time to consult history: " + (this.consult_history_time/this.consult_history_iter) + " (nº of history consults: " + this.consult_history_iter + ")");
                break;
            case "create_message":
                System.err.println("Avg time to create message: " + (this.create_message_time/this.create_message_iter) + " (nº of messages created: " + this.create_message_iter + ")");
                break;
            case "invite":
                System.err.println("Avg time to invite: " + (this.invite_time/this.invite_iter) + " (nº of invites: " + this.invite_iter + ")");
                break;
            case "enter_room":
                System.err.println("Avg time to enter room: " + (this.enter_room_time/this.enter_room_iter) + " (nº of entries: " + this.enter_room_iter + ")");
                break;
            case "enter_channel":
                System.err.println("Avg time to enter channel: " + (this.enter_channel_time/this.enter_channel_iter) + " (nº of entries: " + this.enter_channel_iter + ")");
                break;
            case "ingress_via_invite":
                System.err.println("Avg time to ingress via invite: " + (this.ingress_via_invite_time/this.ingress_via_invite_iter) + " (nº of ingresses: " + this.ingress_via_invite_iter + ")");
                break;
            case "check_invites":
                System.err.println("Avg time to check invites: " + (this.check_invites_time/this.check_invites_iter) + " (nº of checks: " + this.check_invites_iter + ")");
                break;
             case "check_secrets":
                System.err.println("Avg time to check secrets: " + (this.check_secrets_time/this.check_secrets_iter) + " (nº of checks: " + this.check_secrets_iter + ")");
                break;
        }
    }

    @BeforeAll
	public void registerUsers(){
        for(int i = 0; i < MAX_USERS; i++) {
            users[i] = new User("evilITuser" + i, "PasswordOf" + i, "Anony" + i);
        }

        try {
            frontend = new ClientLibrary("localhost", 8080, "localhost", 8081);
            assertEquals("Chat server is up!", frontend.pingDao());
            assertEquals("Register server is up!", frontend.pingRegister());
            for(User usr : users) {
                assertEquals(true, frontend.signup(usr.name, usr.password));
                frontend.getAccountInfo(usr.name, usr.password);
            }
        }
        catch(Exception e) {
            fail();
        }
	}

    @AfterAll
	public void deregisterUsers(){
        try {
            for(User usr : users) {
                File file_user = new File(usr.name);
                File file_user_info = new File(usr.name + "-info");
                file_user.delete();
                file_user_info.delete();
            }
        }
        catch(Exception e) {
            fail();
        }
	}

    @Test
  	public void RoomOfN() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            System.setOut(printStream);

            for(int N = 2, id = 0; N <= MAX_USERS; N++) {
                for(int round = 0; round < 2; round++, id++) {
                    int vote_id = 0;
                    ByteArrayInputStream bais = new ByteArrayInputStream(users[0].pseudonym.getBytes());
                    System.setIn(bais);
                    System.err.println("-->N=" + N + " id=" + id + "<--");

                    //= User0 create a room and add User1, User2, ..., UserN (room id = 1) =//
                    // User0 create room
                    frontend.getAccountInfo(users[0].name, users[0].password);
                    String[] inv_users = new String[N-1];
                    for(int i = 0; i < N-1; i++) {
                        inv_users[i] = users[i+1].name;
                    }
                    frontend.createRoom(users[0].name, users[0].password, "RoomOf" + N, inv_users);
                    String[] lines = baos.toString().split("\n");
                    String result = lines[lines.length-1];
                    String expected = "Insert the pseudonym to be used in this room: Successfully joined the chat room.";
                    assertEquals(expected, result);
                    //User[1-N] join roon
                    for(int i = 1; i < N; i++) {
                        frontend.getAccountInfo(users[i].name, users[i].password);
                        String secret = frontend.getInitialSecret(users[i].name, users[i].password, id);
                        assertEquals(Daoliberate.STATUS.OK, frontend.ingressRoom(id, users[i].pseudonym, secret));
                    }
                    
                    /*if(N == 1) {
                        id++;
                        for(int j = 0; j < 1; j++) {
                            frontend.getAccountInfo(users[0].name, users[0].password);
                            bais = new ByteArrayInputStream(users[0].pseudonym.getBytes());
                            System.setIn(bais);
                            String[] names = new String[MAX_USERS-1];
                            for(int i = 1; i < MAX_USERS; i++) {
                                names[i-1] = users[i].name;
                            }
                            frontend.createRoom(users[0].name, users[0].password, "RoomOf" + N + "-" + j, names);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Insert the pseudonym to be used in this room: Successfully joined the chat room.";
                            assertEquals(expected, result);
                            //for(int i = 1; i < MAX_USERS; i++) {
                            //    frontend.getAccountInfo(users[0].name, users[0].password);
                            //    frontend.inviteUser(id, users[i].name);
                            //    List<String> lines2 = frontend.viewHistory(id);
                            //    expected = "User invited (with name: " + users[i].name + ")";
                            //    assertTrue(lines2.get(lines2.size()-1).contains(expected));
                            //}
                            for(int i = 1; i < MAX_USERS; i++) {
                                frontend.getAccountInfo(users[i].name, users[i].password);
                                startTime("check_secrets");
                                //List<String> invites = frontend.checkInvites(users[i].name, users[i].password);
                                List<String> secrets = frontend.checkSecrets(users[i].name, users[i].password);
                                assertTrue(secrets.get(secrets.size()-1).contains("Initial secret to room \"RoomOf" + N + "-" + j + "\""));
                                endTime("check_secrets");
                                //printTime("check_secrets");
                            }
                            id++;
                        }
                        printTime("check_secrets");
                    }*/
                    
                    if(round == 1 && N > 3) {
                        //= User0 changes its reputation to the maximum value =//
                        frontend.getAccountInfo(users[0].name, users[0].password);
                        String details = String.join(",", frontend.enterRoom(id));

                        // Until User0 has maximum reputation he creates voting processes which are accepted
                        while(!details.contains("| Reputation: " + (N-2.0))) {
                            // User0 create a new channel
                            frontend.createChannel(id, "channel");
                            List<String> lines2 = frontend.viewHistory(id);
                            expected = "Channel created with name \"channel\" through a voting process";
                            for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                // Until action accepted, User[1-N] accept the action through voting
                                frontend.getAccountInfo(users[i].name, users[i].password);
                                frontend.vote(id, vote_id, true);
                                lines = baos.toString().split("\n");
                                result = lines[lines.length-1];
                                expected = "Vote submitted successfully.";
                                assertEquals(expected, result);
                                lines2 = frontend.viewHistory(id);
                                expected = "Channel created with name \"channel\" through a voting process";
                            }
                            vote_id++;

                            // User0 delete a channel
                            frontend.getAccountInfo(users[0].name, users[0].password);
                            frontend.deleteChannel(id, "channel");
                            lines2 = frontend.viewHistory(id);
                            expected = "Channel deleted with name \"channel\" through a voting process";
                            for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                // Until action accepted, User[1-N] accept the action through voting
                                frontend.getAccountInfo(users[i].name, users[i].password);
                                frontend.vote(id, vote_id, true);
                                lines = baos.toString().split("\n");
                                result = lines[lines.length-1];
                                expected = "Vote submitted successfully.";
                                assertEquals(expected, result);
                                lines2 = frontend.viewHistory(id);
                                expected = "Channel deleted with name \"channel\" through a voting process";
                            }
                            vote_id++;
                            details = String.join(",", frontend.enterRoom(id));
                        }
                    }
                    
                    //= User0 try to delete User1 =//
                    // User0 initiate a voting process to delete User1
                    frontend.getAccountInfo(users[0].name, users[0].password);
                    frontend.deleteUser(id, users[1].pseudonym);
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-2];
                    expected = "Deletion of user \"" + users[1].pseudonym + "\" submitted for voting (voting process with id " + vote_id + ").";
                    assertEquals(expected, result);
                    // Verify that action was denied
                    List<String> lines2 = frontend.viewHistory(id);
                    expected = "Attempt to delete a user (with name: " + users[1].pseudonym + ") from room declined through a voting process";
                    for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                        // Until action denied, User[1-N] deny the action through voting
                        frontend.getAccountInfo(users[i].name, users[i].password);
                        frontend.vote(id, vote_id, false);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "Attempt to delete a user (with name: " + users[1].pseudonym + ") from room declined through a voting process";
                    }
                    vote_id++;
                    assertTrue(lines2.get(lines2.size()-1).contains(expected));

                    if(round == 1 && N > 3) {
                        //= User0 changes its reputation to the maximum value =//
                        frontend.getAccountInfo(users[0].name, users[0].password);
                        String details = String.join(",", frontend.enterRoom(id));

                        // Until User0 has maximum reputation he creates voting processes which are accepted
                        while(!details.contains("| Reputation: " + (N-2.0))) {
                            // User0 create a new channel
                            frontend.createChannel(id, "channel");
                            lines2 = frontend.viewHistory(id);
                            expected = "Channel created with name \"channel\" through a voting process";
                            for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                // Until action accepted, User[1-N] accept the action through voting
                                frontend.getAccountInfo(users[i].name, users[i].password);
                                frontend.vote(id, vote_id, true);
                                lines = baos.toString().split("\n");
                                result = lines[lines.length-1];
                                expected = "Vote submitted successfully.";
                                assertEquals(expected, result);
                                lines2 = frontend.viewHistory(id);
                                expected = "Channel created with name \"channel\" through a voting process";
                            }
                            vote_id++;

                            // User0 delete a channel
                            frontend.getAccountInfo(users[0].name, users[0].password);
                            frontend.deleteChannel(id, "channel");
                            lines2 = frontend.viewHistory(id);
                            expected = "Channel deleted with name \"channel\" through a voting process";
                            for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                // Until action accepted, User[1-N] accept the action through voting
                                frontend.getAccountInfo(users[i].name, users[i].password);
                                frontend.vote(id, vote_id, true);
                                lines = baos.toString().split("\n");
                                result = lines[lines.length-1];
                                expected = "Vote submitted successfully.";
                                assertEquals(expected, result);
                                lines2 = frontend.viewHistory(id);
                                expected = "Channel deleted with name \"channel\" through a voting process";
                            }
                            vote_id++;
                            details = String.join(",", frontend.enterRoom(id));
                        }
                    }
                    
                    //= User0 try to punish User1 =//
                    // User0 initiate a voting process to punish User1
                    frontend.getAccountInfo(users[0].name, users[0].password);
                    frontend.punishUser(id, users[1].pseudonym, 1, 0, 0);
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-2];
                    expected = "Punishment of user \"" + users[1].pseudonym + "\" submitted for voting (voting process with id " + vote_id + ").";
                    assertTrue(result.contains(expected));
                    // Verify that action was denied
                    lines2 = frontend.viewHistory(id);
                    expected = "Attempt to punish a user (with name: " + users[1].pseudonym + ") declined through a voting process";
                    for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                        // Until action denied, User[1-N] deny the action through voting
                        frontend.getAccountInfo(users[i].name, users[i].password);
                        frontend.vote(id, vote_id, false);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "Attempt to punish a user (with name: " + users[1].pseudonym + ") declined through a voting process";
                    }
                    vote_id++;
                    assertTrue(lines2.get(lines2.size()-1).contains(expected));

                    if(round == 1 && N > 3) {
                        //= User0 changes its reputation to the maximum value =//
                        frontend.getAccountInfo(users[0].name, users[0].password);
                        String details = String.join(",", frontend.enterRoom(id));

                        // Until User0 has maximum reputation he creates voting processes which are accepted
                        while(!details.contains("| Reputation: " + (N-2.0))) {
                            // User0 create a new channel
                            frontend.createChannel(id, "channel");
                            lines2 = frontend.viewHistory(id);
                            expected = "Channel created with name \"channel\" through a voting process";
                            for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                // Until action accepted, User[1-N] accept the action through voting
                                frontend.getAccountInfo(users[i].name, users[i].password);
                                frontend.vote(id, vote_id, true);
                                lines = baos.toString().split("\n");
                                result = lines[lines.length-1];
                                expected = "Vote submitted successfully.";
                                assertEquals(expected, result);
                                lines2 = frontend.viewHistory(id);
                                expected = "Channel created with name \"channel\" through a voting process";
                            }
                            vote_id++;

                            // User0 delete a channel
                            frontend.getAccountInfo(users[0].name, users[0].password);
                            frontend.deleteChannel(id, "channel");
                            lines2 = frontend.viewHistory(id);
                            expected = "Channel deleted with name \"channel\" through a voting process";
                            for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                // Until action accepted, User[1-N] accept the action through voting
                                frontend.getAccountInfo(users[i].name, users[i].password);
                                frontend.vote(id, vote_id, true);
                                lines = baos.toString().split("\n");
                                result = lines[lines.length-1];
                                expected = "Vote submitted successfully.";
                                assertEquals(expected, result);
                                lines2 = frontend.viewHistory(id);
                                expected = "Channel deleted with name \"channel\" through a voting process";
                            }
                            vote_id++;
                            details = String.join(",", frontend.enterRoom(id));
                        }
                    }

                    //= User0 try to delete message of User1 =//
                    // User1 send a message
                    frontend.getAccountInfo(users[1].name, users[1].password);
                    frontend.createMessage(id, "General", "Hello World!");
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-1];
                    expected = "Message sent successfully.";
                    assertEquals(expected, result);
                    // User0 initiate a voting process to delete message of User1
                    frontend.getAccountInfo(users[0].name, users[0].password);
                    frontend.deleteMessage(id, "General", 0, Daoliberate.REASON.OFFENSIVE);
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-2];
                    expected = "Deletion of message submitted for voting (voting process with id " + vote_id + ").";
                    assertEquals(expected, result);
                    // Verify that action was denied
                    lines2 = frontend.viewHistory(id);
                    expected = "Attempt to delete a message (with id: 0) in channel \"General\" declined through a voting process";
                    for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                        // Until action denied, User[1-N] deny the action through voting
                        frontend.getAccountInfo(users[i].name, users[i].password);
                        frontend.vote(id, vote_id, false);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "Attempt to delete a message (with id: 0) in channel \"General\" declined through a voting process";
                    }
                    vote_id++;
                    assertTrue(lines2.get(lines2.size()-1).contains(expected));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            fail();
        }
  	}
}
