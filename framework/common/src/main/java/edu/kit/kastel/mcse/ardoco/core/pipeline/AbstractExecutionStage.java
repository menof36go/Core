/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.util.List;
import java.util.SortedMap;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.configuration.ChildClassConfigurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Agent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import java.util.List;
import java.util.Map;

/**
 * This abstract class represents an execution step within ArDoCo. Examples are Text-Extraction,
 * Recommendation-Generator, Connection-Generator, and
 * Inconsistency-Checker.
 * <p>
 * Implementing classes need to implement {@link #initializeState()} that cares for setting up
 * the state for processing. Additionally, implementing classes need
 * to implement {@link #getEnabledAgents()} that returns the listof enabled {@link PipelineAgent
 * pipeline agents}
 */
public abstract class AbstractExecutionStage extends Pipeline {
    private final List<? extends PipelineAgent> agents;

    private final MutableList<PipelineAgent> agents;

    @Configurable
    @ChildClassConfigurable
    private List<String> enabledAgents;

    /**
     * Constructor for ExecutionStages
     * 
     * @param agents         the agents that could be executed by this pipeline
     * @param id             the id of the stage
     * @param dataRepository the {@link DataRepository} that should be used
     * @param agents         the pipeline agents this stage supports
     */
    protected AbstractExecutionStage(List<? extends PipelineAgent> agents, String id, DataRepository dataRepository) {
        super(id, dataRepository);
        this.agents = Lists.mutable.withAll(agents);
        this.enabledAgents = this.agents.collect(PipelineAgent::getId);
    }

    @Override
    protected final void preparePipelineSteps() {
        super.preparePipelineSteps();
        initializeState();

        for (var agent : agents) {
            if (enabledAgents.contains(agent.getId())) {
                this.addPipelineStep(agent);
            }
        }
    }

    /**
     * Prepare processing and set up the (internal) state
     */
    protected abstract void initializeState();

    /**
     * Called before all agents
     */
    protected void before() {
        //Nothing by default
    }

    /**
     * Called after all agents
     */
    protected void after() {
        //Nothing by default
    }

    /**
     * Return the enabled {@link PipelineAgent agents}
     *
     * @return the list of agents
     */
    protected abstract List<PipelineAgent> getEnabledAgents();

    /**
     * {@return the {@link PipelineAgent agents}}
     */
    public List<PipelineAgent> getAgents() {
        return List.copyOf(agents);
    }

    /**
     * {@return the class names of all agents including disabled}
     */
    public List<String> getAgentClassNames() {
        return agents.stream().map(Agent::getClass).map(Class::getSimpleName).toList();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        super.delegateApplyConfigurationToInternalObjects(additionalConfiguration);
        for (var agent : agents) {
            agent.applyConfiguration(additionalConfiguration);
        }
    }
}
