package com.elasticcloudservice.predict;

public class Matrix {
    private float[][] value;

    public Matrix(float[][] m) {
        this.value = m;
    }

    public Matrix add(Matrix matrix) {
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                value[i][j] += matrix.value[i][j];
            }
        }
        return this;
    }

    public Matrix multiply(Matrix matrix) {
        float[][] c = strassenMatrixMultiplyRecursive(value, matrix.value);
        Matrix m = new Matrix(c);
        return m;

    }

    public Matrix sigmod() {
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                value[i][j] = sigmod(value[i][j]);
            }
        }
        return this;
    }

    public Matrix tan() {
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                value[i][j] = (float) Math.tan(value[i][j]);
            }
        }
        return this;
    }

    public Matrix cos() {
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                value[i][j] = (float) Math.cos(value[i][j]);
            }
        }
        return this;
    }

    public Matrix backwards() {
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                value[i][j] = 1 / value[i][j];
            }
        }
        return this;
    }

    public Matrix minus() {
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[0].length; j++) {
                value[i][j] = -value[i][j];
            }
        }
        return this;
    }

    private static float sigmod(double value) {
        double ey = Math.pow(Math.E, -value);
        double result = 1 / (1 + ey);
        return (float) result;
    }

    public static float[][] strassenMatrixMultiplyRecursive(float A[][], float B[][]) {
        int n = A.length;

        float C[][] = new float[n][n];
        if (n == 1) {
            C[0][0] = A[0][0] * B[0][0];
        } else {
            float A11[][], A12[][], A21[][], A22[][];
            float B11[][], B12[][], B21[][], B22[][];
            float C11[][], C12[][], C21[][], C22[][];
            float S1[][], S2[][], S3[][], S4[][], S5[][], S6[][], S7[][], S8[][], S9[][], S10[][];
            float P1[][], P2[][], P3[][], P4[][], P5[][], P6[][], P7[][];

            A11 = new float[n / 2][n / 2];
            A12 = new float[n / 2][n / 2];
            A21 = new float[n / 2][n / 2];
            A22 = new float[n / 2][n / 2];
            copyToMatrixArray(A, 0, 0, n / 2, n / 2, A11);
            copyToMatrixArray(A, 0, n / 2, n / 2, n / 2, A12);
            copyToMatrixArray(A, n / 2, 0, n / 2, n / 2, A21);
            copyToMatrixArray(A, n / 2, n / 2, n / 2, n / 2, A22);

            B11 = new float[n / 2][n / 2];
            B12 = new float[n / 2][n / 2];
            B21 = new float[n / 2][n / 2];
            B22 = new float[n / 2][n / 2];
            copyToMatrixArray(B, 0, 0, n / 2, n / 2, B11);
            copyToMatrixArray(B, 0, n / 2, n / 2, n / 2, B12);
            copyToMatrixArray(B, n / 2, 0, n / 2, n / 2, B21);
            copyToMatrixArray(B, n / 2, n / 2, n / 2, n / 2, B22);

            S1 = new float[n / 2][n / 2];
            S2 = new float[n / 2][n / 2];
            S3 = new float[n / 2][n / 2];
            S4 = new float[n / 2][n / 2];
            S5 = new float[n / 2][n / 2];
            S6 = new float[n / 2][n / 2];
            S7 = new float[n / 2][n / 2];
            S8 = new float[n / 2][n / 2];
            S9 = new float[n / 2][n / 2];
            S10 = new float[n / 2][n / 2];
            squareMatrixSub(B12, B22, S1);
            squareMatrixAdd(A11, A12, S2);
            squareMatrixAdd(A21, A22, S3);
            squareMatrixSub(B21, B11, S4);
            squareMatrixAdd(A11, A22, S5);
            squareMatrixAdd(B11, B22, S6);
            squareMatrixSub(A12, A22, S7);
            squareMatrixAdd(B21, B22, S8);
            squareMatrixSub(A11, A21, S9);
            squareMatrixAdd(B11, B12, S10);

            P1 = new float[n / 2][n / 2];
            P2 = new float[n / 2][n / 2];
            P3 = new float[n / 2][n / 2];
            P4 = new float[n / 2][n / 2];
            P5 = new float[n / 2][n / 2];
            P6 = new float[n / 2][n / 2];
            P7 = new float[n / 2][n / 2];
            P1 = strassenMatrixMultiplyRecursive(A11, S1);
            P2 = strassenMatrixMultiplyRecursive(S2, B22);
            P3 = strassenMatrixMultiplyRecursive(S3, B11);
            P4 = strassenMatrixMultiplyRecursive(A22, S4);
            P5 = strassenMatrixMultiplyRecursive(S5, S6);
            P6 = strassenMatrixMultiplyRecursive(S7, S8);
            P7 = strassenMatrixMultiplyRecursive(S9, S10);

            C11 = new float[n / 2][n / 2];
            C12 = new float[n / 2][n / 2];
            C21 = new float[n / 2][n / 2];
            C22 = new float[n / 2][n / 2];
            float temp[][] = new float[n / 2][n / 2];
            squareMatrixAdd(P5, P4, temp);
            squareMatrixSub(temp, P2, temp);
            squareMatrixAdd(temp, P6, C11);

            squareMatrixAdd(P1, P2, C12);
            squareMatrixAdd(P3, P4, C21);

            squareMatrixAdd(P5, P1, temp);
            squareMatrixSub(temp, P3, temp);
            squareMatrixSub(temp, P7, C22);

            copyFromMatrixArray(C, 0, 0, n / 2, n / 2, C11);
            copyFromMatrixArray(C, 0, n / 2, n / 2, n / 2, C12);
            copyFromMatrixArray(C, n / 2, 0, n / 2, n / 2, C21);
            copyFromMatrixArray(C, n / 2, n / 2, n / 2, n / 2, C22);
        }

        return C;
    }

    public static void squareMatrixAdd(float A[][], float B[][], float C[][]) {
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                C[i][j] = A[i][j] + B[i][j];
            }
        }
    }

    public static void squareMatrixSub(float A[][], float B[][], float C[][]) {
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                C[i][j] = A[i][j] - B[i][j];
            }
        }
    }

    public static void copyToMatrixArray(float srcMatrix[][], int startI, int startJ, int iLen, int jLen,
                                         float destMatrix[][]) {
        for (int i = startI; i < startI + iLen; i++) {
            for (int j = startJ; j < startJ + jLen; j++) {
                destMatrix[i - startI][j - startJ] = srcMatrix[i][j];
            }
        }
    }

    public static void copyFromMatrixArray(float destMatrix[][], int startI, int startJ, int iLen, int jLen,
                                           float srcMatrix[][]) {
        for (int i = 0; i < iLen; i++) {
            for (int j = 0; j < jLen; j++) {
                destMatrix[startI + i][startJ + j] = srcMatrix[i][j];
            }
        }
    }

    /*
    private Matrix dervationRowToCloumn(Matrix matrix){

    }
    private Matrix dervationColumnToRow(Matrix matrix){

    }
    public void dervation(Matrix matrix){

    }*/
}