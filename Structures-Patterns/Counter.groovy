class Counter{
private Map<String, ArrayList> map = [:];
//private Map map = [one: ['Some one', 1], two: [2], three: 3, four: [4, 'Forth counter'], five: ['Fifth parameter starts from 5': 5]];

    public Counter(){
    }

    public Counter(Map m){
        //normalize
        m.each{
            switch(it.value){
                case Map:
                    assert 1 == it.value.size();
                    map[it.key] = [ it.value.find{true}.value, it.value.find{true}.key];
                break;

                case List:
                    assert it.value.size() in [1, 2];
                    if (1 == it.value.size()){
                        this.map[it.key] = [ it.value[0] ]
                    }
                    else{
                        if (it.value[0] instanceof Number){
                            this.map[it.key] = [it.value[0], it.value[1]];
                        }
                        else{
                            this.map[it.key] = [it.value[1], it.value[0]];
                        }
                    }
                break;

                case Number:
                    this.map[it.key] = [ it.value ];
                break;

                case String:
                    this.map[it.key] = [ 0, it.value ];
                break;

                default:
                throw new Exception("Counter can't parse parameters");
                break;
            }
        }
    }

    public propertyMissing(String key){
        if (! map[key]) map[key] = [0];
        return map[key][0];
    }

    public void setProperty(String key, value){
        if (! map[key]) map[key] = [0];
        map[key][0] = value;
    }

    public String toString(){
        map.collect{
            "${it.value[1] ?: it.key}: ${it.value[0]};"
        }.join('\n')
    }

    public String inline(){
        map.collect{
            "${it.value[1] ?: it.key} - ${it.value[0]}"
        }.join(', ')
    }
}

//Counter c = new Counter([one: ['Someone': 1]]);
//Counter c = new Counter([two: [2]]);
//Counter c = new Counter([four: [4, 'Forth counter']]);
//Counter c = new Counter([four: ['Forth counter', 4]]);
//Counter c = new Counter([three: 3]);
//Counter c = new Counter([one: ['Some one', 1], two: [2], three: 3, four: [4, 'Forth counter'], five: ['Fifth parameter starts from 5': 5]]);
Counter c = new Counter([five: 'Fifth counter']);
//c.dump();
c.one++
c.one

//c.six++;

//c."some seven initiated counter" += 7;
//c."some seven initiated counter" = 7;
++c."some seven initiated counter";

println c;
//println c.inline();