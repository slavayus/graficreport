package com.ifmo.graficreport.jcubblas;
/* Imports, JCublas */

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.jcublas.JCublas;

class JCublasSample {
    /* Matrix size */
    private static final int N = 275;

    /* Main */
    public static void main(String[] args) {
        float[] h_A;
        float[] h_B;
        float[] h_C;
        Pointer d_A = new Pointer();
        Pointer d_B = new Pointer();
        Pointer d_C = new Pointer();
        float alpha = 1.0f;
        float beta = 0.0f;
        int n2 = N * N;
        int i;

        /* Initialize JCublas */
        jcuda.jcublas.JCublas.cublasInit();

        /* Allocate host memory for the matrices */
        h_A = new float[n2];
        h_B = new float[n2];
        h_C = new float[n2];

        /* Fill the matrices with test data */
        for (i = 0; i < n2; i++) {
            h_A[i] = (float) Math.random();
            h_B[i] = (float) Math.random();
            h_C[i] = (float) Math.random();
        }

        /* Allocate device memory for the matrices */
        jcuda.jcublas.JCublas.cublasAlloc(n2, Sizeof.FLOAT, d_A);
        jcuda.jcublas.JCublas.cublasAlloc(n2, Sizeof.FLOAT, d_B);
        jcuda.jcublas.JCublas.cublasAlloc(n2, Sizeof.FLOAT, d_C);

        /* Initialize the device matrices with the host matrices */
        jcuda.jcublas.JCublas.cublasSetVector(n2, Sizeof.FLOAT, Pointer.to(h_A), 1, d_A, 1);
        jcuda.jcublas.JCublas.cublasSetVector(n2, Sizeof.FLOAT, Pointer.to(h_B), 1, d_B, 1);
        jcuda.jcublas.JCublas.cublasSetVector(n2, Sizeof.FLOAT, Pointer.to(h_C), 1, d_C, 1);

        /* Performs operation using JCublas */
        jcuda.jcublas.JCublas.cublasSgemm('n', 'n', N, N, N, alpha, d_A, N, d_B, N, beta, d_C, N);

        /* Read the result back */
        jcuda.jcublas.JCublas.cublasGetVector(n2, Sizeof.FLOAT, d_C, 1, Pointer.to(h_C), 1);

        /* Memory clean up */
        jcuda.jcublas.JCublas.cublasFree(d_A);
        jcuda.jcublas.JCublas.cublasFree(d_B);
        jcuda.jcublas.JCublas.cublasFree(d_C);

        /* Shutdown */
        JCublas.cublasShutdown();
    }
}