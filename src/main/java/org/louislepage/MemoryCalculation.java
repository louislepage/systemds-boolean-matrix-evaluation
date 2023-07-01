package org.louislepage;

import java.lang.ref.WeakReference;

public class MemoryCalculation {


    /**
     * This method guarantees that garbage collection is
     * done unlike <code>{@link System#gc()}</code>
     */
    public static void gc() {
        Object obj = new Object();
        WeakReference<Object> ref = new WeakReference<Object>(obj);
        obj = null;
        while (ref.get() != null) {
            System.gc();
        }
    }

    public static long getMemoryUsed(){
        gc();
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
}
