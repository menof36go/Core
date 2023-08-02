package edu.kit.kastel.mcse.ardoco.tests;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramDataMock;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunnerExt;
import edu.kit.kastel.mcse.ardoco.erid.DiagramRecognitionMock;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public class TestRunner extends ArDoCoRunnerExt<TestRunner.Parameters> {
    public TestRunner(String projectName) {
        super(projectName);
    }

    public record Parameters(DiagramProject diagramProject) {
    }

    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

    @Override
    public boolean setUp(Parameters p) {
        try {
            definePipeline(p);
        } catch (IOException e) {
            logger.error("Problem in initialising pipeline when loading data (IOException)", e.getCause());
            isSetUp = false;
            return false;
        }
        isSetUp = true;
        return true;
    }

    private void definePipeline(Parameters p) throws IOException {
        ArDoCo arDoCo = getArDoCo();
        var dataRepository = arDoCo.getDataRepository();

        var data = new InputDiagramDataMock(p.diagramProject);
        dataRepository.addData(InputDiagramDataMock.ID, data);

        arDoCo.addPipelineStep(new DiagramRecognitionMock(p.diagramProject().getAdditionalConfigurations(), dataRepository));
    }
}
