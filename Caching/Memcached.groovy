@Grab('net.spy:spymemcached:2.10.5')
import net.spy.memcached.MemcachedClient

//1) Base Example from http://funpdi.blogspot.ru/2014/03/groovy-memcached-client.html
def memcachedClient = new MemcachedClient( new InetSocketAddress('127.0.0.1', 11211 ) );

memcachedClient.set('myKey',3600, "Hello world!")
memcachedClient.set('intKey',3600, 45)

println "myKey = ${memcachedClient.get('myKey')}"
println "intKey = ${memcachedClient.get('intKey')}"

//memcachedClient.set('objectKey',3600, new Expando(one: 1, two: 2, other: 'some'))
//println "objectKey = ${memcachedClient.get('objectKey')}"

//2) Extended examples from http://blogs.bytecode.com.au/glen/2009/08/18/a-first-taste-of-memcache.html

// "add" only puts it in the store if the key doesn't exist
memcachedClient.add("sample", 10, "test");

// "set" forces the value in regardless of the store
memcachedClient.set("mapper", 20, [ a: 10, b: 20 ])

// Retrieve a simple value (synchronously).
println memcachedClient.get("sample");

// But complex objects work great too
memcachedClient.get("mapper").each { println it }; 

// You can delete stuff too
memcachedClient.delete("mapper")

// And get some stats off the cache server
println memcachedClient.getStats()

//3) Make it more Groovy-way

MemcachedClient.metaClass.getOrCreate << {String key, Integer secs, Closure create->
    def res = delegate.get(key);
    if (!res){
        res = create();
        delegate.set(key, secs, res);
    }
    
    return res;
}

MemcachedClient.metaClass.getOrCreate << {String key, Closure create->
    delegate.getOrCreate(key, 3600, create)
}

def mem = new MemcachedClient( new InetSocketAddress('127.0.0.1', 11211 ) );

mem.getOrCreate('test'){ new Date() }
// Test recreate
(0..7).each{
    println mem.getOrCreate('expire', 6){ new Date() }
    sleep 1000
}
