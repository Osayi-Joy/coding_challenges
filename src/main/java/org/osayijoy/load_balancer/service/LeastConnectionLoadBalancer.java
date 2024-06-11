package org.osayijoy.load_balancer.service;

import org.osayijoy.load_balancer.model.Server;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LeastConnectionLoadBalancer implements LoadBalancer {
    private static LeastConnectionLoadBalancer instance;
    private final Map<String, Server> serverRegistry  = new ConcurrentHashMap<>();

    private LeastConnectionLoadBalancer() {
    }

    public static synchronized LeastConnectionLoadBalancer getInstance() {
        if (instance == null) {
            instance = new LeastConnectionLoadBalancer();
        }
        return instance;
    }

    @Override
    public void addServer(Server server) {
        synchronized (this) {
            if (serverRegistry.size() >= 10) {
                throw new IllegalStateException("Server registry is full");
            }
            if (serverRegistry.containsKey(server.getIpAddress())) {
                throw new IllegalArgumentException("Server with the same IP address already exists");
            }
            serverRegistry.put(server.getIpAddress(), server);
        }
    }

    @Override
    public void removeServer(String ipAddress) {
        synchronized (this) {
            serverRegistry.remove(ipAddress);
        }
    }

    @Override
    public void removeAllServer() {
        serverRegistry.clear();
    }

    @Override
    public Server getServer() {
        synchronized (this) {
            if (serverRegistry.isEmpty()) {
                throw new IllegalStateException("No servers available");
            }
            return serverRegistry.values().stream()
                    .min(Comparator.comparingInt(Server::getActiveConnections))
                    .map(server -> {
                        server.incrementActiveConnections();
                        return server;
                    })
                    .orElseThrow(() -> new IllegalStateException("No servers available"));
        }
    }
    @Override
    public void releaseServer(Server server) {
        synchronized (this) {
            server.decrementActiveConnections();
        }
    }
}
