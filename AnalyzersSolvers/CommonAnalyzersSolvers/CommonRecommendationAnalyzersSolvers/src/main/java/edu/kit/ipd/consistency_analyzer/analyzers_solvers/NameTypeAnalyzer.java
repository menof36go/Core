package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;
import edu.kit.ipd.consistency_analyzer.datastructures.IInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.INounMapping;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;

/**
 * This analyzer searches for name type patterns. If these patterns occur
 * recommendations are created.
 *
 * @author Sophie
 * 
 */
@MetaInfServices(IRecommendationAnalyzer.class)
public class NameTypeAnalyzer extends RecommendationAnalyzer {

	/**
	 * Creates a new NameTypeAnalyzer.
	 *
	 * @param graph                the PARSE graph
	 * @param textExtractionState  the text extraction state
	 * @param modelExtractionState the model extraction state
	 * @param recommendationState  the recommendation state
	 */
	public NameTypeAnalyzer(ITextExtractionState textExtractionState, IModelExtractionState modelExtractionState, IRecommendationState recommendationState) {
		super(DependencyType.TEXT_MODEL_RECOMMENDATION, textExtractionState, modelExtractionState, recommendationState);
	}

	public NameTypeAnalyzer() {
		this(null, null, null);
	}

	@Override
	public IRecommendationAnalyzer create(ITextExtractionState textExtractionState, IModelExtractionState modelExtractionState, IRecommendationState recommendationState) {
		return new NameTypeAnalyzer(textExtractionState, modelExtractionState, recommendationState);
	}

	private double probability = GenericRecommendationAnalyzerSolverConfig.NAME_TYPE_ANALYZER_PROBABILITY;

	@Override
	public void exec(IWord n) {
		checkForNameAfterType(textExtractionState, n);
		checkForNameBeforeType(textExtractionState, n);
		checkForNortBeforeType(textExtractionState, n);
		checkForNortAfterType(textExtractionState, n);
	}

	/**
	 * Checks if the current node is a type in the text extraction state. If the
	 * names of the text extraction state contain the previous node. If that's the
	 * case a recommendation for the combination of both is created.
	 *
	 * @param textExtractionState text extraction state
	 * @param n                   the current node
	 */
	private void checkForNameBeforeType(ITextExtractionState textExtractionState, IWord n) {
		IWord pre = n.getPreWord();

		Set<String> identifiers = modelExtractionState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
		identifiers.addAll(modelExtractionState.getInstanceTypes());

		List<String> similarTypes = identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, n.getText())).collect(Collectors.toList());

		if (!similarTypes.isEmpty()) {
			textExtractionState.addType(n, similarTypes.get(0), probability);
			IInstance instance = tryToIdentify(textExtractionState, similarTypes, pre);

			List<INounMapping> typeMappings = textExtractionState.getTypeNodesByNode(n);
			List<INounMapping> nameMappings = textExtractionState.getNameNodesByNode(pre);

			addRecommendedInstanceIfNodeNotNull(n, textExtractionState, instance, nameMappings, typeMappings);

		}
	}

	/**
	 * Checks if the current node is a type in the text extraction state. If the
	 * names of the text extraction state contain the following node. If that's the
	 * case a recommendation for the combination of both is created.
	 *
	 * @param textExtractionState text extraction state
	 * @param n                   the current node
	 */
	private void checkForNameAfterType(ITextExtractionState textExtractionState, IWord n) {
		IWord after = n.getNextWord();

		Set<String> identifiers = modelExtractionState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
		identifiers.addAll(modelExtractionState.getInstanceTypes());

		List<String> sameLemmaTypes = identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, n.getText())).collect(Collectors.toList());
		if (!sameLemmaTypes.isEmpty()) {
			textExtractionState.addType(n, sameLemmaTypes.get(0), probability);
			IInstance instance = tryToIdentify(textExtractionState, sameLemmaTypes, after);

			List<INounMapping> typeMappings = textExtractionState.getTypeNodesByNode(n);
			List<INounMapping> nameMappings = textExtractionState.getNameNodesByNode(after);

			addRecommendedInstanceIfNodeNotNull(n, textExtractionState, instance, nameMappings, typeMappings);

		}
	}

	/**
	 * Checks if the current node is a type in the text extraction state. If the
	 * name_or_types of the text extraction state contain the previous node. If
	 * that's the case a recommendation for the combination of both is created.
	 *
	 * @param textExtractionState text extraction state
	 * @param n                   the current node
	 */
	private void checkForNortBeforeType(ITextExtractionState textExtractionState, IWord n) {

		IWord pre = n.getPreWord();

		Set<String> identifiers = modelExtractionState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
		identifiers.addAll(modelExtractionState.getInstanceTypes());

		List<String> sameLemmaTypes = identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, n.getText())).collect(Collectors.toList());

		if (!sameLemmaTypes.isEmpty()) {
			textExtractionState.addType(n, sameLemmaTypes.get(0), probability);
			IInstance instance = tryToIdentify(textExtractionState, sameLemmaTypes, pre);

			List<INounMapping> typeMappings = textExtractionState.getTypeNodesByNode(n);
			List<INounMapping> nortMappings = textExtractionState.getNortNodesByNode(pre);

			addRecommendedInstanceIfNodeNotNull(n, textExtractionState, instance, nortMappings, typeMappings);
		}
	}

	/**
	 * Adds a RecommendedInstance to the recommendation state if the mapping of the
	 * current node exists. Otherwise a recommendation is added for each existing
	 * mapping.
	 *
	 * @param currentNode         the current node
	 * @param textExtractionState the text extraction state
	 * @param instance            the instance
	 * @param nameMappings        the name mappings
	 * @param typeMappings        the type mappings
	 */
	private void addRecommendedInstanceIfNodeNotNull(//
			IWord currentNode, ITextExtractionState textExtractionState, IInstance instance, List<INounMapping> nameMappings, List<INounMapping> typeMappings) {
		if (textExtractionState.getNounMappingsByNode(currentNode) != null && instance != null) {
			List<INounMapping> nmappings = textExtractionState.getNounMappingsByNode(currentNode);
			for (INounMapping nmapping : nmappings) {
				recommendationState.addRecommendedInstance(instance.getLongestName(), nmapping.getReference(), probability, nameMappings, typeMappings);
			}
		}
	}

	/**
	 * Checks if the current node is a type in the text extraction state. If the
	 * name_or_types of the text extraction state contain the afterwards node. If
	 * that's the case a recommendation for the combination of both is created.
	 *
	 * @param textExtractionState text extraction state
	 * @param n                   the current node
	 */
	private void checkForNortAfterType(ITextExtractionState textExtractionState, IWord n) {
		IWord after = n.getNextWord();

		Set<String> identifiers = modelExtractionState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
		identifiers.addAll(modelExtractionState.getInstanceTypes());

		List<String> sameLemmaTypes = identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, n.getText())).collect(Collectors.toList());
		if (!sameLemmaTypes.isEmpty()) {
			textExtractionState.addType(n, sameLemmaTypes.get(0), probability);
			IInstance instance = tryToIdentify(textExtractionState, sameLemmaTypes, after);

			List<INounMapping> typeMappings = textExtractionState.getTypeNodesByNode(n);
			List<INounMapping> nortMappings = textExtractionState.getNortNodesByNode(after);

			addRecommendedInstanceIfNodeNotNull(n, textExtractionState, instance, nortMappings, typeMappings);
		}
	}

	/**
	 * Tries to identify instances by the given similar types and the name of a
	 * given node. If an unambiguous instance can be found it is returned and the
	 * name is added to the text extraction state.
	 *
	 * @param textExtractioinState the next extraction state to work with
	 * @param similarTypes         the given similar types
	 * @param n                    the node for name identification
	 * @return the unique matching instance
	 */
	private IInstance tryToIdentify(ITextExtractionState textExtractioinState, List<String> similarTypes, IWord n) {
		List<IInstance> matchingInstances = new ArrayList<>();

		for (String type : similarTypes) {
			matchingInstances.addAll(modelExtractionState.getInstancesOfType(type));
		}

		matchingInstances = matchingInstances.stream().filter(i -> SimilarityUtils.areWordsOfListsSimilar(i.getNames(), List.of(n.getText()))).collect(Collectors.toList());

		if (matchingInstances.size() == 1) {

			textExtractioinState.addName(n, matchingInstances.get(0).getLongestName(), probability);
			return matchingInstances.get(0);
		}
		return null;
	}

}