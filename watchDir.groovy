// https://gist.github.com/plecong/4343176

import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds

def watchDir = new File('/home/pasha/temp/test.dir');

def watchService = FileSystems.getDefault().newWatchService();
watchDir.toPath().register(watchService,
    StandardWatchEventKinds.ENTRY_MODIFY,
    StandardWatchEventKinds.ENTRY_CREATE)
 
println "Now watching for files at: ${watchDir.canonicalPath}"

def i = 0;
while (true && i++ <= 100) {
    def key = watchService.take()
 println "<$i>" + new Date()
    if (key) {
        key.pollEvents().each{
            println new File(it.context().toString())
        }
        key.reset()
    }
}