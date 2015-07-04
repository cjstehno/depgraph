# Dependency Graph Exercise

## Instructions

Problem: Given a dependency graph, print a hierarchical view of the dependencies.

Data: The graph is in the attached graph.txt file. The data is provided in the form "X->Y", stating that X depends on Y.

Assignment: Walk the dependency graph, starting at A, printing out each dependency. Repeat for each dependency visited, indenting along the way. E.g.
If A depends on B and C, and B depends on C and D the output should look like this:

    A
    |_ B
    |  |_ C
    |  \_ D
    |_ C

Implement in any JVM-based language of your choice.

## Usage

You can run the application from Gradle:

    ./gradlew -Pfile=<yourfile> run

A summary of the dependency tree will be generated to System.out similar to the following:

    <+>
    |_ A
       |_ B
       |  |_ C
       |     |_ B (*)
       |_ D
    -------------------------------------------
    (* represents cyclic dependency truncation)

> Note that the output format was changed slightly to accommodate the possibility of multiple top-level dependencies.
