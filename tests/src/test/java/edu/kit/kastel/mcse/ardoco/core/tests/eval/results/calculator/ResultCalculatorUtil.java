package edu.kit.kastel.mcse.ardoco.core.tests.eval.results.calculator;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationMetrics;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResultVector;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

public abstract class ResultCalculatorUtil {

    public static <T> EvaluationResults<T> calculateAverageResults(MutableList<EvaluationResults<T>> results) {
        int norm = results.size();
        EvaluationResultVector<T> vector = new EvaluationResultVector<>();

        for (var result: results) {
            var weight = result.getWeight();
            if (weight <= 0) {
                norm--;
                continue;
            }
            vector.add(result);
        }

        vector.scale(norm);
        return vector.toEvaluationResults();
    }

    public static <T> EvaluationResults<T> calculateWeightedAverageResults(MutableList<EvaluationResults<T>> results) {
        double weight = 0.0;
        double precision = .0;
        double recall = .0;
        double f1 = .0;
        double accuracy = .0;
        double specificity = 0.0;
        double phi = 0.0;
        double phiMax = 0.0;
        double phiOverPhiMax = 0.0;
        int truePositives = 0;
        int trueNegatives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;

        for (var result: results) {
            double localWeight = result.getWeight();

            precision += (localWeight * result.precision());
            recall += (localWeight * result.recall());
            f1 += (localWeight * result.f1());
            weight += localWeight;

            accuracy += (localWeight * result.accuracy());
            specificity += (localWeight * result.specificity());
            phi += result.phiCoefficient() * localWeight;
            phiMax += result.phiCoefficientMax() * localWeight;
            phiOverPhiMax += result.phiOverPhiMax() * localWeight;

            truePositives += result.truePositives().size();
            falseNegatives += result.falseNegatives().size();
            falsePositives += result.falsePositives().size();
            trueNegatives += result.trueNegatives();


        }

        precision = precision / weight;
        recall = recall / weight;
        f1 = f1 / weight;
        accuracy = accuracy / weight;
        specificity = specificity / weight;

        if (truePositives > 0) {
            phi = EvaluationMetrics.calculatePhiCoefficient(truePositives, falsePositives, falseNegatives, trueNegatives);
            phiMax = EvaluationMetrics.calculatePhiCoefficientMax(truePositives, falsePositives, falseNegatives, trueNegatives);
            phiOverPhiMax = EvaluationMetrics.calculatePhiOverPhiMax(truePositives, falsePositives, falseNegatives, trueNegatives);

            return new EvaluationResults<>(precision, recall, f1,
                    Lists.immutable.empty(), 0, Lists.immutable.empty(), Lists.immutable.empty(),
                    accuracy, phi, specificity, phiMax, phiOverPhiMax);
        }

        phi = phi / weight;
        phiMax /= weight;
        phiOverPhiMax /= weight;
        return new EvaluationResults<>(precision, recall, f1,
                Lists.immutable.empty(), 0, Lists.immutable.empty(), Lists.immutable.empty(),
                accuracy, phi, specificity, phiMax, phiOverPhiMax);

    }
}
