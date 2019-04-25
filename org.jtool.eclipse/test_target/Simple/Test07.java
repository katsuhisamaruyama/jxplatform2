public class Test07 {

    public void m() {
       int a = 5;
       int b = 3;
       int c = 5;
       int d = 10;
       int e = 11;

       if (c == d) {
           if (a < b) {
               b = a;
               a = a + 1;
           } else if (b < a) {
               a = b;
               b = b+1;
           } else {
               if (a < b) {
                   c = c+1;
                   d = d-1;
               }
               else if (b < a) {
                   c = c+e;
                   d = d-e;
               }
           }
       }

       System.out.println(a);
       System.out.println(e);
    }
}
