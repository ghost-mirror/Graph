package com.gohostmirror.util.graph;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface GraphMap<V, E> {
    @Contract(pure = true)
    int getVertexCount();

    @Contract(pure = true)
    int getEdgeCount();

    @Contract(pure = true)
    boolean isVertex(@NotNull V vertex);

    @Contract(pure = true)
    boolean isEdge (@NotNull E edge);

    @Contract(pure = true)
    boolean isDirectConnection(@NotNull V vertex1, @NotNull V vertex2);

    @Contract(pure = true)
    @NotNull Collection<V> getVertices();

    @Contract(pure = true)
    @NotNull Collection<E> getEdges();

    @Contract(pure = true)
    @NotNull Collection<V> adjacentVertices(@NotNull V vertex);

    @Contract(pure = true)
    @NotNull Collection<E> incidentEdges(@NotNull V vertex);

    @Contract(pure = true)
    @NotNull List<V> incidentVertices(@NotNull E edge);

    boolean addVertex(@NotNull V vertex);

    boolean addDirectEdge(@NotNull E edge, @NotNull V vertex1, @NotNull V vertex2);

    boolean addBidirectionalEdge(@NotNull E edge, @NotNull V vertex1, @NotNull V vertex2);
}
