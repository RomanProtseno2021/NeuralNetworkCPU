package neural_network.layers.reshape;

import nnarrays.NNArray;
import nnarrays.NNArrays;
import nnarrays.NNTensor;
import nnarrays.NNVector;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class GlobalAveragePooling3DLayer extends Flatten3DLayer {

    @Override
    public int info() {
        System.out.println("Global averg|  " + height + ",\t" + width + ",\t" + depth + "\t|  " + countNeuron + "\t\t\t|");
        return 0;
    }

    @Override
    public void save(FileWriter writer) throws IOException {
        writer.write("Global average pooling 3D\n");
        writer.flush();
    }

    @Override
    public void initialize(int[] size) {
        if (size.length != 3) {
            throw new ExceptionInInitializerError("Error size pre layer!");
        }
        depth = size[2];
        height = size[0];
        width = size[1];
        countNeuron = depth;
    }

    @Override
    public void generateOutput(NNArray[] inputs) {
        input = NNArrays.isTensor(inputs);
        output = new NNVector[inputs.length];

        for (int i = 0; i < output.length; i++) {
            output[i] = new NNVector(countNeuron);
            output[i].globalAveragePool(input[i]);
        }
    }

    @Override
    public void generateError(NNArray[] errors) {
        errorNL = getErrorNextLayer(errors);
        error = new NNTensor[errors.length];

        for (int i = 0; i < errors.length; i++) {
            error[i] = new NNTensor(height, width, depth);
            error[i].backGlobalAveragePool(errorNL[i]);
        }
    }

    public static GlobalAveragePooling3DLayer read(Scanner scanner){
        return new GlobalAveragePooling3DLayer();
    }
}
