package neural_network.layers;

import lombok.Getter;
import neural_network.optimizers.Optimizer;
import nnarrays.NNArray;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class LayersBlock extends NeuralLayer{
    @Getter
    private ArrayList<NeuralLayer> layers;

    public LayersBlock() {
        layers = new ArrayList<>();
    }

    public LayersBlock addLayer(NeuralLayer layer){
        layers.add(layer);
        return this;
    }

    @Override
    public int[] size() {
        return layers.get(layers.size() - 1).size();
    }

    @Override
    public void initialize(Optimizer optimizer) {
        for (NeuralLayer layer : layers) {
            layer.initialize(optimizer);
        }
    }

    @Override
    public void update(Optimizer optimizer) {
        for (NeuralLayer layer : layers) {
            layer.update(optimizer);
        }
    }

    @Override
    public int info() {
        int countParam = 0;
        System.out.println("            |           Layers block        |             ");
        System.out.println("____________|_______________________________|_____________");
        for (NeuralLayer neuralLayer : layers) {
            countParam += neuralLayer.info();
            System.out.println("____________|_______________|_______________|_____________");
        }
        System.out.println("____________|_______________|_______________|_____________");
        return countParam;
    }

    @Override
    public void write(FileWriter writer) throws IOException {
        writer.write("Layers block\n");
        for (NeuralLayer layer : layers) {
            layer.write(writer);
        }
        writer.write("End\n");
        writer.flush();
    }

    @Override
    public void initialize(int[] size) {
        layers.get(0).initialize(size);
        for (int i = 1; i < layers.size(); i++) {
            layers.get(i).initialize(layers.get(i - 1).size());
        }
    }

    @Override
    public void generateOutput(NNArray[] input) {
        layers.get(0).generateOutput(input);
        for (int i = 1; i < layers.size(); i++) {
            layers.get(i).generateOutput(layers.get(i - 1).getOutput());
        }
    }

    @Override
    public void generateTrainOutput(NNArray[] input) {
        layers.get(0).generateTrainOutput(input);
        for (int i = 1; i < layers.size(); i++) {
            layers.get(i).generateTrainOutput(layers.get(i - 1).getOutput());
        }
    }

    @Override
    public void generateError(NNArray[] error) {
        layers.get(layers.size() - 1).generateError(error);
        for (int i = layers.size() - 2; i >= 0; i--) {
            layers.get(i).generateError(layers.get(i + 1).getError());
        }
    }

    @Override
    public NNArray[] getOutput() {
        return layers.get(layers.size() - 1).getOutput();
    }

    @Override
    public NNArray[] getError() {
        return layers.get(0).getError();
    }
}