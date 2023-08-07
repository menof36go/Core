package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.models.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramrecognitionmock.InputDiagramDataMock;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.DiagramConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types.MDEInconsistency;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types.MTDEInconsistency;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognitionmock.DiagramRecognitionMock;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;
import edu.kit.kastel.mcse.ardoco.tests.eval.StageTest;

public class DiagramInconsistencyCheckerTest extends StageTest<DiagramInconsistencyChecker, DiagramInconsistencyCheckerTest.Results> {
    private static final Logger logger = LoggerFactory.getLogger(DiagramInconsistencyCheckerTest.class);
    private final static boolean useMockDiagrams = true;

    public record Results(SortedSet<MDEInconsistency> mdeInconsistencies, SortedSet<MTDEInconsistency> mtdeInconsistencies) {
        @Override
        public String toString() {
            return String.format("MissingDiagramElements: %d, MissingTextForDiagramElements: %d", mdeInconsistencies().size(), mtdeInconsistencies().size());
        }
    }

    public DiagramInconsistencyCheckerTest() {
        super(new DiagramInconsistencyChecker(Map.of(), null));
    }

    @Override
    protected Results runComparable(DiagramProject project, Map<String, String> additionalConfigurations, boolean cachePreRun) {
        var dataRepository = run(project, additionalConfigurations, cachePreRun);
        var diagramInconsistencyStates = dataRepository.getData(DiagramInconsistencyStates.ID, DiagramInconsistencyStates.class).orElseThrow();
        //TODO Get Metamodel properly
        var diagramInconsistencyState = diagramInconsistencyStates.getDiagramInconsistencyState(project.getMetamodel());

        var mdeInconsistencies = new TreeSet<>(diagramInconsistencyState.getInconsistencies(MDEInconsistency.class));
        var mtdeInconsistencies = new TreeSet<>(diagramInconsistencyState.getInconsistencies(MTDEInconsistency.class));

        var result = new DiagramInconsistencyCheckerTest.Results(mdeInconsistencies, mtdeInconsistencies);

        logger.info(result.toString());

        return result;
    }

    @Override
    protected DataRepository runPreTestRunner(DiagramProject project) {
        logger.info("Run PreTestRunner for {}", project.name());
        return new AnonymousRunner(project.name()) {
            @Override
            public void initializePipelineSteps() throws IOException {
                ArDoCo arDoCo = getArDoCo();
                var dataRepository = arDoCo.getDataRepository();

                if (useMockDiagrams) {
                    var data = new InputDiagramDataMock(project);
                    dataRepository.addData(InputDiagramDataMock.ID, data);
                    arDoCo.addPipelineStep(new DiagramRecognitionMock(project.getAdditionalConfigurations(), dataRepository));
                } else {
                    arDoCo.addPipelineStep(DiagramRecognition.get(project.getAdditionalConfigurations(), dataRepository));
                }

                var text = CommonUtilities.readInputText(project.getTextFile());
                if (text.isBlank()) {
                    throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
                }
                DataRepositoryHelper.putInputText(dataRepository, text);
                arDoCo.addPipelineStep(TextPreprocessingAgent.get(project.getAdditionalConfigurations(), dataRepository));

                arDoCo.addPipelineStep(ModelProviderAgent.get(project.getModelFile(), project.getArchitectureModelType(), dataRepository));
                arDoCo.addPipelineStep(TextExtraction.get(project.getAdditionalConfigurations(), dataRepository));
                arDoCo.addPipelineStep(RecommendationGenerator.get(project.getAdditionalConfigurations(), dataRepository));
                arDoCo.addPipelineStep(new DiagramConnectionGenerator(project.getAdditionalConfigurations(), dataRepository));
            }
        }.runWithoutSaving();
    }

    @Override
    protected DataRepository runTestRunner(DiagramProject project, Map<String, String> additionalConfigurations, DataRepository dataRepository) {
        logger.info("Run TestRunner for {}", project.name());
        var combinedConfigs = new HashMap<>(project.getAdditionalConfigurations());
        combinedConfigs.putAll(additionalConfigurations);
        return new AnonymousRunner(project.name()) {
            @Override
            public void initializePipelineSteps() {
                ArDoCo arDoCo = getArDoCo();
                var combinedRepository = arDoCo.getDataRepository();
                combinedRepository.addAllData(dataRepository);

                arDoCo.addPipelineStep(new DiagramInconsistencyChecker(combinedConfigs, combinedRepository));
            }
        }.runWithoutSaving();
    }

    @Disabled
    @Test
    void teammatesTest() {
        runComparable(DiagramProject.TEAMMATES);
    }

    @Disabled
    @Test
    void teammatesHistTest() {
        runComparable(DiagramProject.TEAMMATES_HISTORICAL);
    }

    @Disabled
    @Test
    void teastoreTest() {
        runComparable(DiagramProject.TEASTORE);
    }

    @Disabled
    @Test
    void teastoreHistTest() {
        runComparable(DiagramProject.TEASTORE_HISTORICAL);
    }

    @Disabled
    @Test
    void bbbTest() {
        runComparable(DiagramProject.BIGBLUEBUTTON);
    }

    @Disabled
    @Test
    void bbbHistTest() {
        runComparable(DiagramProject.BIGBLUEBUTTON_HISTORICAL);
    }

    @Disabled
    @Test
    void msTest() {
        runComparable(DiagramProject.MEDIASTORE);
    }
}