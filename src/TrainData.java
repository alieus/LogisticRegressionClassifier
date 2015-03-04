
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 *
 * @author sta
 */
public class TrainData {
    
    public static final class TrainDatum {
        public final double[] vector;
        public final boolean cat;
        
        public TrainDatum(double[] vector, boolean cat) {
            this.vector = vector;
            this.cat = cat;
        }
    }
    
    private List<TrainDatum> trainData = new ArrayList<>();

    public List<TrainDatum> getTrainData() {
        return trainData;
    }

    public void setTrainData(List<TrainDatum> trainData) {
        Objects.requireNonNull(trainData, "train data cannot be null");
        this.trainData = trainData;
    }
    
    public long readFile(String path, boolean cat) throws IOException {
        LongHolder read = new LongHolder(0);
        
        java.nio.file.Files.lines(Paths.get(path)).forEach(line -> {
            String[] elements = line.split(" ");
            final double[] data = Stream.of(elements).mapToDouble(Double::parseDouble).toArray();
            trainData.add(new TrainDatum(data, cat));
            read.i++;
        });
        
        return read.i;
    }
    
    public void shufleData() {
        Collections.shuffle(trainData);
    }
}
