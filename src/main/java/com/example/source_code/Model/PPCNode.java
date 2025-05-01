package com.example.source_code.Model;

import java.util.ArrayList;
import java.util.List;

public class PPCNode {
    String item;
    int frequency;
    PPCNode parent;
    List<PPCNode> children;
    int preOrder;
    int postOrder;

    public PPCNode(String item) {
        this.item = item;
        this.frequency = 1;
        this.children = new ArrayList<>();
        this.preOrder = -1;
        this.postOrder = -1;
    }

}
