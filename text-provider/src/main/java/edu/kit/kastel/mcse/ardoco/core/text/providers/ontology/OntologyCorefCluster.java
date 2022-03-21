/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntProperty;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.ICorefCluster;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;

/**
 * @author Jan Keim
 */
public final class OntologyCorefCluster implements ICorefCluster {

    private final Individual corefIndividual;
    private final OntologyConnector ontologyConnector;

    private OntProperty mentionProperty;
    private OntProperty representativeMentionProperty;
    private OntProperty wordsProperty;
    private OntProperty uuidProperty;

    private final OntologyText text;

    private OntologyCorefCluster(OntologyConnector ontologyConnector, Individual corefIndividual, OntologyText text) {
        this.corefIndividual = corefIndividual;
        this.ontologyConnector = ontologyConnector;
        this.text = text;
    }

    static OntologyCorefCluster get(OntologyConnector ontologyConnector, Individual corefIndividual, OntologyText text) {
        if (ontologyConnector == null || corefIndividual == null) {
            return null;
        }

        var occ = new OntologyCorefCluster(ontologyConnector, corefIndividual, text);
        occ.init();
        return occ;
    }

    private void init() {
        mentionProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.HAS_MENTION_PROPERTY.getUri()).orElseThrow();
        representativeMentionProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.REPRESENTATIVE_MENTION_PROPERTY.getUri()).orElseThrow();
        wordsProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.HAS_WORDS_PROPERTY.getUri()).orElseThrow();
        uuidProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.UUID_PROPERTY.getUri()).orElseThrow();
    }

    @Override
    public int id() {
        var optId = ontologyConnector.getPropertyIntValue(corefIndividual, uuidProperty);
        return optId.orElse(-1);
    }

    @Override
    public String representativeMention() {
        var representativeMention = ontologyConnector.getPropertyStringValue(corefIndividual, representativeMentionProperty);
        return representativeMention.orElse(null);
    }

    @Override
    public ImmutableList<ImmutableList<IWord>> mentions() {
        MutableList<ImmutableList<IWord>> mentionList = Lists.mutable.empty();

        ImmutableList<Individual> mentions = ontologyConnector.getObjectsOf(corefIndividual, mentionProperty).collect(n -> n.as(Individual.class));
        if (mentions == null || mentions.isEmpty()) {
            return mentionList.toImmutable();
        }

        for (var mention : mentions) {
            var wordListResource = ontologyConnector.getPropertyValue(mention, wordsProperty).asResource();
            var wordListOpt = ontologyConnector.getListByIri(wordListResource.getURI());
            if (wordListOpt.isEmpty()) {
                continue;
            }
            var wordList = wordListOpt.get();

            MutableList<IWord> words = Lists.mutable.empty();
            for (var wordIndividual : wordList) {
                var word = OntologyWord.get(ontologyConnector, wordIndividual, text);
                words.add(word);
            }
            mentionList.add(words.toImmutable());
        }

        return mentionList.toImmutable();
    }

}
