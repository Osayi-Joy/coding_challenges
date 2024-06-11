package org.osayijoy.load_balancer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osayijoy.load_balancer.model.Server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RoundRobinLoadBalancerTest {
    private RoundRobinLoadBalancer loadBalancer  = RoundRobinLoadBalancer.getInstance();

    @BeforeEach
    void setUp() {
        loadBalancer.removeAllServer();
    }

    @Test
    void addServer_shouldAddServer() {
        Server server = new Server("192.168.1.1");
        Server server1 = new Server("192.168.1.2");

        loadBalancer.addServer(server);
        loadBalancer.addServer(server1);

        assertEquals(server1, loadBalancer.getServer());
    }

    @Test
    void addServer_shouldThrowExceptionWhenRegistryIsFull() {
        for (int i = 1; i <= 10; i++) {
            loadBalancer.addServer(new Server("192.168.1." + i));
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
    void getServer_shouldReturnServersInRoundRobinOrder() {
        Server server1 = new Server("192.168.1.1");
        Server server2 = new Server("192.168.1.2");
        Server server3 = new Server("192.168.1.3");
        loadBalancer.addServer(server1);
        loadBalancer.addServer(server2);
        loadBalancer.addServer(server3);

        assertEquals(server1, loadBalancer.getServer());
        assertEquals(server2, loadBalancer.getServer());
        assertEquals(server3, loadBalancer.getServer());
        assertEquals(server1, loadBalancer.getServer());
        assertEquals(server2, loadBalancer.getServer());
    }

    @Test
    void getServer_shouldThrowExceptionWhenNoServersAvailable() {
        assertThrows(IllegalStateException.class, () -> loadBalancer.getServer());
    }
}
