
/*
3120005 - Aliprantis Efstathios
3120144 - Pappas Dimitrios-Spiridon
3120178 - Sinacheri Anna-Chloe
*/
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.DoubleStream;
import javax.imageio.ImageIO;

public class ImageApplication {
    static final int IMAGE_WIDTH = 28;
    static final int IMAGE_HEIGHT = 28;
    static final String RESULTS_PATH = "heta_lamdba_results.txt";
    
    // Holds the results of the calculation for the best Heta and Lamdba
    static class HetaLamdbaResults {
        // holds the result of the evaluation of a specific heta-lambda pair
        static class IndividualResult {
            final LogisticRegressionClassifier.TestResults trueResult;
            final LogisticRegressionClassifier.TestResults falseResult;
            final double medianRatio;
            final double heta;
            final double lambda;

            public IndividualResult(LogisticRegressionClassifier.TestResults trueResult, LogisticRegressionClassifier.TestResults falseResult, double heta, double lambda) {
                this.trueResult = trueResult;
                this.falseResult = falseResult;
                this.heta = heta;
                this.lambda = lambda;
                this.medianRatio = (trueResult.ratio+falseResult.ratio)/2;
            }
            
            boolean isBetter(IndividualResult other) {
                return other.medianRatio > this.medianRatio;
            }

            @Override
            public String toString() {
                return "heta="+heta+" lambda="+ lambda+" ratio="+medianRatio
                    +" true ratio="+trueResult.passed+"/"+trueResult.read+"="+trueResult.ratio
                    +" false ratio="+falseResult.passed+"/"+falseResult.read+"="+falseResult.ratio;
            }
        }
        
        private IndividualResult best;
        private final List<IndividualResult> results = new LinkedList<>();

        public IndividualResult getBest() {
            return best;
        }
        
        public synchronized boolean append(LogisticRegressionClassifier.TestResults trueResult,
                LogisticRegressionClassifier.TestResults falseResult, double heta, double lambda) {
            IndividualResult result = new IndividualResult(trueResult, falseResult,
                    heta, lambda);
            results.add(result);
            if (best == null || best.isBetter(result)) {
                best = result;
                return true;
            }
            return false;
        }
        
        public synchronized void print(String file) throws IOException {
            List<String> lines = new LinkedList<>();
            lines.add("Train results using various heta and lambda values");
            lines.add("");
            lines.add("Best combination is:");
            lines.add(String.valueOf(best));
            lines.add("");
            lines.add("All results:");
            
            // sort by best ratio
            results.sort((res1, res2) -> (int)Math.signum(res2.medianRatio-res1.medianRatio));
            results.stream().map(String::valueOf).forEach(lines::add);
            java.nio.file.Files.write(Paths.get(file), lines);
        }
    }
    
    // ties to find the best heta and lambda value by testing several combinations
    static HetaLamdbaResults findBestHetaLambda() {
        // logger will ignore failled images
        LogisticRegressionClassifier.FailLogger logger = (data, cat, number) -> {};
        
        double[] hetas = {1e-8, 1e-9, 1e-10, 1e-11, 1e-12, 1e-13, 1e-14, 1e-15, 1e-16, 1e-17};
        double[] lambdas = {1e-8, 1e-9, 1e-10, 1e-11, 1e-12, 1e-13, 1e-14, 1e-15, 1e-16, 1e-17};
        HetaLamdbaResults results = new HetaLamdbaResults();
        
        DoubleStream.of(lambdas).parallel().forEach(lambda -> {
            DoubleStream.of(hetas).parallel().forEach(heta -> {
                try {
                    LogisticRegressionClassifier classifier = 
                            new LogisticRegressionClassifier(IMAGE_WIDTH*IMAGE_HEIGHT, heta, lambda);
                    classifier.readTrainFile("train1.txt", true);
                    classifier.readTrainFile("train7.txt", false);

                    LogisticRegressionClassifier.TestResults trueRatio
                            = classifier.readTestFile("test1.txt", true, logger);
                    LogisticRegressionClassifier.TestResults falseRatio
                            = classifier.readTestFile("test7.txt", false, logger);
                    results.append(trueRatio, falseRatio, heta, lambda);
                } catch (IOException e) {e.printStackTrace();};
            });
        });
        
        return results;
    }
    
    public static void main(String[] args) throws IOException {
        double heta;
        double lambda;
        
        boolean findBestHetaAndLambda = args.length > 0 && "hl".equalsIgnoreCase(args[0]);
        if (findBestHetaAndLambda) {
            // calculatiuon the best heta and lambda values
            System.out.println("Please wait while the best heta and lambda are determined");
            System.out.println("It is suggested that you make a coffee xD");
            HetaLamdbaResults results = findBestHetaLambda();
            System.out.println("Best heta and lambda are:");
            System.out.println(results.getBest());
            results.print(RESULTS_PATH);
            System.out.println("For more info see "+RESULTS_PATH);

            heta = results.getBest().heta;
            lambda = results.best.lambda;
        } else {
            // precalcutated heta and lambda
            heta = 1.0E-11;
            lambda = 1.0E-10;
        }
        
        // logger will save images that failed
        LogisticRegressionClassifier.FailLogger logger = (data, cat, number) -> {
            int[] imgData = DoubleStream.of(data).mapToInt(i -> (int) Math.round(i)).toArray();
            saveImage(imgData, IMAGE_WIDTH, IMAGE_HEIGHT, cat+"-"+number+".png");
        };
        
        System.out.println("\nTrainning with heta="+heta+" and lambda="+lambda);
        LogisticRegressionClassifier classifier =  new LogisticRegressionClassifier(
                IMAGE_WIDTH*IMAGE_HEIGHT, heta, lambda);
        classifier.readTrainFile("train1.txt", true);
        classifier.readTrainFile("train7.txt", false);
        
        LogisticRegressionClassifier.TestResults trueRatio
                = classifier.readTestFile("test1.txt", true, logger);
        System.out.println("true ratio = "+trueRatio.passed+"/"+trueRatio.read+"="+trueRatio.ratio);
        LogisticRegressionClassifier.TestResults falseRatio
                = classifier.readTestFile("test7.txt", false, logger);
        System.out.println("false ratio = "+falseRatio.passed+"/"+falseRatio.read+"="+falseRatio.ratio);
        System.out.println("Overall ratio="+((trueRatio.ratio+falseRatio.ratio)/2));
        
        System.out.println("\nPlease check working directory for images that failed");
    }
    
    static void saveImage(int[] data, int width, int height, String path) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            image.getRaster().setPixels(0, 0, width, height, data);
            ImageIO.write(image, "png", new File(path));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
