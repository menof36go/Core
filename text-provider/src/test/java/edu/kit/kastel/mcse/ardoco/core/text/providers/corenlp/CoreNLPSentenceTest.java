/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.base.SentenceTest;

class CoreNLPSentenceTest extends SentenceTest {
    @Override
    protected ITextConnector getProvider() {
        return CoreNLPProviderTest.getCoreNLPProvider();
    }
}