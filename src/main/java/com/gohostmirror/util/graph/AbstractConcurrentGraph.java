package com.gohostmirror.util.graph;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

abstract class AbstractConcurrentGraph<V, E> implements Graph<V, E> {
    private final AbstractGraphFactory<V, E> factory;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    protected final Lock readLock = lock.readLock();
    protected final Lock writeLock = lock.writeLock();
    protected final GraphMap<V, E> map;

    AbstractConcurrentGraph() {
        this(new MixedGraphFactory<>());
    }

    AbstractConcurrentGraph(AbstractGraphFactory<V, E> factory) {
        this.factory = factory;
        map = this.factory.graphMap();
    }

    @Override
    @Contract(pure = true)
    public int getVertexCount() {
        readLock.lock();
        try {
            return map.getVertexCount();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @Contract(pure = true)
    public int getEdgeCount() {
        readLock.lock();
        try {
            return map.getEdgeCount();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @Contract(pure = true)
    public @NotNull Collection<V> getMap() {
        readLock.lock();
        try {
            return map.getVertices();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @Contract(pure = true)
    public @NotNull Collection<E> getEdges() {
        readLock.lock();
        try {
            return map.getEdges();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @Contract(pure = true)
    public boolean isVertex(@NotNull V vertex) {
        readLock.lock();
        try {
            return map.isVertex(vertex);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @Contract(pure = true)
    public boolean isEdge(@NotNull E edge) {
        readLock.lock();
        try {
            return map.isEdge(edge);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean addVertex(@NotNull V vertex) {
        writeLock.lock();
        try {
            return map.addVertex(vertex);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public @NotNull List<V> getPath(@NotNull V fromVertex, @NotNull V toVertex) {
        readLock.lock();
        try {
            return getPathUnsafe(fromVertex, toVertex);
        } finally {
            readLock.unlock();
        }
    }

    private @NotNull List<V> getPathUnsafe(@NotNull V fromVertex, @NotNull V toVertex) {
        if (!isVertex(fromVertex) || !isVertex(toVertex)) {
            return Collections.emptyList();
        }
        Set<V> vertexMarker = new HashSet<>();
        Queue<VertexNode> vertexQueue = new ArrayDeque<>();
        VertexNode node;

        for (V vertex : map.adjacentVertices(fromVertex)) {
            vertexQueue.add(new VertexNode(vertex, null));
        }

        while ((node = vertexQueue.poll()) != null) {
            if (node.vertex.equals(toVertex)) {
                VertexNode prevNode = null;
                while(node != null) {
                    VertexNode nextNode = node.prevNode;
                    node.prevNode = prevNode;
                    prevNode = node;
                    node = nextNode;
                }
                node = prevNode;

                List<V> result = new ArrayList<>();
                result.add(fromVertex);
                while(node != null) {
                    result.add(node.vertex);
                    node = node.prevNode;
                }
                return result;
            }
            if (vertexMarker.contains(node.vertex)) {
                continue;
            }
            vertexMarker.add(node.vertex);
            for (V adjacentVertex : map.adjacentVertices(node.vertex)) {
                if (!vertexMarker.contains(adjacentVertex)) {
                    vertexQueue.add(new VertexNode(adjacentVertex, node));
                }
            }
        }

        return Collections.emptyList();
    }
    private class VertexNode {
        private V vertex;
        private VertexNode prevNode;

        private VertexNode(V vertex, VertexNode prevNode) {
            this.vertex = vertex;
            this.prevNode = prevNode;
        }
    }

}
