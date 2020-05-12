package com.gohostmirror.util.graph;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface Graph<V, E> {
    @Contract(pure = true)
    boolean isDirectedGraph ();

    @Contract(pure = true)
    int getVertexCount();

    @Contract(pure = true)
    int getEdgeCount();

    @Contract(pure = true)
    boolean isVertex (@NotNull V vertex);

    @Contract(pure = true)
    boolean isEdge (@NotNull E edge);

    @Contract(pure = true)
    @NotNull Collection<V> getVertices();

    @Contract(pure = true)
    @NotNull Collection<E> getEdges();

    @Contract(pure = true)
    @NotNull Collection<E> incidentEdges(@NotNull V vertex);

    @Contract(pure = true)
    @NotNull List<V> incidentVertices(@NotNull E edge);

    @Contract(pure = true)
    @Nullable E getEdge(@NotNull V vertex1, @NotNull V vertex2);

    @Contract(pure = true)
    @NotNull List<V> getPath(@NotNull V fromVertex, @NotNull V toVertex);

    boolean addVertex (@NotNull V vertex);

    boolean addEdge (@NotNull E edge, @NotNull V vertex1, @NotNull V vertex2);

    @Contract(pure = true)
    boolean isConnection(@NotNull V vertex1, @NotNull V vertex2);
}
