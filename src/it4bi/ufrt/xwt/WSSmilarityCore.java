package it4bi.ufrt.xwt;

import java.util.HashMap;
import java.util.Set;

public class WSSmilarityCore {

	private HashMap<String, Set<String>> conceptsFile1 = new HashMap<String, Set<String>>();
	private HashMap<String, Set<String>> conceptsFile2 = new HashMap<String, Set<String>>();

	private OWLSParser parserFile1;
	private OWLSParser parserFile2;

	/**
	 * This method identifies the relationship between two input web services
	 * whether they are substitutable, composable or non-related.
	 * 
	 * @param file1
	 * @param file2
	 */

	public WSSmilarityCore(String file1, String file2) {

		parserFile1 = new OWLSParser(file1);
		parserFile2 = new OWLSParser(file2);

		ConceptsExtractor conceptsExtractor = new ConceptsExtractor();

		// extracts input features
		conceptsExtractor.extractInputs(parserFile1, conceptsFile1);
		conceptsExtractor.extractOutputs(parserFile1, conceptsFile1);

		System.out.println("The concepts of the file " + file1);
		for (String concept : conceptsFile1.keySet()) {
			System.out.println(concept + " == " + conceptsFile1.get(concept));
		}

		// extract output features
		conceptsExtractor.extractInputs(parserFile2, conceptsFile2);
		conceptsExtractor.extractOutputs(parserFile2, conceptsFile2);

		System.out.println("The concepts of the file " + file2);
		for (String concept : conceptsFile2.keySet()) {
			System.out.println(concept + " == " + conceptsFile2.get(concept));
		}

		// Calculate the relationship using WordNet LOD.
		System.out.println("========= WORD NET SIMILARITY ==========");
		int WNResult = WordNetSimilarity.computeWNSimilarity(conceptsFile1, conceptsFile2);

		// Calcualte the relationship using Local Ontology
		System.out.println("========= CUSTOM ONTOLOGY SIMILARITY ==========");
		int JenaResult = CustomOntologySimilarity.computeCustomOntologySimilarity(conceptsFile1, conceptsFile2);
		
		System.out.println("========== FINAL RESULTS ========");
		
		if (WNResult != 5) {
			System.out.println("Substitutability / Composability was identified using WordNet Similarity");
		} else {
			System.out.println("The two services are unrelated according to WordNet Similartity");
		}
		if (JenaResult != 5) {
			System.out.println("Substitutability / Composability was identified using Custom Ontolgoy Similarity");
		} else {
			System.out.println("The two services are unrelated according to Custom Ontolgoy similarity");
		}

	}
}
