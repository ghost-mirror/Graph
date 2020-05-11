package com.gohostmirror.util.graph;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.junit.Assert.*;

class GhraphHelper<V, E> {
    Graph<V,E> graph;

    GhraphHelper(boolean directGraph) {
        if (directGraph) {
            this.graph = Graphs.directedConcurrentGraph();
        } else  {
            this.graph = Graphs.undirectedConcurrentGraph();
        }
    }

    GhraphHelper<V, E> vertexCount(int count) {
        assertEquals(count, graph.getVertexCount());
        return this;
    }

    GhraphHelper<V, E> edgeCount(int count) {
        assertEquals(count, graph.getEdgeCount());
        return this;
    }

    GhraphHelper<V, E> isEmpty() {
        return vertexCount(0).edgeCount(0);
    }

    GhraphHelper<V, E> addOrigVertex(@NotNull V vertex) {
        int vertexCount = graph.getVertexCount();
        int edgeCount = graph.getEdgeCount();

        assertFalse(graph.isVertex(vertex));
        assertTrue(graph.addVertex(vertex));
        assertTrue(graph.isVertex(vertex));

        assertEquals(vertexCount+1, graph.getVertexCount());
        assertEquals(edgeCount, graph.getEdgeCount());
        return this;
    }

    GhraphHelper<V, E> addExistVertex(@NotNull V vertex) {
        int vertexCount = graph.getVertexCount();
        int edgeCount = graph.getEdgeCount();

        assertTrue(graph.isVertex(vertex));
        assertFalse(graph.addVertex(vertex));
        assertTrue(graph.isVertex(vertex));

        assertEquals(vertexCount, graph.getVertexCount());
        assertEquals(edgeCount, graph.getEdgeCount());
        return this;
    }

    GhraphHelper<V, E> addEdge(@NotNull EdgeHelper edge, @NotNull VertexHelper vertex1, @NotNull VertexHelper vertex2) {
        int vertexCount = graph.getVertexCount();
        int edgeCount = graph.getEdgeCount();

        vertex1.assertExist();
        vertex2.assertExist();
        edge.assertExist();

        assertFalse(graph.isConnection(vertex1.vertex, vertex2.vertex));
        assertNotEquals(edge.exist(), graph.addEdge(edge.edge, vertex1.vertex, vertex2.vertex));

        assertTrue(graph.isVertex(vertex1.vertex));
        assertTrue(graph.isVertex(vertex2.vertex));
        assertTrue(graph.isEdge(edge.edge));

        assertEquals(vertexCount + vertex1.orig + vertex2.orig, graph.getVertexCount());
        assertEquals(edgeCount+ edge.orig, graph.getEdgeCount());
        return this;
    }

    GhraphHelper<V, E> addEdge(@NotNull EdgeHelper edge, @NotNull V vertex1, @NotNull V vertex2) {
        int vertexCount = graph.getVertexCount();
        int edgeCount = graph.getEdgeCount();

        assertTrue(graph.isVertex(vertex1));
        assertTrue(graph.isVertex(vertex2));
        edge.assertExist();

        assertTrue(graph.isConnection(vertex1, vertex2));
        assertFalse(graph.addEdge(edge.edge, vertex1, vertex2));

        assertTrue(graph.isVertex(vertex1));
        assertTrue(graph.isVertex(vertex2));
        edge.assertExist();

        assertEquals(vertexCount, graph.getVertexCount());
        assertEquals(edgeCount, graph.getEdgeCount());
        return this;
    }

    class VertexHelper {
        final V vertex;
        final int orig;

        boolean exist() {
            return orig == 0;
        }

        void assertExist() {
            assertEquals(exist(), graph.isVertex(vertex));
        }

        VertexHelper(V vertex, boolean exist) {
            this.vertex = vertex;
            this.orig = exist ? 0 : 1;
        }
    }

    class EdgeHelper {
        final E edge;
        final int orig;

        boolean exist() {
            return orig == 0;
        }

        void assertExist() {
            assertEquals(exist(), graph.isEdge(edge));
        }

        EdgeHelper(E edge, boolean exist) {
            this.edge = edge;
            this.orig = exist? 0 : 1;
        }
    }

    VertexHelper origV(@NotNull V vertex) {
        return new VertexHelper(vertex, false);
    }

    VertexHelper existV(@NotNull V vertex) {
        return new VertexHelper(vertex, true);
    }

    EdgeHelper origE(@NotNull E edge) {
        return new EdgeHelper(edge, false);
    }

    EdgeHelper existE(@NotNull E edge) {
        return new EdgeHelper(edge, true);
    }
}
