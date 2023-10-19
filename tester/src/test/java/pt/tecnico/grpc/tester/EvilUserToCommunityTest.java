package pt.tecnico.grpc.tester;

import pt.tecnico.grpc.client.ClientLibrary;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import java.io.*;
import java.util.List;

import pt.tecnico.grpc.Daoliberate;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EvilUserToCommunityTest {

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
    private int MAX_USERS = 8;
    private int id = 0;
    private User[] users0 = new User[MAX_USERS];
    private User[] users1 = new User[MAX_USERS];
    private User[] users2 = new User[MAX_USERS];
    private User[] users3 = new User[MAX_USERS];

    @BeforeAll
	public void registerUsers(){
        for(int i = 0; i < MAX_USERS; i++) {
            users0[i] = new User("User0" + i, "PasswordOf" + i, "Anony" + i);
        }
        for(int i = 0; i < MAX_USERS; i++) {
            users1[i] = new User("User1" + i, "PasswordOf" + i, "Anony" + i);
        }
        for(int i = 0; i < MAX_USERS; i++) {
            users2[i] = new User("User2" + i, "PasswordOf" + i, "Anony" + i);
        }
        for(int i = 0; i < MAX_USERS; i++) {
            users3[i] = new User("User3" + i, "PasswordOf" + i, "Anony" + i);
        }

        try {
            frontend = new ClientLibrary("localhost", 8080, "localhost", 8081);
            for(User usr : users0) {
                assertEquals(true, frontend.signup(usr.name, usr.password));
                frontend.getAccountInfo(usr.name, usr.password);
            }
            for(User usr : users1) {
                assertEquals(true, frontend.signup(usr.name, usr.password));
                frontend.getAccountInfo(usr.name, usr.password);
            }
            for(User usr : users2) {
                assertEquals(true, frontend.signup(usr.name, usr.password));
                frontend.getAccountInfo(usr.name, usr.password);
            }
            for(User usr : users3) {
                assertEquals(true, frontend.signup(usr.name, usr.password));
                frontend.getAccountInfo(usr.name, usr.password);
            }

            /*= Evil user register =*/
            assertEquals(true, frontend.signup("Evil", "PasswordOfEvil123"));
        }
        catch(Exception e) {
            fail();
        }
	}

    @AfterAll
	public void deregisterUsers(){
        try {
            for(User usr : users0) {
                File file_user = new File(usr.name);
                File file_user_info = new File(usr.name + "-info");
                file_user.delete();
                file_user_info.delete();
            }
            for(User usr : users1) {
                File file_user = new File(usr.name);
                File file_user_info = new File(usr.name + "-info");
                file_user.delete();
                file_user_info.delete();
            }
            for(User usr : users2) {
                File file_user = new File(usr.name);
                File file_user_info = new File(usr.name + "-info");
                file_user.delete();
                file_user_info.delete();
            }
            for(User usr : users3) {
                File file_user = new File(usr.name);
                File file_user_info = new File(usr.name + "-info");
                file_user.delete();
                file_user_info.delete();
            }

            /*= Evil user deregister =*/
            File file_user = new File("Evil");
            File file_user_info = new File("Evil" + "-info");
            file_user.delete();
            file_user_info.delete();
        }
        catch(Exception e) {
            fail();
        }
	}

    @Test
    @Order(1)
  	public void RoomOfNCreateChannel() {
        System.err.println("========== CREATE CHANNEL ==========");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            System.setOut(printStream);

            for(int N = 3, id = 0; N <= MAX_USERS; N++, id++) {
                int vote_id = 0;
                ByteArrayInputStream bais = new ByteArrayInputStream(users0[0].pseudonym.getBytes());
                System.setIn(bais);
                System.err.println("-->N=" + N + " id=" + id + "<--");

                /*= User0 create a room and add User1, User2, ..., UserN (room id = 1) =*/
                // User0 create room
                frontend.getAccountInfo(users0[0].name, users0[0].password);
                String[] inv_users = new String[N-1];
                for(int i = 0; i < N-1; i++) {
                    inv_users[i] = users0[i+1].name;
                }
                frontend.createRoom(users0[0].name, users0[0].password, "RoomOf" + N, inv_users);
                String[] lines = baos.toString().split("\n");
                String result = lines[lines.length-1];
                String expected = "Insert the pseudonym to be used in this room: Successfully joined the chat room.";
                assertEquals(expected, result);
                //User[1-N] join roon
                for(int i = 1; i < N; i++) {
                    frontend.getAccountInfo(users0[i].name, users0[i].password);
                    String secret = frontend.getInitialSecret(users0[i].name, users0[i].password, id);
                    assertEquals(Daoliberate.STATUS.OK, frontend.ingressRoom(id, users0[i].pseudonym, secret));
                }

                if(N > 3) {
                    /*= User0 changes its reputation to the maximum value =*/
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    String details = String.join(",", frontend.enterRoom(id));

                    // Until User0 has maximum reputation he creates voting processes which are accepted
                    while(!details.contains("| Reputation: " + (N-2.0))) {
                        // User0 create a new channel
                        frontend.createChannel(id, "channel");
                        List<String> lines2 = frontend.viewHistory(id);
                        expected = "Channel created with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
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
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        frontend.deleteChannel(id, "channel");
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel deleted with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Channel deleted with name \"channel\" through a voting process";
                        }
                        vote_id++;
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        details = String.join(",", frontend.enterRoom(id));
                    }
                }

                /*= User0 try to create an unwanted channel =*/
                // User0 initiate a voting process to create a channel
                frontend.getAccountInfo(users0[0].name, users0[0].password);
                frontend.createChannel(id, "UnwantedChannel");
                vote_id++;
                List<String> lines2 = frontend.viewHistory(id);
                expected = "Channel created with name \"UnwantedChannel\" through a voting process";
                if(!lines2.get(lines2.size()-1).contains(expected)) {
                    // User0 could not create channel
                    assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                }
                else {
                    // User0 created a channel that is not wanted by the community
                    if(N > 3) {
                        // Community votes to lower User0's reputation
                        frontend.getAccountInfo(users0[1].name, users0[1].password);
                        frontend.setReputation(id, users0[0].pseudonym, 1);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "New reputation of \"" + users0[0].pseudonym + "\" submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Reputation of \"" + users0[0].pseudonym + "\" changed to: 1.0";
                        for(int i = 2; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Reputation of \"" + users0[0].pseudonym + "\" changed to: 1.0";
                        }
                        vote_id++;
                    }
                    else if(N == 3) {
                        // Community votes to lower delete channel threshold
                        frontend.getAccountInfo(users0[1].name, users0[1].password);
                        frontend.thresholdVote(id, Daoliberate.ACTION.DELETE_CHANNEL, 0.5);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "Threshold change submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Threshold changed for action: Delete a channel\nNew defined threshold: 0.5";
                        for(int i = 2; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Threshold changed for action: Delete a channel\nNew defined threshold: 0.5";
                        }
                        vote_id++;
                    }

                    // Community votes to delete unwanted channel
                    frontend.getAccountInfo(users0[1].name, users0[1].password);
                    frontend.deleteChannel(id, "UnwantedChannel");
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-2];
                    expected = "Deletion of channel submitted for voting (voting process with id " + vote_id + ").";
                    assertEquals(expected, result);
                    // Verify that action was accepted
                    lines2 = frontend.viewHistory(id);
                    expected = "Channel deleted with name \"UnwantedChannel\" through a voting process";
                    for(int i = 2; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                        // Until action accepted, User[1-N] accept the action through voting
                        frontend.getAccountInfo(users0[i].name, users0[i].password);
                        frontend.vote(id, vote_id, true);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel deleted with name \"UnwantedChannel\" through a voting process";
                    }
                    vote_id++;
                    assertTrue(lines2.get(lines2.size()-1).contains(expected));

                    // Community votes to increase create channel threshold
                    frontend.getAccountInfo(users0[1].name, users0[1].password);
                    frontend.thresholdVote(id, Daoliberate.ACTION.CREATE_CHANNEL, 0.5);
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-2];
                    expected = "Threshold change submitted for voting (voting process with id " + vote_id + ").";
                    assertEquals(expected, result);
                    // Verify that action was accepted
                    lines2 = frontend.viewHistory(id);
                    expected = "Threshold changed for action: Create a new channel\nNew defined threshold: 0.5";
                    for(int i = 2; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                        // Until action accepted, User[1-N] accept the action through voting
                        frontend.getAccountInfo(users0[i].name, users0[i].password);
                        frontend.vote(id, vote_id, true);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "Threshold changed for action: Create a new channel\nNew defined threshold: 0.5";
                    }
                    vote_id++;

                    // User 0 tries to create a new channel and is put to a vote
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    frontend.createChannel(id, "UnwantedChannel");
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-2];
                    expected = "Creation of a new channel submitted for voting (voting process with id " + vote_id + ").";
                    assertTrue(result.contains(expected));    
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            fail();
        }
  	}

    @Test
    @Order(2)
  	public void RoomOfNDeleteChannel() {
        System.err.println("========== DELETE CHANNEL ==========");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            System.setOut(printStream);

            for(int N = 3, id = MAX_USERS-2; N <= MAX_USERS; N++, id++) {
                int vote_id = 0;
                ByteArrayInputStream bais = new ByteArrayInputStream(users1[0].pseudonym.getBytes());
                System.setIn(bais);
                System.err.println("-->N=" + N + " id=" + id + "<--");

                /*= User0 create a room and add User1, User2, ..., UserN (room id = 1) =*/
                // User0 create room
                frontend.getAccountInfo(users1[0].name, users1[0].password);
                String[] inv_users = new String[N-1];
                for(int i = 0; i < N-1; i++) {
                    inv_users[i] = users1[i+1].name;
                }
                frontend.createRoom(users1[0].name, users1[0].password, "RoomOf" + N, inv_users);
                String[] lines = baos.toString().split("\n");
                String result = lines[lines.length-1];
                String expected = "Insert the pseudonym to be used in this room: Successfully joined the chat room.";
                assertEquals(expected, result);
                //User[1-N] join roon
                for(int i = 1; i < N; i++) {
                    frontend.getAccountInfo(users1[i].name, users1[i].password);
                    String secret = frontend.getInitialSecret(users1[i].name, users1[i].password, id);
                    assertEquals(Daoliberate.STATUS.OK, frontend.ingressRoom(id, users1[i].pseudonym, secret));
                }

                // User0 create a new important channel
                frontend.createChannel(id, "important");
                List<String> lines2 = frontend.viewHistory(id);
                expected = "Channel created with name \"important\" through a voting process";
                for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                    // Until action accepted, User[1-N] accept the action through voting
                    frontend.getAccountInfo(users1[i].name, users1[i].password);
                    frontend.vote(id, vote_id, true);
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-1];
                    expected = "Vote submitted successfully.";
                    assertEquals(expected, result);
                    lines2 = frontend.viewHistory(id);
                    expected = "Channel created with name \"important\" through a voting process";
                }
                vote_id++;

                if(N > 3) {
                    /*= User0 changes its reputation to the maximum value =*/
                    frontend.getAccountInfo(users1[0].name, users1[0].password);
                    String details = String.join(",", frontend.enterRoom(id));

                    // Until User0 has maximum reputation he creates voting processes which are accepted
                    while(!details.contains("| Reputation: " + (N-2.0))) {
                        // User0 create a new channel
                        frontend.createChannel(id, "channel");
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel created with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users1[i].name, users1[i].password);
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
                        frontend.getAccountInfo(users1[0].name, users1[0].password);
                        frontend.deleteChannel(id, "channel");
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel deleted with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users1[i].name, users1[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Channel deleted with name \"channel\" through a voting process";
                        }
                        vote_id++;
                        frontend.getAccountInfo(users1[0].name, users1[0].password);
                        details = String.join(",", frontend.enterRoom(id));
                    }
                }

                /*= User0 try to delete an important channel =*/
                // User0 initiate a voting process to delete a channel
                frontend.getAccountInfo(users1[0].name, users1[0].password);
                frontend.deleteChannel(id, "important");
                vote_id++;
                lines2 = frontend.viewHistory(id);
                expected = "Channel deleted with name: \"important\"";
                if(!lines2.get(lines2.size()-1).contains(expected)) {
                    // User0 could not delete channel
                    assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            fail();
        }
  	}

    @Test
    @Order(3)
  	public void RoomOfNDeleteMessage() {
        System.err.println("========== DELETE MESSAGE ==========");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            System.setOut(printStream);

            for(int N = 3, id = (MAX_USERS*2)-4; N <= MAX_USERS; N++, id++) {
                int vote_id = 0;
                ByteArrayInputStream bais = new ByteArrayInputStream(users2[0].pseudonym.getBytes());
                System.setIn(bais);
                System.err.println("-->N=" + N + " id=" + id + "<--");

                /*= User0 create a room and add User1, User2, ..., UserN (room id = 1) =*/
                // User0 create room
                frontend.getAccountInfo(users2[0].name, users2[0].password);
                String[] inv_users = new String[N-1];
                for(int i = 0; i < N-1; i++) {
                    inv_users[i] = users2[i+1].name;
                }
                frontend.createRoom(users2[0].name, users2[0].password, "RoomOf" + N, inv_users);
                String[] lines = baos.toString().split("\n");
                String result = lines[lines.length-1];
                String expected = "Insert the pseudonym to be used in this room: Successfully joined the chat room.";
                assertEquals(expected, result);
                //User[1-N] join roon
                for(int i = 1; i < N; i++) {
                    frontend.getAccountInfo(users2[i].name, users2[i].password);
                    String secret = frontend.getInitialSecret(users2[i].name, users2[i].password, id);
                    assertEquals(Daoliberate.STATUS.OK, frontend.ingressRoom(id, users2[i].pseudonym, secret));
                }

                // User0 create a new channel
                frontend.createChannel(id, "important");
                List<String> lines2 = frontend.viewHistory(id);
                expected = "Channel created with name \"important\" through a voting process";
                for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                    // Until action accepted, User[1-N] accept the action through voting
                    frontend.getAccountInfo(users2[i].name, users2[i].password);
                    frontend.vote(id, vote_id, true);
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-1];
                    expected = "Vote submitted successfully.";
                    assertEquals(expected, result);
                    lines2 = frontend.viewHistory(id);
                    expected = "Channel created with name \"important\" through a voting process";
                }
                vote_id++;

                if(N > 3) {
                    /*= User0 changes its reputation to the maximum value =*/
                    frontend.getAccountInfo(users2[0].name, users2[0].password);
                    String details = String.join(",", frontend.enterRoom(id));

                    // Until User0 has maximum reputation he creates voting processes which are accepted
                    while(!details.contains("| Reputation: " + (N-2.0))) {
                        // User0 create a new channel
                        frontend.createChannel(id, "channel");
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel created with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users2[i].name, users2[i].password);
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
                        frontend.getAccountInfo(users2[0].name, users2[0].password);
                        frontend.deleteChannel(id, "channel");
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel deleted with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users2[i].name, users2[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Channel deleted with name \"channel\" through a voting process";
                        }
                        vote_id++;
                        frontend.getAccountInfo(users2[0].name, users2[0].password);
                        details = String.join(",", frontend.enterRoom(id));
                    }
                }

                /*= User1 send a message =*/
                frontend.getAccountInfo(users2[1].name, users2[1].password);
                frontend.createMessage(id, "important", "Hello");

                /*= User0 try to delete an message =*/
                // User0 initiate a voting process to delete a message
                frontend.getAccountInfo(users2[0].name, users2[0].password);
                frontend.deleteMessage(id, "important", 0, Daoliberate.REASON.OFFENSIVE);
                vote_id++;
                lines2 = frontend.viewHistory(id);
                expected = "Message deleted (with id: 0)";
                if(!lines2.get(lines2.size()-1).contains(expected)) {
                    // User0 could not delete message
                    assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            fail();
        }
  	}

    @Test
    @Order(4)
  	public void RoomOfNPunishUser() {
        System.err.println("========== PUNISH USER ==========");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            System.setOut(printStream);

            for(int N = 3, id = (MAX_USERS*3)-6; N <= MAX_USERS; N++, id++) {
                int vote_id = 0;
                ByteArrayInputStream bais = new ByteArrayInputStream(users3[0].pseudonym.getBytes());
                System.setIn(bais);
                System.err.println("-->N=" + N + " id=" + id + "<--");

                /*= User0 create a room and add User1, User2, ..., UserN (room id = 1) =*/
                // User0 create room
                frontend.getAccountInfo(users3[0].name, users3[0].password);
                String[] inv_users = new String[N-1];
                for(int i = 0; i < N-1; i++) {
                    inv_users[i] = users3[i+1].name;
                }
                frontend.createRoom(users3[0].name, users3[0].password, "RoomOf" + N, inv_users);
                String[] lines = baos.toString().split("\n");
                String result = lines[lines.length-1];
                String expected = "Insert the pseudonym to be used in this room: Successfully joined the chat room.";
                assertEquals(expected, result);
                //User[1-N] join roon
                for(int i = 1; i < N; i++) {
                    frontend.getAccountInfo(users3[i].name, users3[i].password);
                    String secret = frontend.getInitialSecret(users3[i].name, users3[i].password, id);
                    assertEquals(Daoliberate.STATUS.OK, frontend.ingressRoom(id, users3[i].pseudonym, secret));
                }

                if(N > 3) {
                    /*= User0 changes its reputation to the maximum value =*/
                    frontend.getAccountInfo(users3[0].name, users3[0].password);
                    String details = String.join(",", frontend.enterRoom(id));

                    // Until User0 has maximum reputation he creates voting processes which are accepted
                    while(!details.contains("| Reputation: " + (N-2.0))) {
                        // User0 create a new channel
                        frontend.createChannel(id, "channel");
                        List<String> lines2 = frontend.viewHistory(id);
                        expected = "Channel created with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users3[i].name, users3[i].password);
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
                        frontend.getAccountInfo(users3[0].name, users3[0].password);
                        frontend.deleteChannel(id, "channel");
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel deleted with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users3[i].name, users3[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Channel deleted with name \"channel\" through a voting process";
                        }
                        vote_id++;
                        frontend.getAccountInfo(users3[0].name, users3[0].password);
                        details = String.join(",", frontend.enterRoom(id));
                    }
                }

                /*= User0 try to punish a user =*/
                // User0 initiate a voting process to punish a user
                frontend.getAccountInfo(users3[0].name, users3[0].password);
                frontend.punishUser(id, users3[1].pseudonym, 0, 1, 0);
                vote_id++;
                List<String> lines2 = frontend.viewHistory(id);
                expected = "User punished (with name: " + users3[1].pseudonym + ")";
                if(!lines2.get(lines2.size()-1).contains(expected)) {
                    // User0 could not punish user
                    assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            fail();
        }
  	}

    @Test
    @Order(5)
  	public void RoomOfNDeleteUser() {
        System.err.println("========== DELETE USER ==========");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            System.setOut(printStream);

            for(int N = 3, id = (MAX_USERS*4)-8; N <= MAX_USERS; N++, id++) {
                int vote_id = 0;
                ByteArrayInputStream bais = new ByteArrayInputStream(users3[0].pseudonym.getBytes());
                System.setIn(bais);
                System.err.println("-->N=" + N + " id=" + id + "<--");

                /*= User0 create a room and add User1, User2, ..., UserN (room id = 1) =*/
                // User0 create room
                frontend.getAccountInfo(users3[0].name, users3[0].password);
                String[] inv_users = new String[N-1];
                for(int i = 0; i < N-1; i++) {
                    inv_users[i] = users3[i+1].name;
                }
                frontend.createRoom(users3[0].name, users3[0].password, "RoomOf" + N, inv_users);
                String[] lines = baos.toString().split("\n");
                String result = lines[lines.length-1];
                String expected = "Insert the pseudonym to be used in this room: Successfully joined the chat room.";
                assertEquals(expected, result);
                //User[1-N] join roon
                for(int i = 1; i < N; i++) {
                    frontend.getAccountInfo(users3[i].name, users3[i].password);
                    String secret = frontend.getInitialSecret(users3[i].name, users3[i].password, id);
                    assertEquals(Daoliberate.STATUS.OK, frontend.ingressRoom(id, users3[i].pseudonym, secret));
                }

                if(N > 3) {
                    /*= User0 changes its reputation to the maximum value =*/
                    frontend.getAccountInfo(users3[0].name, users3[0].password);
                    String details = String.join(",", frontend.enterRoom(id));

                    // Until User0 has maximum reputation he creates voting processes which are accepted
                    while(!details.contains("| Reputation: " + (N-2.0))) {
                        // User0 create a new channel
                        frontend.createChannel(id, "channel");
                        List<String> lines2 = frontend.viewHistory(id);
                        expected = "Channel created with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users3[i].name, users3[i].password);
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
                        frontend.getAccountInfo(users3[0].name, users3[0].password);
                        frontend.deleteChannel(id, "channel");
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel deleted with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users3[i].name, users3[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Channel deleted with name \"channel\" through a voting process";
                        }
                        vote_id++;
                        frontend.getAccountInfo(users3[0].name, users3[0].password);
                        details = String.join(",", frontend.enterRoom(id));
                    }
                }

                /*= User0 try to delete a user =*/
                // User0 initiate a voting process to delete a user
                frontend.getAccountInfo(users3[0].name, users3[0].password);
                frontend.deleteUser(id, users3[1].pseudonym);
                vote_id++;
                List<String> lines2 = frontend.viewHistory(id);
                expected = "User marked to be deleted (with name: " + users3[1].pseudonym + ") from room";
                if(!lines2.get(lines2.size()-1).contains(expected)) {
                    // User0 could not delete user
                    assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            fail();
        }
  	}

    @Test
    @Order(6)
  	public void RoomOfNEditMessage() {
        System.err.println("========== EDIT MESSAGE ==========");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            System.setOut(printStream);

            for(int N = 3, id = (MAX_USERS*5)-10; N <= MAX_USERS; N++, id++) {
                int vote_id = 0;
                ByteArrayInputStream bais = new ByteArrayInputStream(users0[0].pseudonym.getBytes());
                System.setIn(bais);
                System.err.println("-->N=" + N + " id=" + id + "<--");

                /*= User0 create a room and add User1, User2, ..., UserN (room id = 1) =*/
                // User0 create room
                frontend.getAccountInfo(users0[0].name, users0[0].password);
                String[] inv_users = new String[N-1];
                for(int i = 0; i < N-1; i++) {
                    inv_users[i] = users0[i+1].name;
                }
                frontend.createRoom(users0[0].name, users0[0].password, "RoomOf" + N, inv_users);
                String[] lines = baos.toString().split("\n");
                String result = lines[lines.length-1];
                String expected = "Insert the pseudonym to be used in this room: Successfully joined the chat room.";
                assertEquals(expected, result);
                //User[1-N] join roon
                for(int i = 1; i < N; i++) {
                    frontend.getAccountInfo(users0[i].name, users0[i].password);
                    String secret = frontend.getInitialSecret(users0[i].name, users0[i].password, id);
                    assertEquals(Daoliberate.STATUS.OK, frontend.ingressRoom(id, users0[i].pseudonym, secret));
                }

                if(N > 3) {
                    /*= User0 changes its reputation to the maximum value =*/
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    String details = String.join(",", frontend.enterRoom(id));

                    // Until User0 has maximum reputation he creates voting processes which are accepted
                    while(!details.contains("| Reputation: " + (N-2.0))) {
                        // User0 create a new channel
                        frontend.createChannel(id, "channel");
                        List<String> lines2 = frontend.viewHistory(id);
                        expected = "Channel created with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
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
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        frontend.deleteChannel(id, "channel");
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel deleted with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Channel deleted with name \"channel\" through a voting process";
                        }
                        vote_id++;
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        details = String.join(",", frontend.enterRoom(id));
                    }
                }

                /*= User1 send a message =*/
                frontend.getAccountInfo(users0[1].name, users0[1].password);
                frontend.createMessage(id, "General", "Hello");

                /*= User0 edit the message =*/
                frontend.getAccountInfo(users0[0].name, users0[0].password);
                frontend.editMessage(id, "General", 0, "Hello XD XD XD !!!", Daoliberate.REASON.OFFENSIVE);
                vote_id++;
                List<String> lines2 = frontend.viewHistory(id);
                expected = "Message edited (with id: 0) in channel \"General\"";
                if(!lines2.get(lines2.size()-1).contains(expected)) {
                    // User0 could not edit message
                    assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                }
                else {
                    // User0 edited the message
                    if(N > 3) {
                        // Community votes to lower User0's reputation
                        frontend.getAccountInfo(users0[1].name, users0[1].password);
                        frontend.setReputation(id, users0[0].pseudonym, 1);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "New reputation of \"" + users0[0].pseudonym + "\" submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Reputation of \"" + users0[0].pseudonym + "\" changed to: 1.0";
                        for(int i = 2; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Reputation of \"" + users0[0].pseudonym + "\" changed to: 1.0";
                        }
                        vote_id++;
                    }
                    else if(N == 3) {
                        // Community votes to upper edit message threshold
                        frontend.getAccountInfo(users0[1].name, users0[1].password);
                        frontend.thresholdVote(id, Daoliberate.ACTION.EDIT_MESSAGE, 0.5);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "Threshold change submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Threshold changed for action: Edit a message\nNew defined threshold: 0.5";
                        for(int i = 2; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Threshold changed for action: Edit a message\nNew defined threshold: 0.5";
                        }
                        vote_id++;
                    }

                    // Community votes to reedit the message
                    frontend.getAccountInfo(users0[1].name, users0[1].password);
                    frontend.editMessage(id, "General", 0, "Hello", Daoliberate.REASON.OFFENSIVE);
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-2];
                    expected = "Editing the message submitted for voting (voting process with id " + vote_id + ").";
                    assertEquals(expected, result);
                    // Verify that action was accepted
                    lines2 = frontend.viewHistory(id);
                    expected = "Message edited (with id: 0) in channel \"General\"";
                    for(int i = 2; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                        // Until action accepted, User[1-N] accept the action through voting
                        frontend.getAccountInfo(users0[i].name, users0[i].password);
                        frontend.vote(id, vote_id, true);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "Message edited (with id: 0) in channel \"General\"";
                    }
                    vote_id++;
                    assertTrue(lines2.get(lines2.size()-1).contains(expected));

                    // User 0 tries to reedit message and is put to a vote
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    frontend.editMessage(id, "General", 0, "Hello XD XD XD !!!", Daoliberate.REASON.OFFENSIVE);
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-2];
                    expected = "Editing the message submitted for voting (voting process with id " + vote_id + ").";
                    assertTrue(result.contains(expected));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            fail();
        }
  	}

    @Test
    @Order(7)
  	public void RoomOfNPinMessage() {
        System.err.println("========== PIN MESSAGE ==========");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            System.setOut(printStream);

            for(int N = 3, id = (MAX_USERS*6)-12; N <= MAX_USERS; N++, id++) {
                int vote_id = 0;
                ByteArrayInputStream bais = new ByteArrayInputStream(users0[0].pseudonym.getBytes());
                System.setIn(bais);
                System.err.println("-->N=" + N + " id=" + id + "<--");

                /*= User0 create a room and add User1, User2, ..., UserN (room id = 1) =*/
                // User0 create room
                frontend.getAccountInfo(users0[0].name, users0[0].password);
                String[] inv_users = new String[N-1];
                for(int i = 0; i < N-1; i++) {
                    inv_users[i] = users0[i+1].name;
                }
                frontend.createRoom(users0[0].name, users0[0].password, "RoomOf" + N, inv_users);
                String[] lines = baos.toString().split("\n");
                String result = lines[lines.length-1];
                String expected = "Insert the pseudonym to be used in this room: Successfully joined the chat room.";
                assertEquals(expected, result);
                //User[1-N] join roon
                for(int i = 1; i < N; i++) {
                    frontend.getAccountInfo(users0[i].name, users0[i].password);
                    String secret = frontend.getInitialSecret(users0[i].name, users0[i].password, id);
                    assertEquals(Daoliberate.STATUS.OK, frontend.ingressRoom(id, users0[i].pseudonym, secret));
                }

                if(N > 3) {
                    /*= User0 changes its reputation to the maximum value =*/
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    String details = String.join(",", frontend.enterRoom(id));

                    // Until User0 has maximum reputation he creates voting processes which are accepted
                    while(!details.contains("| Reputation: " + (N-2.0))) {
                        // User0 create a new channel
                        frontend.createChannel(id, "channel");
                        List<String> lines2 = frontend.viewHistory(id);
                        expected = "Channel created with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
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
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        frontend.deleteChannel(id, "channel");
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel deleted with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Channel deleted with name \"channel\" through a voting process";
                        }
                        vote_id++;
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        details = String.join(",", frontend.enterRoom(id));
                    }
                }

                /*= User1 send a message =*/
                frontend.getAccountInfo(users0[1].name, users0[1].password);
                frontend.createMessage(id, "General", "Hello");

                /*= User0 pin the message =*/
                frontend.getAccountInfo(users0[0].name, users0[0].password);
                frontend.pinMessage(id, "General", 0);
                vote_id++;
                List<String> lines2 = frontend.viewHistory(id);
                expected = "Message pinned (with id: 0) in channel \"General\"";
                if(!lines2.get(lines2.size()-1).contains(expected)) {
                    // User0 could not pin message
                    assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                }
                else {
                    // User0 pined the message
                    if(N > 3) {
                        // Community votes to lower User0's reputation
                        frontend.getAccountInfo(users0[1].name, users0[1].password);
                        frontend.setReputation(id, users0[0].pseudonym, 1);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "New reputation of \"" + users0[0].pseudonym + "\" submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Reputation of \"" + users0[0].pseudonym + "\" changed to: 1.0";
                        for(int i = 2; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Reputation of \"" + users0[0].pseudonym + "\" changed to: 1.0";
                        }
                        vote_id++;
                    }
                    else if(N == 3) {
                        // Community votes to upper pin message threshold
                        frontend.getAccountInfo(users0[1].name, users0[1].password);
                        frontend.thresholdVote(id, Daoliberate.ACTION.PIN_MESSAGE, 0.5);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "Threshold change submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Threshold changed for action: Pin a message\nNew defined threshold: 0.5";
                        for(int i = 2; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Threshold changed for action: Pin a message\nNew defined threshold: 0.5";
                        }
                        vote_id++;
                    }

                    // Community votes to unpin the message
                    frontend.getAccountInfo(users0[1].name, users0[1].password);
                    frontend.unpinMessage(id, "General", 0);
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-2];
                    expected = "Message unpin submitted for voting (voting process with id " + vote_id + ").";
                    assertEquals(expected, result);
                    // Verify that action was accepted
                    lines2 = frontend.viewHistory(id);
                    expected = "Message unpinned (with id: 0) in channel \"General\"";
                    for(int i = 2; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                        // Until action accepted, User[1-N] accept the action through voting
                        frontend.getAccountInfo(users0[i].name, users0[i].password);
                        frontend.vote(id, vote_id, true);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "Message unpinned (with id: 0) in channel \"General\"";
                    }
                    vote_id++;
                    assertTrue(lines2.get(lines2.size()-1).contains(expected));

                    // User 0 tries to pin message and is put to a vote
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    frontend.pinMessage(id, "General", 0);
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-2];
                    expected = "Message pin submitted for voting (voting process with id " + vote_id + ").";
                    assertTrue(result.contains(expected));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            fail();
        }
  	}

    @Test
    @Order(8)
  	public void RoomOfNUnpinMessage() {
        System.err.println("========== UNPIN MESSAGE ==========");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            System.setOut(printStream);

            for(int N = 3, id = (MAX_USERS*7)-14; N <= MAX_USERS; N++, id++) {
                int vote_id = 0;
                ByteArrayInputStream bais = new ByteArrayInputStream(users0[0].pseudonym.getBytes());
                System.setIn(bais);
                System.err.println("-->N=" + N + " id=" + id + "<--");

                /*= User0 create a room and add User1, User2, ..., UserN (room id = 1) =*/
                // User0 create room
                frontend.getAccountInfo(users0[0].name, users0[0].password);
                String[] inv_users = new String[N-1];
                for(int i = 0; i < N-1; i++) {
                    inv_users[i] = users0[i+1].name;
                }
                frontend.createRoom(users0[0].name, users0[0].password, "RoomOf" + N, inv_users);
                String[] lines = baos.toString().split("\n");
                String result = lines[lines.length-1];
                String expected = "Insert the pseudonym to be used in this room: Successfully joined the chat room.";
                assertEquals(expected, result);
                //User[1-N] join roon
                for(int i = 1; i < N; i++) {
                    frontend.getAccountInfo(users0[i].name, users0[i].password);
                    String secret = frontend.getInitialSecret(users0[i].name, users0[i].password, id);
                    assertEquals(Daoliberate.STATUS.OK, frontend.ingressRoom(id, users0[i].pseudonym, secret));
                }

                /*= User1 send a message =*/
                frontend.getAccountInfo(users0[1].name, users0[1].password);
                frontend.createMessage(id, "General", "Hello");

                // Community votes to pin the message
                frontend.getAccountInfo(users0[1].name, users0[1].password);
                frontend.pinMessage(id, "General", 0);
                lines = baos.toString().split("\n");
                result = lines[lines.length-2];
                expected = "Message pin submitted for voting (voting process with id " + vote_id + ").";
                assertEquals(expected, result);
                // Verify that action was accepted
                List<String> lines2 = frontend.viewHistory(id);
                expected = "Message pinned (with id: 0) in channel \"General\"";
                for(int i = 2; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                    // Until action accepted, User[1-N] accept the action through voting
                    frontend.getAccountInfo(users0[i].name, users0[i].password);
                    frontend.vote(id, vote_id, true);
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-1];
                    expected = "Vote submitted successfully.";
                    assertEquals(expected, result);
                    lines2 = frontend.viewHistory(id);
                    expected = "Message pinned (with id: 0) in channel \"General\"";
                }
                vote_id++;
                assertTrue(lines2.get(lines2.size()-1).contains(expected));

                if(N > 3) {
                    /*= User0 changes its reputation to the maximum value =*/
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    String details = String.join(",", frontend.enterRoom(id));

                    // Until User0 has maximum reputation he creates voting processes which are accepted
                    while(!details.contains("| Reputation: " + (N-2.0))) {
                        // User0 create a new channel
                        frontend.createChannel(id, "channel");
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel created with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
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
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        frontend.deleteChannel(id, "channel");
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel deleted with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Channel deleted with name \"channel\" through a voting process";
                        }
                        vote_id++;
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        details = String.join(",", frontend.enterRoom(id));
                    }
                }

                /*= User0 unpin the message =*/
                frontend.getAccountInfo(users0[0].name, users0[0].password);
                frontend.unpinMessage(id, "General", 0);
                vote_id++;
                lines2 = frontend.viewHistory(id);
                expected = "Message unpinned (with id: 0) in channel \"General\"";
                if(!lines2.get(lines2.size()-1).contains(expected)) {
                    // User0 could not unpin message
                    assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                }
                else {
                    // User0 unpined the message
                    if(N > 3) {
                        // Community votes to lower User0's reputation
                        frontend.getAccountInfo(users0[1].name, users0[1].password);
                        frontend.setReputation(id, users0[0].pseudonym, 1);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "New reputation of \"" + users0[0].pseudonym + "\" submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Reputation of \"" + users0[0].pseudonym + "\" changed to: 1.0";
                        for(int i = 2; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Reputation of \"" + users0[0].pseudonym + "\" changed to: 1.0";
                        }
                        vote_id++;
                    }
                    else if(N == 3) {
                        // Community votes to upper unpin message threshold
                        frontend.getAccountInfo(users0[1].name, users0[1].password);
                        frontend.thresholdVote(id, Daoliberate.ACTION.UNPIN_MESSAGE, 0.5);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "Threshold change submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Threshold changed for action: Unpin a message\nNew defined threshold: 0.5";
                        for(int i = 2; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Threshold changed for action: Unpin a message\nNew defined threshold: 0.5";
                        }
                        vote_id++;
                    }

                    // Community votes to pin the message
                    frontend.getAccountInfo(users0[1].name, users0[1].password);
                    frontend.pinMessage(id, "General", 0);
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-2];
                    expected = "Message pin submitted for voting (voting process with id " + vote_id + ").";
                    assertEquals(expected, result);
                    // Verify that action was accepted
                    lines2 = frontend.viewHistory(id);
                    expected = "Message pinned (with id: 0) in channel \"General\"";
                    for(int i = 2; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                        // Until action accepted, User[1-N] accept the action through voting
                        frontend.getAccountInfo(users0[i].name, users0[i].password);
                        frontend.vote(id, vote_id, true);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "Message pinned (with id: 0) in channel \"General\"";
                    }
                    vote_id++;
                    assertTrue(lines2.get(lines2.size()-1).contains(expected));

                    // User 0 tries to unpin message and is put to a vote
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    frontend.unpinMessage(id, "General", 0);
                    lines = baos.toString().split("\n");
                    result = lines[lines.length-2];
                    expected = "Message unpin submitted for voting (voting process with id " + vote_id + ").";
                    assertTrue(result.contains(expected));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            fail();
        }
  	}

    @Test
    @Order(9)
  	public void RoomOfNInviteUser() {
        System.err.println("========== INVITE USER ==========");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            System.setOut(printStream);

            for(int N = 3, id = (MAX_USERS*8)-16; N <= MAX_USERS; N++, id++) {
                int vote_id = 0;
                ByteArrayInputStream bais = new ByteArrayInputStream(users0[0].pseudonym.getBytes());
                System.setIn(bais);
                System.err.println("-->N=" + N + " id=" + id + "<--");

                /*= User0 create a room and add User1, User2, ..., UserN (room id = 1) =*/
                // User0 create room
                frontend.getAccountInfo(users0[0].name, users0[0].password);
                String[] inv_users = new String[N-1];
                for(int i = 0; i < N-1; i++) {
                    inv_users[i] = users0[i+1].name;
                }
                frontend.createRoom(users0[0].name, users0[0].password, "RoomOf" + N, inv_users);
                String[] lines = baos.toString().split("\n");
                String result = lines[lines.length-1];
                String expected = "Insert the pseudonym to be used in this room: Successfully joined the chat room.";
                assertEquals(expected, result);
                //User[1-N] join roon
                for(int i = 1; i < N; i++) {
                    frontend.getAccountInfo(users0[i].name, users0[i].password);
                    String secret = frontend.getInitialSecret(users0[i].name, users0[i].password, id);
                    assertEquals(Daoliberate.STATUS.OK, frontend.ingressRoom(id, users0[i].pseudonym, secret));
                }

                if(N > 3) {
                    /*= User0 changes its reputation to the maximum value =*/
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    String details = String.join(",", frontend.enterRoom(id));

                    // Until User0 has maximum reputation he creates voting processes which are accepted
                    while(!details.contains("| Reputation: " + (N-2.0))) {
                        // User0 create a new channel
                        frontend.createChannel(id, "channel");
                        List<String> lines2 = frontend.viewHistory(id);
                        expected = "Channel created with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
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
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        frontend.deleteChannel(id, "channel");
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel deleted with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Channel deleted with name \"channel\" through a voting process";
                        }
                        vote_id++;
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        details = String.join(",", frontend.enterRoom(id));
                    }
                }

                /*= User0 invite a user =*/
                frontend.getAccountInfo(users0[0].name, users0[0].password);
                frontend.inviteUser(id, "Evil");
                List<String> lines2 = frontend.viewHistory(id);
                
                // User0 could not invite user
                expected = "User invited (with name: Evil)";
                assertTrue(!lines2.get(lines2.size()-1).contains(expected));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            fail();
        }
  	}
}
