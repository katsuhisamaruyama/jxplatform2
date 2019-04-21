
public class BookData {
    
    private String title;
    private int size;
    
    public BookData(String title) {
        this.title = title;
        this.size = 0;
    }
    
    public BookData(String title, int size) {
        this(title);
        this.size = size;
    }
    
    public String getTitle() {
        return title;
    }
    
    public int getSize() {
        return size;
    }
    
    public String toString() {
        if (size < 0) {
            return title;
        } else {
            return title + "@" + size;
        }
    }
}
