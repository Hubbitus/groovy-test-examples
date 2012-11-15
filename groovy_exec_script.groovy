//Process proc = ['sh', '-c', 'ls ; date'].execute(); // Call *execute* on the string
//Process proc = 'sh -c "ls ; date"'.execute(); // Call *execute* on the string
//Process proc = ['sh', '-c "ls ; date"'].execute(); // Call *execute* on the string
//Process proc = ['sh', 'ls', 'date'].execute(); // Call *execute* on the string

//Process proc = '/home/pasha/imus/IdeaProjects/imus/ascug-maket/ascug-esb-modules/data/belrgmain.ais/md/exec.cmd'.execute()

// http://www.intelligrape.com/blog/2011/02/15/using-groovy-execute-bash-scripts/
File tmp = File.createTempFile('Temp', '.cmd');
tmp << new File('/home/pasha/imus/IdeaProjects/imus/ascug-maket/ascug-esb-modules/data/belrgmain.ais/md/exec.cmd').text;
tmp.setExecutable(true);

Process proc = tmp.path.execute()
proc.waitFor(); // Wait for the command to finish

tmp.delete();

println "return code: ${proc.exitValue()}";
println "Put: ${proc.in.text}";
println "Err: ${proc.err.text}";