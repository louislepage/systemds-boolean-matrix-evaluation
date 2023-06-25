package org.louislepage;

import org.apache.commons.math3.linear.MatrixDimensionMismatchException;
import org.apache.sysds.common.Types;
import org.apache.sysds.runtime.data.DenseBlockFactory;
import org.apache.sysds.runtime.data.DenseBlockTrueBool;
import org.apache.sysds.runtime.functionobjects.GreaterThan;
import org.apache.sysds.runtime.functionobjects.ValueFunction;
import org.apache.sysds.runtime.matrix.data.MatrixBlock;
import org.apache.sysds.runtime.matrix.operators.BinaryOperator;
import java.time.Duration;
import java.time.Instant;
import java.util.LongSummaryStatistics;

public class Main {
    public static void main(String[] args) {
        int rlen = 10000;
        int clen = 10000;
        long seed = 1337;
        int numberOfRuns = 25;

        LongSummaryStatistics timesSummaryStatisticsFP64 = new LongSummaryStatistics();
        LongSummaryStatistics timesSummaryStatisticsBOOLEAN = new LongSummaryStatistics();
        LongSummaryStatistics timesSummaryStatisticsTRUE_BOOLEAN = new LongSummaryStatistics();



        System.out.println("Computing metrics for "+numberOfRuns+" random computations each.");
        System.out.println("Matrix Dimensions: "+rlen+" x "+clen);
        for (int i = 0; i < numberOfRuns; i++) {
            System.out.println("=== Round "+(i+1)+" ===");
            MatrixBlock matrix_fp64 = runOperator(GreaterThan.getGreaterThanFnObject(), Types.ValueType.FP64, rlen, clen, seed+i,  timesSummaryStatisticsFP64);
            MatrixBlock matrix_boolean = runOperator(GreaterThan.getGreaterThanFnObject(), Types.ValueType.BOOLEAN, rlen, clen, seed+i,  timesSummaryStatisticsBOOLEAN);
            MatrixBlock matrix_true_boolean = runOperator(GreaterThan.getGreaterThanFnObject(), Types.ValueType.TRUE_BOOLEAN, rlen, clen, seed+i,  timesSummaryStatisticsTRUE_BOOLEAN);
            boolean sameValues = hasSameValues(matrix_fp64, matrix_true_boolean) && hasSameValues(matrix_fp64, matrix_boolean);
            if (!sameValues) throw new ArithmeticException("Results differ.");
        }
        System.out.println("Done.\nMatrix Dimensions: "+rlen+" x "+clen+"\nOperator: "+GreaterThan.getGreaterThanFnObject().getClass().getSimpleName());
        System.out.println("Runtime statistics in ms:");
        System.out.println("FP64:\n"+ timesSummaryStatisticsFP64);
        System.out.println("BOOLEAN:\n"+ timesSummaryStatisticsBOOLEAN);
        System.out.println("TRUE_BOOLEAN with boolean arithmetics:\n"+ timesSummaryStatisticsTRUE_BOOLEAN);
    }

    private static MatrixBlock runOperator(ValueFunction operatorFunction, Types.ValueType resultType, int rlen, int clen, long startSeed, LongSummaryStatistics timeStatistics){
        System.out.print("Operator: "+operatorFunction.getClass().getSimpleName()+" | Result type: "+resultType.toExternalString());


        //Preparation of matrices
        MatrixBlock mb1 = MatrixBlockBuilder.generateRandomDoubleMatrix(rlen,clen, startSeed);
        MatrixBlock mb2 = MatrixBlockBuilder.generateRandomDoubleMatrix(rlen,clen, startSeed+1);
        BinaryOperator op = new BinaryOperator(operatorFunction);
        MatrixBlock ret = new MatrixBlock(3,5, DenseBlockFactory.createDenseBlock(resultType, new int[]{rlen,clen}) );

        //running operator
        Instant start = Instant.now();
        mb1.binaryOperations(op, mb2, ret);
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        timeStatistics.accept(timeElapsed);
        //results
        System.out.println("\t| Took "+timeElapsed+"ms.");
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
}