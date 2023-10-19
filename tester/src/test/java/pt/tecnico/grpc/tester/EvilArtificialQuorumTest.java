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
public class EvilArtificialQuorumTest {

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

    @BeforeAll
	public void registerUsers(){
        for(int i = 0; i < MAX_USERS; i++) {
            users0[i] = new User("User0" + i, "PasswordOf" + i, "Anony" + i);
        }

        try {
            frontend = new ClientLibrary("localhost", 8080, "localhost", 8081);
            for(User usr : users0) {
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

            for(int N = 3, id = 0; N <= MAX_USERS; N++) {
                int MAX_Q = ((N % 2 == 1)? N/2 : (N/2) - 1);
                for(int Q = 2; Q <= MAX_Q; Q++) {
                    int vote_id = 0;
                    ByteArrayInputStream bais = new ByteArrayInputStream(users0[0].pseudonym.getBytes());
                    System.setIn(bais);
                    System.err.println("-->N=" + N + " id=" + id + " Q=" + Q + "<--");

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
                        for(int j = 0; j < Q; j++) {
                            /*= User0, User1, ..., UserQ-1 changes its reputation to the maximum value =*/
                            frontend.getAccountInfo(users0[j].name, users0[j].password);
                            String details = String.join(",", frontend.enterRoom(id));

                            // Until User has maximum reputation he creates voting processes which are accepted
                            while(!details.contains("| Reputation: " + (N-2.0))) {
                                // User0 create a new channel
                                frontend.createChannel(id, "channel");
                                List<String> lines2 = frontend.viewHistory(id);
                                expected = "Channel created with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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

                                // User delete a channel
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                frontend.deleteChannel(id, "channel");
                                lines2 = frontend.viewHistory(id);
                                expected = "Channel deleted with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                details = String.join(",", frontend.enterRoom(id));
                            }
                        }
                    }

                    /*= User0, User1, ..., UserQ-1 try to create an unwanted channel =*/
                    // User0 initiate a voting process to create a channel
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    frontend.createChannel(id, "UnwantedChannel");
                    List<String> lines2 = frontend.viewHistory(id);
                    expected = "Channel created with name \"UnwantedChannel\" through a voting process";
                    for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                        // Until action accepted, User[1-(Q-1)] accept the action through voting
                        frontend.getAccountInfo(users0[i].name, users0[i].password);
                        frontend.vote(id, vote_id, true);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel created with name \"UnwantedChannel\" through a voting process";
                    }
                    vote_id++;
                    if(!lines2.get(lines2.size()-1).contains(expected)) {
                        // User0, User1, ..., UserQ-1 could not create channel
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    else {
                        // User0, User1, ..., UserQ-1 created a channel that is not wanted by the community
                        if(N > 3) {
                            // Community votes to lower User0's, User1's, ..., UserQ-1's reputation
                            for(int j = 0; j < Q; j++) {
                                frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                                frontend.setReputation(id, users0[j].pseudonym, 1);
                                lines = baos.toString().split("\n");
                                result = lines[lines.length-2];
                                expected = "New reputation of \"" + users0[j].pseudonym + "\" submitted for voting (voting process with id " + vote_id + ").";
                                assertEquals(expected, result);
                                // Verify that action was accepted
                                lines2 = frontend.viewHistory(id);
                                expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    // Until action accepted, User[Q-N] accept the action through voting
                                    frontend.getAccountInfo(users0[i].name, users0[i].password);
                                    frontend.vote(id, vote_id, true);
                                    lines = baos.toString().split("\n");
                                    result = lines[lines.length-1];
                                    expected = "Vote submitted successfully.";
                                    assertEquals(expected, result);
                                    lines2 = frontend.viewHistory(id);
                                    expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                }
                                vote_id++;
                            }
                        }
                        
                        // Community votes to lower delete channel threshold
                        frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                        frontend.thresholdVote(id, Daoliberate.ACTION.DELETE_CHANNEL, 0.5);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "Threshold change submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Threshold changed for action: Delete a channel\nNew defined threshold: 0.5";
                        for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
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
                        

                        // Community votes to delete unwanted channel
                        frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                        frontend.deleteChannel(id, "UnwantedChannel");
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "Deletion of channel submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel deleted with name \"UnwantedChannel\" through a voting process";
                        for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
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
                        frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                        frontend.thresholdVote(id, Daoliberate.ACTION.CREATE_CHANNEL, 0.5);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "Threshold change submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Threshold changed for action: Create a new channel\nNew defined threshold: 0.5";
                        for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
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

                        // User0, User1, ..., UserQ-1 tries to create a new channel and is put to a vote
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        frontend.createChannel(id, "UnwantedChannel");
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel created with name \"UnwantedChannel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                            // Until action accepted, User[1-(Q-1)] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Channel created with name \"UnwantedChannel\" through a voting process";
                        }
                        vote_id++;

                        // User0, User1, ..., UserQ-1 could not create channel
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    id++;
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

            for(int N = 3, id = MAX_USERS-2; N <= MAX_USERS; N++) {
                int MAX_Q = ((N % 2 == 1)? N/2 : (N/2) - 1);
                for(int Q = 2; Q <= MAX_Q; Q++) {
                    int vote_id = 0;
                    ByteArrayInputStream bais = new ByteArrayInputStream(users0[0].pseudonym.getBytes());
                    System.setIn(bais);
                    System.err.println("-->N=" + N + " id=" + id + " Q=" + Q + "<--");

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
                        for(int j = 0; j < Q; j++) {
                            /*= User0, User1, ..., UserQ-1 changes its reputation to the maximum value =*/
                            frontend.getAccountInfo(users0[j].name, users0[j].password);
                            String details = String.join(",", frontend.enterRoom(id));

                            // Until User has maximum reputation he creates voting processes which are accepted
                            while(!details.contains("| Reputation: " + (N-2.0))) {
                                // User0 create a new channel
                                frontend.createChannel(id, "channel");
                                List<String> lines2 = frontend.viewHistory(id);
                                expected = "Channel created with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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

                                // User delete a channel
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                frontend.deleteChannel(id, "channel");
                                lines2 = frontend.viewHistory(id);
                                expected = "Channel deleted with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                details = String.join(",", frontend.enterRoom(id));
                            }
                        }
                    }

                    // Community create channel
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
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

                    /*= User0, User1, ..., UserQ-1 try to delete channel =*/
                    // User0 initiate a voting process to delete a channel
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    frontend.deleteChannel(id, "channel");
                    lines2 = frontend.viewHistory(id);
                    expected = "Channel deleted with name \"channel\" through a voting process";
                    for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                        // Until action accepted, User[1-(Q-1)] accept the action through voting
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
                    if(!lines2.get(lines2.size()-1).contains(expected)) {
                        // User0, User1, ..., UserQ-1 could not delete channel
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    else {
                        // User0, User1, ..., UserQ-1 deleted a channel that is important for the community
                        if(N > 3) {
                            // Community votes to lower User0's, User1's, ..., UserQ-1's reputation
                            for(int j = 0; j < Q; j++) {
                                frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                                frontend.setReputation(id, users0[j].pseudonym, 1);
                                lines = baos.toString().split("\n");
                                result = lines[lines.length-2];
                                expected = "New reputation of \"" + users0[j].pseudonym + "\" submitted for voting (voting process with id " + vote_id + ").";
                                assertEquals(expected, result);
                                // Verify that action was accepted
                                lines2 = frontend.viewHistory(id);
                                expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    // Until action accepted, User[Q-N] accept the action through voting
                                    frontend.getAccountInfo(users0[i].name, users0[i].password);
                                    frontend.vote(id, vote_id, true);
                                    lines = baos.toString().split("\n");
                                    result = lines[lines.length-1];
                                    expected = "Vote submitted successfully.";
                                    assertEquals(expected, result);
                                    lines2 = frontend.viewHistory(id);
                                    expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                }
                                vote_id++;
                            }
                        }
                        
                        // Community votes to lower delete channel threshold
                        frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                        frontend.thresholdVote(id, Daoliberate.ACTION.DELETE_CHANNEL, 0.5);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "Threshold change submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Threshold changed for action: Delete a channel\nNew defined threshold: 0.5";
                        for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
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

                        // Community create channel
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
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

                        // User0, User1, ..., UserQ-1 tries to delete channel and is put to a vote
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        frontend.deleteChannel(id, "channel");
                        lines2 = frontend.viewHistory(id);
                        expected = "Channel deleted with name \"channel\" through a voting process";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                            // Until action accepted, User[1-(Q-1)] accept the action through voting
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

                        // User0, User1, ..., UserQ-1 could not delete channel
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    id++;
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

            for(int N = 3, id = (MAX_USERS*2)-4; N <= MAX_USERS; N++) {
                int MAX_Q = ((N % 2 == 1)? N/2 : (N/2) - 1);
                for(int Q = 2; Q <= MAX_Q; Q++) {
                    int vote_id = 0;
                    ByteArrayInputStream bais = new ByteArrayInputStream(users0[0].pseudonym.getBytes());
                    System.setIn(bais);
                    System.err.println("-->N=" + N + " id=" + id + " Q=" + Q + "<--");

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
                        for(int j = 0; j < Q; j++) {
                            /*= User0, User1, ..., UserQ-1 changes its reputation to the maximum value =*/
                            frontend.getAccountInfo(users0[j].name, users0[j].password);
                            String details = String.join(",", frontend.enterRoom(id));

                            // Until User has maximum reputation he creates voting processes which are accepted
                            while(!details.contains("| Reputation: " + (N-2.0))) {
                                // User0 create a new channel
                                frontend.createChannel(id, "channel");
                                List<String> lines2 = frontend.viewHistory(id);
                                expected = "Channel created with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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

                                // User delete a channel
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                frontend.deleteChannel(id, "channel");
                                lines2 = frontend.viewHistory(id);
                                expected = "Channel deleted with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                details = String.join(",", frontend.enterRoom(id));
                            }
                        }
                    }

                    // Community create channel
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
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

                    /*= UserQ send a message =*/
                    frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                    frontend.createMessage(id, "channel", "Hello");

                    /*= User0, User1, ..., UserQ-1 try to delete message =*/
                    // User0 initiate a voting process to delete a message
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    frontend.deleteMessage(id, "channel", 0, Daoliberate.REASON.OFFENSIVE);
                    lines2 = frontend.viewHistory(id);
                    expected = "Message deleted (with id: 0)";
                    for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                        // Until action accepted, User[1-(Q-1)] accept the action through voting
                        frontend.getAccountInfo(users0[i].name, users0[i].password);
                        frontend.vote(id, vote_id, true);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "Message deleted (with id: 0)";
                    }
                    vote_id++;
                    if(!lines2.get(lines2.size()-1).contains(expected)) {
                        // User0, User1, ..., UserQ-1 could not delete message
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    else {
                        // User0, User1, ..., UserQ-1 deleted a message that is important for the community
                        if(N > 3) {
                            // Community votes to lower User0's, User1's, ..., UserQ-1's reputation
                            for(int j = 0; j < Q; j++) {
                                frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                                frontend.setReputation(id, users0[j].pseudonym, 1);
                                lines = baos.toString().split("\n");
                                result = lines[lines.length-2];
                                expected = "New reputation of \"" + users0[j].pseudonym + "\" submitted for voting (voting process with id " + vote_id + ").";
                                assertEquals(expected, result);
                                // Verify that action was accepted
                                lines2 = frontend.viewHistory(id);
                                expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    // Until action accepted, User[Q-N] accept the action through voting
                                    frontend.getAccountInfo(users0[i].name, users0[i].password);
                                    frontend.vote(id, vote_id, true);
                                    lines = baos.toString().split("\n");
                                    result = lines[lines.length-1];
                                    expected = "Vote submitted successfully.";
                                    assertEquals(expected, result);
                                    lines2 = frontend.viewHistory(id);
                                    expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                }
                                vote_id++;
                            }
                        }
                        
                        // Community votes to lower delete message threshold
                        frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                        frontend.thresholdVote(id, Daoliberate.ACTION.DELETE_MESSAGE, 0.5);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "Threshold change submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Threshold changed for action: Delete a message\nNew defined threshold: 0.5";
                        for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Threshold changed for action: Delete a message\nNew defined threshold: 0.5";
                        }
                        vote_id++;

                        /*= UserQ send a message =*/
                        frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                        frontend.createMessage(id, "channel", "Hello");

                        // User0, User1, ..., UserQ-1 tries to delete message and is put to a vote
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        frontend.deleteMessage(id, "channel", 1, Daoliberate.REASON.OFFENSIVE);
                        lines2 = frontend.viewHistory(id);
                        expected = "Message deleted (with id: 1)";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                            // Until action accepted, User[1-(Q-1)] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Message deleted (with id: 1)";
                        }
                        vote_id++;

                        // User0, User1, ..., UserQ-1 could not delete message
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    id++;
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

            for(int N = 3, id = (MAX_USERS*3)-6; N <= MAX_USERS; N++) {
                int MAX_Q = ((N % 2 == 1)? N/2 : (N/2) - 1);
                for(int Q = 2; Q <= MAX_Q; Q++) {
                    int vote_id = 0;
                    ByteArrayInputStream bais = new ByteArrayInputStream(users0[0].pseudonym.getBytes());
                    System.setIn(bais);
                    System.err.println("-->N=" + N + " id=" + id + " Q=" + Q + "<--");

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
                        for(int j = 0; j < Q; j++) {
                            /*= User0, User1, ..., UserQ-1 changes its reputation to the maximum value =*/
                            frontend.getAccountInfo(users0[j].name, users0[j].password);
                            String details = String.join(",", frontend.enterRoom(id));

                            // Until User has maximum reputation he creates voting processes which are accepted
                            while(!details.contains("| Reputation: " + (N-2.0))) {
                                // User0 create a new channel
                                frontend.createChannel(id, "channel");
                                List<String> lines2 = frontend.viewHistory(id);
                                expected = "Channel created with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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

                                // User delete a channel
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                frontend.deleteChannel(id, "channel");
                                lines2 = frontend.viewHistory(id);
                                expected = "Channel deleted with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                details = String.join(",", frontend.enterRoom(id));
                            }
                        }
                    }

                    /*= User0, User1, ..., UserQ-1 try to punish user =*/
                    // User0 initiate a voting process to punish user
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    frontend.punishUser(id, users0[Q].pseudonym, 0, 1, 0);
                    List<String> lines2 = frontend.viewHistory(id);
                    expected = "User punished (with name: " + users0[Q].pseudonym + ")";
                    for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                        // Until action accepted, User[1-(Q-1)] accept the action through voting
                        frontend.getAccountInfo(users0[i].name, users0[i].password);
                        frontend.vote(id, vote_id, true);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "User punished (with name: " + users0[Q].pseudonym + ")";
                    }
                    vote_id++;
                    if(!lines2.get(lines2.size()-1).contains(expected)) {
                        // User0, User1, ..., UserQ-1 could not punish user
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    else {
                        // User0, User1, ..., UserQ-1 punish a user for no reason
                        if(N > 3) {
                            // Community votes to lower User0's, User1's, ..., UserQ-1's reputation
                            for(int j = 0; j < Q; j++) {
                                frontend.getAccountInfo(users0[Q+1].name, users0[Q+1].password);
                                frontend.setReputation(id, users0[j].pseudonym, 1);
                                lines = baos.toString().split("\n");
                                result = lines[lines.length-2];
                                expected = "New reputation of \"" + users0[j].pseudonym + "\" submitted for voting (voting process with id " + vote_id + ").";
                                assertEquals(expected, result);
                                // Verify that action was accepted
                                lines2 = frontend.viewHistory(id);
                                expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                for(int i = Q; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == Q+1) {
                                        continue;
                                    }
                                    // Until action accepted, User[Q-N] accept the action through voting
                                    frontend.getAccountInfo(users0[i].name, users0[i].password);
                                    frontend.vote(id, vote_id, true);
                                    lines = baos.toString().split("\n");
                                    result = lines[lines.length-1];
                                    expected = "Vote submitted successfully.";
                                    assertEquals(expected, result);
                                    lines2 = frontend.viewHistory(id);
                                    expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                }
                                vote_id++;
                            }
                        }
                        
                        // Community votes to lower punish user threshold
                        frontend.getAccountInfo(users0[Q+1].name, users0[Q+1].password);
                        frontend.thresholdVote(id, Daoliberate.ACTION.PUNISH_USER, 0.3);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "Threshold change submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Threshold changed for action: Punish/Unpunish a user\nNew defined threshold: 0.3";
                        for(int i = Q; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            if(i == Q+1) {
                                continue;
                            }
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Threshold changed for action: Punish/Unpunish a user\nNew defined threshold: 0.3";
                        }
                        vote_id++;

                        /*= Community remove punishment =*/
                        frontend.getAccountInfo(users0[Q+1].name, users0[Q+1].password);
                        frontend.punishUser(id, users0[Q].pseudonym, 0, 0, 0);
                        lines2 = frontend.viewHistory(id);
                        expected = "User unpunished (with name: " + users0[Q].pseudonym + ")";
                        for(int i = Q; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            if(i == Q+1) {
                                continue;
                            }
                            // Until action accepted, User[1-(Q-1)] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "User unpunished (with name: " + users0[Q].pseudonym + ")";
                        }
                        vote_id++;
                        assertTrue(lines2.get(lines2.size()-1).contains(expected));

                        // Community votes to upper punish user threshold
                        frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                        frontend.thresholdVote(id, Daoliberate.ACTION.PUNISH_USER, 0.8);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "Threshold change submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Threshold changed for action: Punish/Unpunish a user\nNew defined threshold: 0.8";
                        for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Threshold changed for action: Punish/Unpunish a user\nNew defined threshold: 0.8";
                        }
                        vote_id++;

                        // User0, User1, ..., UserQ-1 tries to punish user and is put to a vote
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        frontend.punishUser(id, users0[Q].pseudonym, 0, 1, 0);
                        lines2 = frontend.viewHistory(id);
                        expected = "User punished (with name: " + users0[Q].pseudonym + ")";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                            // Until action accepted, User[1-(Q-1)] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "User punished (with name: " + users0[Q].pseudonym + ")";
                        }
                        vote_id++;

                        // User0, User1, ..., UserQ-1 could not punish
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    id++;
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
  	public void RoomOfNEditMessage() {
        System.err.println("========== EDIT MESSAGE ==========");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            System.setOut(printStream);

            for(int N = 3, id = (MAX_USERS*4)-8; N <= MAX_USERS; N++) {
                int MAX_Q = ((N % 2 == 1)? N/2 : (N/2) - 1);
                for(int Q = 2; Q <= MAX_Q; Q++) {
                    int vote_id = 0;
                    ByteArrayInputStream bais = new ByteArrayInputStream(users0[0].pseudonym.getBytes());
                    System.setIn(bais);
                    System.err.println("-->N=" + N + " id=" + id + " Q=" + Q + "<--");

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
                        for(int j = 0; j < Q; j++) {
                            /*= User0, User1, ..., UserQ-1 changes its reputation to the maximum value =*/
                            frontend.getAccountInfo(users0[j].name, users0[j].password);
                            String details = String.join(",", frontend.enterRoom(id));

                            // Until User has maximum reputation he creates voting processes which are accepted
                            while(!details.contains("| Reputation: " + (N-2.0))) {
                                // User0 create a new channel
                                frontend.createChannel(id, "channel");
                                List<String> lines2 = frontend.viewHistory(id);
                                expected = "Channel created with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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

                                // User delete a channel
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                frontend.deleteChannel(id, "channel");
                                lines2 = frontend.viewHistory(id);
                                expected = "Channel deleted with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                details = String.join(",", frontend.enterRoom(id));
                            }
                        }
                    }

                    // Community create channel
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
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

                    /*= UserQ send a message =*/
                    frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                    frontend.createMessage(id, "channel", "Hello");

                    /*= User0, User1, ..., UserQ-1 try to edit message =*/
                    // User0 initiate a voting process to edit the message
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    frontend.editMessage(id, "channel", 0, "Hello XD XD XD !!!", Daoliberate.REASON.OFFENSIVE);
                    lines2 = frontend.viewHistory(id);
                    expected = "Message edited (with id: 0)";
                    for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                        // Until action accepted, User[1-(Q-1)] accept the action through voting
                        frontend.getAccountInfo(users0[i].name, users0[i].password);
                        frontend.vote(id, vote_id, true);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "Message edited (with id: 0)";
                    }
                    vote_id++;
                    if(!lines2.get(lines2.size()-1).contains(expected)) {
                        // User0, User1, ..., UserQ-1 could not edit message
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    else {
                        // User0, User1, ..., UserQ-1 edited a message without reason
                        if(N > 3) {
                            // Community votes to lower User0's, User1's, ..., UserQ-1's reputation
                            for(int j = 0; j < Q; j++) {
                                frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                                frontend.setReputation(id, users0[j].pseudonym, 1);
                                lines = baos.toString().split("\n");
                                result = lines[lines.length-2];
                                expected = "New reputation of \"" + users0[j].pseudonym + "\" submitted for voting (voting process with id " + vote_id + ").";
                                assertEquals(expected, result);
                                // Verify that action was accepted
                                lines2 = frontend.viewHistory(id);
                                expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    // Until action accepted, User[Q-N] accept the action through voting
                                    frontend.getAccountInfo(users0[i].name, users0[i].password);
                                    frontend.vote(id, vote_id, true);
                                    lines = baos.toString().split("\n");
                                    result = lines[lines.length-1];
                                    expected = "Vote submitted successfully.";
                                    assertEquals(expected, result);
                                    lines2 = frontend.viewHistory(id);
                                    expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                }
                                vote_id++;
                            }
                        }
                        
                        // Community votes to upper edit message threshold
                        frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                        frontend.thresholdVote(id, Daoliberate.ACTION.EDIT_MESSAGE, 0.5);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "Threshold change submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Threshold changed for action: Edit a message\nNew defined threshold: 0.5";
                        for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
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

                        /*= Community edit message again =*/
                        frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                        frontend.editMessage(id, "channel", 0, "Hello", Daoliberate.REASON.OFFENSIVE);
                        lines2 = frontend.viewHistory(id);
                        expected = "Message edited (with id: 0)";
                        for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-(Q-1)] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Message edited (with id: 0)";
                        }
                        vote_id++;
                        assertTrue(lines2.get(lines2.size()-1).contains(expected));

                        // User0, User1, ..., UserQ-1 tries to edit message and is put to a vote
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        frontend.editMessage(id, "channel", 0, "Hello XD XD XD !!!", Daoliberate.REASON.OFFENSIVE);
                        lines2 = frontend.viewHistory(id);
                        expected = "Message edited (with id: 0)";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                            // Until action accepted, User[1-(Q-1)] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Message edited (with id: 0)";
                        }
                        vote_id++;

                        // User0, User1, ..., UserQ-1 could not edit message
                        assertTrue(!lines2.get(lines2.size()-1).contains("New message content: Hello XD XD XD !!!"));
                    }
                    id++;
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
  	public void RoomOfNPinMessage() {
        System.err.println("========== PIN MESSAGE ==========");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            System.setOut(printStream);

            for(int N = 3, id = (MAX_USERS*5)-10; N <= MAX_USERS; N++) {
                int MAX_Q = ((N % 2 == 1)? N/2 : (N/2) - 1);
                for(int Q = 2; Q <= MAX_Q; Q++) {
                    int vote_id = 0;
                    ByteArrayInputStream bais = new ByteArrayInputStream(users0[0].pseudonym.getBytes());
                    System.setIn(bais);
                    System.err.println("-->N=" + N + " id=" + id + " Q=" + Q + "<--");

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
                        for(int j = 0; j < Q; j++) {
                            /*= User0, User1, ..., UserQ-1 changes its reputation to the maximum value =*/
                            frontend.getAccountInfo(users0[j].name, users0[j].password);
                            String details = String.join(",", frontend.enterRoom(id));

                            // Until User has maximum reputation he creates voting processes which are accepted
                            while(!details.contains("| Reputation: " + (N-2.0))) {
                                // User0 create a new channel
                                frontend.createChannel(id, "channel");
                                List<String> lines2 = frontend.viewHistory(id);
                                expected = "Channel created with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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

                                // User delete a channel
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                frontend.deleteChannel(id, "channel");
                                lines2 = frontend.viewHistory(id);
                                expected = "Channel deleted with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                details = String.join(",", frontend.enterRoom(id));
                            }
                        }
                    }

                    // Community create channel
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
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

                    /*= UserQ send a message =*/
                    frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                    frontend.createMessage(id, "channel", "Hello");

                    /*= User0, User1, ..., UserQ-1 try to pin message =*/
                    // User0 initiate a voting process to pin the message
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    frontend.pinMessage(id, "channel", 0);
                    lines2 = frontend.viewHistory(id);
                    expected = "Message pinned (with id: 0)";
                    for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                        // Until action accepted, User[1-(Q-1)] accept the action through voting
                        frontend.getAccountInfo(users0[i].name, users0[i].password);
                        frontend.vote(id, vote_id, true);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "Message pinned (with id: 0)";
                    }
                    vote_id++;
                    if(!lines2.get(lines2.size()-1).contains(expected)) {
                        // User0, User1, ..., UserQ-1 could not pin message
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    else {
                        // User0, User1, ..., UserQ-1 pinned a message without reason
                        if(N > 3) {
                            // Community votes to lower User0's, User1's, ..., UserQ-1's reputation
                            for(int j = 0; j < Q; j++) {
                                frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                                frontend.setReputation(id, users0[j].pseudonym, 1);
                                lines = baos.toString().split("\n");
                                result = lines[lines.length-2];
                                expected = "New reputation of \"" + users0[j].pseudonym + "\" submitted for voting (voting process with id " + vote_id + ").";
                                assertEquals(expected, result);
                                // Verify that action was accepted
                                lines2 = frontend.viewHistory(id);
                                expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    // Until action accepted, User[Q-N] accept the action through voting
                                    frontend.getAccountInfo(users0[i].name, users0[i].password);
                                    frontend.vote(id, vote_id, true);
                                    lines = baos.toString().split("\n");
                                    result = lines[lines.length-1];
                                    expected = "Vote submitted successfully.";
                                    assertEquals(expected, result);
                                    lines2 = frontend.viewHistory(id);
                                    expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                }
                                vote_id++;
                            }
                        }
                        
                        // Community votes to upper pin message threshold
                        frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                        frontend.thresholdVote(id, Daoliberate.ACTION.PIN_MESSAGE, 0.5);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "Threshold change submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Threshold changed for action: Pin a message\nNew defined threshold: 0.5";
                        for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
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

                        /*= Community unpin message =*/
                        frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                        frontend.unpinMessage(id, "channel", 0);
                        lines2 = frontend.viewHistory(id);
                        expected = "Message unpinned (with id: 0)";
                        for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-(Q-1)] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Message unpinned (with id: 0)";
                        }
                        vote_id++;
                        assertTrue(lines2.get(lines2.size()-1).contains(expected));

                        // User0, User1, ..., UserQ-1 tries to pin message and is put to a vote
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        frontend.pinMessage(id, "channel", 0);
                        lines2 = frontend.viewHistory(id);
                        expected = "Message pinned (with id: 0)";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                            // Until action accepted, User[1-(Q-1)] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Message pinned (with id: 0)";
                        }
                        vote_id++;

                        // User0, User1, ..., UserQ-1 could not pin message
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    id++;
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
  	public void RoomOfNUnpinMessage() {
        System.err.println("========== UNPIN MESSAGE ==========");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            System.setOut(printStream);

            for(int N = 3, id = (MAX_USERS*6)-12; N <= MAX_USERS; N++) {
                int MAX_Q = ((N % 2 == 1)? N/2 : (N/2) - 1);
                for(int Q = 2; Q <= MAX_Q; Q++) {
                    int vote_id = 0;
                    ByteArrayInputStream bais = new ByteArrayInputStream(users0[0].pseudonym.getBytes());
                    System.setIn(bais);
                    System.err.println("-->N=" + N + " id=" + id + " Q=" + Q + "<--");

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
                        for(int j = 0; j < Q; j++) {
                            /*= User0, User1, ..., UserQ-1 changes its reputation to the maximum value =*/
                            frontend.getAccountInfo(users0[j].name, users0[j].password);
                            String details = String.join(",", frontend.enterRoom(id));

                            // Until User has maximum reputation he creates voting processes which are accepted
                            while(!details.contains("| Reputation: " + (N-2.0))) {
                                // User0 create a new channel
                                frontend.createChannel(id, "channel");
                                List<String> lines2 = frontend.viewHistory(id);
                                expected = "Channel created with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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

                                // User delete a channel
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                frontend.deleteChannel(id, "channel");
                                lines2 = frontend.viewHistory(id);
                                expected = "Channel deleted with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                details = String.join(",", frontend.enterRoom(id));
                            }
                        }
                    }

                    // Community create channel
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
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

                    /*= UserQ send a message =*/
                    frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                    frontend.createMessage(id, "channel", "Hello");

                    // Community pin message
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    frontend.pinMessage(id, "channel", 0);
                    lines2 = frontend.viewHistory(id);
                    expected = "Message pinned (with id: 0)";
                    for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                        // Until action accepted, User[1-N] accept the action through voting
                        frontend.getAccountInfo(users0[i].name, users0[i].password);
                        frontend.vote(id, vote_id, true);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "Message pinned (with id: 0)";
                    }
                    vote_id++;

                    /*= User0, User1, ..., UserQ-1 try to unpin message =*/
                    // User0 initiate a voting process to unpin the message
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    frontend.unpinMessage(id, "channel", 0);
                    lines2 = frontend.viewHistory(id);
                    expected = "Message unpinned (with id: 0)";
                    for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                        // Until action accepted, User[1-(Q-1)] accept the action through voting
                        frontend.getAccountInfo(users0[i].name, users0[i].password);
                        frontend.vote(id, vote_id, true);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "Message unpinned (with id: 0)";
                    }
                    vote_id++;
                    if(!lines2.get(lines2.size()-1).contains(expected)) {
                        // User0, User1, ..., UserQ-1 could not unpin message
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    else {
                        // User0, User1, ..., UserQ-1 unpinned a important message
                        if(N > 3) {
                            // Community votes to lower User0's, User1's, ..., UserQ-1's reputation
                            for(int j = 0; j < Q; j++) {
                                frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                                frontend.setReputation(id, users0[j].pseudonym, 1);
                                lines = baos.toString().split("\n");
                                result = lines[lines.length-2];
                                expected = "New reputation of \"" + users0[j].pseudonym + "\" submitted for voting (voting process with id " + vote_id + ").";
                                assertEquals(expected, result);
                                // Verify that action was accepted
                                lines2 = frontend.viewHistory(id);
                                expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    // Until action accepted, User[Q-N] accept the action through voting
                                    frontend.getAccountInfo(users0[i].name, users0[i].password);
                                    frontend.vote(id, vote_id, true);
                                    lines = baos.toString().split("\n");
                                    result = lines[lines.length-1];
                                    expected = "Vote submitted successfully.";
                                    assertEquals(expected, result);
                                    lines2 = frontend.viewHistory(id);
                                    expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                }
                                vote_id++;
                            }
                        }
                        
                        // Community votes to upper unpin message threshold
                        frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                        frontend.thresholdVote(id, Daoliberate.ACTION.UNPIN_MESSAGE, 0.5);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "Threshold change submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Threshold changed for action: Unpin a message\nNew defined threshold: 0.5";
                        for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
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

                        /*= Community pin message =*/
                        frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                        frontend.pinMessage(id, "channel", 0);
                        lines2 = frontend.viewHistory(id);
                        expected = "Message pinned (with id: 0)";
                        for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-(Q-1)] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Message pinned (with id: 0)";
                        }
                        vote_id++;
                        assertTrue(lines2.get(lines2.size()-1).contains(expected));

                        // User0, User1, ..., UserQ-1 tries to unpin message and is put to a vote
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        frontend.unpinMessage(id, "channel", 0);
                        lines2 = frontend.viewHistory(id);
                        expected = "Message unpinned (with id: 0)";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                            // Until action accepted, User[1-(Q-1)] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Message unpinned (with id: 0)";
                        }
                        vote_id++;

                        // User0, User1, ..., UserQ-1 could not unpin message
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    id++;
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
  	public void RoomOfNDeleteUser() {
        System.err.println("========== DELETE USER ==========");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(baos);
            System.setOut(printStream);

            for(int N = 3, id = (MAX_USERS*7)-14; N <= MAX_USERS; N++) {
                int MAX_Q = ((N % 2 == 1)? N/2 : (N/2) - 1);
                for(int Q = 2; Q <= MAX_Q; Q++) {
                    int vote_id = 0;
                    ByteArrayInputStream bais = new ByteArrayInputStream(users0[0].pseudonym.getBytes());
                    System.setIn(bais);
                    System.err.println("-->N=" + N + " id=" + id + " Q=" + Q + "<--");

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
                        for(int j = 0; j < Q; j++) {
                            /*= User0, User1, ..., UserQ-1 changes its reputation to the maximum value =*/
                            frontend.getAccountInfo(users0[j].name, users0[j].password);
                            String details = String.join(",", frontend.enterRoom(id));

                            // Until User has maximum reputation he creates voting processes which are accepted
                            while(!details.contains("| Reputation: " + (N-2.0))) {
                                // User0 create a new channel
                                frontend.createChannel(id, "channel");
                                List<String> lines2 = frontend.viewHistory(id);
                                expected = "Channel created with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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

                                // User delete a channel
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                frontend.deleteChannel(id, "channel");
                                lines2 = frontend.viewHistory(id);
                                expected = "Channel deleted with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                details = String.join(",", frontend.enterRoom(id));
                            }
                        }
                    }

                    /*= User0, User1, ..., UserQ-1 try to delete user UserQ =*/
                    // User0 initiate a voting process to delete user UserQ
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    frontend.deleteUser(id, users0[Q].pseudonym);
                    List<String> lines2 = frontend.viewHistory(id);
                    expected = "User marked to be deleted (with name: " + users0[Q].pseudonym + ") from room";
                    for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                        // Until action accepted, User[1-(Q-1)] accept the action through voting
                        frontend.getAccountInfo(users0[i].name, users0[i].password);
                        frontend.vote(id, vote_id, true);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "User marked to be deleted (with name: " + users0[Q].pseudonym + ") from room";
                    }
                    vote_id++;
                    
                    if(!lines2.get(lines2.size()-1).contains(expected)) {
                        // User0, User1, ..., UserQ-1 could not delete user UserQ
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    else {
                        // User0, User1, ..., UserQ-1 deleted user UserQ
                        if(N > 3) {
                            // Community votes to lower User0's, User1's, ..., UserQ-1's reputation
                            for(int j = 0; j < Q; j++) {
                                frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                                frontend.setReputation(id, users0[j].pseudonym, 1);
                                lines = baos.toString().split("\n");
                                result = lines[lines.length-2];
                                expected = "New reputation of \"" + users0[j].pseudonym + "\" submitted for voting (voting process with id " + vote_id + ").";
                                assertEquals(expected, result);
                                // Verify that action was accepted
                                lines2 = frontend.viewHistory(id);
                                expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    // Until action accepted, User[Q-N] accept the action through voting
                                    frontend.getAccountInfo(users0[i].name, users0[i].password);
                                    frontend.vote(id, vote_id, true);
                                    lines = baos.toString().split("\n");
                                    result = lines[lines.length-1];
                                    expected = "Vote submitted successfully.";
                                    assertEquals(expected, result);
                                    lines2 = frontend.viewHistory(id);
                                    expected = "Reputation of \"" + users0[j].pseudonym + "\" changed to: 1.0";
                                }
                                vote_id++;
                            }
                        }
                        
                        // Community votes to upper delete user threshold
                        frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                        frontend.thresholdVote(id, Daoliberate.ACTION.DELETE_USER, 0.5);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-2];
                        expected = "Threshold change submitted for voting (voting process with id " + vote_id + ").";
                        assertEquals(expected, result);
                        // Verify that action was accepted
                        lines2 = frontend.viewHistory(id);
                        expected = "Threshold changed for action: Delete a user\nNew defined threshold: 0.5";
                        for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-N] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "Threshold changed for action: Delete a user\nNew defined threshold: 0.5";
                        }
                        vote_id++;

                        /*= Community cancel deleted user =*/
                        frontend.getAccountInfo(users0[Q].name, users0[Q].password);
                        frontend.punishUser(id, users0[Q].pseudonym, 0, 0, 0);
                        lines2 = frontend.viewHistory(id);
                        expected = "User unpunished (with name: " + users0[Q].pseudonym + ")";
                        for(int i = Q+1; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                            // Until action accepted, User[1-(Q-1)] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "User unpunished (with name: " + users0[Q].pseudonym + ")";
                        }
                        vote_id++;
                        assertTrue(lines2.get(lines2.size()-1).contains(expected));

                        // User0, User1, ..., UserQ-1 tries to delete user UserQ and is put to a vote
                        frontend.getAccountInfo(users0[0].name, users0[0].password);
                        frontend.deleteUser(id, users0[Q].pseudonym);
                        lines2 = frontend.viewHistory(id);
                        expected = "User marked to be deleted (with name: " + users0[Q].pseudonym + ") from room";
                        for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                            // Until action accepted, User[1-(Q-1)] accept the action through voting
                            frontend.getAccountInfo(users0[i].name, users0[i].password);
                            frontend.vote(id, vote_id, true);
                            lines = baos.toString().split("\n");
                            result = lines[lines.length-1];
                            expected = "Vote submitted successfully.";
                            assertEquals(expected, result);
                            lines2 = frontend.viewHistory(id);
                            expected = "User marked to be deleted (with name: " + users0[Q].pseudonym + ") from room";
                        }
                        vote_id++;

                        // User0, User1, ..., UserQ-1 could not unpin message
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    id++;
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

            for(int N = 3, id = (MAX_USERS*8)-16; N <= MAX_USERS; N++) {
                int MAX_Q = ((N % 2 == 1)? N/2 : (N/2) - 1);
                for(int Q = 2; Q <= MAX_Q; Q++) {
                    int vote_id = 0;
                    ByteArrayInputStream bais = new ByteArrayInputStream(users0[0].pseudonym.getBytes());
                    System.setIn(bais);
                    System.err.println("-->N=" + N + " id=" + id + " Q=" + Q + "<--");

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
                        for(int j = 0; j < Q; j++) {
                            /*= User0, User1, ..., UserQ-1 changes its reputation to the maximum value =*/
                            frontend.getAccountInfo(users0[j].name, users0[j].password);
                            String details = String.join(",", frontend.enterRoom(id));

                            // Until User has maximum reputation he creates voting processes which are accepted
                            while(!details.contains("| Reputation: " + (N-2.0))) {
                                // User0 create a new channel
                                frontend.createChannel(id, "channel");
                                List<String> lines2 = frontend.viewHistory(id);
                                expected = "Channel created with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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

                                // User delete a channel
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                frontend.deleteChannel(id, "channel");
                                lines2 = frontend.viewHistory(id);
                                expected = "Channel deleted with name \"channel\" through a voting process";
                                for(int i = 0; !lines2.get(lines2.size()-1).contains(expected) && i < N; i++) {
                                    if(i == j) {
                                        continue;
                                    }
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
                                frontend.getAccountInfo(users0[j].name, users0[j].password);
                                details = String.join(",", frontend.enterRoom(id));
                            }
                        }
                    }

                    /*= User0, User1, ..., UserQ-1 try to invite Evil user =*/
                    // User0 initiate a voting process to invite Evil user
                    frontend.getAccountInfo(users0[0].name, users0[0].password);
                    frontend.inviteUser(id, "Evil");
                    List<String> lines2 = frontend.viewHistory(id);
                    expected = "User invited (with name: Evil)";
                    for(int i = 1; !lines2.get(lines2.size()-1).contains(expected) && i < Q; i++) {
                        // Until action accepted, User[1-(Q-1)] accept the action through voting
                        frontend.getAccountInfo(users0[i].name, users0[i].password);
                        frontend.vote(id, vote_id, true);
                        lines = baos.toString().split("\n");
                        result = lines[lines.length-1];
                        expected = "Vote submitted successfully.";
                        assertEquals(expected, result);
                        lines2 = frontend.viewHistory(id);
                        expected = "User invited (with name: Evil)";
                    }
                    vote_id++;
                    
                    if(!lines2.get(lines2.size()-1).contains(expected)) {
                        // User0, User1, ..., UserQ-1 could not invite Evil user
                        assertTrue(!lines2.get(lines2.size()-1).contains(expected));
                    }
                    id++;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            fail();
        }
  	}
}
