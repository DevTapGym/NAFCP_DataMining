package com.example.source_code.Model;

import java.util.ArrayList;
import java.util.List;

public class PPCNode {
    public String item;
    PPCNode parent;
    public List<PPCNode> children;
    public int frequency;
    public int preOrder;
    public int postOrder;

    public PPCNode(String item) {
        this.item = item;
        this.frequency = 1;
        this.children = new ArrayList<>();
        this.preOrder = -1;
        this.postOrder = -1;
    }

}
