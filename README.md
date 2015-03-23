# LogisticRegressionClassifier
Implementation of trainable general purpose logistic regression classifier and application for recognition of handwritten digits.
The classifier and the application are both written is Java 8.

## The classifier
The classifier uses the Hill Climbing algorithm maximizing the logarithmic likelihood, as a method to optimize the weights.
The implementation allows configuring parameters heta (weight update term) and lambda (normalization term).
It also allows custom loggers for recording test data that where classified incorrectly.

The classifier can be found in `LogisticRegressionClassifier.java`.
It provides methods for accepting train and test data.


## The application
The application can recognize digits '1' and '7'.
Both train and test data are images in 28*28 pixel grayscale form *.
The application first trains the classifier with train images of '1' and '7', mixed and shuffled, several times.
Then, it determines it's accuracy over the test images.
Images that were classified incorrectly are saved in the current directory.


#### Accuracy of the digit recognition application
The accuracy varies between executions as it depends on the shuffling of the train data; but this variation does exceed 0.03.

* overall ratio=0.995
* digit '1' (true) ratio=1134/1135=0.999
* digit '7' (false) ratio=1019/1028=0.991


#### Values for heta and lamdba parameters
The application provides the method `findBestHetaLambda` which can be used to estimate the best combination of these parameters.
The best combination is found to be:

* heta=1.0E-6
* lambda=1.0E-10

As we can see, normalization parameter lambda is extremely low which means that normalizion is not really needed for this specific application.
A full report of the estimation can be found in `heta_lamdba_results.txt`.
To run the estimation proccess again run the application with the `-hl` command line argument.


#### Number of applications of the train data
Applying the shuffled train data 8 times is found to be adequate.


\* These data were prodived by "Artificial Inteligence" undergraduate cource in AUEB Informatics department, 2014/2015, by Michael Titsias.
