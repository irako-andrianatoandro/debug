package dev.irako.topics.grpc.data;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

/**
 * Factory for creating and managing gRPC server instances.
 */
public final class GrpcServerFactory {

    private GrpcServerFactory() {
        // Utility class
    }

    /**
     * Creates a new gRPC server on the specified port.
     * 
     * @param port the port to bind to
     * @return configured gRPC server (not started)
     */
    public static Server createServer(int port) {
        return ServerBuilder.forPort(port)
                .addService(new StreamingServiceImpl())
                .build();
    }

    /**
     * Creates and starts a gRPC server on the specified port.
     * 
     * @param port the port to bind to
     * @return started gRPC server
     * @throws IOException if server cannot be started
     */
    public static Server createAndStartServer(int port) throws IOException {
        Server server = createServer(port);
        server.start();
        return server;
    }
}
