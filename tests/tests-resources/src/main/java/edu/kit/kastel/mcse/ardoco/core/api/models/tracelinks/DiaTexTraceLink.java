package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;

public class DiaTexTraceLink implements Comparable<DiaTexTraceLink>, Serializable {
    protected final DiagramElement diagramElement;
    protected final Sentence sentence;
    protected final String projectName;

    protected Set<DiaTexTraceLink> related = new LinkedHashSet<>();

    /**
     * Creates a tracelink between a diagram element and a sentence
     *
     * @param diagramElement diagram element
     * @param sentence       sentence
     * @param projectName    project name
     */
    public DiaTexTraceLink(@NotNull DiagramElement diagramElement, @NotNull Sentence sentence, @NotNull String projectName) {
        this.diagramElement = diagramElement;
        this.sentence = sentence;
        this.projectName = projectName;
    }

    public @NotNull DiagramElement getDiagramElement() {
        return diagramElement;
    }

    /**
     * Gets the sentence number, indexing starts at 1.
     *
     * @return sentence number
     */
    public int getSentenceNo() {
        return sentence.getSentenceNumberForOutput();
    }

    public boolean equalEndpoints(DiaTexTraceLink other) {
        return this.getSentenceNo() == other.getSentenceNo() && getDiagramElement().equals(other.getDiagramElement());
    }

    @Override
    public String toString() {
        return MessageFormat.format("[{0}]-[{1}]", getDiagramElement(), getSentence().getText());
    }

    public @NotNull Sentence getSentence() {
        return this.sentence;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        else if (obj instanceof DiaTexTraceLink other) {
            return Objects.equals(getDiagramElement(), other.getDiagramElement()) && Objects.equals(getSentenceNo(), other.getSentenceNo());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSentenceNo(), getDiagramElement());
    }

    @Override
    public int compareTo(@NotNull DiaTexTraceLink o) {
        if (equals(o))
            return 0;
        var comp = getDiagramElement().compareTo(o.getDiagramElement());
        if (comp == 0)
            return getSentenceNo() - o.getSentenceNo();
        return comp;
    }

    public void setRelated(Set<DiaTexTraceLink> related) {
        Set<DiaTexTraceLink> set = new LinkedHashSet<>(related);
        set.remove(this);
        this.related = set;
    }

    public Set<DiaTexTraceLink> getRelated() {
        return Collections.unmodifiableSet(related);
    }
}
