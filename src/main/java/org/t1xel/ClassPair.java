package org.t1xel;

public class ClassPair implements Comparable<ClassPair> {
    public byte[] classData;
    public int priority;
    public ClassInfo classInfo;

    public ClassPair(byte[] byArray) {
        this.classData = byArray;
        this.priority = 0;
    }

    @Override
    public int compareTo(ClassPair classPair) {
        return classPair.priority - this.priority;
    }
}

