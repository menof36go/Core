package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

public abstract class DiagramElement extends Entity implements Comparable<DiagramElement> {
    private final Diagram diagram;

    protected DiagramElement(@NotNull Diagram diagram, @NotNull String name) {
        super(name);
        this.diagram = diagram;
    }

    /**
     * Returns a {@link BoundingBox}, which encases the element.
     *
     * @return the {@link BoundingBox}
     */
    public abstract @NotNull BoundingBox getBoundingBox();

    /**
     * Returns the {@link Diagram}, which this element belongs to.
     *
     * @return the {@link Diagram}
     */
    public @NotNull Diagram getDiagram() {
        return this.diagram;
    }

    /**
     * Returns all elements with a bounding box that is entirely contained in this element's bounding box. See
     * {@link BoundingBox#containsEntirely(BoundingBox)}.
     *
     * @return the set of elements which are considered sub elements
     */
    public @NotNull ImmutableSet<DiagramElement> getSubElements() {
        var all = getDiagram().getBoxes();
        return Sets.immutable.fromStream(all.stream().filter(de -> !de.equals(this) && getBoundingBox().containsEntirely(de.getBoundingBox())));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiagramElement other) {
            try {
                return Files.isSameFile(getDiagram().getLocation().toPath(), other.getDiagram().getLocation().toPath()) && getBoundingBox().equals(
                        other.getBoundingBox());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDiagram(), getBoundingBox());
    }

    @Override
    public int compareTo(DiagramElement o) {
        return hashCode() - o.hashCode();
    }
}