package com.elasticcloudservice.predict;

import java.util.ArrayList;

public class Predict {
    private static final int rowSize = 1;
    private static final int inputSize = 15;
    private static final int hiddenSize = 20;
    private static final int outputSize = 15;
    private static final double learingRate = 0.05;

    public static String[] predictVm(String[] ecsContent, String[] inputContent) {
        int startDate = getDate(inputContent[0]);
        int endDate = getDate(inputContent[inputContent.length - 1]);

        Matrix Win = generateRandomMatrix(hiddenSize, inputSize);
        Matrix bin = genetateDefaultMatrix(hiddenSize, rowSize, 0.1f);
        Matrix Wrec = generateRandomMatrix(hiddenSize, hiddenSize);
        Matrix Ws = generateRandomMatrix(inputSize, hiddenSize);
        Matrix h0 = genetateDefaultMatrix(hiddenSize, outputSize, 0.1f);
        Matrix bout = genetateDefaultMatrix(outputSize, rowSize, 0.1f);

        ArrayList<Matrix> h = new ArrayList<>();
        h.add(h0);
        for (int i = startDate; i < endDate; i++) {
            int index = i - startDate;
            //获得输入和标注
            float[][] input = getEcsAmount(inputContent, i);
            Matrix X = new Matrix(input);

            float[][] output = getEcsAmount(inputContent, i + 1);
            Matrix Y = new Matrix(output);

            //前推
            Matrix ut = Wrec.multiply(h.get(index)).add(Win.multiply(X)).add(bin);
            Matrix ht = ut.sigmod();
            h.add(ht);
            Matrix ot = Ws.multiply(ht).add(bout);
            Matrix yt = ot.tan();

            //后推
            Matrix dot = genetateDefaultMatrix(outputSize, rowSize, 0);
            Matrix db0 = genetateDefaultMatrix(outputSize, rowSize, 0);
            Matrix dbin = genetateDefaultMatrix(hiddenSize, rowSize, 0);
            Matrix dWs = genetateDefaultMatrix(outputSize, hiddenSize, 0);
            Matrix dWrec = genetateDefaultMatrix(hiddenSize, hiddenSize, 0);
            Matrix dWin = genetateDefaultMatrix(hiddenSize, inputSize, 0);
            Matrix dht = genetateDefaultMatrix(hiddenSize, outputSize, 0);
            Matrix dyt = genetateDefaultMatrix(hiddenSize, rowSize, 0);
            for (int j = i; j > startDate; j--) {
                int t = j - startDate;
                dot = Y.multiply(ot.tan().backwards()).multiply(ot.cos().multiply(ot.cos()).backwards()).minus();
                db0 = db0.add(dot);
                dWs = dWs.add(dot.multiply(ht.transform()));
                dht = dht.add(Ws.transform().multiply(dot));
                dyt = yt.sigmod().multiply(genetateDefaultMatrix(hiddenSize, rowSize, 1).add(yt.sigmod().minus())).multiply(dht);
                dWin = dWin.add(dyt.multiply(X.transform()));
                dbin = dbin.add(dyt);
                dWrec = dWrec.add(dyt.multiply(h.get(t - 1).transform()));
                h.set(t - 1, Wrec.transform().multiply(dyt));
            }
            Win = Win.add(dWin.numberMultiply(learingRate));
            bin = bin.add(dbin.numberMultiply(learingRate));
            Wrec = Wrec.add(dWrec.numberMultiply(learingRate));
            Ws = Ws.add(dWs.numberMultiply(learingRate));
            h0 = h0.add(h.get(0).numberMultiply(learingRate));
            bout = bout.add(db0.numberMultiply(learingRate));
        }
    }

    private static Matrix genetateDefaultMatrix(int row, int column, float defaultValue) {
        float[][] matrixs = new float[row][column];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                matrixs[i][j] = defaultValue;
            }
        }
        return new Matrix(matrixs);
    }

    private static Matrix generateRandomMatrix(int row, int column) {
        float[][] matrixs = new float[row][column];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                matrixs[i][j] = (float) Math.random();
            }
        }
        return new Matrix(matrixs);
    }

    private static int getDate(String input) {

    }

    private static float[][] getEcsAmount(String[] inputContent, int date) {
        float[][] floats = new float[inputSize][rowSize];
        for (String record : inputContent) {
            if (getDate(record.split(" ")[2]) == date) {
                int machineNumber = Integer.parseInt(record.split(" ")[1].substring(6));
                if (machineNumber <= inputSize) {
                    floats[machineNumber - 1][0]++;
                }
            } else if (getDate(record.split(" ")[2]) < date) {
                break;
            }
        }
        return floats;
    }

}
