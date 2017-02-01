package it4bi.ufrt.xwt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ConceptsExtractor {
	
	/**
	 * The method to extract Input Concepts from OWL files
	 * Reuses OWLS Parser implementation from Project Phase II
	 * @param parser
	 * @param conceptsFile
	 */
	
	public void extractInputs(OWLSParser parser, HashMap<String, Set<String>> conceptsFile) {
		String inputs = parser.extractProcessInput();
		Set<String> extractedInputs = extractNames(inputs);
		conceptsFile.put("inputs", extractedInputs);
	}
	
	/**
	 * The method to extract Output Concepts from OWL files
	 * Reuses OWLS Parser implementation from Project Phase II
	 * @param parser
	 * @param concepts
	 */

	public void extractOutputs(OWLSParser parser, HashMap<String, Set<String>> concepts) {
		String outputs = parser.extractProcessOutput();
		Set<String> extractedOutputs = extractNames(outputs);
		concepts.put("outputs", extractedOutputs);
	}

	/**
	 * This method helps to extract the exact concept names from 
	 * Input / Output concepts output from OWLS Parser.
	 * @param concept
	 * @return
	 */
	private Set<String> extractNames(String concept) {

		Set<String> extracts = new HashSet<String>();

		if (concept != null && !concept.isEmpty()) {
			String[] splitConcept = concept.split("\n");
			for (String value : splitConcept) {
				int idx = value.indexOf("#");
				String strAfterHash = value.substring(idx + 1);
				int spaceIdx = strAfterHash.indexOf(" ");
				String name = strAfterHash.substring(0, spaceIdx);
				extracts.add(name);
			}
		}
		return extracts;
	}
}
