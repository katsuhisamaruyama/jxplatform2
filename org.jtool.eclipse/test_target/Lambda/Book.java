
public abstract class Book {
    
    private boolean mark = false;
    
    private String title;
    
    public Book(String title) {
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setMark(boolean bool) {
        mark= bool;
    }
    
    public boolean isMarked() {
        return mark;
    }
    
    public String toString() {
        return title;
    }
}
