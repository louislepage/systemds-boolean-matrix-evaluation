package org.louislepage;

import org.apache.sysds.runtime.matrix.data.MatrixBlock;

import java.util.Random;

public class MatrixBlockBuilder {

    public static MatrixBlock generateRandomDoubleBooleanMatrix(int rlen, int clen, long seed){
        Random random = new Random(seed);
        MatrixBlock mb1 = new MatrixBlock(rlen,clen,1.0);
        double[] data = mb1.getDenseBlock().valuesAt(0);
        for (int i = 0; i < mb1.getLength(); i++) {
            data[i] = random.nextBoolean() ? 1.0 : 0.0;
        }
        return mb1;
    }

    public static MatrixBlock generateRandomDoubleMatrix(int rlen, int clen, long seed){
        Random random = new Random(seed);
        MatrixBlock mb1 = new MatrixBlock(rlen,clen,1.0);
        double[] data = mb1.getDenseBlock().valuesAt(0);
        for (int i = 0; i < mb1.getLength(); i++) {
            data[i] = random.nextDouble();
        }
        return mb1;
    }
}
