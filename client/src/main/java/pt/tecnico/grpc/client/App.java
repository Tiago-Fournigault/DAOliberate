package pt.tecnico.grpc.client;

import java.util.Scanner;
import java.io.Console;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import pt.tecnico.grpc.Daoliberate;

/**
 * The App class is the client application used by the user to interact in the chat system.
 */
public class App {

    static String title =           "  _____            ____   _  _  _                        _        \n"
                            .concat(" |  __ \\    /\\    / __ \\ | |(_)| |                      | |       \n")
                            .concat(" | |  | |  /  \\  | |  | || | _ | |__    ___  _ __  __ _ | |_  ___ \n")
                            .concat(" | |  | | / /\\ \\ | |  | || || || '_ \\  / _ \\| '__|/ _` || __|/ _ \\\n")
                            .concat(" | |__| |/ ____ \\| |__| || || || |_) ||  __/| |  | (_| || |_|  __/\n")
                            .concat(" |_____//_/    \\_\\\\____/ |_||_||_.__/  \\___||_|   \\__,_| \\__|\\___|\n")
                            .concat("                                                                  \n");

	public static void main(String[] args) throws Exception {
		System.out.println(App.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 4) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s host port%n", App.class.getName());
			return;
		}

        ClientLibrary library = new ClientLibrary(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));

		Scanner scanner = new Scanner(System.in);
        Console console = System.console();

        System.out.println(title);
        
        String name;
        char[] password;
		char option = '0';
		while (option != '4') {

			System.out.println("===== Menu =====");
            System.out.println("1 - Ping server");
            System.out.println("2 - Log in");
            System.out.println("3 - Sign up");
			System.out.println("4 - Exit");

			System.out.print("Choose an option: ");
            try{
                option = scanner.nextLine().charAt(0);
            } catch (Exception e){
                option = ' ';
            }
			switch (option) {
				case '1':
					System.out.println("\n" + library.pingDao());
                    System.out.println(library.pingRegister() + "\n");
					break;
                case '2':
                    // get name
                    System.out.print("\nInsert name: ");
					name  = scanner.nextLine();
                    // get password
                    password = console.readPassword("Insert password: ");
                    for(int i = 0; i < password.length; i++) {
                        System.out.print("*");
                    }
                    System.out.println("\n");

                    if(library.login(name, new String(password))) {
                        loggedMenu(name, new String(password), library);
                    }
                    
					break;
                case '3':
                    // get new name
					System.out.print("\nInsert new account name: ");
					name  = scanner.nextLine();
                    // get new password
                    password = console.readPassword("Insert new account password (at least 10 characters, with at least one capital letter, one small and one number): ");
                    for(int i = 0; i < password.length; i++) {
                        System.out.print("*");
                    }
                    System.out.println("\n");

                    if(library.signup(name, new String(password))) {
                        loggedMenu(name, new String(password), library);
                    }
                    
					break;
				case '4':
					System.out.println("\nExiting...");
					System.out.println("Goodbye and come back soon!");
                    return;
				default:
					System.out.println("\nInvalid operation!\n");
					break;
			}

		}
	}

    public static void loggedMenu(String name, String password, ClientLibrary library) throws Exception {
        
        Scanner scanner = new Scanner(System.in);
        Console console = System.console();

        library.getAccountInfo(name, password);

        String room_name, pseudonym, channel_name, initial_users, response;
        int room_id;
        
		char option = '0';
		while (option != '5') {

            showProfilePage(name, library);

			System.out.println("\nOptions:");
            System.out.println("1 - Enter in a room");
            System.out.println("2 - Create a new room");
            System.out.println("3 - Join a new room via initial secret");
            System.out.println("4 - Check room invites");
			System.out.println("5 - Logout");

			System.out.print("Choose an option: ");
            try{
                option = scanner.nextLine().charAt(0);
            } catch (Exception e){
                option = ' ';
            }
			switch (option) {
				case '1':
					System.out.print("Insert the room id: ");
					room_id  = scanner.nextInt();
                    scanner.nextLine();

                    if(!library.checkRoomAccess(room_id)) {
                        System.out.println("\nYou don't belong to this chat room.\n");
                        break;
                    }

                    if(showRoomChannels(room_id, library)) {
                        chatRoom(room_id, library);
                    }

					break;
                case '2':
                    System.out.println("\nCreating new room...");
                    System.out.print("Insert the name of the new room: ");
					room_name  = scanner.nextLine();
                    
                    System.out.print("Do you want to invite users? (y/n): ");
                    response = scanner.nextLine();

                    String[] names = {};
                    if(response.equals("y")) {
                        System.out.print("Write the names of the people you want to invite, separated by commas.");
                        System.out.println(" (Ex: Tiago, João, Miguel): ");
                        initial_users = scanner.nextLine();
                        names = initial_users.replaceAll("^[,\\s]+", "").split("[\\s]*,[\\s]*");
                    }
                    
                    library.createRoom(name, password, room_name, names);

					break;
                case '3':
                    if(showSecrets(name, password, library)) {
                        secretsMenu(name, password, library);
                    }
                    System.out.println();
                    break;
                case '4':
                    if(showInvites(name, password, library)) {
                        invitesMenu(name, password, library);
                    }
                    System.out.println();
                    break;
                case '5':
                    System.out.println("\nLogging out...");
					System.out.println("Logged out!");
                    return;
				default:
					System.out.println("\nInvalid operation!\n");
					break;
			}

		}
    }

    public static void invitesMenu(String name, String password, ClientLibrary library) {
        Scanner scanner = new Scanner(System.in);

        char option = '0';
		while (option != '2') {

			System.out.println("\nOptions:");
            System.out.println("1 - Use an invite");
            System.out.println("2 - Leave invites section");

			System.out.print("Choose an option: ");
            try{
                option = scanner.nextLine().charAt(0);
            } catch (Exception e){
                option = ' ';
            }
			switch (option) {
				case '1':
					System.out.print("\nInsert the id of the room you want to join via invite: ");
                    int room_id  = scanner.nextInt();
                    scanner.nextLine();

                    Invite invite = library.getInvite(name, password, room_id);
                    if(invite != null) {
                        Daoliberate.STATUS status = null;
                        do {
                            System.out.print("Insert the pseudonym to be used in this room: ");
                            String pseudonym  = scanner.nextLine();
                            status = library.useInvite(invite, room_id, pseudonym);
                        } while(status == Daoliberate.STATUS.NAME_EXISTS);
                    }

					break;
                case '2':
                    System.out.println("\nLeaving invite section...");
                    System.out.println();
                    return;
				default:
					System.out.println("\nInvalid operation!\n");
					break;
			}

            showInvites(name, password, library);
		}
    }

    public static void secretsMenu(String name, String password, ClientLibrary library) {
        Scanner scanner = new Scanner(System.in);

        char option = '0';
		while (option != '2') {

			System.out.println("\nOptions:");
            System.out.println("1 - Use an initial secret");
            System.out.println("2 - Leave initial secrets section");

			System.out.print("Choose an option: ");
            try{
                option = scanner.nextLine().charAt(0);
            } catch (Exception e){
                option = ' ';
            }
			switch (option) {
				case '1':
					System.out.print("\nInsert the id of the room you want to join via initial secret: ");
                    int room_id  = scanner.nextInt();
                    scanner.nextLine();

                    if(library.checkRoomAccess(room_id)) {
                        System.out.println("You already belong to this chat room.");
                        break;
                    }

                    String secret = library.getInitialSecret(name, password, room_id);
                    if(secret != null) {
                        Daoliberate.STATUS status = null;
                        do {
                            System.out.print("Insert the pseudonym to be used in this room: ");
                            String pseudonym  = scanner.nextLine();
                            status = library.ingressRoom(room_id, pseudonym, secret);
                        } while(status == Daoliberate.STATUS.NAME_EXISTS);
                    }

					break;
                case '2':
                    System.out.println("\nLeaving initial secrets section...");
                    System.out.println();
                    return;
				default:
					System.out.println("\nInvalid operation!\n");
					break;
			}

            showSecrets(name, password, library);
		}
    }

    public static void chatRoom(int room_id, ClientLibrary library) {
        Scanner scanner = new Scanner(System.in);

        String channel_name, invites;
        char option = '0';
		while (option != '5') {

			System.out.println("\nOptions:");
            System.out.println("1 - Enter in a channel");
            System.out.println("2 - Moderation actions");
            System.out.println("3 - View open voting processes");
            System.out.println("4 - View the room's action history");
            System.out.println("5 - Leave room");

			System.out.print("Choose an option: ");
            try{
                option = scanner.nextLine().charAt(0);
            } catch (Exception e){
                option = ' ';
            }
			switch (option) {
				case '1':
                    System.out.print("Insert the channel name: ");
					channel_name  = scanner.nextLine();
                    
                    if(showChannelMessages(room_id, channel_name, library)) {
                        chatChannel(room_id, channel_name, library);
                    }
					break;
                case '2':
                    actionsMenu(room_id, library);
                    break;
                case '3':
                    votingMenu(room_id, library);
                    break;
                case '4':
                    try{
                        for(String string : library.viewHistory(room_id)) {
                            System.out.println(string);
                        }
                    } catch(NullPointerException e){}
                    
                    break;
                case '5':
                    System.out.println("\nLeaving this room...");
                    System.out.println();
                    return;
				default:
					System.out.println("\nInvalid operation!\n");
					break;
			}

            showRoomChannels(room_id, library);
		}
    }

    public static void votingMenu(int room_id, ClientLibrary library) {
        Scanner scanner = new Scanner(System.in);

        char option = '0';
		while (option != '4') {
            try {
                for(String string : library.viewVotes(room_id)) {
                    System.out.println(string);
                }
            } catch(NullPointerException e){return;}

			System.out.println("\nOptions:");
            System.out.println("1 - Send vote");
            System.out.println("2 - Add description to my voting process");
            System.out.println("3 - Cancel a voting process initiated by me");
            System.out.println("4 - Leave voting menu");

            int vote_id;
            String response;

			System.out.print("Choose an option: ");
            try{
                option = scanner.nextLine().charAt(0);
            } catch (Exception e){
                option = ' ';
            }
			switch (option) {
				case '1':
                    System.out.print("Enter the voting process id: ");
					vote_id  = scanner.nextInt();
                    scanner.nextLine();
                    
                    System.out.print("Vote yes or no? (y/n): ");
                    response = scanner.nextLine();

                    boolean choice;
                    if(response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes")) {
                        choice = true;
                    }
                    else if(response.equalsIgnoreCase("n") || response.equalsIgnoreCase("no")) {
                        choice = false;
                    }
                    else{
                        System.out.println("Invalid voting option.");
                        break;
                    }

                    library.vote(room_id, vote_id, choice);
					break;
                case '2':
                    System.out.print("Enter the voting process id: ");
					vote_id  = scanner.nextInt();
                    scanner.nextLine();
                    
                    System.out.print("Enter the description: ");
                    response = scanner.nextLine();

                    library.editVote(room_id, vote_id, response);
                    break;
                case '3':
                    System.out.print("Enter the voting process id: ");
					vote_id  = scanner.nextInt();
                    scanner.nextLine();

                    library.cancelVote(room_id, vote_id);
                    break;
                case '4':
                    System.out.println("\nLeaving this menu...");
                    System.out.println();
                    return;
				default:
					System.out.println("\nInvalid operation!\n");
					break;
			}
		}
    }

    public static void actionsMenu(int room_id, ClientLibrary library) {
        Scanner scanner = new Scanner(System.in);

        char option = '0';
		while (option != '9') {

			System.out.println("\nActions:");
            System.out.println("1 - Create a new channel");
            System.out.println("2 - Delete a channel");
            System.out.println("3 - Invite users");
            System.out.println("4 - Punish/Unpunish users");
            System.out.println("5 - Delete users");
            System.out.println("6 - Set new voting thresholds");
            System.out.println("7 - Set \"experimental interactions\" value");
            System.out.println("8 - Reset a user's reputation to a lower value");
            System.out.println("9 - Leave moderation actions menu");

            String channel_name, invites, pseudonym;
            String[] names;

			System.out.print("Choose an option: ");
            try{
                option = scanner.nextLine().charAt(0);
            } catch (Exception e){
                option = ' ';
            }
			switch (option) {
				case '1':
                    System.out.print("Insert the channel name: ");
					channel_name  = scanner.nextLine();
                    
                    library.createChannel(room_id, channel_name);
                    break;
                case '2':
                    System.out.print("Insert the channel name: ");
					channel_name  = scanner.nextLine();
                    
                    library.deleteChannel(room_id, channel_name);
                    break;
                case '3':
                    System.out.print("Write the names of the people you want to invite, separated by commas.");
                    System.out.println(" (Ex: Tiago, João, Miguel): ");
                    invites = scanner.nextLine();
                    
                    names = invites.replaceAll("^[,\\s]+", "").split("[\\s]*,[\\s]*");
                    for(String name : names) {
                        System.out.print("Sending invite to " + name + ":\n\t");
                        library.inviteUser(room_id, name);
                    }
                    break;
                case '4':
                    System.out.println("\nDo you want to:");
                    System.out.println("1 - Punish");
                    System.out.println("2 - Unpunish");
                    System.out.println("3 - Cancel");
                    System.out.print("Choose an option: ");
                    char op;
                    try{
                        op = scanner.nextLine().charAt(0);
                    } catch (Exception e){
                        op = '1';
                    }

                    switch (op) {
                        case '1':
                            System.out.print("Write the pseudonym of the user you want to punish: ");
                            pseudonym = scanner.nextLine();

                            System.out.print("Write the time you want to punish the user in the format <days>-<hours>-<minutes>: ");
                            invites = scanner.nextLine();
                            break;
                        case '2':
                            System.out.print("Write the pseudonym of the user you want to unpunish: ");
                            pseudonym = scanner.nextLine();

                            invites = "0-0-0";
                            break;
                        default:
                            op = '3';
                            invites="";
                            pseudonym="";
                            break;
                    }
                    if(op == '3') {
                        System.out.println("\nLeaving...");
                        break;
                    }

                    names = invites.replaceAll("^[-\\s]+", "").split("[\\s]*-[\\s]*");

                    if(names.length != 3) {
                        System.out.println("Invalid time format!");
                        break;
                    }
                    try{
                        library.punishUser(room_id, pseudonym, Integer.parseInt(names[0]), Integer.parseInt(names[1]), Integer.parseInt(names[2]));
                    } catch (Exception e) {
                        System.out.println("Invalid time format!");
                    }
                    break;
                case '5':
                    System.out.print("Write the names of the people you want to delete, separated by commas.");
                    System.out.println(" (Ex: Tiago, João, Miguel): ");
                    invites = scanner.nextLine();
                    
                    names = invites.replaceAll("^[,\\s]+", "").split("[\\s]*,[\\s]*");
                    for(String name : names) {
                        System.out.print("Delete user " + name + ":\n\t");
                        library.deleteUser(room_id, name);
                    }
                    break;
                case '6':
                    HashMap<String, Double> thresholds = library.getThresholds(room_id);
                    if(thresholds == null) {
                        break;
                    }
                    System.out.print("Do you want to set a new threshold for which moderation task? ");
                    System.out.println("\nOptions:");
                    System.out.println("1 - Create a new channel (actual threshold = " + thresholds.get("Create_Channel") + ")");
                    System.out.println("2 - Delete a channel (actual threshold = " + thresholds.get("Delete_Channel") + ")");
                    System.out.println("3 - Delete a message (actual threshold = " + thresholds.get("Delete_Message") + ")");
                    System.out.println("4 - Edit a message (actual threshold = " + thresholds.get("Edit_Message") + ")");
                    System.out.println("5 - Pin a message (actual threshold = " + thresholds.get("Pin_Message") + ")");
                    System.out.println("6 - Unpin a message (actual threshold = " + thresholds.get("Unpin_Message") + ")");
                    System.out.println("7 - Invite a user (actual threshold = " + thresholds.get("Invite_User") + ")");
                    System.out.println("8 - Punish/Unpunish a user (actual threshold = " + thresholds.get("Punish_User") + ")");
                    System.out.println("9 - Delete a user (actual threshold = " + thresholds.get("Delete_User") + ")");
                    System.out.print("Choose an option: ");
                    char reason = '0';
                    double threshold = 0.5;
                    try{
                        reason = scanner.nextLine().charAt(0);
                    } catch (Exception e){
                        reason = ' ';
                    }
                    switch(reason) {
                        case '1':
                            System.out.print("Enter the new threshold (between 0 and 1): ");
                            threshold = scanner.nextDouble();
                            scanner.nextLine();
                            library.thresholdVote(room_id, Daoliberate.ACTION.CREATE_CHANNEL, threshold);
                            break;
                        case '2':
                            System.out.print("Enter the new threshold (between 0 and 1): ");
                            threshold = scanner.nextDouble();
                            scanner.nextLine();
                            library.thresholdVote(room_id, Daoliberate.ACTION.DELETE_CHANNEL, threshold);
                            break;
                        case '3':
                            System.out.print("Enter the new threshold (between 0 and 1): ");
                            threshold = scanner.nextDouble();
                            scanner.nextLine();
                            library.thresholdVote(room_id, Daoliberate.ACTION.DELETE_MESSAGE, threshold);
                            break;
                        case '4':
                            System.out.print("Enter the new threshold (between 0 and 1): ");
                            threshold = scanner.nextDouble();
                            scanner.nextLine();
                            library.thresholdVote(room_id, Daoliberate.ACTION.EDIT_MESSAGE, threshold);
                            break;
                        case '5':
                            System.out.print("Enter the new threshold (between 0 and 1): ");
                            threshold = scanner.nextDouble();
                            scanner.nextLine();
                            library.thresholdVote(room_id, Daoliberate.ACTION.PIN_MESSAGE, threshold);
                            break;
                        case '6':
                            System.out.print("Enter the new threshold (between 0 and 1): ");
                            threshold = scanner.nextDouble();
                            scanner.nextLine();
                            library.thresholdVote(room_id, Daoliberate.ACTION.UNPIN_MESSAGE, threshold);
                            break;
                        case '7':
                            System.out.print("Enter the new threshold (between 0 and 1): ");
                            threshold = scanner.nextDouble();
                            scanner.nextLine();
                            library.thresholdVote(room_id, Daoliberate.ACTION.INVITE_USER, threshold);
                            break;
                        case '8':
                            System.out.print("Enter the new threshold (between 0 and 1): ");
                            threshold = scanner.nextDouble();
                            scanner.nextLine();
                            library.thresholdVote(room_id, Daoliberate.ACTION.PUNISH_USER, threshold);
                            break;
                        case '9':
                            System.out.print("Enter the new threshold (between 0 and 1): ");
                            threshold = scanner.nextDouble();
                            scanner.nextLine();
                            library.thresholdVote(room_id, Daoliberate.ACTION.DELETE_USER, threshold);
                            break;
                        default:
                            System.out.println("\nInvalid option!\n");
                            break;
                    }
                    break;
                case '7':
                    if(library.getExperimentalInteractions(room_id)) {
                        int new_value;
                        new_value  = scanner.nextInt();
                        scanner.nextLine();
                        library.setExperimentalInteractions(room_id, new_value);
                    }
                    break;
                case '8':
                    System.out.print("Type the name of the user you want to reset the reputation: ");
                    pseudonym = scanner.nextLine();

                    if(library.getReputation(room_id, pseudonym)) {
                        double new_reputation = 0;
                        new_reputation  = scanner.nextDouble();
                        scanner.nextLine();
                        library.setReputation(room_id, pseudonym, new_reputation);
                    }
                    break;
                case '9':
                    System.out.println("\nLeaving this menu...");
                    System.out.println();
                    return;
				default:
					System.out.println("\nInvalid operation!\n");
					break;
			}
		}
    }

    public static void chatChannel(int room_id, String channel_name, ClientLibrary library) {
        Scanner scanner = new Scanner(System.in);

        String content, response;
        int message_id;
        char option = '0';
		while (option != '8') {

			System.out.println("\nOptions:");
            System.out.println("1 - Send a message");
            System.out.println("2 - Delete a message");
            System.out.println("3 - Edit a message");
            System.out.println("4 - Pin a message");
            System.out.println("5 - Unpin a message");
            System.out.println("6 - View pinned messages");
            System.out.println("7 - Update chat");
            System.out.println("8 - Leave channel");

			System.out.print("Choose an option: ");
            try{
                option = scanner.nextLine().charAt(0);
            } catch (Exception e){
                option = ' ';
            }
			switch (option) {
				case '1':
					System.out.println("Write your message (press enter to send): ");
                    content = scanner.nextLine();
                    System.out.print("Are you sure you want to send this message? (y/n): ");
                    response = scanner.nextLine();
                    
                    if(response.equals("y")){
                        library.createMessage(room_id, channel_name, content);
                    }
					break;
                case '2':
                    System.out.print("Enter the message id: ");
					message_id  = scanner.nextInt();
                    scanner.nextLine();

                    System.out.print("Are you the creator of this message? (y/n): ");
                    response = scanner.nextLine();
                    if(response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes")) {
                        library.deleteMessage(room_id, channel_name, message_id, Daoliberate.REASON.OWNER);
                        break;
                    }
                    else if(response.equalsIgnoreCase("n") || response.equalsIgnoreCase("no")) {
                        System.out.print("What is the reason for deleting this message? ");
                        System.out.println("\nOptions:");
                        System.out.println("1 - Offensive content");
                        System.out.println("2 - Spam");
                        System.out.println("3 - Content out of context");
                        System.out.print("Choose an option: ");
                        char reason = '0';
                        try{
                            reason = scanner.nextLine().charAt(0);
                        } catch (Exception e){
                            reason = ' ';
                        }
                        switch(reason) {
                            case '1':
                                library.deleteMessage(room_id, channel_name, message_id, Daoliberate.REASON.OFFENSIVE);
                                break;
                            case '2':
                                library.deleteMessage(room_id, channel_name, message_id, Daoliberate.REASON.SPAM);
                                break;
                            case '3':
                                library.deleteMessage(room_id, channel_name, message_id, Daoliberate.REASON.OUT_OF_CONTEXT);
                                break;
                            default:
                                System.out.println("\nInvalid option!\n");
                                break;
                        }
                    }
                    else{
                        System.out.println("Invalid option!");
                        break;
                    }
                    break;
                case '3':
                    System.out.print("Enter the message id: ");
					message_id  = scanner.nextInt();
                    scanner.nextLine();

                    System.out.println("Write the new message content (press enter to finish): ");
                    content = scanner.nextLine();

                    System.out.print("Are you the creator of this message? (y/n): ");
                    response = scanner.nextLine();
                    if(response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes")) {
                        library.editMessage(room_id, channel_name, message_id, content, Daoliberate.REASON.OWNER);
                        break;
                    }
                    else if(response.equalsIgnoreCase("n") || response.equalsIgnoreCase("no")) {
                        System.out.print("What is the reason for editing this message? ");
                        System.out.println("\nOptions:");
                        System.out.println("1 - Offensive content");
                        System.out.println("2 - Make the content clearer");
                        System.out.print("Choose an option: ");
                        char reason = '0';
                        try{
                            reason = scanner.nextLine().charAt(0);
                        } catch (Exception e){
                            reason = ' ';
                        }
                        switch(reason) {
                            case '1':
                                library.editMessage(room_id, channel_name, message_id, content, Daoliberate.REASON.OFFENSIVE);
                                break;
                            case '2':
                                library.editMessage(room_id, channel_name, message_id, content, Daoliberate.REASON.CLEARER);
                                break;
                            default:
                                System.out.println("\nInvalid option!\n");
                                break;
                        }
                    }
                    else{
                        System.out.println("Invalid option!");
                        break;
                    }
                    break;
                case '4':
                    System.out.print("Enter the message id: ");
					message_id  = scanner.nextInt();
                    scanner.nextLine();

                    library.pinMessage(room_id, channel_name, message_id);
                    break;
                case '5':
                    System.out.print("Enter the message id: ");
					message_id  = scanner.nextInt();
                    scanner.nextLine();

                    library.unpinMessage(room_id, channel_name, message_id);
                    break;
                case '6':
                    try {
                        for(String string : library.viewPinnedMessages(room_id, channel_name)) {
                            System.out.println(string);
                        }
                    } catch(NullPointerException e){}
                    break;
                case '7':
                    System.out.println("\nUpdating chat...");
                    break;
                case '8':
                    System.out.println("\nLeaving this room...");
                    System.out.println();
                    return;
				default:
					System.out.println("\nInvalid operation!\n");
					break;
			}

            showChannelMessages(room_id, channel_name, library);
		}
    }

    public static void showProfilePage(String name, ClientLibrary library) {
        System.out.println("===== Welcome " + name + " =====");
        System.out.println("Your chat rooms:\n");
        List<String> rooms = library.getRooms();
        if(rooms != null) {
            for(String room : rooms) {
                System.out.println("\t" + room);
            }

            if(rooms.size() == 0) {
                System.out.println("\tNo chat rooms for now.");
            }
        }
    }

    public static boolean showRoomChannels(int room_id, ClientLibrary library) {
        List<String> channels = library.enterRoom(room_id);
        if(channels != null) {
            for(String channel : channels) {
                System.out.println(channel);
            }
            return true;
        }
        return false;
    }

    public static boolean showChannelMessages(int room_id, String channel_name, ClientLibrary library) {
        List<String> messages = library.enterChannel(room_id, channel_name);
        if(messages != null) {
            for(String message : messages) {
                System.out.println(message);
            }
            return true;
        }
        return false;
    }

    public static boolean showInvites(String name, String password, ClientLibrary library) {
        List<String> invites = library.checkInvites(name, password);
        if(invites != null) {
            for(String invite : invites) {
                System.out.println(invite);
            }
            return true;
        }
        return false;
    }

    public static boolean showSecrets(String name, String password, ClientLibrary library) {
        List<String> secrets = library.checkSecrets(name, password);
        if(secrets != null) {
            for(String secret : secrets) {
                System.out.println(secret);
            }
            return true;
        }
        return false;
    }
}
