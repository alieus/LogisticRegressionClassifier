/*
3120005 - Aliprantis Efstathios
3120144 - Pappas Dimitrios-Spiridon
3120178 - Sinacheri Anna-Chloe
*/
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class LogisticRegressionClassifier {
    
    /**
     * Handles data that where placed in the wrong category by readTestFile.
     */
    @FunctionalInterface
    public static interface FailLogger {
        void log(double[] data, boolean cat, long number);
    }
    
    // default heta and lambda
    double heta   = 0.000000000001;
    double lambda = 0.000000000001;
    final double[] w;

    // param l:  the size of the vectors
    public LogisticRegressionClassifier(int l) {
        w = new double[l];
    }

    public LogisticRegressionClassifier(int l, double heta, double lambda) {
        this(l);
        this.heta = heta;
        this.lambda = lambda;
    }
    
    // calculates the propability that the given data belong to the given category
    public double propability(double[] x, boolean cat) {
        if (cat) {
            return 1.0/(1+Math.exp(-mult(w, x)));
        } else {
            return 1 - propability(x, true);
        }
    }
    
    // param cat: the category the given data belong to
    public void train(double[] t, boolean cat) {
        if (t.length != w.length)
            throw new IllegalArgumentException("length of t should be: "
                    +w.length+" found: "+t.length);
        
        double c = cat ? 1 : 0;
        double norm = norm(w);
        if (norm != 0.0) {
            for (int i = 0; i < w.length; i++) {
                w[i] += heta*(c-propability(t, true))*t[i] - lambda*(w[i]/norm);
            }
        } else {
            for (int i = 0; i < w.length; i++) {
                w[i] += heta*(c-propability(t, true))*t[i];
            }
        }
    }

    // determines the category of the given data
    public boolean test(double[] x) {
        if (x.length != w.length)
            throw new IllegalArgumentException("length of x should be: "
                    +w.length+" found: "+x.length);
        
        return mult(w, x) > 0;
    }
    
    public void applyTrainData(TrainData trainData) {
        trainData.getTrainData().stream().forEach((trainDatum) -> {
            train(trainDatum.vector, trainDatum.cat);
        });
    }
    
    public static class TestResults {
        public final long read;
        public final long passed;
        public final double ratio;

        public TestResults(long read, long passed) {
            this.read = read;
            this.passed = passed;
            this.ratio = passed / (double) read;
        }
    }
    
    // param logger: handles failures
    public TestResults readTestFile(String path, boolean cat, FailLogger logger) throws IOException {
        Objects.requireNonNull(logger, "Logger can not be null");
        LongHolder read = new LongHolder(0);
        LongHolder pass = new LongHolder(0);
        
        java.nio.file.Files.lines(Paths.get(path)).forEach(line -> {
            String[] elements = line.split(" ");
            final double[] data = Stream.of(elements).mapToDouble(Double::parseDouble).toArray();
            if (test(data) == cat) {
                pass.i++;
            } else {
                logger.log(data, cat, read.i);
            }
            read.i++;
        });
        
        return new TestResults(read.i, pass.i);
    }
    
    
    // multiplies the given vectors
    private static double mult(double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length)
            throw new IllegalArgumentException("Must be |vec1|==|vec2|");
        double sum = 0;
        for (int i = 0; i < vec1.length; i++) {
            sum += vec1[i] * vec2[i];
        }

        return sum;
    }
    
    // calculates the euclidian norm of the given vector
    private static double norm(double[] vec) {
        return Math.sqrt(DoubleStream.of(vec).map(i -> i*i).sum());
    }
}