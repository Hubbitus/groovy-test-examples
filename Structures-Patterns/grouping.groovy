class T{
    public String severity;
    public String code;
    public boolean dvis;

    public T(String severity, String code, boolean dvis){
    this.severity = severity;
    this.code = code;
    this.dvis = dvis;
    }

    public String toString(){
    return "T<severity: $severity, code: $code, dvis: $dvis>"
    }
}

def l = [ new T('warning', '0', false), new T('fatal', '0', false), new T('fatal', '1', false), new T('fatal', '1', true), new T('warning', '0', false), new T('warning', '1', false), new T('warning', '2', false), new T('warning', '2', false), new T('warning', '3', false)]

//[  l, '===========================', l.countBy{ [it.severity, it.code, it.dvis ] }, '===========================', l.groupBy{ [it.severity, it.dvis ] }  ]

//l.groupBy{ [it.severity, it.code ] }

list = [[2, 0, 1], [1, 5, 2], [1, 0, 3]]

list = list.sort{ a,b -> a[2] <=> b[2] }
list = list.sort{ a,b -> a[1] <=> b[1] }
list = list.sort{ a,b -> a[0] <=> b[0] }

//list.sort{ a,b -> (a[0] <=> b[0]) || (a[1] <=> b[1]) || (a[2] <=> b[2]) }
list.sort{ a,b -> ((a[0] <=> b[0]) ?: (a[1] <=> b[1])) ?: (a[2] <=> b[2]) }

l.groupBy{ [it.severity, it.code ] }
//l.groupBy{ [it.severity, it.code ] }.sort{a,b-> println a.key.dump(); 0 }
l.groupBy{ [it.severity, it.code ] }.sort{a,b-> ( (a.key[0] <=> b.key[0]) ?: (a.key[1] <=> b.key[1]) ) }