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
package org.javanetworkanalyzer.data;

import java.util.Set;

/**
 * Vertices which have an id and predecessor(s) on shortest paths from a source
 * node.
 *
 * @param <V> Vertex
 *
 * @author Adam Gouge
 */
public interface VPred<V extends VPred, E> {

    /**
     * Returns the predecessors.
     *
     * @return The predecessors.
     */
    Set<V> getPredecessors();

    /**
     * Adds a predecessor to the predecessor list of this node
     *
     * @param pred Node to be added since it is a predecessor of this node
     */
    void addPredecessor(V pred);

    /**
     * Returns the predecessor edges.
     *
     * @return The predecessor edges
     */
    Set<E> getPredecessorEdges();

    /**
     * Adds a predecessor edge to the predecessor list of this node
     *
     * @param pred Node to be added since it is a predecessor of this node
     */
    void addPredecessorEdge(E pred);

    /**
     * Clears the edge and vertex predecessors of this node.
     */
    void clear();
}
