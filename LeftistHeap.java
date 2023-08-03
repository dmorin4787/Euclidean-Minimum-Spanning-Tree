package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;

public class LeftistHeap<Key extends Comparable<Key>, Value> {
	
	class LHNode { 
		Key key; // key (priority)
		Value value; // value (application dependent)
		LHNode left, right; // children
		int npl; // null path length
		boolean unlink;
		
		/*
		 * Constructor with known key and value
		 */
		public LHNode(Key key, Value value) {
			this.key = key;
			this.value = value;
			this.left = null;
			this.right = null;
			this.npl = 0;
			unlink = false;
		}
		
		/*
		 * Copy Constructor
		 */
		public LHNode(LHNode copyNode) {
			this.key = copyNode.key;
			this.value = copyNode.value;
			this.left = copyNode.left;
			this.right = copyNode.right;
			this.npl = copyNode.npl;
			this.unlink = copyNode.unlink;
		}
	}

	private LHNode root;
	private ArrayList<String> list;

	/** 
	 * Initializes an empty heap by setting the root to null
	 */
	public LeftistHeap() { 
		root = null;
	}
	
	public LeftistHeap(LHNode u) { 
		root = u;
	}
	
	/** 
	 * Checks if the heap is empty
	 */
	public boolean isEmpty() { 
		if (root == null) {
			return true;
		} else {
			return false;
		}
	}
	
	/** 
	 * Clears the contents of the heap
	 */
	public void clear() { 
		root = null;
	}
	
	/**
	 * This inserts the key-value pair (x, v), where x is the key
	 * and v is the value.
	 */
	public void insert(Key x, Value v) {
		if (root == null) {
			root = new LHNode(x, v);
		} else {
			LHNode newNode = new LHNode(x, v);
			
			root = merge(this.root, newNode);
		}
	}
	
	/**
	 * This merges the current heap with the heap h2
	 */
	public void mergeWith(LeftistHeap<Key, Value> h2) {
		if (h2 != null && this != h2) {
			root = merge(this.root, h2.root);
			h2.root = null;
		}
	}
	
	
	public LeftistHeap<Key, Value> split(Key x) {return null;}
	
	/**
	 * This returns the smallest key in the heap, but makes no changes to the
	 * heap’s contents or structure. If the heap is empty, it returns null.
	 */
	public Key getMinKey() { 
		if (root == null) {
			return null;
		} else {
			return this.root.key;
		}
	 }
	
	/**
	 * This locates the entry with the minimum key value, deletes it
	 * from the heap, and returns its associated value. 	
	 */
	public Value extractMin() throws Exception {
		if (root == null) {
			throw new Exception("Empty heap");
		} else {
			LHNode temp = root;
			root = merge(root.left, root.right);
			return temp.value;
		}
	}
	
	/**
	 * lists the contents of your tree in the form of a Java ArrayList of strings.
	 */
	public ArrayList<String> list() {
		list = new ArrayList<>();
		listHelper(root);
		return list;
	}
	
	/**
	 * helper for list() method that populates the list ArrayList through a
	 * preorder left to right traversal of the heap
 	 */
	private void listHelper(LHNode u) {
		if (u == null) {
			list.add("[]");
		} else {
			list.add("(" + u.key + ", " + u.value + ") [" + u.npl + "]");
			listHelper(u.right);
			listHelper(u.left);
		}
	}
	
	/**
	 * Helper method for mergeWith that takes two nodes and merges their contents
	 */ 
	private LHNode merge(LHNode u, LHNode v) { 
		if (u == null)
			return v; // if one is empty, return the other
		if (v == null)
			return u;
		if (u.key.compareTo(v.key) > 0) { // swap so that u is smaller
			LHNode t = u;
			u = v;
			v = t;
		}
		if (u.left == null) { // u has nothing to its left
			u.left = v; // put v here then
		} else { // merge v on right and swap if needed
			u.right = merge(u.right, v); // recursively merge u's right subtree
			if (u.left.npl < u.right.npl) { // fail the leftist property?
				LHNode t = u.left; // swap children
				u.left = u.right;
				u.right = t;
			}
			u.npl = u.right.npl + 1; // update npl value
		}
		return u; // return the root of final tree
	}
}