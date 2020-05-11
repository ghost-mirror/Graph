package com.gohostmirror.util.graph;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Graphs {
    static  <V, E> Graph<V, E> directedConcurrentGraph() {
        return new DirectedConcurrentGraph<>();
    }

    static <V, E> Graph<V, E> undirectedConcurrentGraph() {
        return new UndirectedConcurrentGraph<>();
    }
}

class DirectedConcurrentGraph<V, E> extends AbstractConcurrentGraph<V, E> {
    @Override
    @Contract(pure = true)
    public boolean isDirectedGraph() {
        return true;
    }

    @Override
    public boolean addEdge(@NotNull E edge, @NotNull V vertex1, @NotNull V vertex2) {
        writeLock.lock();
        try {
            return map.addDirectEdge(edge, vertex1, vertex2);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    @Contract(pure = true)
    public boolean isConnection(@NotNull V vertex1, @NotNull V vertex2) {
        readLock.lock();
        try {
            return map.isDirectConnection(vertex1, vertex2);
        } finally {
            readLock.unlock();
        }
    }

}

class UndirectedConcurrentGraph<V, E> extends AbstractConcurrentGraph<V, E> {
    @Override
    @Contract(pure = true)
    public boolean isDirectedGraph() {
        return false;
    }

    @Override
    public boolean addEdge(@NotNull E edge, @NotNull V vertex1, @NotNull V vertex2) {
        writeLock.lock();
        try {
            return map.addBidirectionalEdge(edge, vertex1, vertex2);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    @Contract(pure = true)
    public boolean isConnection(@NotNull V vertex1, @NotNull V vertex2) {
        readLock.lock();
        try {
            return map.isDirectConnection(vertex1, vertex2) && map.isDirectConnection(vertex2, vertex1);
        } finally {
            readLock.unlock();
        }
    }
}
