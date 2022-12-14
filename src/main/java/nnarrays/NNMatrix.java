package nnarrays;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class NNMatrix extends NNArray {
    @Getter
    private final int column;
    @Getter
    private final int row;
    @Getter
    private final int[] rowIndex;

    public NNMatrix(int row, int column) {
        super(column * row);
        this.column = column;
        this.row = row;

        rowIndex = new int[row];
        for (int i = 0; i < row; i++) {
            rowIndex[i] = i * column;
        }
        countAxes = 2;
    }

    public NNMatrix(int row, int column, float[] data) {
        super(data);
        this.column = column;
        this.row = row;

        rowIndex = new int[row];
        for (int i = 0; i < row; i++) {
            rowIndex[i] = i * column;
        }
        countAxes = 2;
    }

    public NNMatrix(NNMatrix matrix) {
        this(matrix.row, matrix.column);
    }

    public NNVector[] toVectors() {
        NNVector[] vectors = new NNVector[row];
        for (int i = 0; i < row; i++) {
            vectors[i] = new NNVector(column);
            System.arraycopy(data, rowIndex[i], vectors[i].data, 0, column);
        }

        return vectors;
    }

    public void set(NNVector vector, int index_t) {
        int index = rowIndex[index_t];
        System.arraycopy(vector.data, 0, data, index, vector.size);
    }

    public void set(NNMatrix matrix, int index_t) {
        int index = rowIndex[index_t];
        System.arraycopy(matrix.data, 0, data, index, matrix.size);
    }

    @Override
    public int[] shape() {
        return new int[]{row, column};
    }

    public float get(int i, int j) {
        return data[rowIndex[i] + j];
    }

    public void set(int i, int j, float val) {
        data[rowIndex[i] + j] = val;
    }

    @SneakyThrows
    public void add(NNMatrix matrix) {
        if (size != matrix.size) {
            throw new Exception("Vector has difference size");
        }
        for (int i = 0; i < size; i++) {
            data[i] += matrix.data[i];
        }
    }

    public void add(int i, int j, float val) {
        data[rowIndex[i] + j] += val;
    }

    public NNMatrix transpose() {
        NNMatrix nnMatrix = new NNMatrix(this.column, this.row);
        int index;
        for (int i = 0; i < row; i++) {
            index = rowIndex[i];
            for (int j = 0; j < column; j++, index++) {
                nnMatrix.data[i + nnMatrix.rowIndex[j]] = data[index];
            }
        }
        return nnMatrix;
    }

    public void save(FileWriter writer) throws IOException {
        writer.write(row + " " + column + "\n");
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                writer.write(data[rowIndex[i] + j] + " ");
                if (j % 1000 == 0) {
                    writer.flush();
                }
            }
            writer.write("\n");
            writer.flush();
        }
    }

    public NNMatrix dotT(NNMatrix matrix) {
        NNMatrix result = new NNMatrix(row, matrix.getRow());

        for (int n = 0, indR = 0; n < row; n++) {
            for (int i = 0, index = 0; i < matrix.getRow(); i++, indR++) {
                for (int j = 0, indI = rowIndex[n]; j < matrix.getColumn(); j++, index++, indI++) {
                    result.data[indR] += data[indI] * matrix.data[index];
                }
            }
        }

        return result;
    }

    public NNMatrix dot(NNMatrix matrix) {
        NNMatrix result = new NNMatrix(row, matrix.column);
        float val;

        for (int n = 0, indR = 0; n < row; n++) {
            for (int i = 0; i < matrix.column; i++, indR++) {
                val = 0;
                for (int j = 0; j < column; j++) {
                    val += get(n, j) * matrix.get(j, i);
                }
                result.data[indR] = val;
            }
        }

        return result;
    }

    public NNMatrix dot(NNVector vector) {
        NNMatrix result = new NNMatrix(row, vector.size);
        float val;

        for (int n = 0, indR = 0; n < row; n++) {
            for (int i = 0; i < vector.size; i++, indR++) {
                val = 0;
                for (int j = 0; j < column; j++) {
                    val += get(n, j) * vector.get(i);
                }
                result.data[indR] = val;
            }
        }
        return result;
    }

    public NNMatrix dotT(NNVector vector) {
        NNMatrix result = new NNMatrix(row, 1);
        float val;

        for (int n = 0; n < row; n++) {
            val = 0;
            for (int i = 0; i < column; i++) {
                val += get(n, i) * vector.get(i);
            }
            result.data[n] = val;
        }
        return result;
    }

    public static NNMatrix read(Scanner scanner) {
        int[] size = Arrays.stream(scanner.nextLine().split(" ")).mapToInt(Integer::parseInt).toArray();
        NNMatrix matrix = new NNMatrix(size[0], size[1]);
        for (int i = 0; i < matrix.row; i++) {
            double[] arr = Arrays.stream(scanner.nextLine().split(" ")).mapToDouble(Float::parseFloat).toArray();
            for (int j = 0; j < matrix.column; j++) {
                matrix.data[matrix.rowIndex[i] + j] = (float) arr[j];
            }
        }
        return matrix;
    }

    public void convolution(NNMatrix input, NNTensor weight, int step, int pad) {
        int x0, inputIndex, weightIndex, outputIndex;
        float val;

        for (int x = -pad, w = 0; w < row; x += step, w++) {
            outputIndex = rowIndex[w];
            for (int d = 0; d < weight.getRows(); d++, outputIndex++) {
                val = 0;
                for (int j = 0; j < weight.getColumns(); j++) {
                    x0 = x + j;
                    if (x0 < 0 || x0 >= input.row) {
                        continue;
                    }
                    weightIndex = weight.getRowsIndex()[d] + weight.getColumnsIndex()[j];
                    inputIndex = input.rowIndex[x0];
                    for (int c = 0; c < weight.getDepth(); c++, inputIndex++, weightIndex++) {
                        val += input.data[inputIndex] * weight.data[weightIndex];
                    }
                }
                data[outputIndex] = val;
            }
        }
    }

    public void transposeConvolution(NNMatrix input, NNTensor weight, int padding) {
        int x0, inputIndex, weightIndex, outputIndex;
        int pad = weight.getColumns() - 1 - padding;
        int sCore = weight.getColumns() - 1;
        int sC;

        float val;

        for (int x = -pad, w = 0; w < row; x++, w++) {
            outputIndex = rowIndex[w];
            for (int d = 0; d < weight.getDepth(); d++, outputIndex++) {
                val = 0;
                for (int j = 0; j < weight.getColumns(); j++) {
                    x0 = x + j;
                    if (x0 < 0 || x0 >= input.row) {
                        continue;
                    }
                    sC = sCore - j;
                    weightIndex = weight.getColumnsIndex()[sC] + d;
                    inputIndex = input.rowIndex[x0];

                    for (int c = 0; c < weight.getRows(); c++, inputIndex++) {
                        val += input.data[inputIndex] * weight.data[weight.getRowsIndex()[c] + weightIndex];
                    }
                }
                data[outputIndex] = val;
            }
        }
    }

    @SneakyThrows
    public void add(NNVector vector) {
        if (column != vector.size) {
            throw new Exception("Array has difference size");
        }
        int inputIndex = 0;
        for (int i = 0; i < row; i++) {
            for (int k = 0; k < column; k++, inputIndex++) {
                data[inputIndex] += vector.data[k];
            }
        }
    }

    @SneakyThrows
    public NNMatrix sum(NNVector vector) {
        if (column != vector.size) {
            throw new Exception("Array has difference size");
        }
        NNMatrix result = new NNMatrix(this);
        int inputIndex = 0;
        for (int i = 0; i < row; i++) {
            for (int k = 0; k < column; k++, inputIndex++) {
                result.data[inputIndex] = data[inputIndex] + vector.data[k];
            }
        }

        return result;
    }

    @SneakyThrows
    public NNVector sum() {
        NNVector result = new NNVector(column);
        int inputIndex = 0;
        for (int i = 0; i < row; i++) {
            for (int k = 0; k < column; k++, inputIndex++) {
                result.data[k] = data[inputIndex];
            }
        }

        return result;
    }

    public void backGlobalMaxPool(NNMatrix input, NNVector output, NNVector error) {
        int index = 0;
        for (int i = 0; i < input.row; i++) {
            for (int k = 0; k < input.column; k++, index++) {
                if (output.data[k] == input.data[index]) {
                    data[index] = error.data[k];
                }
            }
        }
    }

    public void backGlobalAveragePool(NNVector error) {
        int index = 0;
        error.div(row);
        for (int i = 0; i < row; i++) {
            for (int k = 0; k < column; k++, index++) {
                data[index] = error.data[k];
            }
        }
    }

    public void addMulT(int n_row, NNVector vector, NNMatrix matrix) {
        for (int i = 0, index = 0; i < matrix.getRow(); i++) {
            for (int j = 0, indexOutput = rowIndex[n_row]; j < matrix.getColumn(); j++, index++, indexOutput++) {
                data[indexOutput] += vector.data[i] * matrix.data[index];
            }
        }
    }

    public NNMatrix stride(int stride) {
        if (stride == 1) {
            return this;
        }
        NNMatrix result = new NNMatrix(row * stride, column);
        int inputIndex, outpuIndex;
        for (int i = 0; i < row; i++) {
            inputIndex = rowIndex[i];
            outpuIndex = result.rowIndex[i * stride];
            for (int k = 0; k < column; k++, inputIndex++, outpuIndex++) {
                result.data[outpuIndex] = data[inputIndex];
            }
        }
        return result;
    }

    public void softmax(NNMatrix input) {
        int index;
        for (int k = 0; k < row; k++) {
            float sum = 0;
            index = k * column;
            float max = input.data[index];
            for (int i = 1; i < column; i++, index++) {
                if (max < input.data[index])
                    max = input.data[index];
            }
            index = k * column;
            for (int i = 0; i < column; i++, index++) {
                data[index] = (float) (Math.pow(Math.E, input.data[index] - max));
                sum += data[index];
            }
            sum += 0.00000001f;

            index = k * column;
            for (int i = 0; i < column; i++, index++) {
                data[index] /= sum;
            }
        }
    }

    public void derSoftmax(NNMatrix output, NNMatrix error) {
        int index, indexI, indexJ;
        for (int k = 0; k < row; k++) {
            float value;
            index = k * column;
            indexI = index;
            for (int i = 0; i < column; i++, indexI++) {
                data[indexI] = 0;
                indexJ = index;
                for (int j = 0; j < column; j++, indexJ++) {
                    if (i != j) {
                        value = output.data[indexI] * -output.data[indexJ];
                    } else {
                        value = output.data[indexI] * (1 - output.data[indexI]);
                    }
                    data[indexI] += error.getData()[indexJ] * value;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "NNMatrix [" +
                "size: (" + column +
                ", " + row +
                "), data: " + Arrays.toString(data) +
                ']';
    }
}
