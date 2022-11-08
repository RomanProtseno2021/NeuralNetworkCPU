package test.classification.ciraf;

import data.ciraf.Ciraf100Loader3D;
import neural_network.activation.FunctionActivation;
import neural_network.layers.convolution_3d.*;
import neural_network.layers.convolution_3d.inception.InceptionBlock;
import neural_network.layers.convolution_3d.inception.InceptionModule;
import neural_network.layers.dense.DropoutLayer;
import neural_network.layers.reshape.Flatten3DLayer;
import neural_network.layers.reshape.GlobalAveragePooling3DLayer;
import neural_network.loss.FunctionLoss;
import neural_network.network.NeuralNetwork;
import neural_network.optimizers.AdaBeliefOptimizer;
import neural_network.optimizers.AdamOptimizer;
import trainer.DataMetric;
import trainer.DataTrainer;

import java.io.FileWriter;
import java.io.IOException;

public class TestInceptionV1 {
    public static void main(String[] args) throws IOException {
        NeuralNetwork inception = new NeuralNetwork()
                .addInputLayer(32, 32, 3)
                .addLayer(new ConvolutionLayer(48, 5, 1, 2))
                .addActivationLayer(new FunctionActivation.ReLU())
                .addLayer(new MaxPoolingLayer(3, 2, 1))
                .addLayer(new InceptionModule()
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(16, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(24, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                                .addLayer(new ConvolutionLayer(32, 3, 1, 1))
                                .addLayer(new BatchNormalizationLayer3D())
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(4, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                                .addLayer(new ConvolutionLayer(8, 5, 1, 2))
                                .addLayer(new BatchNormalizationLayer3D())
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new MaxPoolingLayer(3, 1, 1))
                                .addLayer(new ConvolutionLayer(8, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                )
                .addLayer(new InceptionModule()
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(32, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(32, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                                .addLayer(new ConvolutionLayer(48, 3, 1, 1))
                                .addLayer(new BatchNormalizationLayer3D())
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(8, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                                .addLayer(new ConvolutionLayer(16, 5, 1, 2))
                                .addLayer(new BatchNormalizationLayer3D())
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new MaxPoolingLayer(3, 1, 1))
                                .addLayer(new ConvolutionLayer(16, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                )
                .addLayer(new MaxPoolingLayer(3, 2, 1))
                .addLayer(new InceptionModule()
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(48, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(24, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                                .addLayer(new ConvolutionLayer(52, 3, 1, 1))
                                .addLayer(new BatchNormalizationLayer3D())
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(4, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                                .addLayer(new ConvolutionLayer(12, 5, 1, 2))
                                .addLayer(new BatchNormalizationLayer3D())
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new MaxPoolingLayer(3, 1, 1))
                                .addLayer(new ConvolutionLayer(16, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                )
//                .addLayer(new InceptionModule()
//                        .addInceptionBlock(new InceptionBlock()
//                                .addLayer(new ConvolutionLayer(32, 1, 1, 0))
//                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
//                        )
//                        .addInceptionBlock(new InceptionBlock()
//                                .addLayer(new ConvolutionLayer(32, 1, 1, 0))
//                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
//                                .addLayer(new ConvolutionLayer(64, 3, 1, 1))
//                                .addLayer(new BatchNormalizationLayer3D())
//                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
//                        )
//                        .addInceptionBlock(new InceptionBlock()
//                                .addLayer(new ConvolutionLayer(6, 1, 1, 0))
//                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
//                                .addLayer(new ConvolutionLayer(16, 5, 1, 2))
//                                .addLayer(new BatchNormalizationLayer3D())
//                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
//                        )
//                        .addInceptionBlock(new InceptionBlock()
//                                .addLayer(new MaxPoolingLayer(3, 1, 1))
//                                .addLayer(new ConvolutionLayer(16, 1, 1, 0))
//                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
//                        )
//                )
                .addLayer(new InceptionModule()
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(40, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(28, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                                .addLayer(new ConvolutionLayer(56, 3, 1, 1))
                                .addLayer(new BatchNormalizationLayer3D())
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(6, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                                .addLayer(new ConvolutionLayer(16, 5, 1, 2))
                                .addLayer(new BatchNormalizationLayer3D())
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new MaxPoolingLayer(3, 1, 1))
                                .addLayer(new ConvolutionLayer(16, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                )
//                .addLayer(new InceptionModule()
//                        .addInceptionBlock(new InceptionBlock()
//                                .addLayer(new ConvolutionLayer(28, 1, 1, 0))
//                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
//                        )
//                        .addInceptionBlock(new InceptionBlock()
//                                .addLayer(new ConvolutionLayer(36, 1, 1, 0))
//                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
//                                .addLayer(new ConvolutionLayer(72, 3, 1, 1))
//                                .addLayer(new BatchNormalizationLayer3D())
//                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
//                        )
//                        .addInceptionBlock(new InceptionBlock()
//                                .addLayer(new ConvolutionLayer(8, 1, 1, 0))
//                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
//                                .addLayer(new ConvolutionLayer(16, 5, 1, 2))
//                                .addLayer(new BatchNormalizationLayer3D())
//                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
//                        )
//                        .addInceptionBlock(new InceptionBlock()
//                                .addLayer(new MaxPoolingLayer(3, 1, 1))
//                                .addLayer(new ConvolutionLayer(16, 1, 1, 0))
//                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
//                        )
//                )
                .addLayer(new InceptionModule()
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(64, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(40, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                                .addLayer(new ConvolutionLayer(80, 3, 1, 1))
                                .addLayer(new BatchNormalizationLayer3D())
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(8, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                                .addLayer(new ConvolutionLayer(32, 5, 1, 2))
                                .addLayer(new BatchNormalizationLayer3D())
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new MaxPoolingLayer(3, 1, 1))
                                .addLayer(new ConvolutionLayer(32, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                )
                .addLayer(new MaxPoolingLayer(3, 2, 1))
                .addLayer(new InceptionModule()
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(64, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(40, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                                .addLayer(new ConvolutionLayer(80, 3, 1, 1))
                                .addLayer(new BatchNormalizationLayer3D())
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(8, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                                .addLayer(new ConvolutionLayer(32, 5, 1, 2))
                                .addLayer(new BatchNormalizationLayer3D())
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new MaxPoolingLayer(3, 1, 1))
                                .addLayer(new ConvolutionLayer(32, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                )
                .addLayer(new InceptionModule()
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(96, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(48, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                                .addLayer(new ConvolutionLayer(96, 3, 1, 1))
                                .addLayer(new BatchNormalizationLayer3D())
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new ConvolutionLayer(12, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                                .addLayer(new ConvolutionLayer(32, 5, 1, 2))
                                .addLayer(new BatchNormalizationLayer3D())
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                        .addInceptionBlock(new InceptionBlock()
                                .addLayer(new MaxPoolingLayer(3, 1, 1))
                                .addLayer(new ConvolutionLayer(32, 1, 1, 0))
                                .addLayer(new ActivationLayer3D(new FunctionActivation.ReLU()))
                        )
                )
                .addLayer(new GlobalAveragePooling3DLayer())
                .addLayer(new DropoutLayer(0.4))
                .addDenseLayer(100, new FunctionActivation.Softmax())
                .setFunctionLoss(new FunctionLoss.CrossEntropy())
                .setOptimizer(new AdamOptimizer())
                .create();

        inception.info();

        Ciraf100Loader3D loader = new Ciraf100Loader3D();

        DataTrainer trainer = new DataTrainer(5000, 1000, loader);

        for (int i = 0; i < 100; i++) {
            inception.save(new FileWriter("D:/NetworkTest/ciraf/inceptionV1.txt"));
            trainer.train(inception, 64, 1, new DataMetric.Top1());
            trainer.score(inception, new DataMetric.Top1());
        }
    }
}