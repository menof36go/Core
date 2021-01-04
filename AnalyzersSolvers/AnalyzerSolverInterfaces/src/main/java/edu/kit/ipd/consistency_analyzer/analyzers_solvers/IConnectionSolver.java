package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import edu.kit.ipd.consistency_analyzer.datastructures.IConnectionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;

public interface IConnectionSolver extends ISolver {

	IConnectionSolver create(//
			ITextExtractionState textExtractionState, IModelExtractionState modelExtractionState, //
			IRecommendationState recommendationState, IConnectionState connectionState);
}