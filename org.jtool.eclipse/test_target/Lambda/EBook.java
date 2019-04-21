
public class EBook extends Book {
    
    private int size; 
    
    public EBook(String title, int size) {
        super(title);
        this.size = size;
    }
    
    public int getSize() {
        return size;
    }
    
    public String toString() {
        return super.toString() + "@" + size;
    }
}
