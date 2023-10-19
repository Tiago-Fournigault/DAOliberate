package pt.tecnico.grpc.register;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.ClientAuth;

import javax.net.ssl.SSLException;
import java.io.File;

/**
 * The RegisterServer class is responsible for starting the Register server
 */
public class RegisterServer {

	/** Server host port. */
	private static int port;
	private static String server_host;
	private static int server_port;

	public static void main(String[] args) throws Exception {
		System.out.println(RegisterServer.class.getSimpleName());

		// Print received arguments.
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// Check arguments.
		if (args.length < 3) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s port%n", RegisterServer.class.getName());
			return;
		}

		port = Integer.valueOf(args[0]);
		server_host = args[1];
		server_port = Integer.valueOf(args[2]);
		AuthorizationInterceptor interceptor = new AuthorizationInterceptor();
		Database data_base = new Database(interceptor);
		final BindableService impl = new RegisterServiceImpl(data_base, server_host, server_port);
		SslContext sslContext = RegisterServer.loadTLSCredentials();

		// Create a new server to listen on port.
		Server server = NettyServerBuilder.forPort(port)
						.sslContext(sslContext)
						.addService(impl)
						.intercept(interceptor)
						.build();
		// Start the server.
		server.start();
		// Server threads are running in the background.
		System.out.println("Register Server started");

		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
	}

	public static SslContext loadTLSCredentials() throws SSLException {
		File serverCertFile = new File("../cert/register-cert.pem");
		File serverKeyFile = new File("../cert/register-key.pem");
		File clientCACertFile = new File("../cert/ca-cert.pem");

		SslContextBuilder ctxBuilder = SslContextBuilder.forServer(serverCertFile, serverKeyFile)
			.clientAuth(ClientAuth.REQUIRE)
			.trustManager(clientCACertFile);

		return GrpcSslContexts.configure(ctxBuilder).build();
	}

}
