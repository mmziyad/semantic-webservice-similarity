package it4bi.ufrt.xwt;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

public class WordNetSimilarity {

	// The instantiation of WordNet database
	private static ILexicalDatabase db = new NictWordNet();
	
	// Threshold value for similarity between two words

	/**
	 * The method to compute the similarity between two web services using WordNet
	 * Compares Input and Output concepts of Web services
	 * @param conceptsFile1
	 * @param conceptsFile2
	 * @return
	 */
	public static int computeWNSimilarity(HashMap<String, Set<String>> conceptsFile1,
			HashMap<String, Set<String>> conceptsFile2) {
		// TODO Auto-generated method stub
		// Result shows the type of correlation between two web services
		int result = 0;
		
		//WordNet configuration
		WS4JConfiguration.getInstance().setMFS(true);

		// Check for exact match between concepts
		if (conceptsFile1.equals(conceptsFile2)) {
			System.out.println("The two Servies have exactly same Inputs and Outputs. "
					+ "Hence they are Substitutable");
			result = 1;
		} else { // Calculate the similarity using Word Net WUP
			
			// get input and output concepts
			Set<String> inputs1 = conceptsFile1.get("inputs");
			Set<String> inputs2 = conceptsFile2.get("inputs");

			Set<String> outputs1 = conceptsFile1.get("outputs");
			Set<String> outputs2 = conceptsFile2.get("outputs");

			// compare inputs of two services
			System.out.println("Comparing input vs input");
			double inputMatch = compareConceptSets(inputs1, inputs2);

			// compare outputs of two services
			System.out.println("Comparing output vs output");
			double outputMatch = compareConceptSets(outputs1, outputs2);

			if (inputMatch == 1 && outputMatch == 1) {
				// the services are equivalent
				System.out.println("The two Servies are equivalent. Hence they are Substitutable");
				System.out.println("WordNet similarity = 1.0");
				result = 1;
			} else if (inputMatch > 0 && outputMatch > 0) {

				// the services are highly similar.
				// the similarity of inputs and outputs are inputMatch,
				// outputMatch respectively.

				System.out.println("The services are Substitutable with a Simiarity");
				System.out.println("Input Similarity = " + inputMatch);
				System.out.println("Output Similarity = " + outputMatch);
				System.out.println("The threshold used is = " + Properties.THRESHOLD);

				result = 2;
			} else {

				// services are not substitutable
				// Check whether they are composable

				System.out.println("Comparing input vs output");
				double firstToSecondComposability = compareConceptSets(inputs1, outputs2);
				System.out.println("Comparing output vs input");
				double SecondToFirstComposability = compareConceptSets(inputs2, outputs1);

				if (firstToSecondComposability == 1) {
					// the services are composable perfectly

					System.out.println("The services are Composable perfectly (similarity = 1.0)");
					System.out.println("The Inputs of first Service and Ouptuts of Second Service are equivalent");

					result = 3;
				} else if (firstToSecondComposability > 0) {

					System.out.println("The services are Composable with a Simiarity");
					System.out.println("The Inputs of first Service and Ouptuts of Second Service are similar");
					System.out.println(" Similarity = " + firstToSecondComposability);
					System.out.println("The threshold used is = " + Properties.THRESHOLD);

					// the services are composable to some similarity given by
					// firstToSecondComposability
					result = 4;
				} else if (SecondToFirstComposability == 1) {
					// the services are composable perfectly

					System.out.println("The services are Composable perfectly (similarity = 1.0)");
					System.out.println("The Inputs of second Service and Ouptuts of first Service are equivalent");

					result = 3;
				} else if (SecondToFirstComposability > 0) {
					// the services are composable to some similarity given by
					// SecondToFirstComposability

					System.out.println("The services are Composable with a Simiarity");
					System.out.println("The Inputs of second Service and Ouptuts of first Service are similar");
					System.out.println(" Similarity = " + SecondToFirstComposability);
					System.out.println("The threshold used is = " + Properties.THRESHOLD);
					result = 4;
				} else {
					// the services are not composable
					System.out.println("The services are neither composable or substitutable");
					result = 5;
				}
			}
		}

		return result;
	}

	/**
	 * This method identifies the largest set among the input concepts sets and
	 * compute the similarity value using word net
	 * 
	 * @param set1
	 * @param set2
	 * @return
	 */
	private static double compareConceptSets(Set<String> set1, Set<String> set2) {
		double match = 0;
		if (!set1.isEmpty() && !set2.isEmpty()) {
			if (set1.size() >= set2.size()) {
				match = computeWNSimilarity(set1, set2);
			} else {
				match = computeWNSimilarity(set2, set1);
			}
		}
		return match;
	}

	/**
	 * This method iterates through the input concepts sets and determine
	 * whether they are equivalent, similar or non-related
	 * 
	 * @param inputSet1
	 * @param inputSet2
	 * @return
	 */
	private static double computeWNSimilarity(Set<String> inputSet1, Set<String> inputSet2) {
		// TODO Auto-generated method stub

		double minSimilarity = 1;

		for (String inputConcept1 : inputSet1) {

			double bestMatch = 0;
			for (String inputConcept2 : inputSet2) {

				double match = 0;

				if (inputConcept1.equalsIgnoreCase(inputConcept2)) {
					match = 1;
				} else {
					match = computeRelatedness(inputConcept1, inputConcept2);
				}
				System.out.println("Match = " + match);
				if (match > bestMatch) {
					bestMatch = match;
				}

				System.out.println("BestMatch = " + bestMatch);
			}

			if (bestMatch >= Properties.THRESHOLD) {
				if (bestMatch < minSimilarity)
					minSimilarity = bestMatch;
				continue;
			} else {
				return 0;
			}
		}
		System.out.println("minSimilarity" + minSimilarity);
		return minSimilarity;
	}

	/**
	 * The WordNet similarity implementation for a pair of words
	 * Returns the relatedness using WUP method of WordNet.
	 * @param word1
	 * @param word2
	 * @return
	 */
	public static double computeRelatedness(String word1, String word2) {
		
		// Choose the required Relatedness Calculator
		// Currently we are using Wu Palmer Implemnetation
		
		//RelatednessCalculator rc = new Lin(db);
		//RelatednessCalculator rc = new Path(db);
		
		RelatednessCalculator rc = new WuPalmer(db);
		List<POS[]> posPairs = rc.getPOSPairs();
		double maxScore = -1D;

		for(POS[] posPair: posPairs) {
		    List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word1, posPair[0].toString());
		    List<Concept> synsets2 = (List<Concept>)db.getAllConcepts(word2, posPair[1].toString());

		    for(Concept synset1: synsets1) {
		        for (Concept synset2: synsets2) {
		            Relatedness relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
		            double score = relatedness.getScore();
		            if (score > maxScore) { 
		                maxScore = score;
		            }
		        }
		    }
		}

		if (maxScore == -1D) {
		    maxScore = 0.0;
		}
		System.out.println("sim('" + word1 + "', '" + word2 + "') =  " + maxScore);
		return maxScore;
	}
}