Agent7 aims to be simple, for use by standalone JVM programs to reload .class
files in development mode. It only reloads .class files in classpath when they
are changed.

"7" in "Agent7" is because Agent7 uses file watch API available in Java 7+.
You need to use Java 7+.

## Usage

Download [agent7.jar](https://github.com/xitrum-framework/agent7/releases/download/v1.0/agent7-1.0.jar).

```
java -javaagent:path/to/agent7.jar [other options] <YourMainClass>
```

Whenever a .class file in the current working directory (or its subdirectories)
changes, Agent7 will reload the corresponding class in all class loaders.

## DCEVM

Agent7 can run with normal Java. But it is intended for use together with
[DCEVM](https://github.com/dcevm/dcevm).

> The Dynamic Code Evolution Virtual Machine (DCE VM) is a modification of the Java HotSpot(TM) VM that allows unlimited redefinition of loaded classes at runtime. The current hotswapping mechanism of the HotSpot(TM) VM allows only changing method bodies. Our enhanced VM allows adding and removing fields and methods as well as changes to the super types of a class.

Although DCEVM supports advanced class changes, it itself doesn't reload classes.
You need a javaagent like Agent7.

You can install DCEVM in 2 ways:
* [Patch](https://github.com/dcevm/dcevm/releases) your existing Java installation.
* Or install [prebuilt](http://dcevm.nentjes.com/) version.

See [DCEVM - A JRebel free alternative](http://javainformed.blogspot.jp/2014/01/jrebel-free-alternative.html)
for more info.

## Alternatives

If you have more complex programs, you can use:
* [JRebel](http://zeroturnaround.com/software/jrebel/)
* [HotswapAgent](https://github.com/HotswapProjects/HotswapAgent)
* [spring-loaded](https://github.com/spring-projects/spring-loaded)

Also see [AgentSmith](https://github.com/ffissore/agentsmith).
