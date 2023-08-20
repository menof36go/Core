package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents.DiagramDisambiguationAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents.DiagramReferenceAgent;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognitionStateImpl;
import edu.kit.kastel.mcse.ardoco.tests.eval.GoldStandardDiagrams;
import java.util.List;
import java.util.Map;

public class DiagramRecognitionMock extends ExecutionStage {
    @Configurable
    private List<String> enabledAgents;
    private final GoldStandardDiagrams goldStandardProject;

    public DiagramRecognitionMock(GoldStandardDiagrams goldStandardProject,
                                  Map<String, String> additionalConfigs,
                                  DataRepository dataRepository) {
        super("DiagramRecognitionMock", dataRepository,
                List.of(new DiagramDisambiguationAgent(dataRepository), new DiagramReferenceAgent(dataRepository)), additionalConfigs);
        this.goldStandardProject = goldStandardProject;
        enabledAgents = getAgentClassNames();
    }

    @Override
    protected void initializeState() {
        logger.info("Creating DiagramRecognitionMock State");
        var diagramRecognitionState = new DiagramRecognitionStateImpl();
        var diagrams = goldStandardProject.getDiagramsGoldStandard();
        for (var diagram : diagrams) {
            logger.debug("Loaded Diagram {}", diagram.getResourceName());
            diagramRecognitionState.addDiagram(diagram);
        }
        getDataRepository().addData(DiagramRecognitionState.ID, diagramRecognitionState);
    }

    @Override
    protected List<PipelineAgent> getEnabledAgents() {
        return findByClassName(enabledAgents, getAgents());
    }
}
