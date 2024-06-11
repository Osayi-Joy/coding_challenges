package org.osayijoy.load_balancer.model;

public class Server {
    private String ipAddress;
    private int currentWeight;
    private int capacity;
    private boolean health;
    private int activeConnections;

    public Server(String ipAddress, int capacity) {
        this.ipAddress = ipAddress;
        this.capacity = capacity;
        this.currentWeight = 0;
        this.health = true;
        this.activeConnections = 0;
    }


    public Server(String ipAddress) {
        this.ipAddress = ipAddress;
        this.currentWeight = 0;
        this.health = true;
        this.activeConnections = 0;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(int currentWeight) {
        this.currentWeight = currentWeight;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isHealthy() {
        return health;
    }

    public void setHealth(boolean health) {
        this.health = health;
    }

    public int getActiveConnections() {
        return activeConnections;
    }

    public void incrementActiveConnections() {
        activeConnections++;
    }

    public void decrementActiveConnections() {
        activeConnections--;
    }
}

