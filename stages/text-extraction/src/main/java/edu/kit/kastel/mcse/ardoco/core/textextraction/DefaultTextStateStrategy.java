/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.PhraseAbbreviation;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.WordAbbreviation;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public abstract class DefaultTextStateStrategy implements TextStateStrategy {

    private TextStateImpl textState;

    public void setTextState(TextStateImpl textExtractionState) {
        textState = textExtractionState;
    }

    public TextStateImpl getTextState() {
        return textState;
    }

    @Override
    public NounMapping createNounMappingStateless(ImmutableSet<Word> words, MutableMap<MappingKind, Confidence> distribution,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms, String reference) {
        if (reference == null) {
            reference = calculateNounMappingReference(referenceWords);
        }

        return new NounMappingImpl(System.currentTimeMillis(), words.toSortedSet().toImmutable(), distribution, referenceWords, surfaceForms, reference);
    }

    @Override
    public NounMapping addNounMapping(ImmutableSet<Word> words, MutableMap<MappingKind, Confidence> distribution, ImmutableList<Word> referenceWords,
            ImmutableList<String> surfaceForms, String reference) {
        //Do not add noun mappings to the state, which do not have any claimants
        assert distribution.values().stream().anyMatch(d -> d.getClaimants().size() > 0);

        NounMapping nounMapping = createNounMappingStateless(words, distribution, referenceWords, surfaceForms, reference);
        getTextState().addNounMappingAddPhraseMapping(nounMapping);
        return nounMapping;
    }

    @Override
    public NounMapping addNounMapping(ImmutableSet<Word> words, MappingKind kind, Claimant claimant, double probability, ImmutableList<Word> referenceWords,
            ImmutableList<String> surfaceForms, String reference) {
        MutableMap<MappingKind, Confidence> distribution = Maps.mutable.empty();
        distribution.put(MappingKind.NAME, new Confidence(DEFAULT_AGGREGATOR));
        distribution.put(MappingKind.TYPE, new Confidence(DEFAULT_AGGREGATOR));
        var nounMapping = createNounMappingStateless(words, distribution, referenceWords, surfaceForms, reference);
        nounMapping.addKindWithProbability(kind, claimant, probability);
        getTextState().addNounMappingAddPhraseMapping(nounMapping);
        return nounMapping;
    }

    public NounMapping mergeNounMappings(NounMapping nounMapping, MutableList<NounMapping> nounMappingsToMerge, Claimant claimant) {
        for (NounMapping nounMappingToMerge : nounMappingsToMerge) {

            if (!textState.getNounMappings().contains(nounMappingToMerge)) {

                final NounMapping finalNounMappingToMerge = nounMappingToMerge;
                var fittingNounMappings = textState.getNounMappings().select(nm -> nm.getWords().containsAllIterable(finalNounMappingToMerge.getWords()));
                if (fittingNounMappings.isEmpty()) {
                    continue;
                } else if (fittingNounMappings.size() == 1) {
                    nounMappingToMerge = fittingNounMappings.get(0);
                } else {
                    throw new IllegalStateException();
                }
            }

            assert textState.getNounMappings().contains(nounMappingToMerge);

            var references = nounMapping.getReferenceWords().toList();
            references.addAllIterable(nounMappingToMerge.getReferenceWords());
            textState.mergeNounMappings(nounMapping, nounMappingToMerge, claimant, references.toImmutable());

            var mergedWords = SortedSets.mutable.empty();
            mergedWords.addAllIterable(nounMapping.getWords());
            mergedWords.addAllIterable(nounMappingToMerge.getWords());

            var mergedNounMapping = textState.getNounMappings().select(nm -> nm.getWords().toSet().equals(mergedWords));

            assert (mergedNounMapping.size() == 1);

            nounMapping = mergedNounMapping.get(0);
        }

        return nounMapping;
    }

    protected final Confidence putAllConfidencesTogether(Confidence confidence, Confidence confidence1) {

        Confidence result = confidence.createCopy();
        result.addAllConfidences(confidence1);
        return result;
    }

    @Override
    public WordAbbreviation addOrExtendWordAbbreviation(String abbreviation, Word word) {
        var wordAbbreviation = getTextState().getWordAbbreviations(word).stream().filter(e -> e.getAbbreviation().equals(abbreviation)).findFirst();
        if (wordAbbreviation.isPresent()) {
            return extendWordAbbreviation(wordAbbreviation.orElseThrow(), word);
        } else {
            var newWordAbbreviation = new WordAbbreviation(abbreviation, Sets.mutable.of(word));
            getTextState().wordAbbreviations.add(newWordAbbreviation);
            return newWordAbbreviation;
        }
    }

    protected WordAbbreviation extendWordAbbreviation(WordAbbreviation wordAbbreviation, Word word) {
        wordAbbreviation.addWord(word);
        return wordAbbreviation;
    }

    @Override
    public PhraseAbbreviation addOrExtendPhraseAbbreviation(String abbreviation, Phrase phrase) {
        var phraseAbbreviation = getTextState().getPhraseAbbreviations(phrase).stream().filter(e -> e.getAbbreviation().equals(abbreviation)).findFirst();
        if (phraseAbbreviation.isPresent()) {
            return extendPhraseAbbreviation(phraseAbbreviation.orElseThrow(), phrase);
        } else {
            var newPhraseAbbreviation = new PhraseAbbreviation(abbreviation, Sets.mutable.of(phrase));
            getTextState().phraseAbbreviations.add(newPhraseAbbreviation);
            return newPhraseAbbreviation;
        }
    }

    protected PhraseAbbreviation extendPhraseAbbreviation(PhraseAbbreviation phraseAbbreviation, Phrase phrase) {
        phraseAbbreviation.addPhrase(phrase);
        return phraseAbbreviation;
    }
}
