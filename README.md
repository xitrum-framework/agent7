Agent7 aims to be simple, for use by standalone JVM programs to reload .class
files in development mode. It only reloads .class files in classpath when they
are changed.

"7" in "Agent7" is because Agent7 uses file watch API available in Java 7+.
You need to use Java 7+.

## Usage

Download [agent7.jar](https://github.com/xitrum-framework/agen7/releases/download/v1.0/agent7-1.0.jar).

```
java -javaagent:path/to/agent7.jar [other options] <YourMainClass>
```

## DCEVM

Agent7 can run with normal Java. But it is intended for use together with
[DCEVM](https://github.com/dcevm/dcevm).

You can install DCEVM in 2 ways:
* [Patch](https://github.com/dcevm/dcevm/releases) your existing Java installation.
* Or install [prebuilt](http://dcevm.nentjes.com/) version.

## More info

See [DCEVM - A JRebel free alternative](http://javainformed.blogspot.jp/2014/01/jrebel-free-alternative.html)
for more info.

If you have more complex programs, you can use
[HotswapAgent](https://github.com/HotswapProjects/HotswapAgent) or
[spring-loaded](https://github.com/spring-projects/spring-loaded).

Also see [AgentSmith](https://github.com/ffissore/agentsmith).
