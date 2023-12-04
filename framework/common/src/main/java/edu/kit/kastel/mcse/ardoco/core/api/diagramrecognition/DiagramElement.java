/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.eclipse.collections.impl.factory.SortedSets;
import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelElement;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityComparable;
import edu.kit.kastel.mcse.ardoco.core.data.MetaData;

/**
 * This box represents a geometrical shape with an arbitrary amount of text from a diagram. An element can be uniquely identified by its bounding box or UID and
 * the diagram it belongs to.
 */
public abstract class DiagramElement extends Entity implements SimilarityComparable<DiagramElement> {
    private final Diagram diagram;

    private final transient LazyInitializer<DiagramElement> parent = new LazyInitializer<>() {
        @Override
        protected DiagramElement initialize() {
            var all = getDiagram().getBoxes();
            return all.stream()
                    .filter(de -> !de.equals(DiagramElement.this) && de.getBoundingBox().containsEntirely(getBoundingBox())) //Find boxes containing this element
                    .min(Comparator.comparingDouble(de -> de.getBoundingBox().area()))
                    .orElse(null);
        }
    };

    private final transient LazyInitializer<MutableSortedSet<DiagramElement>> children = new LazyInitializer<>() {
        @Override
        protected MutableSortedSet<DiagramElement> initialize() {
            var all = getDiagram().getBoxes();
            return SortedSets.mutable.withAll(all.stream()
                    .filter(de -> !de.equals(DiagramElement.this) && de.getParent().map(p -> p == DiagramElement.this).orElse(false))
                    .map(b -> (DiagramElement) b)
                    .toList());
        }
    };

    /**
     * Creates a new diagram element that is associated with the given diagram and unique identifier.
     *
     * @param diagram the diagram this element is associated with
     * @param uuid    the unique identifier
     */
    protected DiagramElement(@NotNull Diagram diagram, @NotNull String uuid) {
        super(uuid);
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
     * {@return the set of elements which are direct children of this diagram element} Determined indirectly by searching for diagram elements in the diagram
     * which reference this element as their parent.
     *
     * @see #getParent()
     */
    public @NotNull ImmutableSortedSet<DiagramElement> getChildren() {
        try {
            return children.get().toImmutable();
        } catch (ConcurrentException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * {@return the optional parent of this element, empty if this diagram element is at the top-most level} Searches the diagram for diagram elements whose
     * bounding box entirely contain this element. The diagram element with the smallest area is chosen as parent.
     *
     * @see BoundingBox#containsEntirely(BoundingBox)
     */
    public Optional<DiagramElement> getParent() {
        try {
            return Optional.ofNullable(parent.get());
        } catch (ConcurrentException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiagramElement other) {
            return Objects.equals(getDiagram().getResourceName(), other.getDiagram().getResourceName()) && getBoundingBox().equals(other.getBoundingBox());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDiagram().getResourceName(), getBoundingBox());
    }

    @Override
    public int compareTo(@NotNull ModelElement o) {
        if (equals(o))
            return 0;
        if (o instanceof DiagramElement other) {
            return Comparator.comparing(DiagramElement::getDiagram).thenComparing(DiagramElement::getBoundingBox).compare(this, other);
        }
        return super.compareTo(o);
    }

    @Override
    public boolean similar(MetaData metaData, DiagramElement obj) {
        if (equals(obj))
            return true;
        if (diagram.getResourceName().equals(obj.diagram.getResourceName()))
            return getBoundingBox().similar(metaData, obj.getBoundingBox());
        return false;
    }
}
