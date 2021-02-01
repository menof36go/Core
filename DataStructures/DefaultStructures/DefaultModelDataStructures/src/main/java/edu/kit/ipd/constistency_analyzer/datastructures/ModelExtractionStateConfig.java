package edu.kit.ipd.constistency_analyzer.datastructures;

import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public class ModelExtractionStateConfig {

    private ModelExtractionStateConfig() {
        throw new IllegalAccessError();
    }

    private static final SystemParameters CONFIG = loadParameters("/configs/modelExtractionState.properties");

    /**
     * The minimal amount of parts of the type that the type is splitted and can be identified by parts.
     */
    public static final int EXTRACTION_STATE_MIN_TYPE_PARTS = CONFIG.getPropertyAsInt("ExtractionState_MinTypeParts");

    private static SystemParameters loadParameters(String filePath) {
        return new SystemParameters(filePath, true);
    }
}
