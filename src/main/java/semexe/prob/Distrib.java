package semexe.prob;

import java.util.Random;

/**
 * A probability distribution.
 * Should have the following functionality:
 * - logProb(data or sufficient statistics)
 * - sample(Random random)
 * - get parameters of the distribution
 * - getMean(), getVar()
 * Sampling and evaluating log probability should exist in both static and
 * non-static versions.
 * The reason we don't write the functions in the interface
 * is because distributions have different return types,
 * and we don't want to impose restrictions or use heavy templates.
 */
public interface Distrib<T> {
    double logProb(SuffStats stats); // \log p_{this}(stats)

    double logProbObject(T x); // \log p_{this}(x)

    T sampleObject(Random random); // return x \sim p_{this}

    double crossEntropy(Distrib<T> distrib); // \E_{p_{this}} \log p_{distrib}
}
