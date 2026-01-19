package dev.irako.topics.grpc.api;

import dev.irako.topics.grpc.data.GrpcServerFactory;
import io.grpc.Server;

import java.io.IOException;

/**
 * Entry point for starting the gRPC server.
 *
 * Usage:
 *
 * <pre>
 * java GrpcServerApp [port]
 * </pre>
 *
 * Default port is 50051 if not specified.
 */
public final class GrpcServerApp {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GrpcServerApp.class);
	private static final int DEFAULT_PORT = 50051;

	public static void main(String[] args) throws IOException, InterruptedException {
		int port = DEFAULT_PORT;
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				logger.error("Invalid port number: {}", args[0]);
				System.exit(1);
			}
		}

		Server server = GrpcServerFactory.createAndStartServer(port);
		logger.info("gRPC server started on port {}", port);

		// Add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Shutting down gRPC server");
			server.shutdown();
			try {
				if (!server.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
					server.shutdownNow();
				}
			} catch (InterruptedException e) {
				server.shutdownNow();
				Thread.currentThread().interrupt();
			}
			logger.info("gRPC server stopped");
		}));

		server.awaitTermination();
	}
}
