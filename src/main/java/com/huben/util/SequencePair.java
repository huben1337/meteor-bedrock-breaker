package com.huben.util;

public class SequencePair {
    public SequencePair () {
        values[0] = -1;
        values[1] = -1;
    }
    public SequencePair (int first) {
        values[0] = first;
        values[1] = -1;
    }
    public SequencePair(int first, int last) {
        values[0] = first;
        values[1] = last;
    }
    private final int[] values = new int[2];

    public int getFirst () {
        return values[0];
    }
    public void setFirst (int value) {
        values[0] = value;
    }
    public int getLast () {
        return values[1];
    }
    public void setLast (int value) {
        values[1] = value;
    }

    public boolean hasFirst () {
        return values[0] != -1;
    }
    public boolean hasLast () {
        return values[1] != -1;
    }
}