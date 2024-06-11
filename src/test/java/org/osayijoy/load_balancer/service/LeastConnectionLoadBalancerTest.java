package org.osayijoy.load_balancer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osayijoy.load_balancer.model.Server;

import static org.junit.jupiter.api.Assertions.*;

public class LeastConnectionLoadBalancerTest {
    private LeastConnectionLoadBalancer loadBalancer = LeastConnectionLoadBalancer.getInstance();

    @BeforeEach
    void setUp(){
        loadBalancer.removeAllServer();
    }


    @Test
    void addServer_shouldAddServer() {
        Server server = new Server("192.168.1.1");
        loadBalancer.addServer(server);

        assertEquals(server, loadBalancer.getServer());
    }

    @Test
    void addServer_shouldThrowExceptionWhenRegistryIsFull() {
        for (int i = 1; i <= 10; i++) {
            loadBalancer.addServer(new Server("192.168.2." + i));
        }

        assertThrows(IllegalStateException.class, () -> loadBalancer.addServer(new Server("192.168.1.11")));
    }

    @Test
    void addServer_shouldThrowExceptionWhenServerAlreadyExists() {
        Server server = new Server("192.168.1.1");
        loadBalancer.addServer(server);

        assertThrows(IllegalArgumentException.class, () -> loadBalancer.addServer(new Server("192.168.1.1")));
    }

    @Test
    void removeServer_shouldRemoveServer() {
        Server server = new Server("192.168.1.1");
        loadBalancer.addServer(server);
        loadBalancer.removeServer("192.168.1.1");

        assertThrows(IllegalStateException.class, () -> loadBalancer.getServer());
    }

    @Test
    void getServer_shouldReturnLeastConnectionServer() {
        Server server1 = new Server("192.168.1.1");
        Server server2 = new Server("192.168.1.2");
        loadBalancer.addServer(server1);
        loadBalancer.addServer(server2);

        Server selectedServer1 = loadBalancer.getServer();
        loadBalancer.releaseServer(selectedServer1);
        Server selectedServer2 = loadBalancer.getServer();

        assertEquals(selectedServer1, selectedServer2);
    }

    @Test
    void getServer_shouldThrowExceptionWhenNoServersAvailable() {
        assertThrows(IllegalStateException.class, () -> loadBalancer.getServer());
    }

    @Test
    void releaseServer_shouldDecrementActiveConnections() {
        Server server1 = new Server("192.168.1.1");
        loadBalancer.addServer(server1);
        Server selectedServer = loadBalancer.getServer();

        assertEquals(1, selectedServer.getActiveConnections());

        loadBalancer.releaseServer(selectedServer);

        assertEquals(0, selectedServer.getActiveConnections());
    }

    @Test
    void getServer_shouldBalanceLoad() {
        Server server1 = new Server("192.168.1.1");
        Server server2 = new Server("192.168.1.2");
        Server server3 = new Server("192.168.1.3");
        loadBalancer.addServer(server1);
        loadBalancer.addServer(server2);
        loadBalancer.addServer(server3);

        Server selectedServer1 = loadBalancer.getServer();
        Server selectedServer2 = loadBalancer.getServer();
        Server selectedServer3 = loadBalancer.getServer();

        loadBalancer.releaseServer(selectedServer1);
        loadBalancer.releaseServer(selectedServer2);
        loadBalancer.releaseServer(selectedServer3);

        assertNotSame(selectedServer1, selectedServer2);
        assertNotSame(selectedServer2, selectedServer3);
        assertNotSame(selectedServer1, selectedServer3);
    }
}
