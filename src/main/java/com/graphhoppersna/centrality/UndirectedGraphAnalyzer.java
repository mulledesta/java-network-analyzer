/**
 * GraphHopper-SNA implements a collection of social network analysis
 * algorithms. It is based on the <a
 * href="http://graphhopper.com/">GraphHopper</a> library.
 *
 * GraphHopper-SNA is distributed under the GPL 3 license. It is produced by the
 * "Atelier SIG" team of the <a href="http://www.irstv.fr">IRSTV Institute</a>,
 * CNRS FR 2488.
 *
 * Copyright 2012 IRSTV (CNRS FR 2488)
 *
 * GraphHopper-SNA is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * GraphHopper-SNA is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GraphHopper-SNA. If not, see <http://www.gnu.org/licenses/>.
 */
package com.graphhoppersna.centrality;

import com.graphhopper.routing.util.PrepareRoutingSubnetworks;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIterator;
import com.graphhoppersna.data.NodeBetweennessInfo;
import com.graphhoppersna.data.PathLengthData;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.set.hash.TIntHashSet;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Adam Gouge
 */
public class UndirectedGraphAnalyzer {

    /**
     * The (undirected) graph to be analyzed.
     */
    private final Graph graph;
//    *************************************************************************
    /**
     * Histogram of shortest path lengths.
     *
     * <p><code>sPathLength[0]</code> stores the number of nodes processed so
     * far.
     *
     * <p><code>sPathLength[i]</code> for
     * <code>i > 0</code> stores the number of shortest paths of length
     * <code>i</code> found so far. </p>
     */
    private long[] sPathLengths;
    /**
     * Histogram of pairs of nodes that share common neighbors.
     *
     * The i-th element of this array accumulates the number of node pairs that
     * share i neighbors.
     */
    private long[] sharedNeighborsHist;
    /**
     * Round doubles in attributes to
     * <code>roundingDigits</code> decimals after the point.
     */
    private int roundingDigits;
    /**
     * Set of visited nodes.
     *
     * <p> This set is used exclusively by the method
     * {@link #computeSPandSN(Node)}. </p>
     */
    private final TIntHashSet visited;
    /**
     * Map of all nodes with their respective node betweenness information,
     * which stores information needed for the node betweenness calculation.
     */
    private Map<Integer, NodeBetweennessInfo> nodeBetweenness;
    /**
     * Map of all edges with their respective betweenness.
     */
    private TIntDoubleHashMap edgeBetweenness;
    /**
     * Map of all nodes with their respective stress.
     *
     * I.e., the number of shortest paths passing through a node.
     */
    private TIntLongHashMap stress;
    /**
     * The number of nodes in this graph.
     */
    private int nodeCount;

    /**
     * Initializes a new instance of {@link UndirectedNetworkAnalyzer}.
     *
     * @param graph The graph to be analyzed.
     */
    public UndirectedGraphAnalyzer(
            Graph graph) {
        this.graph = graph;
        nodeCount = graph.getNodes();
        sPathLengths = new long[nodeCount];
        sharedNeighborsHist = new long[nodeCount];
        visited = new TIntHashSet(nodeCount);
        nodeBetweenness = new HashMap<Integer, NodeBetweennessInfo>();
        edgeBetweenness = new TIntDoubleHashMap();
        stress = new TIntLongHashMap();
        roundingDigits = 8;
    }

    public TIntDoubleHashMap computeAll() {

        long time = System.currentTimeMillis();

//        int edgeCount = 0;
//        int maxConnectivity = 0;

        // Closeness centrality
        TIntDoubleHashMap closenessCentrality = new TIntDoubleHashMap();

        // node betweenness
        TIntDoubleHashMap nodeBetweennessArray = new TIntDoubleHashMap();

        // average shortest path length
        TIntDoubleHashMap aplMap = new TIntDoubleHashMap();

//        // stress
//        LogBinDistribution stressDist = new LogBinDistribution();

        // Compute number of connected components
        PrepareRoutingSubnetworks prepareSubnetworks =
                new PrepareRoutingSubnetworks(graph);
        // Delete singleton nodes.
        int deletedNodes = prepareSubnetworks.deleteZeroDegreeNodes();
        System.out.println("Deleted " + deletedNodes
                + " 0-degree nodes.");
        // Identify connected components.
        Map<Integer, Integer> componentsMap = prepareSubnetworks.
                findSubnetworks();
        System.out.println("Number of connected components: " + componentsMap.
                size());
        // Print out this information.
        for (Map.Entry entry : componentsMap.entrySet()) {
            System.out.println("The component containing node " + entry.getKey()
                    + " has a total of " + entry.getValue()
                    + " nodes.");
        }

        // TODO: For the moment, we assume the graph has only one
        // connected component.

        // Recover the node set and an iterator on it.
        TIntHashSet nodeSet = graph.nodeSet();
        TIntIterator nodeIter = nodeSet.iterator();

        // Initialize the parameters for the betweenness calculation.
        nodeBetweenness.clear();
        edgeBetweenness.clear();
        stress.clear();
        aplMap.clear();
        while (nodeIter.hasNext()) {
            final int node = nodeIter.next();
            nodeBetweenness.put(node, new NodeBetweennessInfo(0, -1, 0.0));
            stress.put(node, Long.valueOf(0));
        }

        // Re-initialize the iterator and begin network analysis.
        nodeIter = nodeSet.iterator();
        while (nodeIter.hasNext()) {
            
            // Start timing for this node.
            long start = System.currentTimeMillis();

            // Get the next node. (final so this node stays fixed)
            final int node = nodeIter.next();

            // SHORTEST PATHS COMPUTATION
//            System.out.println("Computing shortest paths for node " + node);
            PathLengthData pathLengths = computeSPandSN(node);
//            System.out.println("Number of shortest path lengths accumulated: " 
//                    + pathLengths.getCount());

            // Recover the eccentricity for this node.
            final int eccentricity = pathLengths.getMaxLength();

            // Get the average path length for this node and store it.
            final double apl =
                    (pathLengths.getCount() > 0)
                    ? pathLengths.getAverageLength() : 0.0;
            aplMap.put(node, apl);

            // Once we have the average path length for this node, 
            // we have the closeness centrality for this node.
            final double closeness = (apl > 0.0) ? 1 / apl : 0.0;
            // Store it.
            closenessCentrality.put(node, closeness);
            
            // Stop timing for this node.
            long stop = System.currentTimeMillis();
            
            System.out.println("Node: " + node
                    + ", Closeness: " + closeness
                    + ", Time: " + (stop-start)
                    + " ms.");

//            // Node and edge betweenness calculation
//            computeNBandEB(node);

            // Reset everything except the betweenness value
            TIntIterator nodeIterReset = nodeSet.iterator();
            while (nodeIterReset.hasNext()) {
                final int nodeToReset = nodeIterReset.next();
                NodeBetweennessInfo nodeInfo = nodeBetweenness.get(nodeToReset);
                nodeInfo.reset();
            }

        } // End node iteration.

        // TODO: Normalize and save node betweenness.
        // TODO: Save edge betweenness.
        
//        // Print out closeness centrality.
//        TIntDoubleIterator ccIt = closenessCentrality.iterator();
//        while (ccIt.hasNext()) {
//            ccIt.advance();
//            System.out.println(
//                    "Vertex: " + ccIt.key()
//                    + ", Closeness: " + ccIt.value()
//                    );
//        }

        time = System.currentTimeMillis() - time;
        System.out.println("Network analysis took "
                + (time / 1000)
                + " seconds.");
        return closenessCentrality;
    }

//    /**
//     * Accumulates the node and edge betweenness of all nodes in a connected
//     * component. The node betweenness is calculate using the algorithm of
//     * Brandes (U. Brandes: A Faster Algorithm for Betweenness Centrality.
//     * Journal of Mathematical Sociology 25(2):163-177, 2001). The edge
//     * betweenness is calculated as used by Newman and Girvan (M.E. Newman and
//     * M. Girvan: Finding and Evaluating Community Structure in Networks. Phys.
//     * Rev. E Stat. Nonlin. Soft. Matter Phys., 69, 026113.). In each run of
//     * this method a different source node is chosen and the betweenness of all
//     * nodes is replaced by the new one. For the final result this method has to
//     * be run for all nodes of the connected component.
//     *
//     * This method uses a breadth-first search through the network, starting
//     * from a specified source node, in order to find all paths to the other
//     * nodes in the network and to accumulate their betweenness.
//     *
//     * @param source Node where a run of breadth-first search is started, in
//     *               order to accumulate the node and edge betweenness of all
//     *               other nodes
//     */
//    private void computeNBandEB(int source) {
//        
//        
//        TIntLinkedList done_nodes = new TIntLinkedList();
//        TIntLinkedList reached = new TIntLinkedList();
//        TIntDoubleHashMap edgeDependency = new TIntDoubleHashMap();
//        TIntLongHashMap stressDependency = new TIntLongHashMap();
//
//        final NodeBetweennessInfo sourceNBInfo = nodeBetweenness.get(source);
//        
//        sourceNBInfo.setSource();
//        reached.add(source);
//        stressDependency.put(source, 0);
//
//        // Use BFS to find shortest paths from source to all nodes in the
//        // network
//        while (!reached.isEmpty()) {
//            
//            final int current = reached.removeAt(0); // TODO: reached.removeFirst();
//            
//            done_nodes.addFirst(current);
//            final NodeBetweenInfo currentNBInfo = nodeBetweenness.get(current);
//            final Set<Node> neighbors = getNeighbors(current);
//            for (Node neighbor : neighbors) {
//                final NodeBetweenInfo neighborNBInfo = nodeBetweenness.get(
//                        neighbor);
//                final List<Edge> edges = CyNetworkUtils.getConnEdge(network,
//                        current, neighbor);
//                final int expectSPLength = currentNBInfo.getSPLength() + 1;
//                if (neighborNBInfo.getSPLength() < 0) {
//                    // Neighbor traversed for the first time
//                    reached.add(neighbor);
//                    neighborNBInfo.setSPLength(expectSPLength);
//                    stressDependency.put(neighbor, Long.valueOf(0));
//                }
//                // shortest path via current to neighbor found
//                if (neighborNBInfo.getSPLength() == expectSPLength) {
//                    neighborNBInfo.addSPCount(currentNBInfo.getSPCount());
//                    // check for long overflow 
//                    if (neighborNBInfo.getSPCount() < 0) {
//                        computeNB = false;
//                    }
//                    // add predecessors and outgoing edges, needed for
//                    // accumulation of betweenness scores
//                    neighborNBInfo.addPredecessor(current);
//                    for (final Edge edge : edges) {
//                        currentNBInfo.addOutedge(edge);
//                    }
//                }
//                // initialize edge dependency
//                for (final Edge edge : edges) {
//                    if (!edgeDependency.containsKey(edge)) {
//                        edgeDependency.put(edge, new Double(0.0));
//                    }
//                }
//            }
//        }
//        // Return nodes in order of non-increasing distance from source
//        while (!done_nodes.isEmpty()) {
//            final Node current = done_nodes.removeFirst();
//            final NodeBetweenInfo currentNBInfo = nodeBetweenness.get(current);
//            if (currentNBInfo != null) {
//                final long currentStress = stressDependency.get(current).
//                        longValue();
//                while (!currentNBInfo.isEmptyPredecessors()) {
//                    final Node predecessor = currentNBInfo.pullPredecessor();
//                    final NodeBetweenInfo predecessorNBInfo = nodeBetweenness.
//                            get(predecessor);
//                    predecessorNBInfo.addDependency((1.0 + currentNBInfo.
//                            getDependency())
//                            * ((double) predecessorNBInfo.getSPCount() / (double) currentNBInfo.
//                            getSPCount()));
//                    // accumulate all sp count
//                    final long oldStress = stressDependency.get(predecessor).
//                            longValue();
//                    stressDependency.put(predecessor, new Long(
//                            oldStress + 1 + currentStress));
//                    // accumulate edge betweenness
//                    final List<Edge> edges = CyNetworkUtils.getConnEdge(network,
//                            predecessor,
//                            current);
//                    if (edges.size() != 0) {
//                        final Edge compEdge = edges.get(0);
//                        final LinkedList<Edge> currentedges = currentNBInfo.
//                                getOutEdges();
//                        double oldbetweenness = 0.0;
//                        double newbetweenness = 0.0;
//                        for (final Edge edge : edges) {
//                            if (edgeBetweenness.containsKey(edge)) {
//                                oldbetweenness = edgeBetweenness.get(edge).
//                                        doubleValue();
//                                break;
//                            }
//                        }
//                        // if the node is a leaf node in this search tree
//                        if (currentedges.size() == 0) {
//                            newbetweenness = (double) predecessorNBInfo.
//                                    getSPCount()
//                                    / (double) currentNBInfo.getSPCount();
//                        } else {
//                            double neighbourbetw = 0.0;
//                            for (Edge neighbouredge : currentedges) {
//                                if (!edges.contains(neighbouredge)) {
//                                    neighbourbetw += edgeDependency.get(
//                                            neighbouredge)
//                                            .doubleValue();
//                                }
//                            }
//                            newbetweenness = (1 + neighbourbetw)
//                                    * ((double) predecessorNBInfo.getSPCount() / (double) currentNBInfo.
//                                    getSPCount());
//                        }
//                        edgeDependency.put(compEdge, new Double(newbetweenness));
//                        for (final Edge edge : edges) {
//                            edgeBetweenness.put(edge,
//                                    new Double(newbetweenness + oldbetweenness));
//                        }
//                    }
//                }
//                // accumulate node betweenness in each run
//                if (!current.equals(source)) {
//                    currentNBInfo.addBetweenness(currentNBInfo.getDependency());
//                    // accumulate number of shortest paths
//                    final long allSpPaths = stress.get(current).longValue();
//                    stress.put(current, new Long(allSpPaths + currentNBInfo.
//                            getSPCount()
//                            * currentStress));
//                }
//            }
//        }
//    }
    /**
     * Computes the shortest path lengths from the given node to all other nodes
     * in the network.
     *
     * In addition, this method accumulates values in the
     * {@link #sharedNeighborsHist} histogram.
     *
     * <p> This method stores the lengths found in the array
     * {@link #sPathLengths}.<br/>
     * <code>sPathLengths[i] == 0</code> when i is the index of
     * <code>aNode</code>.<br/>
     * <code>sPathLengths[i] == Integer.MAX_VALUE</code> when node i and
     * <code>aNode</code> are disconnected.<br/>
     * <code>sPathLengths[i] == d &gt; 0</code> when every shortest path between
     * node i and
     * <code>aNode</code> contains
     * <code>d</code> edges. </p> <p> This method uses a breadth-first traversal
     * through the network, starting from the specified node, in order to find
     * all reachable nodes and accumulate their distances to
     * <code>aNode</code> in {@link #sPathLengths}. </p>
     *
     * @param aNode Starting node of the shortest paths to be found.
     *
     * @return Data on the shortest path lengths from the current node to all
     *         other reachable nodes in the network.
     */
    private PathLengthData computeSPandSN(int aNode) {

        visited.clear();
        visited.add(aNode);

        TIntHashSet nbs = null;

        // TODO: Replace by TIntLinkedList.
        LinkedList<Integer> reachedNodes = new LinkedList<Integer>();
        reachedNodes.add(aNode);
        reachedNodes.add(null);

        int currentDist = 1;

        PathLengthData result = new PathLengthData();

        for (Integer currentNode = reachedNodes.removeFirst();
                !reachedNodes.isEmpty();
                currentNode = reachedNodes.removeFirst()
                ) {
            if (currentNode == null) {
                // Next level of the BFS tree
                currentDist++;
                reachedNodes.add(null);
            } else {
                // Traverse next reached node
                final TIntHashSet neighbors = getNeighbors(currentNode.intValue());
                if (nbs == null) {
                    nbs = neighbors;
                }

                TIntIterator it = neighbors.iterator();
                while (it.hasNext()) {
                    final int neighbor = it.next();
                    if (visited.add(neighbor)) {
                        final int snCount = (currentDist > 2)
                                ? 0
                                : countNeighborsIn(nbs, neighbor);
                        sharedNeighborsHist[snCount]++;
                        sPathLengths[currentDist]++;
                        result.addSPL(currentDist);
                        reachedNodes.add(neighbor);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gets all the neighbors of the given node.
     *
     * @param aNode Node, whose neighbors are to be found.
     *
     * @return <code>Set</code> of <code>Node</code> instances, containing all
     *         the neighbors of <code>aNode</code>; empty set if the node
     *         specified is an isolated vertex.
     *
     * @see CyNetworkUtils#getNeighbors(CyNetwork, Node, int[])
     */
    private TIntHashSet getNeighbors(int aNode) {
        TIntHashSet neighbors = new TIntHashSet();

        EdgeIterator incomingIt = graph.getIncoming(aNode);
        EdgeIterator outgoingIt = graph.getOutgoing(aNode);

        while (incomingIt.next()) {
            neighbors.add(incomingIt.fromNode());
        }
        while (outgoingIt.next()) {
            neighbors.add(outgoingIt.node());
        }

        return neighbors;
    }

    /**
     * Counts the number of neighbors of the given node that occur in the given
     * set of nodes.
     *
     * @param aSet Set of nodes to be searched in.
     * @param aNode Node whose neighbors will be searched in <code>aSet</code>.
     *
     * @return Number of nodes in <code>aSet</code> that are neighbors
     *         of <code>aNode</code>.
     */
    private int countNeighborsIn(TIntHashSet aSet, int aNode) {
        TIntHashSet nbs = getNeighbors(aNode);
        nbs.retainAll(aSet);
        return nbs.size();
    }
    
}