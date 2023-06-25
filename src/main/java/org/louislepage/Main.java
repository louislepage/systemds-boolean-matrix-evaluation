package org.louislepage;

import org.apache.sysds.common.Types;
import org.apache.sysds.runtime.data.DenseBlockFactory;
import org.apache.sysds.runtime.matrix.data.MatrixBlock;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        MatrixBlock mb1 = new MatrixBlock(3,5, DenseBlockFactory.createDenseBlock(Types.ValueType.TRUE_BOOLEAN, new int[] {3,5}));
    }
}