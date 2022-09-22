package neural_network.layers.dense;

import lombok.Setter;
import lombok.SneakyThrows;
import neural_network.initialization.Initializer;
import neural_network.optimizers.Optimizer;
import neural_network.regularization.Regularization;
import nnarrays.NNArray;
import nnarrays.NNArrays;
import nnarrays.NNMatrix;
import nnarrays.NNVector;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DenseLayer extends DenseNeuralLayer {
    //trainable parts
    private Regularization regularization;
    private Initializer initializer;
    @Setter
    private boolean loadWeight;

    //weight and threshold
    @Setter
    private NNMatrix weight;
    private NNMatrix derWeight;
    private NNMatrix[] optimizeWeight;
    @Setter
    private NNVector threshold;
    private NNVector derThreshold;
    private NNVector[] optimizeThreshold;

    public DenseLayer(int countNeuron) {
        super();
        this.countNeuron = countNeuron;
        this.trainable = true;
        initializer = new Initializer.HeNormal();
    }

    @Override
    public void initialize(Optimizer optimizer) {
        if (optimizer.getCountParam() > 0) {
            optimizeThreshold = new NNVector[optimizer.getCountParam()];
            optimizeWeight = new NNMatrix[optimizer.getCountParam()];

            for (int i = 0; i < optimizer.getCountParam(); i++) {
                optimizeThreshold[i] = new NNVector(threshold);
                optimizeWeight[i] = new NNMatrix(weight);
            }
        }
    }

    @Override
    public void update(Optimizer optimizer) {
        if (trainable) {
            if (input.length != 1) {
                derWeight.div(input.length);
                derThreshold.div(input.length);
            }

            if (optimizer.getClipValue() != 0) {
                derWeight.clip(optimizer.getClipValue());
                derThreshold.clip(optimizer.getClipValue());
            }

            if (regularization != null) {
                regularization.regularization(weight);
                regularization.regularization(threshold);
            }

            optimizer.updateWeight(weight, derWeight, optimizeWeight);
            optimizer.updateWeight(threshold, derThreshold, optimizeThreshold);
        }
    }

    public DenseLayer setTrainable(boolean trainable) {
        this.trainable = trainable;

        return this;
    }

    public DenseLayer setInitializer(Initializer initializer) {
        this.initializer = initializer;

        return this;
    }

    public DenseLayer setRegularization(Regularization regularization) {
        this.regularization = regularization;

        return this;
    }

    @Override
    public int info() {
        int countParam = weight.size() + threshold.size();
        System.out.println("Dense \t\t|  " + weight.getColumn() + "\t\t\t|  " + countNeuron + "\t\t\t|\t" + countParam);
        return countParam;
    }

    @Override
    public void write(FileWriter writer) throws IOException {
        writer.write("Dense layer\n");
        writer.write(countNeuron + "\n");
        threshold.save(writer);
        weight.save(writer);
        if (regularization != null) {
            regularization.write(writer);
        } else {
            writer.write("null\n");
        }
        writer.write(trainable + "\n");
        writer.flush();
    }

    @Override
    public void initialize(int[] size) {
        if (size.length != 1) {
            throw new ExceptionInInitializerError("Error size pre layer!");
        }
        if (!loadWeight) {
            threshold = new NNVector(countNeuron);
            derThreshold = new NNVector(countNeuron);
            weight = new NNMatrix(countNeuron, size[0]);
            derWeight = new NNMatrix(countNeuron, size[0]);
            initializer.initialize(weight);
        }
    }

    @SneakyThrows
    @Override
    public void generateOutput(NNArray[] inputs) {
        this.input = NNArrays.isVector(inputs);
        this.output = new NNVector[input.length];

        int countC = Runtime.getRuntime().availableProcessors() + 2;
        ExecutorService executor = Executors.newFixedThreadPool(countC);
        for (int t = 0; t < countC; t++) {
            final int firstIndex = t * input.length / countC;
            final int lastIndex = Math.min(input.length, (t + 1) * input.length / countC);
            executor.execute(() -> {
                for (int i = firstIndex; i < lastIndex; i++) {
                    output[i] = input[i].mul(weight);
                    output[i].add(threshold);
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    @Override
    public void generateTrainOutput(NNArray[] input) {
        generateOutput(input);
    }

    @SneakyThrows
    @Override
    public void generateError(NNArray[] errors) {
        errorNL = getErrorNextLayer(errors);
        this.error = new NNVector[errors.length];

        int countC = Runtime.getRuntime().availableProcessors() + 2;
        ExecutorService executor = Executors.newFixedThreadPool(countC);
        for (int t = 0; t < countC; t++) {
            final int firstIndex = t * input.length / countC;
            final int lastIndex = Math.min(input.length, (t + 1) * input.length / countC);
            executor.execute(() -> {
                for (int i = firstIndex; i < lastIndex; i++) {
                    error[i] = errorNL[i].mulT(weight);
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {}

        if (trainable) {
            derivativeWeight(errorNL);
        }
    }

    private void derivativeWeight(NNVector[] error) {
        int countC = Runtime.getRuntime().availableProcessors() + 2;
        ExecutorService executor = Executors.newFixedThreadPool(countC);
        for (int t = 0; t < countC; t++) {
            final int firstIndex = t * input.length / countC;
            final int lastIndex = Math.min(input.length, (t + 1) * input.length / countC);
            executor.execute(() -> {
                for (int i = firstIndex; i < lastIndex; i++) {
                    for (int j = 0, index = 0; j < error[i].size(); j++) {
                        for (int k = 0; k < input[i].size(); k++, index++) {
                            derWeight.getData()[index] += error[i].getData()[j] * input[i].getData()[k];
                        }
                    }
                    derThreshold.add(error[i]);
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    public static DenseLayer read(Scanner scanner) {
        DenseLayer denseLayer = new DenseLayer(Integer.parseInt(scanner.nextLine()));
        denseLayer.threshold = NNVector.read(scanner);
        denseLayer.weight = NNMatrix.read(scanner);
        denseLayer.setRegularization(Regularization.read(scanner));
        denseLayer.setTrainable(Boolean.parseBoolean(scanner.nextLine()));
        denseLayer.loadWeight = true;
        return denseLayer;
    }
}