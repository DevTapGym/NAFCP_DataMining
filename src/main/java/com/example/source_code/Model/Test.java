package com.example.source_code.Model;

import java.util.*;

public class Test {
    private final PPCNode root;
    private final Map<String, List<PPCode>> nListMap;
    private final List<String> frequentItems;
    private final Map<String, Integer> itemFrequency;
    private double minSup;
    private int transactionCount;
    private final List<Set<String>> frequentClosedItemsets;
    private final Map<Integer, List<Set<String>>> supportToFCIs; // Bảng băm cho isSubsumed


    public Test() {
        root = new PPCNode("null");
        nListMap = new HashMap<>();
        frequentItems = new ArrayList<>();
        itemFrequency = new HashMap<>();
        frequentClosedItemsets = new ArrayList<>();
        supportToFCIs = new HashMap<>();
    }

    // Bước 1: Quét dữ liệu và xây dựng PPC-tree
    public void buildPPCTree(List<List<String>> transactions, double minSup) {
        this.minSup = minSup;
        this.transactionCount = transactions.size();
        int threshold = (int) Math.ceil(minSup * transactionCount);

        System.out.println("Step 1: Building PPC-tree");
        System.out.println("Transaction count: " + transactionCount);
        System.out.println("Minimum support threshold: " + threshold);

        // Quét lần 1: Tính tần suất các item
        for (List<String> transaction : transactions) {
            for (String item : transaction) {
                itemFrequency.put(item, itemFrequency.getOrDefault(item, 0) + 1);
            }
        }
        System.out.println("Item frequencies: " + itemFrequency);

        // Lọc các item thường xuyên
        for (Map.Entry<String, Integer> entry : itemFrequency.entrySet()) {
            if (entry.getValue() >= threshold) {
                frequentItems.add(entry.getKey());
            }
        }
        frequentItems.sort((a, b) -> itemFrequency.get(b) - itemFrequency.get(a));
        System.out.println("Frequent items (sorted by frequency): " + frequentItems);

        // Quét lần 2: Xây dựng PPC-tree
        System.out.println("Building PPC-tree with transactions:");
        for (List<String> transaction : transactions) {
            List<String> sortedTransaction = new ArrayList<>();
            for (String item : transaction) {
                if (frequentItems.contains(item)) {
                    sortedTransaction.add(item);
                }
            }
            sortedTransaction.sort((a, b) -> frequentItems.indexOf(a) - frequentItems.indexOf(b));
            System.out.println("Inserting sorted transaction: " + sortedTransaction);
            insertTree(sortedTransaction, root);
        }

        // Gán giá trị pre-order và post-order
        assignPrePostOrder(root, new int[]{0}, new int[]{0});
        System.out.println("PPC-tree constructed. Printing tree structure:");
        printTree(root, 0);
    }

    // Chèn giao dịch vào PPC-tree
    private void insertTree(List<String> transaction, PPCNode currentNode) {
        for (String item : transaction) {
            PPCNode child = null;
            for (PPCNode node : currentNode.children) {
                if (node.item.equals(item)) {
                    child = node;
                    child.frequency++;
                    break;
                }
            }
            if (child == null) {
                child = new PPCNode(item);
                child.parent = currentNode;
                currentNode.children.add(child);
            }
            currentNode = child;
        }
    }

    // In cấu trúc cây
    private void printTree(PPCNode node, int level) {
        if (node == null) return;
        System.out.println("  ".repeat(level) + node.item + ": " + node.frequency +
                " [pre=" + node.preOrder + ", post=" + node.postOrder + "]");
        for (PPCNode child : node.children) {
            printTree(child, level + 1);
        }
    }

    // Gán giá trị pre-order và post-order
    private void assignPrePostOrder(PPCNode node, int[] pre, int[] post) {
        if (node == null) return;
        node.preOrder = pre[0]++;
        for (PPCNode child : node.children) {
            assignPrePostOrder(child, pre, post);
        }
        node.postOrder = post[0]++;
    }

    // Bước 2: Tạo N-list
    public void generateNLists() {
        System.out.println("\nStep 2: Generating N-lists");
        for (String item : frequentItems) {
            nListMap.put(item, new ArrayList<>());
        }
        generateNListForNode(root);
        System.out.println("N-lists for frequent items:");
        for (Map.Entry<String, List<PPCode>> entry : nListMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    private void generateNListForNode(PPCNode node) {
        if (node == null || node.item.equals("null")) {
            for (PPCNode child : node.children) {
                generateNListForNode(child);
            }
            return;
        }
        if (nListMap.containsKey(node.item)) {
            nListMap.get(node.item).add(new PPCode(node.preOrder, node.postOrder, node.frequency));
        }
        for (PPCNode child : node.children) {
            generateNListForNode(child);
        }
    }

    // Bước 3: Khai thác các mẫu đóng thường xuyên
    public void findFCIs() {
        System.out.println("\nStep 3: Finding Frequent Closed Itemsets (FCIs)");
        List<Set<String>> candidates = new ArrayList<>();
        for (String item : frequentItems) {
            Set<String> itemset = new HashSet<>(Collections.singletonList(item));
            candidates.add(itemset);
        }
        System.out.println("Initial candidates: " + candidates);
        findFCIsRecursive(candidates, 1);
        System.out.println("Final FCIs: " + frequentClosedItemsets);
    }

    private void findFCIsRecursive(List<Set<String>> candidates, int iteration) {
        System.out.println("Iteration " + iteration + ":");
        List<Set<String>> newCandidates = new ArrayList<>();
        Set<Set<String>> removed = new HashSet<>();

        for (int i = 0; i < candidates.size(); i++) {
            Set<String> XA = candidates.get(i);
            if (removed.contains(XA)) continue;

            List<PPCode> nListXA = getNList(XA);
            int supportXA = calculateSupport(nListXA);
            System.out.println("  Processing XA: " + XA + ", Support: " + supportXA);

            for (int j = i + 1; j < candidates.size(); j++) {
                Set<String> XB = candidates.get(j);
                if (removed.contains(XB)) continue;

                List<PPCode> nListXB = getNList(XB);
                int supportXB = calculateSupport(nListXB);
                System.out.println("    Checking with XB: " + XB + ", Support: " + supportXB);

                if (isNListSubset(nListXB, nListXA)) {
                    // XB ⊆ XA
                    Set<String> XAB = new HashSet<>(XA);
                    XAB.addAll(XB);

                    if (supportXA == supportXB) {
                        // Theorem 2: Cả XA và XB bị thay bằng XAB
                        List<PPCode> nListXAB = new ArrayList<>(nListXB); // dùng lại NList(XB)
                        nListMap.put(String.join(",", XAB), nListXAB);

                        removed.add(XA);
                        removed.add(XB);
                        newCandidates.add(XAB);
                        System.out.println("Theorem 2: Replaced XA and XB with XAB: " + XAB);
                    } else {
                        // Theorem 1: chỉ thay thế XB bằng XBA, XA giữ nguyên
                        List<PPCode> nListXBA = generateNListFromInclusion(nListXB, nListXA);
                        nListMap.put(String.join(",", XAB), nListXBA);

                        removed.add(XB); // ❗ chỉ loại XB
                        newCandidates.add(XAB);
                        System.out.println("Theorem 1: Replaced XB with XAB (XBA): " + XAB);
                    }
                }
                else {
                    // Không bao hàm → giao tổ tiên-con
                    Set<String> XAB = new HashSet<>(XA);
                    XAB.addAll(XB);
                    List<PPCode> nListXAB = intersectNLists(nListXA, nListXB);
                    if (calculateSupport(nListXAB) >= minSup * transactionCount) {
                        if (!isSubsumed(XAB)) {
                            frequentClosedItemsets.add(XAB);
                            System.out.println("    Added XAB to FCIs: " + XAB);
                        }
                        nListMap.put(String.join(",", XAB), nListXAB);
                        newCandidates.add(XAB);
                    }
                }
            }

            if (!removed.contains(XA) && !isSubsumed(XA)) {
                frequentClosedItemsets.add(XA);
                System.out.println("  Added XA to FCIs: " + XA);
            }
        }

        if (!newCandidates.isEmpty()) {
            findFCIsRecursive(newCandidates, iteration + 1);
        }
    }

    private List<PPCode> generateNListFromInclusion(List<PPCode> nListXB, List<PPCode> nListXA) {
        List<PPCode> result = new ArrayList<>();
        for (PPCode XB : nListXB) {
            for (PPCode XA : nListXA) {
                if (XA.preOrder < XB.preOrder && XA.postOrder > XB.postOrder) {
                    result.add(new PPCode(XB.preOrder, XB.postOrder, XB.frequency));
                }
            }
        }
        return result;
    }


    // Kiểm tra tính bao hàm N-list (Theorem 1 và 2)
    private boolean isNListSubset(List<PPCode> nListXB, List<PPCode> nListXA) {
        for (PPCode codeXB : nListXB) {
            boolean hasAncestor = false;
            for (PPCode codeXA : nListXA) {
                if (codeXA.preOrder < codeXB.preOrder && codeXA.postOrder > codeXB.postOrder) {
                    hasAncestor = true;
                    break;
                }
            }
            if (!hasAncestor) return false;
        }
        return true;
    }

    // Kiểm tra tần suất của tập hợp
    private boolean isFrequent(Set<String> itemset) {
        List<PPCode> nList = getNList(itemset);
        int support = calculateSupport(nList);
        return support >= minSup * transactionCount;
    }

    // Lấy N-list của tập hợp
    private List<PPCode> getNList(Set<String> itemset) {
        if(itemset.size() > 1){
            return nListMap.getOrDefault(String.join(",", itemset), new ArrayList<>());
        }
        else{
            List<PPCode> intersection = null;
            for (String item : itemset) {
                List<PPCode> nList = nListMap.get(item);
                if (nList == null) return new ArrayList<>();
                if (intersection == null) {
                    intersection = new ArrayList<>(nList);
                } else {
                    intersection = intersectNLists(intersection, nList);
                }
            }
            return intersection != null ? intersection : new ArrayList<>();
        }
    }

    // Lấy N-list của tập hợp
//    private List<PPCode> getNList(Set<String> itemset) {
//        List<PPCode> intersection = null;
//        for (String item : itemset) {
//            List<PPCode> nList = nListMap.get(item);
//            if (nList == null) return new ArrayList<>();
//            if (intersection == null) {
//                intersection = new ArrayList<>(nList);
//            } else {
//                intersection = intersectNLists(intersection, nList);
//            }
//        }
//        return intersection != null ? intersection : new ArrayList<>();
//    }

    // Giao các N-list dựa trên quan hệ tổ tiên-con
    private List<PPCode> intersectNLists(List<PPCode> nListXA, List<PPCode> nListXB) {
        List<PPCode> result = new ArrayList<>();
        for (PPCode codeXA : nListXA) {
            for (PPCode codeXB : nListXB) {
                if (codeXA.preOrder < codeXB.preOrder && codeXA.postOrder > codeXB.postOrder) {
                    boolean exists = false;
                    for (PPCode existing : result) {
                        if (existing.preOrder == codeXA.preOrder && existing.postOrder == codeXA.postOrder) {
                            existing.frequency += codeXB.frequency;
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        result.add(new PPCode(codeXA.preOrder, codeXA.postOrder, codeXB.frequency));
                    }
                }
            }
        }
        return result;
    }


    // Tính tần suất hỗ trợ
    private int calculateSupport(List<PPCode> nList) {
        int support = 0;
        for (PPCode code : nList) {
            support += code.frequency;
        }
        return support;
    }

    // Kiểm tra tính đóng với bảng băm
    private boolean isSubsumed(Set<String> itemset) {
        List<PPCode> itemsetNList = getNList(itemset);
        int itemsetSupport = calculateSupport(itemsetNList);
        List<Set<String>> fcis = supportToFCIs.getOrDefault(itemsetSupport, new ArrayList<>());
        for (Set<String> fci : fcis) {
            if (fci.containsAll(itemset)) {
                return true;
            }
        }
        fcis.add(itemset);
        supportToFCIs.put(itemsetSupport, fcis);
        return false;
    }

    // Trả về kết quả
    public List<Set<String>> getFrequentClosedItemsets() {
        return frequentClosedItemsets;
    }

    // Hàm main để thử nghiệm
    public static void main(String[] args) {
        // Dữ liệu giao dịch mẫu
        List<List<String>> transactions = new ArrayList<>();
        transactions.add(Arrays.asList("A", "C", "T", "W"));
        transactions.add(Arrays.asList("C", "D", "W"));
        transactions.add(Arrays.asList("A", "C", "T", "W"));
        transactions.add(Arrays.asList("A", "C", "D", "W"));
        transactions.add(Arrays.asList("A", "C", "D", "T", "W", "E"));
        transactions.add(Arrays.asList("C", "D", "T", "E"));

        Test nafcp = new Test();
        nafcp.buildPPCTree(transactions, 0.5); // minSup = 50%
        nafcp.generateNLists();
        nafcp.findFCIs();

        // In kết quả cuối cùng
        System.out.println("\nFinal Frequent Closed Itemsets:");
        for (Set<String> itemset : nafcp.getFrequentClosedItemsets()) {
            System.out.println(itemset + " #SUP: " + nafcp.calculateSupport(nafcp.getNList(itemset)));
        }
    }
}