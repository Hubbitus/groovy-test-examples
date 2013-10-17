class T {
    def id;
    def name;
    def field;
    
    public T(id, name, field){
        this.id = id;
        this.name = name;
        this.field = field;
    }
    
    public String toString(){
        "{id:$id,name:$name,field:$field}"
    }
}

List l = [ new T(1, 'n1', 'f1'), new T(2, 'n2', 'f2'), new T(3, 'n3', 'f3'), new T(1, 'n2', 'f1'), new T(7, 'n7', 'f7'), new T(1, 'n1', 'f2'), new T(2, 'n2', 'f22'), new T(1, 'n1', 'f1') ]

print l.groupBy{ it.id }.findAll{ it.value.size() > 1 }[1].sort()
//print l.groupBy{ it.id }.findAll{ 1 == it.value.size() }*.value.flatten()

class DtoDup {
    public String id;
    public String idFieldName = 'id';
    public List dupes = [];

    public static String mdObjSafeDescription(/*IMasterDataObj*/ obj, String prefixName = ''){
        if (null == obj){
            return "[$prefixName:null]";
        }
        if (obj.class.simpleName.contains('closure') && obj.delegate){
            obj = obj.delegate;
        }
        return "[$prefixName(${obj.class.simpleName}):" + [
                "${try{ "{id:${obj.id}}" }catch(Exception ignored){ null }}"
                ,"${try{ "{oid:${obj.origId}}" }catch(Exception ignored){ null }}"
                ,"${try{ "{ocs:${obj.ownerCodeSystem}}" }catch(Exception ignored){ null }}"
                ,"${try{ "{name:${obj.nickname}}" }catch(Exception ignored){ null }}"
            ].findAll{ 'null' != it }.join(',') + ']';
    }

    public Map<String,List> fieldsDiff(){
        dupes[0].properties.collectEntries{
            List uvals = dupes*."${it.key}".unique();
            [ (it.key): uvals.size() > 1 ? uvals : null ]
        }.findAll{ it.value }
    }

    public String toString(){
        "Dupes in ${mdObjSafeDescription(dupes[0])}. Different in values are: " + fieldsDiff();
    }
}

//List dupes =
def m = l.groupBy{ it.id }.findAll{ it.value.size() > 1 }.find{true}

DtoDup dd = new DtoDup(id: m.key, dupes: m.value);

//List res = 
//dupes[0].properties.collectEntries{  }

class A{
    public static some(){ 'A' }
}

class B extends A{
    @Override
    public static String some(){ 'B' }
}

B.some()