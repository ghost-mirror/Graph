package com.gohostmirror.util.graph;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class GraphTest {
    @Test
    public void pathDirectedGraphTest() {
        Graph<Integer, Object> graph = Graphs.directedConcurrentGraph();
        List<Integer> path;
        assemblyGraph(graph);
/*
                  (3)---------------*(4)-------*(5)
                 * * \                *           \
               /   |   \              |             \
             /     |     \            |               \
           /       |       *          |                 *
        (1)------*(2)      (6)       (7)------*(8)-----*(9)--
          \        *       /          * *       |       / *  \
            \      |     /            |  \      |     /    \_/
              \    |   /              |    \    |   /
                *  | *                *      \  * *
                 (10)--------------*(11)*-----(12)
*/
        assertEquals(0, graph.getPath(1, 1).size());
        assertEquals(0, graph.getPath(4, 3).size());

        verifyPath(graph.getPath(1, 2), 1, 2);
        verifyPath(graph.getPath(1, 3), 1, 3);
        verifyPath(graph.getPath(1, 4), 1, 3, 4);
        verifyPath(graph.getPath(1, 5), 1, 3, 4, 5);
        verifyPath(graph.getPath(1, 6), 1, 3, 6);
        verifyPath(graph.getPath(1, 7), 1, 10, 11, 7);
        verifyPath(graph.getPath(1, 9), 1, 3, 4, 5, 9);
        verifyPath(graph.getPath(3, 8), 3, 6, 10, 11, 7, 8);

        verifyPath(graph.getPath(7, 11), 7, 11);
        verifyPath(graph.getPath(11, 7), 11, 7);

        verifyPath(graph.getPath(7, 7), 7, 11, 7);
        verifyPath(graph.getPath(8, 8), 8, 12, 7, 8);
        verifyPath(graph.getPath(10, 10), 10, 2, 3, 6, 10);
        verifyPath(graph.getPath(9, 9), 9, 9);
    }

    @Test
    public void pathUndirectedGraphTest() {
        Graph<Integer, Object> graph = Graphs.undirectedConcurrentGraph();
        List<Integer> path;
        assemblyGraph(graph);
/*
                  (3)----------------(4)--------(5)
                 / | \                |           \
               /   |   \              |             \
             /     |     \            |               \
           /       |       \          |                 \
        (1)-------(2)      (6)       (7)-------(8)------(9)--
          \        |       /          | \       |       / \  \
            \      |     /            |  \      |     /    \_/
              \    |   /              |    \    |   /
                \  | /                |      \  | /
                 (10)---------------(11)------(12)
*/
        assertEquals(3, graph.getPath(1, 1).size());
        verifyPath(graph.getPath(4, 3), 4, 3);

        verifyPath(graph.getPath(1, 2), 1, 2);
        verifyPath(graph.getPath(1, 3), 1, 3);
        verifyPath(graph.getPath(1, 4), 1, 3, 4);
        verifyPath(graph.getPath(1, 5), 1, 3, 4, 5);
        assertEquals(3, graph.getPath(1, 6).size());
        assertEquals(4, graph.getPath(1, 7).size());
        assertEquals(5, graph.getPath(1, 9).size());
        verifyPath(graph.getPath(3, 8), 3, 4, 7, 8);

        verifyPath(graph.getPath(7, 11), 7, 11);
        verifyPath(graph.getPath(11, 7), 11, 7);

        assertEquals(3, graph.getPath(7, 7).size());
        assertEquals(3, graph.getPath(8, 8).size());
        assertEquals(3, graph.getPath(10, 10).size());
        verifyPath(graph.getPath(9, 9), 9, 9);
    }

    @Test
    public void addVertexTest() {
        addVertexTest(new GhraphHelper<>(false));
        addVertexTest(new GhraphHelper<>(true));
    }

    @Test
    public void addEdgeTest() {
        addEdgeTest(new GhraphHelper<>(false));
        addEdgeTest(new GhraphHelper<>(true));
    }

    @Test
    public void concurrentGraphTest() {
        concurrentGraphTest(Graphs.directedConcurrentGraph());
        concurrentGraphTest(Graphs.undirectedConcurrentGraph());
    }

    private void concurrentGraphTest(Graph<Integer, Integer> graph) {
        final int THREAD_COUNT = 5;
        final int EDGE_COUNT = 500;
        final int VERTEX_BOUND = 50;
        final int GET_PATH_FACTOR = 3;
        CountDownLatch startLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch finishLatch = new CountDownLatch(THREAD_COUNT + 1);

        Runnable task = new Runnable() {
            private final Random rnd = ThreadLocalRandom.current();

            @Override
            public void run() {
                startLatch.countDown();
                try {
                    startLatch.await();
                } catch (InterruptedException ignore) {}
                for (int edge = 0; edge < EDGE_COUNT; edge++) {
                    graph.addEdge(rnd.nextInt(EDGE_COUNT), rnd.nextInt(VERTEX_BOUND), rnd.nextInt(VERTEX_BOUND));
                    if (rnd.nextInt(GET_PATH_FACTOR) == GET_PATH_FACTOR-1) {
                        List<Integer> path = graph.getPath(rnd.nextInt(VERTEX_BOUND), rnd.nextInt(VERTEX_BOUND));
                        if (path.size() >= 2) {
                            graph.addEdge(rnd.nextInt(EDGE_COUNT), path.get(path.size() - 1), path.get(0));
                        }
                    }
                }
                finishLatch.countDown();
                try {
                    finishLatch.await();
                } catch (InterruptedException ignore) {}
            }
        };

        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(task).start();
        }
        finishLatch.countDown();
        try {
            finishLatch.await();
        } catch (InterruptedException ignore) {}

        verifyGraphConnections(graph);
    }

    private void assemblyGraph(Graph<Integer, Object> graph) {
/*
                  (3)---------------*(4)-------*(5)
                 * * \                *           \
               /   |   \              |             \
             /     |     \            |               \
           /       |       *          |                 *
        (1)------*(2)      (6)       (7)------*(8)-----*(9)--
          \        *       /          * *       |       / *  \
            \      |     /            |  \      |     /    \_/
              \    |   /              |    \    |   /
                *  | *                *      \  * *
                 (10)--------------*(11)*-----(12)
*/
        assertTrue(graph.addEdge(new Object(), 1, 2));
        assertTrue(graph.addEdge(new Object(), 1, 3));
        assertTrue(graph.addEdge(new Object(), 1, 10));
        assertTrue(graph.addEdge(new Object(), 2, 3));
        assertTrue(graph.addEdge(new Object(), 3, 4));
        assertTrue(graph.addEdge(new Object(), 3, 6));
        assertTrue(graph.addEdge(new Object(), 4, 5));
        assertTrue(graph.addEdge(new Object(), 5, 9));
        assertTrue(graph.addEdge(new Object(), 6, 10));
        assertTrue(graph.addEdge(new Object(), 7, 4));
        assertTrue(graph.addEdge(new Object(), 7, 8));
        assertTrue(graph.addEdge(new Object(), 7, 11));
        assertTrue(graph.addEdge(new Object(), 8, 9));
        assertTrue(graph.addEdge(new Object(), 8, 12));
        assertTrue(graph.addEdge(new Object(), 9, 9));
        assertTrue(graph.addEdge(new Object(), 9, 12));
        assertTrue(graph.addEdge(new Object(), 10, 2));
        assertTrue(graph.addEdge(new Object(), 10, 11));
        if (graph.isDirectedGraph()) {
            assertTrue(graph.addEdge(new Object(), 11, 7));
        } else {
            assertFalse(graph.addEdge(new Object(), 11, 7));
        }
        assertTrue(graph.addEdge(new Object(), 12, 7));
        assertTrue(graph.addEdge(new Object(), 12, 11));

        verifyGraphConnections(graph);
    }

    private void addVertexTest(@NotNull GhraphHelper<Integer, ?> helper) {
        helper.isEmpty()
                .addOrigVertex(1)
                .addOrigVertex(2)
                .addOrigVertex(3)
                .addOrigVertex(10)
                .addOrigVertex(42)
                .addOrigVertex(-33)
                .addExistVertex(1)
                .addExistVertex(2)
                .addExistVertex(3)
                .addExistVertex(10)
                .addExistVertex(42)
                .addExistVertex(-33)
                .vertexCount(6)
                .edgeCount(0);

        helper.addOrigVertex(55)
                .addOrigVertex(-333)
                .vertexCount(8)
                .edgeCount(0);

        verifyGraphConnections(helper.graph);
    }

    private void addEdgeTest(@NotNull GhraphHelper<String, Double> helper) {
        helper.isEmpty()
                .addEdge(helper.origE(11d), helper.origV("1"), helper.origV("2"))
                .addEdge(helper.origE(22d), helper.origV("3"), helper.origV("4"))
                .addOrigVertex("5")
                .addExistVertex("4")
                .addEdge(helper.existE(11d), helper.existV("1"), helper.existV("3"))
                .addEdge(helper.origE(33d), helper.existV("1"), helper.existV("4"))
                .addEdge(helper.origE(44d), helper.origV("6"), helper.existV("4"))
                .addEdge(helper.origE(55d), helper.existV("5"), helper.origV("7"))
                .addEdge(helper.existE(11d), "1", "2")
                .addEdge(helper.origE(222d), "3", "4")
                .vertexCount(7)
                .edgeCount(5);

        verifyGraphConnections(helper.graph);
    }

    private static <V, E> void verifyGraphConnections(@NotNull Graph<V, E> graph) {
        Collection<V> vertices = graph.getVertices();
        for (E edge : graph.getEdges()) {
            for (V vertex1 : vertices) {
                for (V vertex2 : vertices) {
                    GhraphHelper.verifyConnections(graph, edge, vertex1, vertex2);
                }
            }
        }
    }

    @SafeVarargs
    private static <V> void verifyPath(@NotNull List<V> path, @NotNull V ...steps) {
        assertEquals(steps.length, path.size());

        for (int i = 0; i<steps.length; i++) {
            assertEquals(path.get(i), steps[i]);
        }
    }
}
