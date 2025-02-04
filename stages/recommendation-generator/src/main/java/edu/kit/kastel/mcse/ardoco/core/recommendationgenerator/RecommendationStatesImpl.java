/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.EnumMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendationStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.data.GlobalConfiguration;

public class RecommendationStatesImpl implements RecommendationStates {
    private final EnumMap<Metamodel, RecommendationStateImpl> recommendationStates;

    private RecommendationStatesImpl() {
        recommendationStates = new EnumMap<>(Metamodel.class);
    }

    public static RecommendationStates build(GlobalConfiguration globalConfiguration) {
        var recStates = new RecommendationStatesImpl();
        for (Metamodel mm : Metamodel.values()) {
            RecommendationStateStrategy rss = new DefaultRecommendationStateStrategy(globalConfiguration);
            recStates.recommendationStates.put(mm, new RecommendationStateImpl(rss));
        }
        return recStates;
    }

    @Override
    public RecommendationStateImpl getRecommendationState(Metamodel mm) {
        return recommendationStates.get(mm);
    }
}
