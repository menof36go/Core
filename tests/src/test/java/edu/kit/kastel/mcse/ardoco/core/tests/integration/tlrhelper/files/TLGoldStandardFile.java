/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TestLink;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

public class TLGoldStandardFile {

    private TLGoldStandardFile() {
        // no instantiation
        throw new IllegalAccessError("No instantiation allowed");
    }

    public static MutableList<TestLink> loadLinks(Project project) throws IOException {
        Path path = project.getTlrGoldStandardFile().toPath();
        List<String> lines = Files.readAllLines(path);

        return Lists.mutable.ofAll(
                lines.stream()
                .skip(1) // skip csv header
                .map(line -> line.split(",")) // modelElementId,sentenceNr
                .map(array -> new TestLink(array[0], Integer.parseInt(array[1])))
                .map(link -> new TestLink(link.modelId(), link.sentenceNr() - 1))
                // ^ goldstandard sentences start with 1 while ISentences are zero indexed
                .toList());
    }

}
