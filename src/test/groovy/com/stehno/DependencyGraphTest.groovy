package com.stehno

import org.junit.Before
import org.junit.Test

class DependencyGraphTest {

    private DependencyGraph graph

    @Before void before() {
        graph = new DependencyGraph()
    }

    @Test void 'file: simple.txt'() {
        graph.populate fileResource('/simple.txt')

        assertRenderedContent SIMPLE_LINES, renderGraph()
    }

    @Test void 'file: graph.txt'() {
        graph.populate fileResource('/graph.txt')

        assertRenderedContent GRAPH_LINES, renderGraph()
    }

    @Test void 'string: 3-dependencies'() {
        graph.populate('''
            A->B
            B->C
            A->D
        ''')

        assertRenderedContent THREE_DEPS_LINES, renderGraph()
    }

    @Test void 'string: cyclic dependency'() {
        this.graph.populate('''
            A->B
            B->C
            A->D
            C->B
        ''')

        assertRenderedContent CYCLE_LINES, renderGraph()
    }

    @Test void 'string: 3-roots'() {
        graph.populate('''
            A->B
            B->C
            E->F
            A->D
            G->H
            H->I
        ''')

        assertRenderedContent THREE_ROOTS_LINES, renderGraph()
    }

    @Test void 'string: comments & blanks'() {
        graph.populate('''
            A->B
            #X->Z
            B->C

            A->D
        ''')

        assertRenderedContent THREE_DEPS_LINES, renderGraph()
    }

    private File fileResource(String path) {
        new File(getClass().getResource(path).toURI())
    }

    private String renderGraph() {
        def writer = new StringWriter()
        new BufferedWriter(writer).withWriter { w ->
            graph.render(w)
        }
        writer.buffer.toString()
    }

    private static void assertRenderedContent(List<String> expectedLines, String actual) {
        def actualLines = actual.trim().readLines()

        assert expectedLines.size() == actualLines.size()

        expectedLines.eachWithIndex { line, idx ->
            assert actualLines[idx] == line
        }
    }

    // Let's keep the ugliness down here out of sight

    private static final THREE_DEPS_LINES = '''<+>
|_ A
   |_ B
   |  |_ C
   |_ D
-------------------------------------------
(* represents cyclic dependency truncation)'''.trim().readLines()

    private static final CYCLE_LINES = '''<+>
|_ A
   |_ B
   |  |_ C
   |     |_ B (*)
   |_ D
-------------------------------------------
(* represents cyclic dependency truncation)'''.trim().readLines()

    private static final THREE_ROOTS_LINES = '''<+>
|_ A
|  |_ B
|  |  |_ C
|  |_ D
|_ E
|  |_ F
|_ G
   |_ H
      |_ I
-------------------------------------------
(* represents cyclic dependency truncation)'''.trim().readLines()

    private static final SIMPLE_LINES = '''<+>
|_ A
   |_ B
   |  |_ C
   |  |  |_ E
   |  |     |_ H
   |  |     |_ M
   |  |_ D
   |     |_ F
   |     |  |_ H
   |     |_ G
   |     |_ J
   |_ J
-------------------------------------------
(* represents cyclic dependency truncation)'''.trim().readLines()

    private static final GRAPH_LINES = '''<+>
|_ A
   |_ B
   |  |_ C
   |  |  |_ E
   |  |     |_ H
   |  |     |  |_ L
   |  |     |     |_ I
   |  |     |        |_ O
   |  |     |        |  |_ P
   |  |     |        |     |_ Q
   |  |     |        |_ P
   |  |     |        |  |_ Q
   |  |     |        |_ K
   |  |     |           |_ N
   |  |     |           |_ L (*)
   |  |     |_ M
   |  |        |_ N
   |  |        |_ H
   |  |           |_ L
   |  |              |_ I
   |  |                 |_ O
   |  |                 |  |_ P
   |  |                 |     |_ Q
   |  |                 |_ P
   |  |                 |  |_ Q
   |  |                 |_ K
   |  |                    |_ N
   |  |                    |_ L (*)
   |  |_ D
   |     |_ F
   |     |  |_ H
   |     |     |_ L
   |     |        |_ I
   |     |           |_ O
   |     |           |  |_ P
   |     |           |     |_ Q
   |     |           |_ P
   |     |           |  |_ Q
   |     |           |_ K
   |     |              |_ N
   |     |              |_ L (*)
   |     |_ G
   |     |_ J
   |        |_ I
   |        |  |_ O
   |        |  |  |_ P
   |        |  |     |_ Q
   |        |  |_ P
   |        |  |  |_ Q
   |        |  |_ K
   |        |     |_ N
   |        |     |_ L
   |        |        |_ I (*)
   |        |_ Q
   |_ J
      |_ I
      |  |_ O
      |  |  |_ P
      |  |     |_ Q
      |  |_ P
      |  |  |_ Q
      |  |_ K
      |     |_ N
      |     |_ L
      |        |_ I (*)
      |_ Q
-------------------------------------------
(* represents cyclic dependency truncation)'''.trim().readLines()
}
