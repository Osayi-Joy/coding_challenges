package org.osayijoy.load_balancer.service;

import org.osayijoy.load_balancer.model.Server;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer {
    private static RoundRobinLoadBalancer instance;
    private final ConcurrentLinkedQueue<Server> serverRegistry = new ConcurrentLinkedQueue<>();
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    private RoundRobinLoadBalancer() {
    }

    public static RoundRobinLoadBalancer getInstance() {
        if (instance == null) {
            synchronized (RoundRobinLoadBalancer.class) {
                if (instance == null) {
                    instance = new RoundRobinLoadBalancer();
                }
            }
        }
        return instance;
    }

    @Override
    public void addServer(Server server) {
        if (serverRegistry.size() >= 10) {
            throw new IllegalStateException("Server registry is full");
        }

        if (serverRegistry.stream().anyMatch(s -> s.getIpAddress().equals(server.getIpAddress()))) {
            throw new IllegalArgumentException("Server with the same ID already exists");
        }
        serverRegistry.add(server);
    }

    @Override
    public void removeServer(String serverId) {
        boolean removed = serverRegistry.removeIf(server -> server.getIpAddress().equals(serverId));
        if (!removed) {
            throw new IllegalArgumentException("Server with ID " + serverId + " not found in the registry");
        }
    }

    @Override
    public void removeAllServer() {
        serverRegistry.clear();
    }

    @Override
    public Server getServer() {
        if (serverRegistry.isEmpty()) {
            throw new IllegalStateException("No servers available");
        }

        int index = currentIndex.getAndUpdate(current -> (current + 1) % serverRegistry.size());
        int i = 0;
        for (Server server : serverRegistry) {
            if (i++ == index) {
                if (isServerHealthy(server)) {
                    return server;
                }
            }
        }
        throw new IllegalStateException("No healthy servers available");
    }

    private boolean isServerHealthy(Server server) {
        return server.isHealthy();
    }

}
