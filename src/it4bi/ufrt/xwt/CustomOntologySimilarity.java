package it4bi.ufrt.xwt;

import java.util.HashMap;
import java.util.Set;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;

public class CustomOntologySimilarity {
	
	// the Jena ontology model
	static OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

	public static int computeCustomOntologySimilarity(HashMap<String, Set<String>> conceptsFile1,
			HashMap<String, Set<String>> conceptsFile2) {
		// TODO Auto-generated method stub

		int result = 0;
		
		loadModel(m);

		if (conceptsFile1.equals(conceptsFile2)) {
			System.out.println("The two Servies have exactly same Inputs and Outputs." 
							+ " Hence they are Substitutable");
			result = 1;
		} else { // Calculate the similarity using Jena API

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
				System.out.println("Jena similarity = 1.0");
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
	 * compute the similarity value using Jena API
	 * @param set1
	 * @param set2
	 * @return
	 */
	private static double compareConceptSets(Set<String> set1, Set<String> set2) {
		double match = 0;
		if (!set1.isEmpty() && !set2.isEmpty()) {
			if (set1.size() >= set2.size()) {
				match = computeJenaSimilarity(set1, set2);
			} else {
				match = computeJenaSimilarity(set2, set1);
			}
		}
		return match;
	}

	/**
	 * This method iterates through the input concepts sets and determine
	 * whether they are equivalent, similar or non-related
	 * @param inputSet1
	 * @param inputSet2
	 * @return
	 */
	private static double computeJenaSimilarity(Set<String> inputSet1, Set<String> inputSet2) {
		// TODO Auto-generated method stub
		double minSimilarity = 1;

		for (String inputConcept1 : inputSet1) {

			double bestMatch = 0;
			for (String inputConcept2 : inputSet2) {

				double match = 0;

				if (inputConcept1.equalsIgnoreCase(inputConcept2)) {
					match = 1;
				} else {
					match = computeJenaRelatedness(inputConcept1, inputConcept2);
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
	 * This method load the Ontology model using the Custom Ontology provided
	 * @param m
	 */

	protected static void loadModel(OntModel m) {
		FileManager.get().getLocationMapper().addAltEntry(Properties.SOURCE_URL, Properties.SOURCE_FILE);
		Model baseOntology = FileManager.get().loadModel(Properties.SOURCE_URL);
		m.addSubModel(baseOntology);

		// for compactness, add a prefix declaration st: (for Sam Thomas)
		m.setNsPrefix("st", Properties.NS);
	}

	/**
	 * Similarity using Jena API
	 * The two concepts are considered to be similar, in the following cases
	 * Case 1: Identical class (Similarity = 1.0)
	 * Case 2: Has the same super class, ie. siblings (Similarity = 0.9)
	 * Case 3: first concept is a superClass or Subclass of the second concept
	 * (Similarity = 0.8)
	 * In all other cases, they are considered to be unrelated.
	 * @param word1
	 * @param word2
	 * @return
	 */
	private static double computeJenaRelatedness(String word1, String word2) {
		// TODO Auto-generated method stub
		
		double match = 0;
		
		OntClass concept1 = m.getOntClass(Properties.NS + word1);
		OntClass concept2 = m.getOntClass(Properties.NS + word2);
		
		if (concept1.getSuperClass().equals(concept2.getSuperClass())){
			System.out.println("Rel(" + word1 + ", " + word2 + ") = Siblings");
			match = 0.9;
		}else if (concept1.hasSubClass(concept2) || concept1.hasSuperClass(concept2)){
			System.out.println("Rel(" + word1 + ", " + word2 + ") = Parent-Child");
			match = 0.8;
		}else{	
			// Any additional conditions, add here
			match = 0;
		}
		return match;
	}
}
