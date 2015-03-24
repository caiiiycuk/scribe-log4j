# Running example application #

  1. Open console and clone project:
```
$ hg clone https://scribe-log4j.googlecode.com/hg/ scribe-log4j
```
  1. Install scribe-client to local maven repository:
```
$ cd scribe-log4j/scribe-client
$ mvn install
```
  1. Install scribe-log4j to local maven repository:
```
$ cd scribe-log4j/scribe-log4j
$ mvn install
```
  1. Run your scribe server that`s agregate logs. (See short instruction: ConfigureScribe).
  1. Fix log4j.properties in "scribe-log4j/scribe-example/src/main/resources/log4j.properties" if neaded. Default configuration is:
```
log4j.rootLogger=DEBUG, console, scribe

log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.layout=org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=%d{yy/MM/dd HH:mm:ss} %p %c{2}: %m%n

log4j.appender.scribe=name.caiiiycuk.scribe.ScribeAppender
log4j.appender.scribe.hostname=my-app.ru
log4j.appender.scribe.scribeHost=127.0.0.1
log4j.appender.scribe.scribePort=1463
log4j.appender.scribe.scribeCategory=my-app
log4j.appender.scribe.printExceptionStack=true
log4j.appender.scribe.addStackTraceToMessage=true
log4j.appender.scribe.timeToWaitBeforeRetry=6000
log4j.appender.scribe.sizeOfInMemoryStoreForward=100
log4j.appender.scribe.layout=org.apache.log4j.PatternLayout
log4j.appender.scribe.layout.ConversionPattern=%d{yy/MM/dd HH:mm:ss} %p %c{2}: %m%n
```
  1. Now if you assembly and run scribe-example, you see logs of this application on your scribe server.
```
$ cd scribe-log4j/scribe-example
$ mvn package
$ cd target
$ java -jar scribe-example-1.0-jar-with-dependencies.jar
```

## log4j.properties ##

| Property | Descriprion |
|:---------|:------------|
| `log4j.appender.scribe=name.caiiiycuk.scribe.AsyncScribeAppender` | Appender that sends logs to server `[` `ScribeAppender`, `AsyncScribeAppender` `]` |
| `log4j.appender.scribe.hostname` | Application host name will be appended to log line |
| `log4j.appender.scribe.scribeHost` | Sribe server host |
| `log4j.appender.scribe.scribePort` | Scribe server port |
| `log4j.appender.scribe.scribeCategory` | Category of this application used by scribe to categorize |
| `log4j.appender.scribe.printExceptionStack` | If exception occurs when sending log or connection to server it will be printed to sys log if this property is true |
| `log4j.appender.scribe.addStackTraceToMessage` | Also adds the stack trace to the message |
| `log4j.appender.scribe.timeToWaitBeforeRetry` | When connection is not present or when it goes away. the "timeToWaitBeforeRetry" property is used to determine how long after connection failure to retry again. |
| `log4j.appender.scribe.localStoreForwardClassName` | Classname of local store forward provider |
| `log4j.appender.scribe.sizeOfInMemoryStoreForward` | If the "sizeOfInMemoryStoreForward" property is present, then it will use a default in memroy implementation of local storage forward provider. As always, you can supply own by implementing same interface and giving the following config 

&lt;param name="localStoreForwardClassName" value="my.domain.scribe.ILocalStoreForwardImpl" /&gt;

 |