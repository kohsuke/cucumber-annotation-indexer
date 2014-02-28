Cucumber for Java Annotation Indexer
====================================

[Cucumber for Java](http://cukes.info/install-cucumber-jvm.html) requires that you specify the packages
in which your step definitions exist. At runtime, cucumber uses some hack to try to list all the classes
in this package, loads them one by one, and finds those that have step definition annotations like `@When` and
`@Then`. This is both poor user experience (Can't you just find my step definitions!?) and poor performance.

This library offers a much better alternative. It uses
[annotation indexer](https://github.com/jenkinsci/lib-annotation-indexer) to create an index of
step definitions and hooks at compile time. Thanks to [JSR-269](https://www.jcp.org/en/jsr/detail?id=269),
this happens automatically on Java6 and later.

`BetterJavaBackend` implements this discovery mechanism, which extends from the standard `JavaBackend`.
Cucumber should find this backend automatically.