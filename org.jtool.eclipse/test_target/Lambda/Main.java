
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Main {
    
    private static List<Book> getList(TestCase testcase) {
        List<Book> books = new ArrayList<Book>();
        List<BookData> data = testcase.getBookData();
        for (BookData d : data) {
            if (d.getSize() == 0) {
                books.add(new PBook(d.getTitle()));
            } else {
                books.add(new EBook(d.getTitle(), d.getSize()));
            }
        }
        return books;
    }
    
    private static Result run(TestCase testcase) {
        Function<Object, Object> action = (Object obj) -> {
            if (obj instanceof PBook) {
                PBook book = (PBook)obj;
                if (book.getTitle().startsWith("m")) {
                    book.setMark(true);
                }
                return null;
            } else if (obj instanceof EBook) {
                EBook book = (EBook)obj;
                if (book.getTitle().startsWith("m") && book.getSize() >= 20000) {
                    book.setMark(true);
                }
                return null;
            }
            return null;
        };
        
        List<Book> books = getList(testcase);
        
        long start = System.nanoTime();
        
        books.stream().parallel().map(action);
        
        long end = System.nanoTime();
        long duration = end - start;
        
        Result result = new Result(testcase.getName(), duration, books);
        return result;
    }
    
    public static void main(String[] args) {
        List<TestCase> testcases = TestCase.getTestCases();
        for (int i = 0; i < testcases.size(); i++) {
            run(testcases.get(i));
        }
        
        Result[] results = new Result[testcases.size()];
        for (int i = 0; i < testcases.size(); i++) {
            results[i] = run(testcases.get(i));
        }
        
        long sum = 0;
        for (int i = 0; i < results.length; i++) {
            System.out.println(results[i].toString());
            sum = sum + results[i].getTime();
        }
        long average = sum / testcases.size();
        System.out.println("Average = " + average + " ns");
    }
}
