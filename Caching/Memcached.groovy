@Grab('net.spy:spymemcached:2.10.5')
import net.spy.memcached.MemcachedClient

// Example from http://funpdi.blogspot.ru/2014/03/groovy-memcached-client.html
def memcachedClient = new MemcachedClient( new InetSocketAddress('127.0.0.1', 11211 ) );

//memcachedClient.set('myKey',3600, "Hello world!")
//memcachedClient.set('intKey',3600, 45)

println "myKey = ${memcachedClient.get('myKey')}"
println "intKey = ${memcachedClient.get('intKey')}"

//memcachedClient.set('objectKey',3600, new Expando(one: 1, two: 2, other: 'some'))
//println "objectKey = ${memcachedClient.get('objectKey')}"
