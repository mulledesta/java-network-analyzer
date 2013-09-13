package org.javanetworkanalyzer.alg;

import org.javanetworkanalyzer.data.PathLengthData;
import org.javanetworkanalyzer.data.VCent;
import org.javanetworkanalyzer.data.VPred;

/**
 * Root interface for {@link GraphSearchAlgorithm}s that do centrality
 * calculations.
 *
 * @author Adam Gouge
 */
public interface CentralityAlg<V extends VCent, E, S extends PathLengthData>
        extends TraversalAlg<V, E> {

    /**
     * Returns the path length data.
     *
     * @return The path length data
     */
    S getPaths();
}
