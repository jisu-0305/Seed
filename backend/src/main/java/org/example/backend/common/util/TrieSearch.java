package org.example.backend.common.util;

import java.util.*;

public class TrieSearch {

    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        List<String> results = new ArrayList<>();
    }

    static TrieNode root = new TrieNode();

    public static void insert(String name, String data) {
        if (name == null || data == null || name.trim().isEmpty()) return;
        name = name.trim();

        for (int i = 0; i < name.length(); i++) {
            TrieNode node = root;
            for (int j = i; j < name.length(); j++) {
                char c = name.charAt(j);
                node.children.putIfAbsent(c, new TrieNode());
                node = node.children.get(c);
                if (!node.results.contains(data)) {
                    node.results.add(data);
                }
            }
        }
    }

    public static List<String> search(String keyword) {
        TrieNode node = root;
        for (char c : keyword.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return new ArrayList<>();
            }
            node = node.children.get(c);
        }
        return new ArrayList<>(node.results);
    }

    public static void clear() {
        root = new TrieNode();
    }

    public static List<String> getAll() {
        Set<String> uniqueResults = new HashSet<>();
        collectAll(root, uniqueResults);
        return new ArrayList<>(uniqueResults);
    }

    private static void collectAll(TrieNode node, Set<String> result) {
        result.addAll(node.results);
        for (TrieNode child : node.children.values()) {
            collectAll(child, result);
        }
    }
}
