package com.elasticcloudservice.predict;

public class Predict {
    private static final int rowSize = 1;
    private static final int inputSize = 15;
    private static final int hiddenSize = 20;
    private static final int outputSize = 15;

    public static String[] predictVm(String[] ecsContent, String[] inputContent) {
        int startDate = getDate(inputContent[0]);
        int endDate = getDate(inputContent[inputContent.length - 1]);

        Matrix Win = generateRandomMatrix(hiddenSize, inputSize);
        Matrix bin = genetateDefaultMatrix(hiddenSize, rowSize, 0.1f);
        Matrix Wrec = generateRandomMatrix(hiddenSize, hiddenSize);
        Matrix Ws = generateRandomMatrix(inputSize, hiddenSize);
        Matrix h0 = genetateDefaultMatrix(hiddenSize, outputSize, 0.1f);
        Matrix bout = genetateDefaultMatrix(outputSize, rowSize, 0.1f);

        Matrix lastHt = h0;
        for (int i = startDate; i < endDate; i++) {
            //获得输入和标注
            float[][] input = getEcsAmount(i);
            Matrix X = new Matrix(input);

            float[][] output = getEcsAmount(i + 1);
            Matrix Y = new Matrix(output);

            //前推
            Matrix ut = Wrec.multiply(lastHt).add(Win.multiply(X)).add(bin);
            Matrix ht = ut.sigmod();
            lastHt = ht;
            Matrix ot = Ws.multiply(ht).add(bout);
            Matrix yt = ot.tan();

            //后推
            Matrix dot = genetateDefaultMatrix(outputSize, rowSize, 0);
            Matrix db0 = genetateDefaultMatrix(outputSize, rowSize, 0);
            Matrix dbin = genetateDefaultMatrix(hiddenSize, rowSize, 0);
            Matrix dWs = genetateDefaultMatrix(outputSize, hiddenSize, 0);
            Matrix dWrec = genetateDefaultMatrix(hiddenSize, hiddenSize, 0);
            Matrix dWin = genetateDefaultMatrix(hiddenSize, inputSize, 0);
            for (int j = i; j >= startDate; i--) {
                dot = Y.multiply(ot.tan().backwards()).multiply(ot.cos().multiply(ot.cos()).backwards()).minus();
                
            }
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

    private static float[][] getEcsAmount(int date) {

    }

}
