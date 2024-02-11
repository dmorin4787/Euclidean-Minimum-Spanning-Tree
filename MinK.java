/**
 * @author David Morin
 * This class represents a data structure for storing the smallest
 * k items in a max heap. The main function is to assist with the
 * k-nearest neighbors of the XkdTree
 */

package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;
import java.util.Collections;


public class MinK<Key extends Comparable<Key>, Value> {
	
	/*
	 * Represents a Key, Value pair
	 */
	class Entry implements Comparable<Entry> {
		Key key;
		Value val;
		
		public Entry(Key key, Value val) {
			this.key = key;
			this.val = val;
		}

		public Key getKey() {
			return key;
		}

		public void setKey(Key key) {
			this.key = key;
		}

		public Value getVal() {
			return val;
		}

		public void setVal(Value val) {
			this.val = val;
		}
		
		public int compareTo(Entry other) {
			return key.compareTo(other.key);
		}
	}
	
	private int k;
	private Key maxKey;
	private int size;
	private ArrayList<Entry> maxHeap;

	/**
	 * Constructor
	 * @param k: the max number of elements in the MinK data structure
	 * @param maxKey: Maximum key value
	 */
	public MinK(int k, Key maxKey) { 
		this.k = k;
		this.maxKey = maxKey;
		this.maxHeap = new ArrayList<>(k + 1);
		maxHeap.add(null);
		this.size = 0;
	}
	
	/**
	 * Gives the size in terms of the number of entries in the heap
	 * @return the size of maxHeap
	 */
	public int size() { 
		return size; 
	}
	
	/**
	 * Removes all elements from the heap
	 */
	public void clear() { 
		maxHeap.clear();
		maxHeap.add(null);
		size = 0;
	}
	
	/**
	 * Gives the maximum Key value among the elements
	 * @return the maximum key value if there are k elements in the heap, otherwise
	 * maxKey is returned
	 */
	public Key getKth() {
		if(size == k) {
			return maxHeap.get(1).getKey();
		} else {
			return maxKey;
		}
	}
	
	/**
	 * Adds the key, value pair to the MinK structure
	 * @param x: Key to be added
	 * @param v: Value to be added
	 */
	public void add(Key x, Value v) {
		if (size < k) {
			size++;
			int i = siftUp(x);
			
			if(i >= size) {
				maxHeap.add(i, new Entry(x, v));
			} else {
				maxHeap.set(i, new Entry(x, v));
			}
			
		} else if(size == k && x.compareTo(getKth()) < 0) {
			maxHeap.set(1, new Entry(x, v));
			int i = siftDown(x);

			maxHeap.set(i, new Entry(x, v));
			//maxHeap.set(1, new Entry(x, v));
		}
	}
	
	/**
	 * Gives a list of the values, sorted by their key values in ascending order
	 * @return a list of Values in ascending key order
	 */
	public ArrayList<Value> list() {
		ArrayList<Entry> copy = new ArrayList<>();
		
		copy = (ArrayList<MinK<Key, Value>.Entry>) maxHeap.clone();
		copy.remove(0);
		Collections.sort(copy);
		
		ArrayList<Value> result = new ArrayList<>(size);
		for (Entry e: copy) {
			result.add(e.getVal());
		}
		
		return result; 
	}
	
	/**
	 * Sifts entry up the heap to its proper location
	 */
	private int siftUp(Key x) {
		int i = size;
		
		while(i > 1 && (x.compareTo(maxHeap.get(parent(i)).key)) > 0) {
			if(i >= size) {
				maxHeap.add(i, maxHeap.get(parent(i)));
			} else {
				maxHeap.set(i, maxHeap.get(parent(i)));
			}
			
			i = parent(i);
		}
		
		return i;
	}
	
	/**
	 * Sifts entry down the heap to its proper location
	 */
	private int siftDown(Key x) {
		int i = 1;
		int u = 0,v = 0;
		
		while (left(i) != -1) {
			u = left(i);
			v = right(i);
			
			if(v != -1 && (maxHeap.get(v).getKey().compareTo(maxHeap.get(u).getKey())) > 0) {
				u = v;
			}
			
			if((maxHeap.get(u).getKey().compareTo(x)) > 0) {
				if(i >= size) {
					maxHeap.add(i, maxHeap.get(u));
				} else {
					maxHeap.set(i, maxHeap.get(u));
				}
				
				i = u;
			} else {
				break;
			}
		}
		
		return i;
	}
	
	/**
	 * Returns the parent of i
	 * @param i: the entry in the heap whose parent is being returned 
	 * @return: the index of the parent, otherwise -1 if there is no parent
	 */
	private int parent(int i) {
		if(i >= 2) {
			return i/2;
		} else {
			return -1;
		}
	}
	
	/**
	 * Returns the left child of i
	 * @param i: the entry in the heap whose left child is being returned 
	 * @return: the index of the left child, otherwise -1 if there is no left child
	 */
	private int left(int i) {
		if(size >= 2 * i) {
			return 2 * i;
		} else {
			return -1;
		}
	}
	
	/**
	 * Returns the right child of i
	 * @param i: the entry in the heap whose right child is being returned 
	 * @return: the index of the right child, otherwise -1 if there is no right child
	 */
	private int right(int i) {
		if(size >= ((2 * i) + 1)) {
			return (2 * i) + 1;
		} else {
			return -1;
		}
	}
}
