package com.example.source_code.Model;
import java.util.Set;

public class Rule {
    private final Set<String> antecedent;
    private final Set<String> consequent;
    private final double confidence;
    private final double lift;
    private final double support;

    public Rule(Set<String> antecedent, Set<String> consequent, double support, double confidence, double lift) {
        this.antecedent = antecedent;
        this.consequent = consequent;
        this.confidence = confidence;
        this.lift = lift;
        this.support = support;
    }

    @Override
    public String toString() {
        return antecedent + " → " + consequent + " [Sup=" + String.format("%.2f",support) +", Conf=" + String.format("%.2f",confidence) + ", Lift=" + String.format("%.2f",lift) + "]";
    }
}

