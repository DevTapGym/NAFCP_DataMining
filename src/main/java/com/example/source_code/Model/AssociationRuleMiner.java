package com.example.source_code.Model;

import java.util.*;

public class AssociationRuleMiner {
    private final Map<Set<String>, Integer> supportMap;
    private final int transactionCount;
    private final double minConfidence;
    private final double lift;
    Map<Set<String>, Integer> frequentItemsets = new HashMap<>();

    public AssociationRuleMiner(
                                Map<Set<String>, Integer> supportMap,
                                int transactionCount,
                                double minConfidence,
                                double lift) {
        this.supportMap = supportMap;
        this.transactionCount = transactionCount;
        this.minConfidence = minConfidence;
        this.lift = lift;
    }

    public Map<Set<String>, Integer> getFrequentItemsets() {
        return frequentItemsets;
    }

    public List<Rule> generateRules() {
        // Bước 1: mở rộng từ tập phổ biến đóng thành tập phổ biến
        this.frequentItemsets = expandClosedToFrequent(supportMap);

        List<Rule> rules = new ArrayList<>();
        for (Map.Entry<Set<String>, Integer> entry : frequentItemsets.entrySet()) {
            Set<String> itemset = entry.getKey();
            int supportItemset = entry.getValue();
            if (itemset.size() < 2) continue;

            List<Set<String>> subsets = getNonEmptyProperSubsets(itemset);
            for (Set<String> antecedent : subsets) {
                Set<String> consequent = new HashSet<>(itemset);
                consequent.removeAll(antecedent);
                if (consequent.isEmpty()) continue;

                int supportAntecedent = frequentItemsets.getOrDefault(antecedent, -1);
                int supportConsequent = frequentItemsets.getOrDefault(consequent, -1);
                if (supportAntecedent <= 0 || supportConsequent <= 0) continue;

                double confidence = (double) supportItemset / supportAntecedent;
                double lift = confidence / ((double) supportConsequent / transactionCount);
                if (confidence >= minConfidence && lift > this.lift) {
                    double support = (double) supportItemset / transactionCount;
                    rules.add(new Rule(antecedent, consequent, support, confidence, lift));
                }
            }
        }
        return rules;
    }

    private Map<Set<String>, Integer> expandClosedToFrequent(Map<Set<String>, Integer> closedItemsets) {
        Map<Set<String>, Integer> frequentItemsets = new HashMap<>();
        for (Map.Entry<Set<String>, Integer> entry : closedItemsets.entrySet()) {
            Set<String> closedSet = entry.getKey();
            int support = entry.getValue();
            List<Set<String>> subsets = getAllSubsets(closedSet);
            for (Set<String> subset : subsets) {
                if (subset.isEmpty()) continue;
                frequentItemsets.merge(subset, support, Math::max);
            }
        }
        return frequentItemsets;
    }

    private List<Set<String>> getAllSubsets(Set<String> set) {
        List<Set<String>> result = new ArrayList<>();
        List<String> items = new ArrayList<>(set);
        int n = items.size();
        for (int i = 0; i < (1 << n); i++) {
            Set<String> subset = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) {
                    subset.add(items.get(j));
                }
            }
            result.add(subset);
        }
        return result;
    }

    private List<Set<String>> getNonEmptyProperSubsets(Set<String> set) {
        List<Set<String>> result = new ArrayList<>();
        List<String> items = new ArrayList<>(set);
        int n = items.size();
        for (int i = 1; i < (1 << n) - 1; i++) {
            Set<String> subset = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) {
                    subset.add(items.get(j));
                }
            }
            result.add(subset);
        }
        return result;
    }
}
