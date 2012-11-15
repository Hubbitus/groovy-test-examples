// http://chrisbroadfoot.id.au/2008/08/06/groovy-threads/

import java.util.concurrent.locks.ReentrantLock

def startTime = System.currentTimeMillis()

ReentrantLock.metaClass.withLock = {
	lock()
	try {
		it()
	}
	finally {
		unlock()
	}
}

def lock = new ReentrantLock()

def worker = { threadNum ->
	4.times { count ->
		lock.withLock {
			print (("_" * threadNum) + "<$threadNum>")
			print "." * (count + 1)
			println " ${System.currentTimeMillis() - startTime}"
		}
		Thread.sleep(100);
	}
}

5.times {
	Thread.start worker.curry(it)
}

println "ROCK!"
