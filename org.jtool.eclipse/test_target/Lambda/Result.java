
import java.util.List;

class Result {
    
    private String name = "";
    private long time = 0;
    private int countPBook = 0;
    private int countEBook = 0;
    private int markedPBook = 0;
    private int markedEBook = 0;
    
    Result(String name, long time, List<Book> books) {
        this.name = name;
        this.time = time;
        
        for (Book book : books) {
            if (book instanceof PBook) {
                countPBook++;
                if (book.isMarked()) {
                    markedPBook++;
                }
            } else if (book instanceof EBook) {
                countEBook++;
                if (book.isMarked()) {
                    markedEBook++;
                }
            }
        }
    }
    
    long getTime() {
        return time;
    }
    
    public String toString() {
        return name + " = " + time + " ns" +
                " [" + markedPBook + "/" + countPBook +
                " " + markedEBook + "/" + countEBook + "]";
    }
}
