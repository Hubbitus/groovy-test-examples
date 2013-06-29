import groovy.transform.ToString

@ToString
class BenchTestItem{
    String id;
    String name;
    int amountOfRun = 1;
    Closure runPrepare = {}; // Once bootstrap, optional
    Closure runBody;
    Closure out = System.out.&println;
    def data; // Some outer data needed for tests

    def res;

    private Binding bind = new Binding(one: 1);

    public run(){
        runPrepare.delegate = this;
        runPrepare();
        runPrepare.delegate = this;
        runBody();
    }
}

BenchTestItem bt = new BenchTestItem(
    id: '1'
    ,name: 'Some test'
    ,runPrepare: { String str = 'Some string from Prepare' }
    ,runBody: { println "Println str, initialized in runPrepare: $str" }
);
bt.run()