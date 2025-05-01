package com.example.source_code.Model;

public class PPCode {
    int preOrder;
    int postOrder;
    int frequency;

    public PPCode(int pre, int post, int freq) {
        this.preOrder = pre;
        this.postOrder = post;
        this.frequency = freq;
    }

    @Override
    public String toString() { return "[" + preOrder + ", " + postOrder + ", " + frequency + "]"; }
}
