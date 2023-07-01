package org.louislepage;

import org.apache.commons.math3.linear.MatrixDimensionMismatchException;
import org.apache.sysds.common.Types;
import org.apache.sysds.runtime.data.DenseBlockFactory;
import org.apache.sysds.runtime.functionobjects.GreaterThan;
import org.apache.sysds.runtime.functionobjects.ValueFunction;
import org.apache.sysds.runtime.functionobjects.Xor;
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
    public static void main(String[] args) throws IOException {
        long seed = 1337;
        int numberOfRuns = 5;
        int loopCount = 20;

        //operator to run
        ValueFunction operator = GreaterThan.getGreaterThanFnObject();
        ValueFunction booleanOperator = Xor.getXorFnObject();

        //Map to store matrix sizes and their resulting metrics
        HashMap<Pair<Integer, Integer>, HashMap<String, LongSummaryStatistics>> resultStatistics= new LinkedHashMap<>();

        //add different sizes to compute


        resultStatistics.put(new Pair<>(500,500), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(1000,1000), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(2000,2000), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(2500,2500), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(3000,3000), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(3250,3250), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(3500,3500), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(3750,3750), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(4000,4000), new LinkedHashMap<>());


        resultStatistics.put(new Pair<>(100,500), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(100,1000), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(100,5000), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(100,10000), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(100,50000), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(100,100000), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(100,160000), new LinkedHashMap<>());

        resultStatistics.put(new Pair<>(500,100), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(1000,100), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(5000,100), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(10000,100), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(50000,100), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(100000,100), new LinkedHashMap<>());
        resultStatistics.put(new Pair<>(160000,100), new LinkedHashMap<>());


        // resultStatistics.put(new Pair<>(10000,10000), new LinkedHashMap<>());

        String fileName = createFile(operator, booleanOperator, numberOfRuns, loopCount);

        //run for each size
        resultStatistics.forEach((size, results) -> {
            System.out.println("Computing metrics for "+numberOfRuns+" random computations each.");
            runForMatrixSize(operator, booleanOperator, size.getKey(), size.getValue(), numberOfRuns, loopCount, seed, results);
            System.out.println("----------------------------------------------------------------");
            results.forEach((kind, metrics) -> {
                try {
                    appendStatisticsToFile(fileName, kind, size, metrics);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });


        printResultStatistics(resultStatistics);


    }

    private static void runForMatrixSize(ValueFunction operatorFunction, ValueFunction booleanOperator, int rlen, int clen, int numberOfRuns, int loopCount, long seed, HashMap<String, LongSummaryStatistics> resultStatistics){

        //set bits during operator
        LongSummaryStatistics timesSummaryStatisticsFP64_DoubleToBool_Runtime = new LongSummaryStatistics();
        LongSummaryStatistics timesSummaryStatisticsBITSET_DoubleToBool_Runtime = new LongSummaryStatistics();
        LongSummaryStatistics timesSummaryStatisticsBOOLEAN_DoubleToBool_Runtime = new LongSummaryStatistics();

        LongSummaryStatistics timesSummaryStatisticsFP64_DoubleToBool_Memory = new LongSummaryStatistics();
        LongSummaryStatistics timesSummaryStatisticsBITSET_DoubleToBool_Memory = new LongSummaryStatistics();
        LongSummaryStatistics timesSummaryStatisticsBOOLEAN_DoubleToBool_Memory = new LongSummaryStatistics();

        LongSummaryStatistics timesSummaryStatisticsFP64_BoolToBool_Runtime = new LongSummaryStatistics();
        LongSummaryStatistics timesSummaryStatisticsBITSET_BoolToBool_Runtime = new LongSummaryStatistics();
        LongSummaryStatistics timesSummaryStatisticsBOOLEAN_BoolToBool_Runtime = new LongSummaryStatistics();

        LongSummaryStatistics timesSummaryStatisticsFP64_BoolToBool_Memory = new LongSummaryStatistics();
        LongSummaryStatistics timesSummaryStatisticsBITSET_BoolToBool_Memory = new LongSummaryStatistics();
        LongSummaryStatistics timesSummaryStatisticsBOOLEAN_BoolToBool_Memory = new LongSummaryStatistics();

        for (int i = 0; i < numberOfRuns; i++) {
            System.out.println("=== Matrix Dimensions: "+rlen+" x "+clen+ " | Round "+(i+1)+" ===");

            //run Double to Boolean
            MatrixBlock matrix_fp64 = runComparisonOperator(operatorFunction, Types.ValueType.FP64, rlen, clen, seed+i,  timesSummaryStatisticsFP64_DoubleToBool_Runtime, timesSummaryStatisticsFP64_DoubleToBool_Memory);
            MatrixBlock matrix_bitset = runComparisonOperator(operatorFunction, Types.ValueType.BITSET, rlen, clen, seed+i,  timesSummaryStatisticsBITSET_DoubleToBool_Runtime, timesSummaryStatisticsBITSET_DoubleToBool_Memory);
            MatrixBlock matrix_boolean = runComparisonOperator(operatorFunction, Types.ValueType.BOOLEAN, rlen, clen, seed+i,  timesSummaryStatisticsBOOLEAN_DoubleToBool_Runtime, timesSummaryStatisticsBOOLEAN_DoubleToBool_Memory);
            validateResult(matrix_fp64, matrix_fp64);
            boolean sameValues = validateResult(matrix_fp64, matrix_boolean) && validateResult(matrix_fp64, matrix_bitset);
            if (!sameValues) throw new ArithmeticException("Results of Double to Boolean differ.");

            //run BooleanToBoolean
            MatrixBlock matrix_fp64_boolean = runBooleanToBoolean(booleanOperator, Types.ValueType.FP64, rlen, clen, loopCount, seed+i,  timesSummaryStatisticsFP64_BoolToBool_Runtime, timesSummaryStatisticsFP64_BoolToBool_Memory);
            MatrixBlock matrix_bitset_boolean = runBooleanToBoolean(booleanOperator, Types.ValueType.BITSET, rlen, clen, loopCount,seed+i,  timesSummaryStatisticsBITSET_BoolToBool_Runtime, timesSummaryStatisticsBITSET_BoolToBool_Memory);
            MatrixBlock matrix_boolean_boolean = runBooleanToBoolean(booleanOperator, Types.ValueType.BOOLEAN, rlen, clen, loopCount,seed+i,  timesSummaryStatisticsBOOLEAN_BoolToBool_Runtime, timesSummaryStatisticsBOOLEAN_BoolToBool_Memory);

            sameValues = validateResult(matrix_fp64_boolean, matrix_boolean_boolean)  && validateResult(matrix_fp64_boolean, matrix_bitset_boolean);
            if (!sameValues) throw new ArithmeticException("Results of Boolean to Boolean differ.");

        }


        resultStatistics.put("FP64_DoubleToBool_Runtime", timesSummaryStatisticsFP64_DoubleToBool_Runtime);
        resultStatistics.put("BITSET_DoubleToBool_Runtime", timesSummaryStatisticsBITSET_DoubleToBool_Runtime);
        resultStatistics.put("BOOLEAN_DoubleToBool_Runtime", timesSummaryStatisticsBOOLEAN_DoubleToBool_Runtime);

        resultStatistics.put("FP64_BoolToBool_Runtime", timesSummaryStatisticsFP64_BoolToBool_Runtime);
        resultStatistics.put("BITSET_BoolToBool_Runtime", timesSummaryStatisticsBITSET_BoolToBool_Runtime);
        resultStatistics.put("BOOLEAN_BoolToBool_Runtime", timesSummaryStatisticsBOOLEAN_BoolToBool_Runtime);

        resultStatistics.put("FP64_DoubleToBool_Memory", timesSummaryStatisticsFP64_DoubleToBool_Memory);
        resultStatistics.put("BITSET_DoubleToBool_Memory", timesSummaryStatisticsBITSET_DoubleToBool_Memory);
        resultStatistics.put("BOOLEAN_DoubleToBool_Memory", timesSummaryStatisticsBOOLEAN_DoubleToBool_Memory);

        resultStatistics.put("FP64_BoolToBool_Memory", timesSummaryStatisticsFP64_BoolToBool_Memory);
        resultStatistics.put("BITSET_BoolToBool_Memory", timesSummaryStatisticsBITSET_BoolToBool_Memory);
        resultStatistics.put("BOOLEAN_BoolToBool_Memory", timesSummaryStatisticsBOOLEAN_BoolToBool_Memory);
    }

    private static MatrixBlock runBooleanToBoolean(ValueFunction operatorFunction, Types.ValueType booleanType, int rlen, int clen, int  loopCount, long startSeed, LongSummaryStatistics timeStatistics, LongSummaryStatistics memoryStatistics){
        long memBefore = MemoryCalculation.getMemoryUsed();
        MatrixBlock mb1;
        MatrixBlock mb2;
        MatrixBlock ret;
        if(booleanType == Types.ValueType.FP64){
            mb1 = MatrixBlockBuilder.generateRandomDoubleBooleanMatrix(rlen, clen, startSeed);
            mb2 = MatrixBlockBuilder.generateRandomDoubleBooleanMatrix(rlen, clen, startSeed+1);
        }else{
            mb1 = MatrixBlockBuilder.generateRandomBooleanMatrix(rlen, clen, startSeed, booleanType);
            mb2 = MatrixBlockBuilder.generateRandomBooleanMatrix(rlen, clen, startSeed+1, booleanType);
        }


        ret = new MatrixBlock(rlen,clen, DenseBlockFactory.createDenseBlock(booleanType, new int[]{rlen,clen}) );

        //if this throws an error, you probably have the default version of systemds where you can not force an operator to be not sparse safe
        //fix by adding a constructor that sets this in the superclass
        BinaryOperator op = new BinaryOperator(operatorFunction, false);


        long start = System.nanoTime();
        for (int i = 0; i < loopCount; i++) {
            mb1.binaryOperations(op, mb2, ret);
            mb1 = ret;
        }
        long finish = System.nanoTime();
        long memUsed = MemoryCalculation.getMemoryUsed() - memBefore;
        //force mb1 and mb2 to stay in memory until memUsed is computed
        if (mb1.getDenseBlock() != null && mb2.getDenseBlock() != null){
            mb1 = null;
            mb2 = null;
        }
        long timeElapsedMicroS = (finish-start)/1000;
        timeStatistics.accept(timeElapsedMicroS);
        memoryStatistics.accept(memUsed);
        //results
        //System.out.println(" \t| Took "+timeElapsedMicroS+" microseconds.");
        return ret;
    }

    private static MatrixBlock runComparisonOperator(ValueFunction operatorFunction, Types.ValueType resultType, int rlen, int clen, long startSeed, LongSummaryStatistics timeStatistics, LongSummaryStatistics memoryStatistics){
        long memBefore = MemoryCalculation.getMemoryUsed();
        //System.out.print("Operator: "+operatorFunction.getClass().getSimpleName()+" | Result type: "+resultType.toExternalString());


        //Preparation of matrices
        MatrixBlock mb1 = MatrixBlockBuilder.generateRandomDoubleMatrix(rlen,clen, startSeed);
        MatrixBlock mb2 = MatrixBlockBuilder.generateRandomDoubleMatrix(rlen,clen, startSeed+1);
        BinaryOperator op = new BinaryOperator(operatorFunction, false);


        MatrixBlock ret = new MatrixBlock(rlen,clen, DenseBlockFactory.createDenseBlock(resultType, new int[]{rlen,clen}) );

        //running operator
        long start = System.nanoTime();
        ret = mb1.binaryOperations(op, mb2, ret);
        long finish = System.nanoTime();
        long memUsed = MemoryCalculation.getMemoryUsed() - memBefore;

        //force mb1 and mb2 to stay in memory until memUsed is computed
        if (mb1.getDenseBlock() != null && mb2.getDenseBlock() != null){
            mb1 = null;
            mb2 = null;
        }
        long timeElapsedMicroS = (finish-start)/1000;
        timeStatistics.accept(timeElapsedMicroS);
        memoryStatistics.accept(memUsed);
        //results
        //System.out.println(" \t| Took "+timeElapsedMicroS+" microseconds.");
        return ret;
    }

    private static boolean validateResult(MatrixBlock mb1, MatrixBlock mb2){
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
                if(kind.contains("Runtime")) System.out.printf("%-30s: average of %15.2f milliseconds or %5.2f microseconds.%n", kind, metrics.getAverage()/1000, metrics.getAverage());
                if(kind.contains("Memory")) System.out.printf("%-30s: average of %15.2f bytes used.%n", kind, metrics.getAverage());
            });
            System.out.println("----------------------------------------------");
        });

    }

    private static void safeToFile(ValueFunction operatorFunction, ValueFunction booleanOperator, int numberOfRuns, int loopCount,HashMap<Pair<Integer, Integer>, HashMap<String, LongSummaryStatistics>> resultStatistics) throws IOException {
        File fout = new File("results_"+operatorFunction.getClass().getSimpleName()+"_"+resultStatistics.size()+"_"+getCurrentTimeStamp()+"_FINAL.txt");
        FileOutputStream fos = new FileOutputStream(fout);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        bw.write(String.format("# numberOfRuns: %s ; loopSize: %s ; operator: %s booleanToBooleanOperator: %s ; runtime in microseconds ; memory in bytes", numberOfRuns, loopCount, operatorFunction.getClass().getSimpleName(), booleanOperator.getClass().getSimpleName()));
        bw.newLine();
        bw.write("Matrix_rlen,Matrix_clen,output_type,operation,value_type,value");
        bw.newLine();


        resultStatistics.forEach((size, result) -> {
            result.forEach((kind, metrics) -> {
                try {
                    bw.write(String.format("%s,%s,%s,%s,%s,%s", size.getKey(), size.getValue(), kind.split("_")[0],kind.split("_")[1],kind.split("_")[2].toLowerCase(),metrics.getAverage()));
                    bw.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        bw.close();
    }

    private static String createFile(ValueFunction operatorFunction, ValueFunction booleanOperator, int numberOfRuns, int loopCount) throws IOException {
        String fileName = "results_"+operatorFunction.getClass().getSimpleName()+"_"+booleanOperator.getClass().getSimpleName()+"_"+getCurrentTimeStamp()+".txt";
        File fout = new File(fileName);
        FileOutputStream fos = new FileOutputStream(fout);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        bw.write(String.format("# numberOfRuns: %s ; loopSize: %s ; operator: %s booleanToBooleanOperator: %s ; runtime in microseconds ; memory in bytes", numberOfRuns, loopCount, operatorFunction.getClass().getSimpleName(), booleanOperator.getClass().getSimpleName()));
        bw.newLine();
        bw.write("Matrix_rlen,Matrix_clen,output_type,operation,value_type,value");
        bw.newLine();
        bw.close();
        return fileName;
    }
    private static void appendStatisticsToFile(String fileName, String kind, Pair<Integer,Integer> size, LongSummaryStatistics metrics)throws IOException {
        FileWriter fw = new FileWriter(fileName, true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(String.format("%s,%s,%s,%s,%s,%s", size.getKey(), size.getValue(), kind.split("_")[0],kind.split("_")[1],kind.split("_")[2].toLowerCase(),metrics.getAverage()));
        bw.newLine();
        bw.close();
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH-mm-ss");
        Date now = new Date();
        String strDate = sdf.format(now);
        return strDate;
    }
}