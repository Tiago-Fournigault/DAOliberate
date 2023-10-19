package pt.tecnico.grpc.tester;

import pt.tecnico.grpc.client.ClientLibrary;

public class Tester {
	
	public static void main(String[] args) throws Exception {
		System.out.println(Tester.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

        final String dao_host = args[0];
        final int dao_port = Integer.parseInt(args[1]);
        final String register_host = args[2];
        final int register_port = Integer.parseInt(args[3]);

		ClientLibrary frontend = new ClientLibrary(dao_host, dao_port, register_host, register_port);

        frontend.signup("user1", "Password123");
	}
	
}
