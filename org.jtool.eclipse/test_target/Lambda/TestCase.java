
import java.util.List;
import java.util.ArrayList;

public class TestCase {
    
    private String name;
    
    private List<BookData> bookdata;
    
    TestCase(String filename) {
        name = filename;
        bookdata = GenList.load(filename);
    }
    
    public String getName() {
        return name;
    }
    
    public List<BookData> getBookData() {
        return bookdata;
    }
    
    public static List<TestCase> getTestCases() {
        List<TestCase> testcases = new ArrayList<TestCase>();
        testcases.add(new TestCase("bookdata0.txt"));
        testcases.add(new TestCase("bookdata25.txt"));
        testcases.add(new TestCase("bookdata50.txt"));
        testcases.add(new TestCase("bookdata75.txt"));
        testcases.add(new TestCase("bookdata100.txt"));
        return testcases;
    }
}
