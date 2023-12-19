/**
 *
 */
package com.allendowney.thinkdast;

import java.util.*;

/**
 * Implementation of a Map using a binary search tree.
 *
 * @param <K>
 * @param <V>
 *
 */
public class MyTreeMap<K, V> implements Map<K, V> {

	private int size = 0;
	private Node root = null;

	/**
	 * Represents a node in the tree.
	 *
	 */
	protected class Node {
		public K key;
		public V value;
		public Node left = null;
		public Node right = null;

		/**
		 * @param key
		 * @param value
		 * @param left
		 * @param right
		 */
		public Node(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}

	@Override
	public void clear() {
		size = 0;
		root = null;
	}

	@Override
	public boolean containsKey(Object target) {
		return findNode(target) != null;
	}

	/**
	 * Returns the entry that contains the target key, or null if there is none.
	 *
	 * @param target
	 */
	private Node findNode(Object target) {
		// some implementations can handle null as a key, but not this one
		if (target == null) {
			throw new IllegalArgumentException();
		}

		// something to make the compiler happy
		@SuppressWarnings("unchecked")
		Comparable<? super K> k = (Comparable<? super K>) target;

		Node node = root;
		while (node != null) {
            if (k.compareTo(node.key) > 0) {
				node = node.right;
            } else if (k.compareTo(node.key) < 0) {
				node = node.left;
			} else {
				return node;
			}
        }
		return null;
	}

	/**
	 * Compares two keys or two values, handling null correctly.
	 *
	 * @param target
	 * @param obj
	 * @return
	 */
	private boolean equals(Object target, Object obj) {
		if (target == null) {
			return obj == null;
		}
		return target.equals(obj);
	}

	@Override
	public boolean containsValue(Object target) {
		return containsValueHelper(root, target);
	}

	// 정답은 node l,r 미리 확인하는 식으로 진행함 -> 내가 구현한 것보다 재귀 호출 횟수가 낮음
	private boolean containsValueHelper(Node node, Object target) {
		if (node == null) {
			return false;
		}
		if (Objects.equals(node.value, target)) {
			return true;
		}
		return containsValueHelper(node.right, target) || containsValueHelper(node.left, target);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public V get(Object key) {
		Node node = findNode(key);
		if (node == null) {
			return null;
		}
		return node.value;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public Set<K> keySet() {
		Set<K> set = new LinkedHashSet<K>();
		return keySetHelper(root, set);
	}

	private Set<K> keySetHelper(Node node, Set<K> set) {
		if (node == null) {
			return set;
		}
		keySetHelper(node.left, set);
		//inorder dfs 방식으로 구현
		set.add(node.key);
		keySetHelper(node.right, set);
		return set;
	}

	@Override
	public V put(K key, V value) {
		if (key == null) {
			throw new NullPointerException();
		}
		if (root == null) {
			root = new Node(key, value);
			size++;
			return null;
		}
		return putHelper(root, key, value);
	}

	private V putHelper(Node node, K key, V value) {
		Comparable<? super K> k = (Comparable<? super K>) key;

		int cmp = k.compareTo(node.key);

			if (cmp > 0) {
				if (node.right == null) {
					node.right = new Node(key, value);
					size++;
					return null;
				} else {
					putHelper(node.right, key, value);
				}
			}

			if (cmp < 0) {
				if (node.left == null) {
					node.left = new Node(key, value);
					size++;
					return null;
				} else {
					putHelper(node.left, key, value);
				}
			}

			V oldVal = node.value;
			node.value = value;
			return oldVal;
	}

	/*
	공식 코드는 재귀 사용해서 품
		private V putHelper(Node node, K key, V value) {
		Comparable<? super K> k = (Comparable<? super K>) key;

		while (true) {
			if (k.compareTo(node.key) > 0) {
				if (node.right == null) {
					node.right = new Node(key, value);
					size++;
					return null;
				} else {
					node = node.right;
				}
			} else if (k.compareTo(node.key) < 0) {
				if (node.left == null) {
					node.left = new Node(key, value);
					size++;
					return null;
				} else {
					node = node.left;
				}
			} else {
				V oldVal = node.value;
				node.value = value;
				return oldVal;
			}
		}
	}
	 */

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Map.Entry<? extends K, ? extends V> entry: map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	// 삭제하려는 node의 자식이 존재하는 경우, 오른쪽 서브트리에서 가장 작은 노드와 대체한다.
	// 만약 오른쪽 서브트리가 없는 경우, 왼쪽 서브트리에서 가장 큰 노드와 대체한다.
	// 대체한 후, 대체한(삭제하려는 node)를 null로 변경하여 삭제한다.

	/*
	제거 수행 과정
	노드 찾기: 먼저 제거하려는 노드를 찾습니다. 이 작업은 트리를 탐색하여 수행됩니다.
	노드 제거: 찾은 노드를 제거합니다. 이 노드가 리프 노드(자식이 없는 노드)인 경우, 그냥 제거하면 됩니다. 그렇지 않은 경우, 재배치된 자식 노드가 원래 노드의 위치를 대체하게 됩니다. (자식 노드 재배치 과정에서 덮어씌워짐)
	자식 노드 재배치: 제거하려는 노드가 자식 노드를 가지고 있는 경우, 이 자식 노드들을 재배치해야 합니다. 일반적으로, 오른쪽 서브트리에서 가장 작은 노드(또는 왼쪽 서브트리에서 가장 큰 노드)를 찾아 제거된 노드의 위치로 옮깁니다.
	 */
	@Override
	public V remove(Object key) {
		// OPTIONAL TODO: FILL THIS IN!
		throw new UnsupportedOperationException();
		// 찾아봤는데, 이해잘 안가서 일단 스킵함
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Collection<V> values() {
		Set<V> set = new HashSet<V>();
		Deque<Node> stack = new LinkedList<Node>();
		stack.push(root);
		while (!stack.isEmpty()) {
			Node node = stack.pop();
			if (node == null) continue;
			set.add(node.value);
			stack.push(node.left);
			stack.push(node.right);
		}
		return set;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, Integer> map = new MyTreeMap<String, Integer>();
		map.put("Word1", 1);
		map.put("Word2", 2);
		Integer value = map.get("Word1");
		System.out.println(value);

		for (String key: map.keySet()) {
			System.out.println(key + ", " + map.get(key));
		}
	}

	/**
	 * Makes a node.
	 *
	 * This is only here for testing purposes.  Should not be used otherwise.
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public MyTreeMap<K, V>.Node makeNode(K key, V value) {
		return new Node(key, value);
	}

	/**
	 * Sets the instance variables.
	 *
	 * This is only here for testing purposes.  Should not be used otherwise.
	 *
	 * @param node
	 * @param size
	 */
	public void setTree(Node node, int size ) {
		this.root = node;
		this.size = size;
	}

	/**
	 * Returns the height of the tree.
	 *
	 * This is only here for testing purposes.  Should not be used otherwise.
	 *
	 * @return
	 */
	public int height() {
		return heightHelper(root);
	}

	private int heightHelper(Node node) {
		if (node == null) {
			return 0;
		}
		int left = heightHelper(node.left);
		int right = heightHelper(node.right);
		return Math.max(left, right) + 1;
	}
}
