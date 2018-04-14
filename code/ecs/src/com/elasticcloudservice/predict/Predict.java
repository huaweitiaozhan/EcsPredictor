package com.elasticcloudservice.predict;

import java.util.ArrayList;

public class Predict {
    private static final int rowSize = 1;
    private static final int inputSize = 15;
    private static final int hiddenSize = 20;
    private static final int outputSize = 15;
    private static final double learingRate = 0.005;

    private static final int maxEsc = 15;
    private static final int paramNum = 20;

    public static String[] predictVm(String[] ecsContent, String[] inputContent) {
        //读入规格
        String[] standard = inputContent[0].split(" ");
        int coreNum = Integer.parseInt(standard[0]);
        int memNum = Integer.parseInt(standard[1]);
        int hardNum = Integer.parseInt(standard[2]);
        int allEsc = Integer.parseInt(inputContent[2]);
        int[] standards = new int[allEsc];
        for (int i = 0; i < allEsc; i++) {
            standards[i] = Integer.parseInt(inputContent[3 + i].split(" ")[0].substring(6));
        }
        boolean toArrange = inputContent[4 + allEsc].equals("CPU") ? true : false;
        String startPredDate = inputContent[6 + allEsc].split(" ")[0];
        String endPredDate = inputContent[7 + allEsc].split(" ")[0];
        int toPredDay = getDate(endPredDate) - getDate(startPredDate);

        //训练预测
        int startDate = getDate(ecsContent[0].split(" ")[0].split("\t")[2]);
        int endDate = getDate(ecsContent[ecsContent.length - 1].split(" ")[0].split("\t")[2]);
        ArrayList<float[][]> allData = new ArrayList<>();
        for (int i = startDate; i <= endDate; i++) {
            float[][] input = getEcsAmount(ecsContent, i);
            allData.add(input);
        }
        int maxDay = endDate - startDate + 1;
        int maxPick = maxDay - paramNum;

        Matrix[] thetas = new Matrix[maxEsc];
        Matrix[][] X = new Matrix[maxEsc][maxPick];
        for (int i = 0; i < maxEsc; i++) {
            for (int j = 0; j < maxPick; j++) {
                float[][] floats = new float[1][paramNum];
                for (int p = 0; p < paramNum; p++) {
                    floats[0][p] = allData.get(j + p)[i][0];
                }
                X[i][j] = new Matrix(floats);
            }
        }
        Matrix[][] Y = new Matrix[maxEsc][maxPick];
        for (int i = 0; i < maxEsc; i++) {
            for (int j = 0; j < maxPick; j++) {
                float[][] floats = new float[1][1];
                floats[0][0] = allData.get(j + paramNum)[i][0];
                Y[i][j] = new Matrix(floats);
            }
        }

        float leastLoss = 1000;
        for (int m = 0; m < 1; m++) {
            Matrix[] tempThetas = new Matrix[maxEsc];
            for (int i = 0; i < maxEsc; i++) {
                tempThetas[i] = generateRandomMatrix(paramNum, 1).numberMultiply(0.03);
            }
            for (int p = 0; p < 1000; p++) {
                for (int q = 0; q < maxPick; q++) {
                    for (int i : standards) {
                        Matrix delta = X[i][q].transform().multiply(X[i][q]).multiply(tempThetas[i]).add(X[i][q].transform().multiply(Y[i][q]).minus());
                        tempThetas[i] = tempThetas[i].add(delta.numberMultiply(learingRate).minus());
                    }
                }
                if (p % 10 == 0) {
                    float totalLoss = 0;
                    for (int q = 0; q < maxEsc; q++) {
                        Matrix j = X[q][maxPick - 5].multiply(tempThetas[q]).add(Y[q][maxPick - 5].minus()).Squart().numberMultiply(0.5);
                        totalLoss += j.getValue()[0][0];
                    }
                    if (p == 990) {
                        if (totalLoss < leastLoss) {
                            leastLoss = totalLoss;
                            for (int i = 0; i < maxEsc; i++) {
                                thetas[i] = tempThetas[i];
                            }
                        }
                    }
                }
            }
        }

        System.out.println("end");

        //预测
        float[][][] originDays = new float[15][1][10];
        for (int i = 0; i < 15; i++) {
            originDays[i] = X[i][maxPick - 1].getValue();
        }
        float[] floatPredResults = new float[15];
        for (int i = 0; i < 15; i++) {
            floatPredResults[i] = 0.0f;
        }
        for (int i = 0; i < toPredDay; i++) {
            for (int q = 0; q < 15; q++) {
                Matrix toPredData = new Matrix(originDays[q]);
                Matrix matrix = toPredData.multiply(thetas[q]);
                floatPredResults[q] += matrix.getValue()[0][0];

                originDays[q] = moveForward(originDays[q]);
                originDays[q][0][paramNum - 1] = matrix.getValue()[0][0];
            }
        }
        int[] intPredResults = new int[15];
        for (int i = 0; i < toPredDay; i++) {
            if (floatPredResults[i] < 0) {
                intPredResults[i] = 0;
            } else {
                intPredResults[i] = Math.round(floatPredResults[i]);
            }
        }

        //输出
        ArrayList<String> results = new ArrayList<>();
        int total = 0;
        for (int i = 0; i < allEsc; i++) {
            total += intPredResults[standards[i] - 1];
        }
        results.add(String.valueOf(total));
        for (int i = 0; i < allEsc; i++) {
            results.add("flavor" + standards[i] + " " + intPredResults[standards[i] - 1]);
        }
        int[] toArrangeResults = new int[total];
        int m = 0;
        for (int i = 0; i < allEsc; i++) {
            for (int j = 0; j < intPredResults[standards[i] - 1]; j++) {
                toArrangeResults[m] = standards[i];
                m++;
            }
        }
        StandardUtil standardUtil = new StandardUtil(coreNum, memNum, hardNum);
        ArrayList<ArrayList<Integer>> list = ArrangeUtil.arrangeVm(standardUtil, toArrange, toArrangeResults);

        results.add("");
        results.add(String.valueOf(list.size()));

        for (int i = 0; i <= list.size() - 1; i++) {
            String midStr = (i + 1) + " ";
            for (int j = 1; j <= 15; j++) {
                if (list.get(i).get(j) != 0) {
                    midStr = midStr + "flavor" + j + " " + list.get(i).get(j) + " ";
                }
            }

            midStr = midStr.substring(0, midStr.length() - 1);
            results.add(midStr);

        }

        for (int i = 0; i < results.size(); i++) {
            System.out.println(results.get(i));
        }


//        Matrix Win = generateRandomMatrix(hiddenSize, inputSize);
//        Matrix bin = generateRandomMatrix(hiddenSize, rowSize);
//        Matrix Wrec = generateRandomMatrix(hiddenSize, hiddenSize);
//        Matrix Ws = generateRandomMatrix(inputSize, hiddenSize);
//        Matrix h0 = genetateDefaultMatrix(hiddenSize, rowSize, 0.0f);
//        Matrix bout = generateRandomMatrix(outputSize, rowSize);
//
//        ArrayList<Matrix> h = new ArrayList<>();
//        ArrayList<Matrix> u = new ArrayList<>();
//        ArrayList<Matrix> y = new ArrayList<>();
//        ArrayList<Matrix> Y = new ArrayList<>();
//        ArrayList<Matrix> X = new ArrayList<>();
//        ArrayList<Matrix> O = new ArrayList<>();
//        h.add(h0);
//        for (int p = 0; p < 100; p++) {
//            for (int i = startDate; i < endDate; i++) {
//                for (int k = startDate; k < endDate; k++) {
//                    int index = k - startDate;
//                    //获得输入和标注
//                    float[][] input = getEcsAmount(inputContent, k);
//                    Matrix Xt = new Matrix(input);
//                    X.add(Xt);
//
//                    float[][] output = getEcsAmount(inputContent, k + 1);
//                    Matrix Yt = new Matrix(output);
//                    Y.add(Yt);
//
//                    //前推
//                    Matrix ut = Wrec.multiply(h.get(index)).add(Win.multiply(Xt)).add(bin);
//                    u.add(ut);
//                    Matrix ht = ut.sigmod();
//                    h.add(ht);
//                    Matrix ot = Ws.multiply(ht).add(bout);
//                    O.add(ot);
//                    Matrix yt = ot;
//                    y.add(yt);
//
//                    float loss = Yt.calulateLoss(yt);
//                    if (k == endDate - 1) {
//                        for (int xx = 0; xx < 15; xx++) {
//                            System.out.print(output[xx][0] + " ");
//                        }
//                        System.out.println();
//                        for (int xx = 0; xx < 15; xx++) {
//                            System.out.print(yt.getValue()[xx][0] + " ");
//                        }
//                        System.out.println();
//                        System.out.println(loss);
//                    }
//                }
//
//                //后推
//                Matrix dot = genetateDefaultMatrix(outputSize, rowSize, 0);
//                Matrix dut = genetateDefaultMatrix(hiddenSize, rowSize, 0);
////                Matrix db0 = genetateDefaultMatrix(outputSize, rowSize, 0);
////                Matrix dbin = genetateDefaultMatrix(hiddenSize, rowSize, 0);
//                Matrix dWs = genetateDefaultMatrix(outputSize, hiddenSize, 0);
//                Matrix dWrec = genetateDefaultMatrix(hiddenSize, hiddenSize, 0);
//                Matrix dWin = genetateDefaultMatrix(hiddenSize, inputSize, 0);
//                Matrix dht = genetateDefaultMatrix(hiddenSize, rowSize, 0);
//                int reverseEnd = i - startDate >= 5 ? i - 5 : startDate;
//                for (int j = i; j > reverseEnd; j--) {
//                    int t = j - startDate;
//                    dot = Y.get(t - 1).multiply(O.get(t - 1).tan().backwards()).multiply(O.get(t - 1).cos().multiply(O.get(t - 1).cos()).backwards()).minus().multiply(y.get(t - 1)).multiply((y.get(t - 1).Squart().add(Y.get(t - 1).Squart().minus())).Sqrt());
////                    db0 = db0.add(dot);
//                    dWs = dWs.add(dot.multiply(h.get(t).transform()));
//                    dht = dht.add(Ws.transform().multiply(dot));
//                    dut = u.get(t - 1).sigmod().multiply(genetateDefaultMatrix(outputSize, rowSize, 1).add(u.get(t - 1).sigmod().minus())).multiply(dht);
//                    dWin = dWin.add(dut.multiply(X.get(t - 1).transform()));
////                    dbin = dbin.add(dut);
//                    dWrec = dWrec.add(dut.multiply(h.get(t - 1).transform()));
////                    h.set(t - 1, Wrec.transform().multiply(dut));
//                }
//                Win = Win.add(dWin.numberMultiply(learingRate).minus());
////                bin = bin.add(dbin.numberMultiply(learingRate).minus());
//                Wrec = Wrec.add(dWrec.numberMultiply(learingRate).minus());
//                Ws = Ws.add(dWs.numberMultiply(learingRate).minus());
////                h0 = h0.add(h.get(0).numberMultiply(learingRate).minus());
////                bout = bout.add(db0.numberMultiply(learingRate).minus());
//            }
//        }
        return results.toArray(new String[results.size()]);
    }

    private static float[][] moveForward(float[][] floats) {
        for (int i = 0; i < floats.length; i++) {
            for (int j = 0; j < floats[0].length - 1; j++) {
                floats[i][j] = floats[i][j + 1];
            }
        }
        return floats;
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

    static int[] monthList1 = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
    static int[] monthList2 = {0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335};

    private static int getDate(String input) {
        int result = 0;
        String[] temp = input.split("-");
        int year = Integer.parseInt(temp[0]);
        int month = Integer.parseInt(temp[1]);
        int day = Integer.parseInt(temp[2]);
        if (judge(year)) {
            result = result + monthList2[month - 1] + day;
        } else {
            result = result + monthList1[month - 1] + day;
        }
        return result;
    }

    private static boolean judge(int y) {
        boolean is = false;
        if (y % 400 == 0) {
            is = true;
        } else {
            if (y % 100 != 0) {
                if (y % 4 == 0) {
                    is = true;
                }
            }
        }
        return is;
    }

    private static float[][] getEcsAmount(String[] inputContent, int date) {
        float[][] floats = new float[inputSize][rowSize];
        for (String record : inputContent) {
            if (getDate(record.split(" ")[0].split("\t")[2]) == date) {
                int machineNumber = Integer.parseInt(record.split(" ")[0].split("\t")[1].substring(6));
                if (machineNumber <= inputSize) {
                    floats[machineNumber - 1][0]++;
                }
            } else if (getDate(record.split(" ")[0].split("\t")[2]) > date) {
                break;
            }
        }
        return floats;
    }

}

