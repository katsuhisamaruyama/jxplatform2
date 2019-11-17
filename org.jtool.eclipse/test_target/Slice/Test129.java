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
    }
}

class S {
    private P p = new P();

    public P getP() {
        return p;
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
