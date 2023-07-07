package org.louislepage;

import org.apache.sysds.common.Types;
import org.apache.sysds.runtime.data.DenseBlockBool;
import org.apache.sysds.runtime.data.DenseBlockBoolArray;
import org.apache.sysds.runtime.data.DenseBlockFactory;
import org.apache.sysds.runtime.matrix.data.MatrixBlock;

import java.util.Random;


public class MatrixBlockBuilder {
    enum MatrixBlockType {
        FP64, BOOLEAN, BITSET
    }

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

    public static MatrixBlock generateRandomBooleanMatrix(int rlen, int clen, long seed, MatrixBlockType matrixBlockType){
        Random random = new Random(seed);
        MatrixBlock mb1 = createMatrixBlock(rlen, clen, matrixBlockType);
        for (int i = 0; i < rlen; i++) {
            for (int j = 0; j < clen; j++) {
                ((DenseBlockBool)mb1.getDenseBlock()).set(i,j,random.nextBoolean());
            }
        }
        return mb1;
    }

    static MatrixBlock createMatrixBlock(int rlen, int clen, MatrixBlockType matrixBlockType) {
        MatrixBlock mb1;
        if (matrixBlockType == MatrixBlockType.BOOLEAN){
            mb1 = new MatrixBlock(rlen, clen, new DenseBlockBoolArray(new int[]{rlen, clen}));
        } else {
            Types.ValueType relsultValueType = matrixBlockType == MatrixBlockType.BITSET ? Types.ValueType.BOOLEAN : Types.ValueType.FP64;
            mb1 = new MatrixBlock(rlen,clen, DenseBlockFactory.createDenseBlock(relsultValueType, new int[]{rlen,clen}) );
        }
        return mb1;
    }
}
