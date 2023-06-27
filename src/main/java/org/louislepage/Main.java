package org.louislepage;

import org.apache.commons.math3.linear.MatrixDimensionMismatchException;
import org.apache.sysds.common.Types;
import org.apache.sysds.runtime.data.DenseBlockFactory;
import org.apache.sysds.runtime.functionobjects.GreaterThan;
import org.apache.sysds.runtime.functionobjects.ValueFunction;
import org.apache.sysds.runtime.matrix.data.MatrixBlock;
import org.apache.sysds.runtime.matrix.data.Pair;
import org.apache.sysds.runtime.matrix.operators.BinaryOperator;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LongSummaryStatistics;

public class Main {
    public static void main(String[] args) {
        long seed = 1337;
        int numberOfRuns = 15;

        //operator to run
        ValueFunction operator = GreaterThan.getGreaterThanFnObject();

        //Map to store matrix sizes and their resulting metrics
        HashMap<Pair<Integer, Integer>, HashMap<String, LongSummaryStatistics>> resultStatistics= new LinkedHashMap<>();

        //add different sizes to compute
        resultStatistics.put(new Pair<>(100,100), new HashMap<>());
        resultStatistics.put(new Pair<>(1000,1000), new HashMap<>());
        resultStatistics.put(new Pair<>(10000,10000), new HashMap<>());

        //run for each size
        resultStatistics.forEach((size, results) -> {
            System.out.println("Computing metrics for "+numberOfRuns+" random computations each.");
            runForMatrixSize(operator, size.getKey(), size.getValue(), numberOfRuns, seed, results);
            System.out.println("----------------------------------------------------------------");
        });


        printResultStatistics(resultStatistics);
        try {
            safeToFile(operator, resultStatistics);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private static void runForMatrixSize(ValueFunction operatorFunction, int rlen, int clen, int numberOfRuns, long seed, HashMap<String, LongSummaryStatistics> resultStatistics){

        LongSummaryStatistics timesSummaryStatisticsFP64 = new LongSummaryStatistics();
        LongSummaryStatistics timesSummaryStatisticsBITSET = new LongSummaryStatistics();
        LongSummaryStatistics timesSummaryStatisticsBOOLEAN = new LongSummaryStatistics();

        for (int i = 0; i < numberOfRuns; i++) {
            System.out.println("=== Matrix Dimensions: "+rlen+" x "+clen+ " | Round "+(i+1)+" ===");

            MatrixBlock matrix_fp64 = runOperator(operatorFunction, Types.ValueType.FP64, rlen, clen, seed+i,  timesSummaryStatisticsFP64);
            MatrixBlock matrix_boolean = runOperator(operatorFunction, Types.ValueType.BITSET, rlen, clen, seed+i,  timesSummaryStatisticsBITSET);
            MatrixBlock matrix_true_boolean = runOperator(operatorFunction, Types.ValueType.BOOLEAN, rlen, clen, seed+i,  timesSummaryStatisticsBOOLEAN);
            boolean sameValues = hasSameValues(matrix_fp64, matrix_true_boolean) && hasSameValues(matrix_fp64, matrix_boolean);

            if (!sameValues) throw new ArithmeticException("Results differ.");
        }

        resultStatistics.put("FP64_runtime", timesSummaryStatisticsFP64);
        resultStatistics.put("BITSET_runtime", timesSummaryStatisticsBITSET);
        resultStatistics.put("BOOLEAN_runtime", timesSummaryStatisticsBOOLEAN);
    }
    private static MatrixBlock runOperator(ValueFunction operatorFunction, Types.ValueType resultType, int rlen, int clen, long startSeed, LongSummaryStatistics timeStatistics){
        //System.out.print("Operator: "+operatorFunction.getClass().getSimpleName()+" | Result type: "+resultType.toExternalString());


        //Preparation of matrices
        MatrixBlock mb1 = MatrixBlockBuilder.generateRandomDoubleMatrix(rlen,clen, startSeed);
        MatrixBlock mb2 = MatrixBlockBuilder.generateRandomDoubleMatrix(rlen,clen, startSeed+1);
        BinaryOperator op = new BinaryOperator(operatorFunction);
        MatrixBlock ret = new MatrixBlock(3,5, DenseBlockFactory.createDenseBlock(resultType, new int[]{rlen,clen}) );

        //running operator
        long start = System.nanoTime();
        mb1.binaryOperations(op, mb2, ret);
        long finish = System.nanoTime();
        long timeElapsedMicroS = (finish-start)/1000;
        timeStatistics.accept(timeElapsedMicroS);
        //results
        //System.out.println(" \t| Took "+timeElapsedMicroS+" microseconds.");
        return ret;
    }

    private static boolean hasSameValues(MatrixBlock mb1, MatrixBlock mb2){
        if (mb1.getNumRows() != mb2.getNumRows() || mb1.getNumColumns() != mb2.getNumColumns()){
            throw new MatrixDimensionMismatchException(mb2.getNumRows(), mb2.getNumColumns() , mb1.getNumRows(), mb1.getNumColumns());
        }

        for (int i = 0; i < mb1.getNumRows(); i++) {
            for (int j = 0; j < mb1.getNumColumns(); j++) {
                if ( mb1.getValue(i,j) != mb2.getValue(i,j) ){
                    return false;
                };
            }
        }
        return true;
    }

    private static void printResultStatistics(HashMap<Pair<Integer, Integer>, HashMap<String, LongSummaryStatistics>> resultStatistics){
        System.out.println("\n\n----------------------------------------------");
        System.out.println("================= STATISTICS =================");

        resultStatistics.forEach((size, results) -> {
            System.out.println("Matrix Size: "+size.getKey()+" x "+size.getValue());
            results.forEach((kind, metrics) -> {
                System.out.printf("%-16s: average of %5.2f milliseconds or %5.2f microseconds.%n", kind, metrics.getAverage()/1000, metrics.getAverage());
            });
            System.out.println("----------------------------------------------");
        });

    }

    private static void safeToFile(ValueFunction operatorFunction, HashMap<Pair<Integer, Integer>, HashMap<String, LongSummaryStatistics>> resultStatistics) throws IOException {
        File fout = new File("results_"+operatorFunction.getClass().getSimpleName()+"_"+getCurrentTimeStamp()+".txt");
        FileOutputStream fos = new FileOutputStream(fout);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        bw.write("Matrix_rlen,Matrix_clen,output_type,runtime_microseconds");
        bw.newLine();

        resultStatistics.forEach((size, result) -> {
            result.forEach((kind, metrics) -> {
                try {
                    bw.write(String.format("%s,%s,%s,%s", size.getKey(), size.getValue(), kind.split("_")[0],metrics.getAverage()));
                    bw.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        bw.close();
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH-mm-ss");
        Date now = new Date();
        String strDate = sdf.format(now);
        return strDate;
    }
}