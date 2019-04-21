class Test04 {

    public static void main(String argv[]) {
        int a = 0;
        int out = Test04.doReturn(a);
        System.out.println(out);
    }
    
   static public int doReturn(int in) {
       in++;
       return in;
    }
}
