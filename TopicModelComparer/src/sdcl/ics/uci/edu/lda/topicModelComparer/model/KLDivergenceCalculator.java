package sdcl.ics.uci.edu.lda.topicModelComparer.model;

import java.util.Collection;

/**
 * Calculates KL divergence between two term vectors (vectors with probabilities
 * for each term)
 * 
 * @author nlopezgi
 * 
 */
public class KLDivergenceCalculator {

	/**
	 * Constant for KL divergence algorithm. THe algorithm fails miserably
	 * whenever any probability in a vector =0. To compensate (and references
	 * online support this approach) we set all =0 probs to the
	 * MIN_PROBABILITY_VALUE. TO compensate for sum of probability>1 we also
	 * remove these marginal probabilities from any non zero probability in the
	 * vector (see KL divergence method for more info)
	 * 
	 */
	private static final double MIN_PROBABILITY_VALUE = 1.0e-250;

	/**
	 * Calculates the vector space model distance between vectors representing
	 * the term distribution for the given topics USING KL divergence
	 * similarity. Receives a vector with PROBABILITIES for each term.
	 * 
	 * @param oneTopic
	 * @param another
	 * @return
	 */
	public static double getKLDivergenceVectorSpaceDistance(
			double[] oneTopicTermVector, double[] anotherTopicTermVector,
			int vectorLength) {

		// no copies of vectors created, they must not be reused

		double sum = 0.0;
		double sum2 = 0.0;

		int discountFromOne = 0;
		int discountFromAnother = 0;
		int nonZeroFromOne = 0;
		int nonZeroFromAnother = 0;

		int i = 0;

		// We need to count how many terms are present in one but not in another
		for (i = 0; i < vectorLength; i++) {
			if (oneTopicTermVector[i] != 0) {
				nonZeroFromOne++;
			}
			if (anotherTopicTermVector[i] != 0) {
				nonZeroFromAnother++;
			}
			if (oneTopicTermVector[i] == 0 && anotherTopicTermVector[i] != 0) {
				discountFromOne++;
			}
			if (oneTopicTermVector[i] != 0 && anotherTopicTermVector[i] == 0) {
				discountFromAnother++;
			}
		}

		double totalDiscountForOne = discountFromOne * MIN_PROBABILITY_VALUE;
		double totalDiscountForAnother = discountFromAnother
				* MIN_PROBABILITY_VALUE;
		double individualDiscountForNonZeroForOne = totalDiscountForOne
				/ nonZeroFromOne;
		double individualDiscountForNonZeroForAnother = totalDiscountForAnother
				/ nonZeroFromAnother;

		for (i = 0; i < vectorLength; i++) {

			// If at least one of them is not 0
			if (!(oneTopicTermVector[i] == 0 && anotherTopicTermVector[i] == 0)) {
				if (oneTopicTermVector[i] == 0) {
					oneTopicTermVector[i] = MIN_PROBABILITY_VALUE;
				} else {
					oneTopicTermVector[i] = oneTopicTermVector[i]
							- individualDiscountForNonZeroForOne;
				}
				if (anotherTopicTermVector[i] == 0) {
					anotherTopicTermVector[i] = MIN_PROBABILITY_VALUE;
				} else {
					anotherTopicTermVector[i] = anotherTopicTermVector[i]
							- individualDiscountForNonZeroForAnother;
				}

				double log = Math.log(oneTopicTermVector[i]
						/ anotherTopicTermVector[i])
						/ Math.log(2);
				double log2 = Math.log(anotherTopicTermVector[i]
						/ oneTopicTermVector[i])
						/ Math.log(2);
				// sum += (oneTopicVector[i] - anotherTopicVector[i]) * log;
				sum += oneTopicTermVector[i] * log;
				sum2 += anotherTopicTermVector[i] * log2;
			}
		}
		return (sum * 0.5) + (sum2 * 0.5);

	}
}
