// Play with Traits chaining from http://groovy-lang.org/objectorientation.html#_chaining_behavior

interface MessageHandler {
    void on(String message, Map payload)
}

trait DefaultHandler implements MessageHandler {
    void on(String message, Map payload) {
        println "Received [$message] with payload $payload"
    }
}

trait LoggingHandler implements MessageHandler {
    void on(String message, Map payload) {
        println "Seeing [$message] with payload $payload"
        super.on(message, payload)
    }
}

trait LoggingHandlerSpaceReplacer implements MessageHandler {
    void on(String message, Map payload) {
        println "Space replaced message [${message.replaceAll(/ /, '_')}] with payload $payload"
        super.on(message, payload)
    }
}

//class HandlerWithLogger implements DefaultHandler, LoggingHandler, LoggingHandlerSpaceReplacer {}
//def loggingHandler = new HandlerWithLogger()
//loggingHandler.on('test logging message', [:])

// Runtime (from config?) http://groovy-lang.org/objectorientation.html#_runtime_implementation_of_traits
class HandlerWithLogger {}
def loggingHandler = new HandlerWithLogger()
//def handler = loggingHandler.withTraits DefaultHandler, LoggingHandler, LoggingHandlerSpaceReplacer
def handler = loggingHandler.withTraits Class.forName('DefaultHandler'), Class.forName('LoggingHandler'), Class.forName('LoggingHandlerSpaceReplacer')
handler.on('test logging message', [:])

//Class.forName('LoggingHandlerSpaceReplacer')