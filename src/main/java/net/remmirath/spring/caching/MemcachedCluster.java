package net.remmirath.spring.caching;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds memcached node map to be consumed by XMemcachedClientBuilder
 * Takes a list of nodes with priorities in the following format:
 * HostEntry ::= host1:port1:priority1
 * Cluster ::= HostEntry;|Cluster;HostEntry
 * Example:
 * host1:port1:priority1;host2:port2:priority2;
 * @author otsvinev
 *
 */
public class MemcachedCluster {

    public static class Node {
        public final String host;
        public final int port;
        public final int priority;

        public Node(String host, int port, int priority) {
            checkNotNull(host, "Host cannot be null");
            checkArgument(!host.isEmpty(), "host cannot be empty");
            this.host = host;
            this.port = port;
            this.priority = priority;
        }

        @Override
        public String toString() {
            return String.format("%s:%d:%d", host, port, priority);
        }

    }

    // defaults to standard single-node memcached running on local host
    private String originalConfiguration = "localhost:11211:1";
    private List<Node> nodes = parseNodes(originalConfiguration);

    public MemcachedCluster(String nodes) {
        checkAndParseNodes(nodes);
    }

    public MemcachedCluster() {
    }

    private void checkAndParseNodes(String nodes) {
        checkNotNull(nodes, "List of nodes cannot be null");
        checkArgument(!nodes.isEmpty(), "List of nodes cannot be empty");
        originalConfiguration = nodes;
        this.nodes = parseNodes(originalConfiguration);
    }

    private List<Node> parseNodes(String nodeList) {
        List<Node> nodes = new ArrayList<Node>();
        for(String node : nodeList.split(";")) {
            String[] elements = node.split(":");
            checkArgument(3 == elements.length, "All 3 components host:port:priority are required");
            nodes.add(new Node(elements[0], Integer.parseInt(elements[1]), Integer.parseInt(elements[2])));
        }
        return nodes;
    }

    public List<InetSocketAddress> getEndpoints() {
        List<InetSocketAddress> endpoints = new ArrayList<InetSocketAddress>(nodes.size());
        for(Node n : nodes) {
            endpoints.add(new InetSocketAddress(n.host, n.port));
        }
        return endpoints;
    }

    public List<Integer> getWeights() {
        List<Integer> result = new ArrayList<Integer>();
        for(Node n : nodes) {
            result.add(n.priority);
        }
        return result;
    }

    public int[] getWeightsArray() {
        int[] weights = new int[nodes.size()];
        for(int i=0; i<nodes.size(); i++) {
            weights[i] = nodes.get(i).priority;
        }
        return weights;
    }

    public void setNodes(String nodes) {
        checkAndParseNodes(nodes);
    }

}
