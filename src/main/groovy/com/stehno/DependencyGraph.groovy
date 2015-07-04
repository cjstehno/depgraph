package com.stehno

import groovy.transform.Canonical

/**
 * Collects dependency mappings and renders an ascii summary of them.
 */
class DependencyGraph {

    private final DependencyNode rootNode = new DependencyNode('<+>')

    static void main(args) {
        def graph = new DependencyGraph()
        graph.populate(new File(args[0]))

        new BufferedWriter(new PrintWriter(System.out)).withWriter { w ->
            graph.render(w)
        }
    }

    /**
     * Populates the dependency graph using the data from the specified file or multi-line String. Each line should represent a
     * single dependency with the form of "A->B" meaning "A depends on B".
     *
     * Empty lines and lines starting with a pound sign (#) will be ignored.
     *
     * Calling this method will clear out any existing graph data and replace it with the incoming data set.
     *
     * @param content the String or File containing the lines of dependency mappings
     */
    void populate(content) {
        rootNode.children.clear()

        def dependencyNodes = [:]

        content.readLines().findAll { it?.trim() && !it.trim().startsWith('#') }.each { String line ->
            def (a, b) = line.trim().split('->')

            DependencyNode nodeA = findOrCreateNode(dependencyNodes, a)
            DependencyNode nodeB = findOrCreateNode(dependencyNodes, b)

            // the "b" node depends on somebody so it's not a top-level node
            nodeB.root = false

            nodeA << nodeB
        }

        dependencyNodes.findAll { name, node -> node.root }.each { name, node ->
            // since we are adding it to the root, this node is no longer a root node
            node.root = false

            rootNode << node
        }
    }

    /**
     * Renders the dependency graph as an ascii tree.
     */
    void render(BufferedWriter writer) {
        rootNode.writeNode(writer)

        writer.write '-------------------------------------------'
        writer.newLine()
        writer.write '(* represents cyclic dependency truncation)'
        writer.newLine()
    }

    private static DependencyNode findOrCreateNode(Map<String, DependencyNode> nodes, String name) {
        nodes[name] ?: addNode(nodes, name)
    }

    private static DependencyNode addNode(Map<String, DependencyNode> nodes, String name) {
        def node = new DependencyNode(name)
        nodes[name] = node
        node
    }
}

/**
 * Representation of a dependency in the tree, with references to it's dependent children.
 * This class is not intended for use outside of the DependencyGraph scope.
 */
@Canonical
class DependencyNode {

    /**
     * The printable node name or label.
     */
    String name

    /**
     * Whether or not the node is a root node. Defaults to true since when nodes are created they are considered top-level until they are found to
     * be dependent on another node.
     */
    boolean root = true

    final List<DependencyNode> children = []

    /**
     * Adds the child node as a dependency of this node.
     *
     * @param child the dependent child node
     */
    void leftShift(DependencyNode child) {
        children << child
    }

    /**
     * Writes the contents of the node to the provided writer in ascii tree format.
     * Generally, this will be called on a root node.
     *
     * @param writer the writer to which the rendered content will be written
     */
    void writeNode(BufferedWriter writer) {
        output(writer, [], 0, '', true)
    }

    private void output(BufferedWriter writer, List<String> visited, int depth, String prefix, boolean leaf) {
        writer.write "${root ? '' : "$prefix|_ "}$name"

        if (cyclicDependencyCheck(visited, depth)) {
            writer.write ' (*)'
            writer.newLine()
            return
        }

        writer.newLine()

        children.eachWithIndex { DependencyNode child, int i ->
            String nextPrefix = root ? '' : (prefix + (leaf ? "   " : "|  "))
            child.output(writer, visited, depth + 1, nextPrefix, i == children.size() - 1)
        }
    }

    private boolean cyclicDependencyCheck(List<String> visited, int depth) {
        if (visited.size() > depth) {
            def truncated = new LinkedList(visited.subList(0, depth))
            visited.clear()
            visited.addAll(truncated)
        }

        boolean cycle = visited.contains(name)

        if (!cycle) {
            visited.add(name)
        }

        return cycle
    }
}
