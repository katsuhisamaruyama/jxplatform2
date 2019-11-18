import java.util.HashMap;

class Test129 {
    private S s1 = new S();
    private S s2 = new S();

    public void m() {
        int a = 0;
        s1.getP().set1("A", "AAAA");
        s2.getP().set2("B", "BBBB");
        int b = a + 1;
        String v1 = s1.getP().get1("A");
        String v2 = s2.getP().get2("B");

        T t = new T();
        t.set1("C", "CCCC");
        String v3 = t.get1("C");

        U u = new U();
        u.set1("D", "DDDD");
        String v4 = u.get1("D");
    }
}

class S {
    private P p = new P();

    public P getP() {
        return p;
    }
}

class T {
    private P p = new P();
    
    public void set1(String key, String value) {
        p.set1(key, value);
    }

    public String get1(String key) {
        return p.get1(key);
    }
}

class U {
    private T t = new T();
    
    public void set1(String key, String value) {
        t.set1(key, value);
    }

    public String get1(String key) {
        return t.get1(key);
    }
}

class P {
    private HashMap<String, String> map = new HashMap<>();
    private String value;
    private String key;
    
    public void set1(String key, String value) {
        map.put(key, value);
    }

    public String get1(String key) {
        return map.get(key);
    }

    public void set2(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String get2(String key) {
        if (key.equals(this.key)) {
            return value;
        }
        return null;
    }
}
