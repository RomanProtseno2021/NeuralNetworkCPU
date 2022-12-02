package neural_network.layers.convolution_3d;

import nnarrays.NNArray;
import nnarrays.NNArrays;
import nnarrays.NNTensor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MaxPoolingLayer extends ConvolutionNeuralLayer{
    private final int heightKernel;
    private final int widthKernel;
    private final int step;
    private final int paddingX;
    private final int paddingY;

    public MaxPoolingLayer(int sizeKernel, int step, int padding) {
        this(sizeKernel, sizeKernel, step, padding, padding);
    }

    public MaxPoolingLayer(int sizeKernel, int step) {
        this(sizeKernel, sizeKernel, step, 0, 0);
    }

    public MaxPoolingLayer(int sizeKernel) {
        this(sizeKernel, sizeKernel, sizeKernel, 0, 0);
    }

    public MaxPoolingLayer(int heightKernel, int widthKernel, int step, int paddingY, int paddingX) {
        this.heightKernel = heightKernel;
        this.widthKernel = widthKernel;
        this.step = step;
        this.paddingX = paddingX;
        this.paddingY = paddingY;
    }

    @Override
    public void initialize(int[] size) {
        if (size.length != 3) {
            throw new ExceptionInInitializerError("Error size pre layer!");
        }
        this.depth = size[2];
        this.height = size[0];
        this.width = size[1];

        outDepth = depth;
        outWidth = (width - widthKernel + 2 * paddingX) / step + 1;
        outHeight = (height - heightKernel + 2 * paddingY) / step + 1;
    }

    @Override
    public void generateOutput(NNArray[] inputs) {
        this.input = NNArrays.isTensor(inputs);
        this.output = new NNTensor[inputs.length];

        int countC = getCountCores();
        ExecutorService executor = Executors.newFixedThreadPool(countC);
        for (int t = 0; t < countC; t++) {
            final int firstIndex = t * input.length / countC;
            final int lastIndex = Math.min(input.length, (t + 1) * input.length / countC);
            executor.execute(() -> {
                for (int i = firstIndex; i < lastIndex; i++) {
                    output[i] = new NNTensor(outHeight, outWidth, outDepth);
                    output[i].maxPool(input[i], heightKernel, widthKernel, step, paddingY, paddingX);
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    @Override
    public void generateError(NNArray[] errors) {
        errorNL = getErrorNextLayer(errors);
        this.error = new NNTensor[errors.length];

        int countC = getCountCores();
        ExecutorService executor = Executors.newFixedThreadPool(countC);
        for (int t = 0; t < countC; t++) {
            final int firstIndex = t * input.length / countC;
            final int lastIndex = Math.min(input.length, (t + 1) * input.length / countC);
            executor.execute(() -> {
                for (int i = firstIndex; i < lastIndex; i++) {
                    this.error[i] = new NNTensor(height, width, depth);
                    this.error[i].backMaxPool(errorNL[i], input[i], output[i], heightKernel, widthKernel, step, paddingY, paddingX);
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    @Override
    public int info() {
        System.out.println("Max pool\t| " + height + ",\t" + width + ",\t" + depth + "\t| "
                + outHeight + ",\t"+ outWidth + ",\t" + outDepth + "\t|");
        return 0;
    }

    @Override
    public void save(FileWriter writer) throws IOException {
        writer.write("Max pooling layer 3D\n");
        writer.write(heightKernel + " " + widthKernel + " "  + step + " "  + paddingY + " "  + paddingX + "\n");
        writer.flush();
    }

    public static MaxPoolingLayer read(Scanner scanner){
        int[] param = Arrays.stream(scanner.nextLine().split(" ")).mapToInt(Integer::parseInt).toArray();
        MaxPoolingLayer layer = new MaxPoolingLayer(param[0], param[1], param[2], param[3], param[4]);
        return layer;
    }
}
