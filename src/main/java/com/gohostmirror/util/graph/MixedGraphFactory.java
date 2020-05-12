package com.gohostmirror.util.graph;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MixedGraphFactory<V, E> implements AbstractGraphFactory<V, E> {
    @Override
    public GraphMap<V, E> graphMap() {
        return new MixedGraphMap();
    }

    private class MixedGraphMap implements GraphMap<V, E> {
        private final SafeMap<V, E> dummy = new SafeMap<>(Collections.emptyMap());
        private final Map<V, SafeMap<V, E>> vertices = new HashMap<>();
        private final Map<E, VertexPair<V>> edges = new HashMap<>();

        private final Set<V> verticesKeySet = Collections.unmodifiableSet(vertices.keySet());
        private final Set<E> edgesKeySet = Collections.unmodifiableSet(edges.keySet());

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
            return verticesKeySet;
        }

        @Override
        @Contract(pure = true)
        public @NotNull Collection<E> getEdges() {
            return edgesKeySet;
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
        @Contract(pure = true)
        public @Nullable E getEdge(@NotNull V vertex1, @NotNull V vertex2) {
            return getConnectionMap(vertex1).get(vertex2);
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

        private @NotNull SafeMap<V, E> getConnectionMap(@NotNull V vertex) {
            return Objects.requireNonNullElse(vertices.get(vertex), dummy);
        }

        private @NotNull SafeMap<V, E> getOrCreateNotDummyConnectionMap(@NotNull V vertex) {
            SafeMap<V, E> connectionMap = vertices.get(vertex);
            if (connectionMap != dummy) {
                return connectionMap;
            }
            SafeMap<V, E> map = new SafeMap<>();
            vertices.put(vertex, map);
            return map;
        }
    }

    private static class SafeMap<V, E> {
        private final Map<V, E> map;
        private final Set<V> keySet;
        private final Collection<E> values;

        SafeMap() {
            this(new HashMap<>());
        }

        SafeMap(Map<V, E> map) {
            this.map = map;
            this.keySet = Collections.unmodifiableSet(map.keySet());
            this.values = Collections.unmodifiableCollection(map.values());
        }

        void put(V key, E value) {
            map.put(key, value);
        }

        E get(V key) {
            return map.get(key);
        }

        Set<V> keySet() {
            return keySet;
        }

        Collection<E> values() {
            return values;
        }
    }

    private static class VertexPair<V> {
        private final List<V> vertices = new ArrayList<>(2);
        private final List<V> unmodifiableVertices = Collections.unmodifiableList(vertices);

        private VertexPair(@NotNull V vertex1, @NotNull V vertex2) {
            vertices.add(vertex1);
            vertices.add(vertex2);
        }

        @Contract(pure = true)
        @NotNull List<V> list () {
            return unmodifiableVertices;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VertexPair<?> that = (VertexPair<?>) o;
            return Objects.equals(vertices.get(0), that.vertices.get(0)) &&
                   Objects.equals(vertices.get(1), that.vertices.get(1));
        }

        @Override
        public int hashCode() {
            return Objects.hash(vertices.get(0), vertices.get(1));
        }
    }
}
