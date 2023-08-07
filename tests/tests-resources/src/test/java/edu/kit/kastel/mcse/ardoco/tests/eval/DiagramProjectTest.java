package edu.kit.kastel.mcse.ardoco.tests.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class DiagramProjectTest {

    @DisplayName("Test Diagram Project")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(1)
    void getDiagramsFromGoldstandard(DiagramProject diagramProject) {
        assertEquals(diagramProject.getDiagramsGoldStandardFile(), diagramProject.getDiagramsGoldStandardFile());
    }
}