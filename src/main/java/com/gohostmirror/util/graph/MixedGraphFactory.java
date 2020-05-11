package com.gohostmirror.util.graph;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MixedGraphFactory<V, E> implements AbstractGraphFactory<V, E> {
    @Override
    public GraphMap<V, E> graphMap() {
        return new MixedGraphMap();
    }

    private class MixedGraphMap implements GraphMap<V, E> {
        private final Map<V, E> dummy = Collections.emptyMap();
        private final Map<V, Map<V, E>> vertices = new HashMap<>();
        private final Map<E, VertexPair<V>> edges = new HashMap<>();

        @Override
        @Contract(pure = true)
        public int getVertexCount() {
            return vertices.size();
        }

        @Override
        @Contract(pure = true)
        public int getEdgeCount() {
            return edges.size();
        }

        @Override
        @Contract(pure = true)
        public boolean isVertex(@NotNull V vertex) {
            return vertices.containsKey(vertex);
        }

        @Override
        @Contract(pure = true)
        public boolean isEdge(@NotNull E edge) {
            return edges.containsKey(edge);
        }

        @Override
        @Contract(pure = true)
        public boolean isDirectConnection(@NotNull V vertex1, @NotNull V vertex2) {
            return adjacentVertices(vertex1).contains(vertex2);
        }

        @Override
        @Contract(pure = true)
        public @NotNull Collection<V> getVertices() {
            return vertices.keySet();
        }

        @Override
        @Contract(pure = true)
        public @NotNull Collection<E> getEdges() {
            return edges.keySet();
        }

        @Override
        @Contract(pure = true)
        public @NotNull Set<V> adjacentVertices(@NotNull V vertex) {
            return getConnectionMap(vertex).keySet();
        }

        @Override
        @Contract(pure = true)
        public @NotNull Collection<E> incidentEdges(@NotNull V vertex) {
            return getConnectionMap(vertex).values();
        }

        @Override
        @Contract(pure = true)
        public @NotNull List<V> incidentVertices(@NotNull E edge) {
            VertexPair<V> vertexPair = edges.get(edge);
            if (vertexPair == null) {
                return Collections.emptyList();
            }
            return vertexPair.list();
        }

        @Override
        public boolean addVertex(@NotNull V vertex) {
            if (isVertex(vertex)) {
                return false;
            }
            return (vertices.put(vertex, dummy) == null);
        }

        @Override
        public boolean addDirectEdge(@NotNull E edge, @NotNull V vertex1, @NotNull V vertex2) {
            if (isEdge(edge) || isDirectConnection(vertex1, vertex2)) {
                return false;
            }

            addVertex(vertex1);
            addVertex(vertex2);
            getOrCreateNotDummyConnectionMap(vertex1).put(vertex2, edge);
            edges.put(edge, new VertexPair<>(vertex1, vertex2));

            return true;
        }

        @Override
        public boolean addBidirectionalEdge(@NotNull E edge, @NotNull V vertex1, @NotNull V vertex2) {
            if (isEdge(edge)
                    || isDirectConnection(vertex1, vertex2)
                    || isDirectConnection(vertex2, vertex1)) {
                return false;
            }

            addVertex(vertex1);
            addVertex(vertex2);
            getOrCreateNotDummyConnectionMap(vertex1).put(vertex2, edge);
            getOrCreateNotDummyConnectionMap(vertex2).put(vertex1, edge);
            edges.put(edge, new VertexPair<>(vertex1, vertex2));

            return true;
        }

        private @NotNull Map<V, E> getConnectionMap(@NotNull V vertex) {
            Map<V, E> connectionMap = vertices.get(vertex);
            if (connectionMap != null) {
                return connectionMap;
            }
            return dummy;
        }

        private @NotNull Map<V, E> getOrCreateNotDummyConnectionMap(@NotNull V vertex) {
            Map<V, E> connectionMap = vertices.get(vertex);
            if (connectionMap != dummy) {
                return connectionMap;
            }
            Map<V, E> map = new HashMap<>();
            vertices.put(vertex, map);
            return map;
        }
    }

    private static class VertexPair<V> {
        private final List<V> vertises = new ArrayList<>(2);

        private VertexPair(@NotNull V vertex1, @NotNull V vertex2) {
            vertises.add(vertex1);
            vertises.add(vertex2);
        }

        @NotNull List<V> list () {
            return vertises;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VertexPair<?> that = (VertexPair<?>) o;
            return Objects.equals(vertises.get(0), that.vertises.get(0)) &&
                    Objects.equals(vertises.get(1), that.vertises.get(1));
        }

        @Override
        public int hashCode() {
            return Objects.hash(vertises.get(0), vertises.get(1));
        }
    }
}
