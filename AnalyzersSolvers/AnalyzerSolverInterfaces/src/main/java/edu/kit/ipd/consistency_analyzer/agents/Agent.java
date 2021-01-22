package edu.kit.ipd.consistency_analyzer.agents;

public abstract class Agent implements IAgent {

	protected DependencyType dependencyType;

	public abstract Agent create(AgentDatastructure data);

	/**
	 * Creates a new agent of the specified type.
	 *
	 * @param type the agent type
	 */
	protected Agent(DependencyType dependencyType) {
		this.dependencyType = dependencyType;
	}

	/**
	 * Returns the dependency type of the current agent.
	 *
	 * @return the dependency type of the current agent
	 */
	public DependencyType getDependencyType() {
		return dependencyType;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
