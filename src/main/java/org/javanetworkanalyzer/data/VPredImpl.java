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

import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of the {@link VPred} interface.
 *
 * @author Adam Gouge
 */
public class VPredImpl<V extends VPred, E> extends VId implements VPred<V, E> {

    /**
     * List of the predecessors of this node.
     *
     * I.e., the nodes lying on the shortest path to this node
     */
    private Set<V> predecessors = new HashSet<V>();
    private Set<E> predecessorEdges = new HashSet<E>();

    /**
     * Constructor: sets the id.
     *
     * @param id Id
     */
    public VPredImpl(Integer id) {
        super(id);
    }

    @Override
    public Set<V> getPredecessors() {
        return predecessors;
    }

    @Override
    public void addPredecessor(V pred) {
        predecessors.add(pred);
    }

    @Override
    public Set<E> getPredecessorEdges() {
        return predecessorEdges;
    }

    @Override
    public void addPredecessorEdge(E pred) {
        predecessorEdges.add(pred);
    }

    @Override
    public void clear() {
        predecessors.clear();
        predecessorEdges.clear();
    }
}
