public class Test03 {

    public static void main(String argv[]) {
        int a = 0;
        int b = 0;

        switch(a){
        case 0:
            System.out.println(a);
            b = a;
            break;
    
        case 1:
            System.out.println(a);
            a++;
            break;
    
        case 2:
            System.out.println(a);
            break;
        }

        System.out.println(a);
        System.out.println(b);
    }
}
