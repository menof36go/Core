/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.extractors;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

public class ProjectNameExtractor extends Informant {

    @Configurable
    public static final double PENALTY = Double.NEGATIVE_INFINITY;

    public ProjectNameExtractor(DataRepository dataRepository) {
        super("ProjectNameExtractor", dataRepository);
    }

    @Override
    public void run() {
        DataRepository dataRepository = getDataRepository();
        var projectName = DataRepositoryHelper.getProjectPipelineData(dataRepository).getProjectName();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);
            Metamodel metamodel = modelState.getMetamodel();
            var recommendationState = recommendationStates.getRecommendationState(metamodel);

            checkForProjectNameInRecommendedInstances(projectName, recommendationState);
        }
    }

    private void checkForProjectNameInRecommendedInstances(String projectName, RecommendationState recommendationState) {
        for (var recommendedInstance : recommendationState.getRecommendedInstances()) {
            checkForProjectNameInNounMappingsOfRecommendedInstance(projectName, recommendedInstance);
        }
    }

    private void checkForProjectNameInNounMappingsOfRecommendedInstance(String projectName, RecommendedInstance recommendedInstance) {
        for (var nm : recommendedInstance.getNameMappings()) {
            checkWordsInNounMapping(projectName, recommendedInstance, nm);
        }
    }

    private void checkWordsInNounMapping(String projectName, RecommendedInstance recommendedInstance, NounMapping nm) {
        for (var word : nm.getWords()) {
            String wordText = word.getText().toLowerCase();
            if (projectName.contains(wordText)) {
                var words = expandWordForName(projectName, word);
                var expandedWord = concatenateWords(words);
                if (SimilarityUtils.areWordsSimilar(projectName, expandedWord)) {
                    recommendedInstance.addProbability(this, PENALTY);
                }
            }
        }
    }

    private String concatenateWords(MutableList<Word> words) {
        var sortedWords = words.sortThisByInt(Word::getPosition);
        String concatenatedWords = "";
        for (var word : sortedWords) {
            concatenatedWords += word.getText().toLowerCase();
        }
        return concatenatedWords;
    }

    private MutableList<Word> expandWordForName(String projectName, Word word) {
        MutableList<Word> words = Lists.mutable.with(word);
        var editedProjectName = getEditedProjectName(projectName);

        expandWordForNameLeft(editedProjectName, words);
        expandWordForNameRight(editedProjectName, words);

        return words.distinct().sortThisByInt(Word::getPosition);
    }

    private void expandWordForNameLeft(String name, MutableList<Word> words) {
        Objects.requireNonNull(name);
        if (words.isEmpty()) {
            throw new IllegalArgumentException("List cannot be empty");
        }

        Word currWord = words.sortThisByInt(Word::getPosition).getFirstOptional().orElseThrow(IllegalArgumentException::new);
        expandWordForName(name, currWord, words, (w) -> w.getPreWord(), (text, addition) -> addition + text);
    }

    private void expandWordForNameRight(String name, MutableList<Word> words) {
        Objects.requireNonNull(name);
        if (words.isEmpty()) {
            throw new IllegalArgumentException("List cannot be empty");
        }

        var currWord = words.sortThisByInt(Word::getPosition).getLastOptional().orElseThrow(IllegalArgumentException::new);
        expandWordForName(name, currWord, words, (w) -> w.getNextWord(), (text, addition) -> text + addition);
    }

    private void expandWordForName(String name, Word currWord, MutableList<Word> words, Function<Word, Word> wordExpansion,
            BiFunction<String, String, String> concatenation) {
        Objects.requireNonNull(name);
        if (words.isEmpty()) {
            throw new IllegalArgumentException("List cannot be empty");
        }

        var testWordText = concatenateWords(words);
        while (name.contains(testWordText)) {
            words.add(currWord);

            currWord = wordExpansion.apply(currWord);
            var wordText = "NON-MATCHING";
            if (currWord != null) {
                wordText = currWord.getText().toLowerCase();
            }
            testWordText = concatenation.apply(testWordText, wordText);
        }
    }

    private static String getEditedProjectName(String projectName) {
        // remove white spaces from project name
        return projectName.replaceAll("\s", "");
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // handle additional configuration
    }
}
