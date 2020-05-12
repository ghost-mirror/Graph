package com.gohostmirror.util.graph;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

class GraphHelper<V, E> {
    Graph<V,E> graph;

    GraphHelper(boolean directGraph) {
        if (directGraph) {
            this.graph = Graphs.directedConcurrentGraph();
        } else  {
            this.graph = Graphs.undirectedConcurrentGraph();
        }
    }

    GraphHelper<V, E> vertexCount(int count) {
        assertEquals(count, graph.getVertexCount());
        return this;
    }

    GraphHelper<V, E> edgeCount(int count) {
        assertEquals(count, graph.getEdgeCount());
        return this;
    }

    GraphHelper<V, E> isEmpty() {
        return vertexCount(0).edgeCount(0);
    }

    GraphHelper<V, E> addOrigVertex(@NotNull V vertex) {
        int vertexCount = graph.getVertexCount();
        int edgeCount = graph.getEdgeCount();

        assertFalse(graph.isVertex(vertex));
        assertTrue(graph.addVertex(vertex));
        assertTrue(graph.isVertex(vertex));

        assertEquals(vertexCount+1, graph.getVertexCount());
        assertEquals(edgeCount, graph.getEdgeCount());
        return this;
    }

    GraphHelper<V, E> addExistVertex(@NotNull V vertex) {
        int vertexCount = graph.getVertexCount();
        int edgeCount = graph.getEdgeCount();

        assertTrue(graph.isVertex(vertex));
        assertFalse(graph.addVertex(vertex));
        assertTrue(graph.isVertex(vertex));

        assertEquals(vertexCount, graph.getVertexCount());
        assertEquals(edgeCount, graph.getEdgeCount());
        return this;
    }

    GraphHelper<V, E> addEdge(@NotNull EdgeHelper edgeHelper, @NotNull VertexHelper vertexHelper1, @NotNull VertexHelper vertexHelper2) {
        int vertexCount = graph.getVertexCount();
        int edgeCount = graph.getEdgeCount();

        vertexHelper1.assertExist();
        vertexHelper2.assertExist();
        edgeHelper.assertExist();

        assertFalse(graph.isConnection(vertexHelper1.vertex, vertexHelper2.vertex));
        if(!graph.isDirectedGraph()) {
            assertFalse(graph.isConnection(vertexHelper2.vertex, vertexHelper1.vertex));
        }

        verifyConnections(edgeHelper.edge, edgeHelper.exist(), vertexHelper1.vertex, vertexHelper2.vertex);
        boolean added = graph.addEdge(edgeHelper.edge, vertexHelper1.vertex, vertexHelper2.vertex);
        assertNotEquals(added, edgeHelper.exist());
        verifyConnections(edgeHelper.edge, added || edgeHelper.exist(), vertexHelper1.vertex, vertexHelper2.vertex);

        assertTrue(graph.isVertex(vertexHelper1.vertex));
        assertTrue(graph.isVertex(vertexHelper2.vertex));
        assertTrue(graph.isEdge(edgeHelper.edge));

        assertEquals(vertexCount + vertexHelper1.orig + vertexHelper2.orig, graph.getVertexCount());
        assertEquals(edgeCount+ edgeHelper.orig, graph.getEdgeCount());
        return this;
    }

    GraphHelper<V, E> addEdge(@NotNull EdgeHelper edgeHelper, @NotNull V vertex1, @NotNull V vertex2) {
        int vertexCount = graph.getVertexCount();
        int edgeCount = graph.getEdgeCount();

        assertTrue(graph.isVertex(vertex1));
        assertTrue(graph.isVertex(vertex2));
        edgeHelper.assertExist();

        assertTrue(graph.isConnection(vertex1, vertex2));
        if(!graph.isDirectedGraph()) {
            assertTrue(graph.isConnection(vertex2, vertex1));
        }

        verifyConnections(edgeHelper.edge, edgeHelper.exist(), vertex1, vertex2);
        assertFalse(graph.addEdge(edgeHelper.edge, vertex1, vertex2));
        verifyConnections(edgeHelper.edge, edgeHelper.exist(), vertex1, vertex2);

        assertTrue(graph.isVertex(vertex1));
        assertTrue(graph.isVertex(vertex2));
        edgeHelper.assertExist();

        assertEquals(vertexCount, graph.getVertexCount());
        assertEquals(edgeCount, graph.getEdgeCount());
        return this;
    }

    static <V, E> void verifyConnections(@NotNull Graph<V, E> graph, @NotNull E edge, @NotNull V vertex1, @NotNull V vertex2) {
        List<V> incidentVertices = graph.incidentVertices(edge);
        Collection<E> incidentEdges1 = graph.incidentEdges(vertex1);
        Collection<E> incidentEdges2 = graph.incidentEdges(vertex2);

        if (edge.equals(graph.getEdge(vertex1, vertex2))) {
            assertTrue(incidentEdges1.contains(edge));
            if(!graph.isDirectedGraph()) {
                assertTrue(incidentEdges2.contains(edge));
            }
        }
        if (edge.equals(graph.getEdge(vertex2, vertex1))) {
            assertTrue(incidentEdges2.contains(edge));
            if(!graph.isDirectedGraph()) {
                assertTrue(incidentEdges1.contains(edge));
            }
        }

        assertEquals(graph.isConnection(vertex1, vertex2), graph.getEdge(vertex1, vertex2) != null);
        assertEquals(graph.isConnection(vertex2, vertex1), graph.getEdge(vertex2, vertex1) != null);

        if (edge.equals(graph.getEdge(vertex1, vertex2))) {
            assertEquals(2, incidentVertices.size());
            assertTrue(graph.isConnection(vertex1, vertex2));
            if(graph.isDirectedGraph()) {
                assertEquals(vertex1, incidentVertices.get(0));
                assertEquals(vertex2, incidentVertices.get(1));
            } else {
                assertTrue(graph.isConnection(vertex2, vertex1));
                assertTrue((vertex1.equals(incidentVertices.get(0)) && vertex2.equals(incidentVertices.get(1))) ||
                        (vertex1.equals(incidentVertices.get(1)) && vertex2.equals(incidentVertices.get(0))));
            }
        }
    }

    private void verifyConnections(@NotNull E edge, boolean isEdgeexist, @NotNull V vertex1, @NotNull V vertex2) {
        List<V> incidentVertices = graph.incidentVertices(edge);
        assertEquals(isEdgeexist, incidentVertices.size() == 2);
        assertEquals(!isEdgeexist, incidentVertices.size() == 0);
        verifyConnections(graph, edge, vertex1, vertex2);
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
