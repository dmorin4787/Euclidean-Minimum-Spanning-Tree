/**
 * @author David Morin
 * This class defines a data structure to assist
 * with implementing the k-capacitated facility location problem
 */

package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;

public class KCapFL<LPoint extends LabeledPoint2D> {
	
	private int capacity;
	private XkdTree<LPoint> kdTree;
	private LeftistHeap<Double, ArrayList<LPoint>> heap;
	
	/**
	 * Constructor
	 * @param capacity: Maximum capacity of any service center
	 * @param bucketSize: the maximum bucket size for the kdTree 
	 * @param bbox: the bounding box for the kdTree
	 */
	public KCapFL(int capacity, int bucketSize, Rectangle2D bbox) { 
		this.capacity = capacity;
		this.kdTree = new XkdTree<>(bucketSize, bbox);
		this.heap = new LeftistHeap<>();
	}
	
	/**
	 * Clears the data structure
	 */
	public void clear() {
		kdTree.clear();
		heap.clear();
	}
	
	/**
	 * Initializes the structure by building the kdTree and heap
	 * @param pts: the points to be added to the kdTree
	 * @throws Exception if the points ArrayList is empty or its size is not
	 * evenly divisible by the capacity
	 */
	public void build(ArrayList<LPoint> pts) throws Exception { 
		if(pts.isEmpty() || (pts.size() % capacity) != 0) {
			throw new Exception("Invalid point set size");
		}
		
		kdTree.bulkInsert(pts);
		
		//ArrayList<LPoint> temp = new ArrayList<>();
		double distance = 0;
		
		for(LPoint p: pts) {
			ArrayList<LPoint> temp = kdTree.kNearestNeighbor(p.getPoint2D(), capacity);
			
			distance = p.getPoint2D().distanceSq(temp.get(temp.size() - 1).getPoint2D());
			
			heap.insert(distance, temp);
		}
	}
	
	/**
	 * Performs a single step of the greedy algorithm
	 * @return the cluster if kdTree is not empty or null if there are no more clusters
	 */
	public ArrayList<LPoint> extractCluster() {
		if(kdTree.size() == 0) {
			return null; 
		}
		
		boolean clusterFound = false;
		ArrayList<LPoint> result = new ArrayList<>();
		
		while (!clusterFound) {
			try {
				boolean allIn = true;
				
				ArrayList<LPoint> minList = heap.extractMin();
				
				for(LPoint p: minList) {
					if (kdTree.find(p.getPoint2D()) == null) {
						allIn = false;
						break;
					}
				}
				
				if(allIn) {
					for(LPoint p: minList) {
						kdTree.delete(p.getPoint2D());
					}
					
					clusterFound = true;
					result = minList;
				} else if(kdTree.find(minList.get(0).getPoint2D()) != null) {
					ArrayList<LPoint> temp = new ArrayList<>();
					temp = kdTree.kNearestNeighbor(minList.get(0).getPoint2D(), capacity);
					double distance = temp.get(0).getPoint2D().distanceSq(temp.get(temp.size() - 1).getPoint2D());
					heap.insert(distance, temp);
				}
			} catch (Exception e) {
				System.out.println("Not supposed to be here");
			}
		}
		
		return result;
	}
	
	/**
	 * Invokes list operation on kdTree
	 * @return list representation of XkdTree
	 */
	public ArrayList<String> listKdTree() { 
		return kdTree.list();
	}
	
	/**
	 * Invokes list operation on heap
	 * @return list representation of Leftist Heap
	 */
	public ArrayList<String> listHeap() { 
		return heap.list();
	}
}
