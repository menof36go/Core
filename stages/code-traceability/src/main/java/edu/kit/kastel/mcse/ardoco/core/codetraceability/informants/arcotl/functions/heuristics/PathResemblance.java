package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.functions.heuristics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.NameComparisonUtils;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.Confidence;

public class PathResemblance extends StandaloneHeuristic {

    @Override
    protected Confidence calculateConfidence(ArchitectureComponent archComponent, CodeCompilationUnit compUnit) {
        return calculatePathResemblance(archComponent, compUnit);
    }

    @Override
    protected Confidence calculateConfidence(ArchitectureInterface archInterface, CodeCompilationUnit compUnit) {
        if (!archInterface.getSignatures().isEmpty()) {
            return new Confidence();
        }
        return calculatePathResemblance(archInterface, compUnit);
    }

    private Confidence calculatePathResemblance(Entity archEndpoint, CodeCompilationUnit compUnit) {
        List<String> codeNames = NameComparisonUtils.getProcessedSplit(compUnit.getPathElements());
        Set<String> allPackageNames = getAllPackageNames();
        if (compUnit.hasParent()) {
            codeNames.removeAll(allPackageNames);
        }
        double similarity = NameComparisonUtils.getContainedRatio(archEndpoint, codeNames, NameComparisonUtils.PreprocessingMethod.NONE);
        if (similarity == 0) {
            return new Confidence();
        }
        return new Confidence(similarity);
    }

    private Set<String> getAllPackageNames() {
        Set<? extends CodePackage> packages = getCodeModel().getAllPackages();
        Set<String> allPackageNames = new HashSet<>();
        for (CodePackage codePackage : packages) {
            allPackageNames.add(codePackage.getName());
        }
        return allPackageNames;
    }

    @Override
    public String toString() {
        return "PathResemblance";
    }
}
