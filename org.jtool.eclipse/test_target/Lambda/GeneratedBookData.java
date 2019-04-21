
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class GeneratedBookData extends BookData {
    
    long sequenceNumber;
    
    GeneratedBookData(String title, int size, long seq) {
        super(title, size);
        sequenceNumber = seq;
    }
    
    static void sort(List<GeneratedBookData> data) {
        Collections.sort(data, new Comparator<GeneratedBookData>() {
            public int compare(GeneratedBookData d1, GeneratedBookData d2) {
                if (d1.sequenceNumber == d2.sequenceNumber) {
                    return 0;
                } else if (d1.sequenceNumber > d2.sequenceNumber) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }
}
