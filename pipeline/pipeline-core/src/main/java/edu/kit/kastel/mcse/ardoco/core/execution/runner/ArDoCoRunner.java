/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;

public abstract class ArDoCoRunner implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoRunner.class);

    private transient final ArDoCo arDoCo;

    private File outputDirectory;
    protected boolean isSetUp = false;

    protected ArDoCoRunner(String projectName) {
        this.arDoCo = new ArDoCo(projectName);
        outputDirectory = null;
    }

    public boolean isSetUp() {
        return isSetUp;
    }

    public final ArDoCoResult run() {
        if (this.isSetUp() && outputDirectory != null) {
            return this.getArDoCo().runAndSave(outputDirectory);
        } else {
            logger.error("Cannot run ArDoCo because the runner is not properly set up.");
            return null;
        }
    }

    public final DataRepository runWithoutSaving() {
        if (this.isSetUp()) {
            this.getArDoCo().run();
            return this.getArDoCo().getDataRepository();
        } else {
            logger.error("Cannot run ArDoCo because the runner is not properly set up.");
            return null;
        }
    }

    public ArDoCo getArDoCo() {
        return this.arDoCo;
    }

    protected void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
