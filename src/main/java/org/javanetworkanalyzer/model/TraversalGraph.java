/*
 * Java Network Analyzer provides a collection of graph theory and social
 * network analysis algorithms implemented on mathematical graphs using the
 * <a href="http://www.jgrapht.org/">JGraphT</a> library.
 *
 * Java Network Analyzer is developed by the GIS group of the DECIDE team of the 
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 * It is part of the OrbisGIS tool ecosystem.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 * 
 * Java Network Analyzer is distributed under LGPL 3 license.
 *
 * Copyright (C) 2012-2014 CNRS (IRSTV CNRS FR 2488)
 * Copyright (C) 2015-2018 CNRS (Lab-STICC CNRS UMR 6285)
 *
 * Java Network Analyzer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Java Network Analyzer is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * Java Network Analyzer. If not, see <http://www.gnu.org/licenses/>.
 * 
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.javanetworkanalyzer.model;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;

/**
 * A simple directed graph for shortest path "trees" (multiple shortest paths
 * are allowed) in {@link org.javanetworkanalyzer.alg.Dijkstra} and {@link
 * org.javanetworkanalyzer.alg.BFS} as well as {@link
 * org.javanetworkanalyzer.alg.DFS} traversal graphs.
 *
 * @author Adam Gouge
 */
public class TraversalGraph<V, E> extends DirectedMultigraph<V, E> {

    private V root;

    /**
     * Constructor
     *
     * @param edgeClass class on which to base factory for edges
     * @param root      Root
     */
    public TraversalGraph(Class<? extends E> edgeClass, V root) {
        super(edgeClass);
        this.root = root;
        addVertex(root);
    }

    /**
     * Constructor
     *
     * @param ef   the edge factory of the new graph.
     * @param root Root
     */
    public TraversalGraph(EdgeFactory<V, E> ef, V root) {
        super(ef);
        this.root = root;
    }

    /**
     * Return the root.
     *
     * @return The root
     */
    public V getRoot() {
        return root;
    }
}
