package org.osayijoy.load_balancer.service;

import org.osayijoy.load_balancer.model.Server;

public interface LoadBalancer {
    void addServer(Server server);
    void removeServer(String ipAddress);

    void removeAllServer();

    Server getServer();
    default void releaseServer(Server server){}
}

