## Apache Flink
We use `1.4.0` version

#### Run Scala code with sbt
Run the code out of the box by `sbt "flink/run"`
If you want to pass in parameters you can just add them in: `sbt "flink/run --port 9000"`
Here is the output for a simple word count program.

```
$ sbt "flink/run"
[info] Loading settings from idea.sbt ...
[info] Loading global plugins from /Users/intelliuser/.sbt/1.0/plugins
[info] Loading settings from plugins.sbt ...
[info] Loading project definition from /Users/intelliuser/Code/intellithing/rules_engine/project
[info] Loading settings from build.sbt ...
[info] Set current project to rules_engine (in build file:/Users/intelliuser/Code/intellithing/rules_engine/)
[info] Packaging /Users/intelliuser/Code/intellithing/rules_engine/flink/target/scala-2.11/rules-engine_2.11-1.0.0.jar ...
[info] Done packaging.
[info] Running com.intellithing.FlinkRulesEngine
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Connected to JobManager at Actor[akka://flink/user/jobmanager_1#-1577037198] with leader session id 82286a04-d861-4045-b62f-521a2b9c5fd3.
02/13/2018 15:55:29	Job execution switched to status RUNNING.
02/13/2018 15:55:29	Source: Collection Source(1/1) switched to SCHEDULED
02/13/2018 15:55:29	Flat Map -> Map(1/8) switched to SCHEDULED
02/13/2018 15:55:29	Flat Map -> Map(2/8) switched to SCHEDULED
02/13/2018 15:55:29	Flat Map -> Map(3/8) switched to SCHEDULED
...
WordWithCount(place,1)
WordWithCount(hello,1)
WordWithCount(hello,2)
WordWithCount(world,1)
WordWithCount(intellithing,1)
WordWithCount(a,1)
WordWithCount(great,1)
WordWithCount(to,1)
WordWithCount(work,1)
WordWithCount(intellithing,2)
WordWithCount(is,1)
02/13/2018 15:55:29	aggregation(2/8) switched to FINISHED
...
[success] Total time: 3 s, completed Feb 13, 2018 3:55:29 PM
```

#### Run flink with sbt
Use `sbt "flink/run"`

### Reference
- [Flink quickstart](https://ci.apache.org/projects/flink/flink-docs-release-1.4/quickstart/setup_quickstart.html)
- [Examples](https://github.com/apache/flink/tree/master/flink-examples/flink-examples-streaming/src/main/scala/org/apache/flink/streaming/scala/examples)
