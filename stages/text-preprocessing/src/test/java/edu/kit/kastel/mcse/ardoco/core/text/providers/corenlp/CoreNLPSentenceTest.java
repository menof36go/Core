/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.NlpInformant;
import edu.kit.kastel.mcse.ardoco.core.text.providers.base.SentenceTest;

class CoreNLPSentenceTest extends SentenceTest {
    @Override
    protected NlpInformant getProvider() {
        return CoreNLPProviderTest.getCoreNLPProvider();
    }
}