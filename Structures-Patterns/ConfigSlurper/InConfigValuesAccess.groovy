// Deep demonstrate ConfigObject inner path defines access
// Base example from: http://stackoverflow.com/a/26564451/307525

String configText = '''
res = [column1: 1, column2: 2, column3: 3]
file{
    rows="${->res.column1}|${->res.column2}|${-> res.column3}"
    rowsClosure={
        println delegate
        """
        ${res.column1}|${res.column2}|${res.column3}
        ${->res.column1}|${->res.column2}|${->res.column3}
        """
    }

    infile = 'it is in file { } block'
    getInfile_AsLocalBinding = { infile }
    getInfile_ByFullConfigPath = { file.infile }
}
'''
def slurper = new ConfigSlurper()
def conf = slurper.parse(configText)

println "conf.file.rows=${conf.file.rows}"
println "conf.file.rowsClosure=${conf.file.rowsClosure}"
println "conf.file.rowsClosure()=${conf.file.rowsClosure()}"
conf.res = [column1: 4, column2: 5, column3: 6]
println "conf.file.rows=${conf.file.rows}"
println "conf.file.rowsClosure()=${conf.file.rowsClosure()}"

println "conf.file.getInfile_AsLocalBinding()=${conf.file.getInfile_AsLocalBinding()}"
println "conf.file.getInfile_AsLocalBindinggetInfile_ByFullConfigPath()=${conf.file.getInfile_ByFullConfigPath()}"

// Main checks:
assert 'it is in file { } block' == conf.file.getInfile_ByFullConfigPath()
// No local 'var' infile accessible from closure:
assert new ConfigObject() == conf.file.getInfile_AsLocalBinding()
assert '[:]' == conf.file.getInfile_AsLocalBinding().toString()
// provide binding at desired level as 'local' (full path acess stile also continue to work)!!!:
conf.file.getInfile_AsLocalBinding.binding = new Binding(conf.file);
assert 'it is in file { } block' == conf.file.getInfile_ByFullConfigPath()
// No local 'var' infile accessible from closure:
assert 'it is in file { } block' == conf.file.getInfile_AsLocalBinding()


// Try change base for closure values, result also must change because of Late binding:
conf.file.infile = 'changed infile value'
assert 'changed infile value' == conf.file.getInfile_ByFullConfigPath()
// No local 'var' infile accessible from closure:
assert 'changed infile value' == conf.file.getInfile_AsLocalBinding()