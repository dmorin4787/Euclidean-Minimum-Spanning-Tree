/**
 * @author David Morin
 * This class represents an extended kd-tree to store a set of points in 2D space.
 */

package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class XkdTree<LPoint extends LabeledPoint2D> {
	
	private int numPoints;
	private int bucketSize;
	private Rectangle2D bbox;
	private Node root;
	private ArrayList<String> list;
	
	/**
	 * Comparator class of type LPoint to sort the points by X value,
	 * followed by the Y value
	 */
	private class ByXThenY implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			/* compare pt1 and pt2 lexicographically by x then y */
			if(pt1.getPoint2D().getX() < pt2.getPoint2D().getX()) {
				return -1;
			}
			else if (pt1.getPoint2D().getX() > pt2.getPoint2D().getX()) {
				return 1;
			}
			else if (pt1.getPoint2D().getY() < pt2.getPoint2D().getY()) {
				return -1;
			}
			else if (pt1.getPoint2D().getY() > pt2.getPoint2D().getY()) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

	/**
	 * Comparator class of type LPoint to sort the points by Y value,
	 * followed by the X value
	 */
	private class ByYThenX implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			/* compare pt1 and pt2 lexicographically by y then x */
			if(pt1.getPoint2D().getY() < pt2.getPoint2D().getY()) {
				return -1;
			}
			else if (pt1.getPoint2D().getY() > pt2.getPoint2D().getY()) {
				return 1;
			}
			else if (pt1.getPoint2D().getX() < pt2.getPoint2D().getX()) {
				return -1;
			}
			else if (pt1.getPoint2D().getX() > pt2.getPoint2D().getX()) {
				return 1;
			}
			else {
				return 0;
			}
			
		}
	}
	
	/**
	 * Comparator class of type LPoint to sort the points by
	 * alphabetical order
	 */
	private class Lexicographical implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			/* compare pt1 and pt2 lexicographically by x then y */
			if(pt1.getLabel().compareTo(pt2.getLabel()) > 0) {
				return 1;
			}
			else if (pt1.getLabel().compareTo(pt2.getLabel()) < 0) {
				return -1;
			}
			else {
				return 0;
			}
		}
	}
	
	private abstract class Node { // generic node (purely abstract)
		abstract LPoint find(Point2D pt); // find helper - abstract
		abstract Node bulkInsert(ArrayList<LPoint> pts);
		abstract void list();
		abstract LPoint nearestNeighbor(Point2D center, LPoint best, Rectangle2D cell);
		abstract Node delete(Point2D pt);
		abstract void kNearestNeighbor(Point2D center, int k, Rectangle2D cell, MinK<Double, LPoint> minK);
	}
	
	/**
	 * Represents an internal node of the extended kd-tree
	 */
	private class InternalNode extends Node {
		int cutDim; // the cutting dimension (0 = x, 1 = y)
		double cutVal; // the cutting value
		Node left, right; // children
		
		/**
		 * Constructor
		 */
		public InternalNode(int cutDim, double cutVal, Node left, Node right) {
			this.cutDim = cutDim;
			this.cutVal = cutVal;
			this.left = left;
			this.right = right;
		}
		
		/**
		 * Helper method for find(Point2D q) that traverses an internalNode to find
		 * if q occurs within the tree
		 * @param q: the point that is being searched for within the tree
		 */
		LPoint find(Point2D q) { 
			
			if(q.get(cutDim) < cutVal) {
				return left.find(q);
			} 
			else if(q.get(cutDim) > cutVal) {
				return right.find(q);
			} 
			else {
				LPoint result;
				
				result = left.find(q);
				
				if(result != null) {
					return result;
				} 
				else {
					result = right.find(q);
					
					if(result != null) {
						return result;
					}
				}
				
				return null;
			}
		}
		
		/**
		 * Helper method for inserting a list of points into an internal
		 * node of an XkdTree. 
		 * @param pts: the ArrayList of LPoints being added into the XkdTree
		 * @return: method returns the updated Node once the insertion is completed
		 */
		Node bulkInsert(ArrayList<LPoint> pts) {
			if(cutDim == 0) {
				Collections.sort(pts, new ByXThenY());
			}
			else {
				Collections.sort(pts, new ByYThenX());
			}
			
			int index = pts.size() - 1;
			
			for(int i = 0; i < pts.size(); i++) {
				if(pts.get(i).getPoint2D().get(cutDim) >= cutVal) {
					index = i;
					break;
				}
			}
			
			if(index == pts.size() - 1) {
				if (pts.get(index).getPoint2D().get(cutDim) < cutVal) {
					left = left.bulkInsert(pts);
				} 
				else {
					ArrayList<LPoint> leftList =  new ArrayList<>(pts.subList(0, index));
					ArrayList<LPoint> rightList = new ArrayList<>(pts.subList(index, pts.size()));
					
					if(!leftList.isEmpty()) {
						left = left.bulkInsert(leftList);
					}
						
					
					if(!rightList.isEmpty()) {
						right = right.bulkInsert(rightList);
					}
						
				}
			} 
			else {
				ArrayList<LPoint> leftList =  new ArrayList<>(pts.subList(0, index));
				ArrayList<LPoint> rightList = new ArrayList<>(pts.subList(index, pts.size()));
				
				if(!leftList.isEmpty()) {
					left = left.bulkInsert(leftList);
				}
					
				
				if(!rightList.isEmpty()) {
					right = right.bulkInsert(rightList);
				}
			}
			
			return this;
		}
		
		/**
		 * Helper method for adding internal nodes to the list 
		 * representation of the xKD tree. 
		 */
		void list() {
			
			String added;
			
			if(cutDim == 0) {
				added = "(x=" + cutVal + ")";
			} 
			else {
				added = "(y=" + cutVal + ")";
			}
			
			list.add(added);
			
			right.list();
			left.list();	
		}
		
		/**
		 * Helper method for nearestNeighbor that traverses the internal node 
		 * and recursively examines the relevant subtrees
		 * @param center: the query point of the nearest neighbor search
		 * @param best: the current closest LPoint in the xKDtree to center
		 * @param: cell: the corresponding bounding box of the current node
		 * @return: method returns the LPoint in the xKDtree closest in
		 * squared Euclidean distance to the query point (center)
		 */
		LPoint nearestNeighbor(Point2D center, LPoint best, Rectangle2D cell) {
			
			double cellDistance, bestDistance;
			
			Rectangle2D leftCell = cell.leftPart(cutDim, cutVal);
			Rectangle2D rightCell = cell.rightPart(cutDim, cutVal);
			
			if(center.get(cutDim) < cutVal) {
				best = left.nearestNeighbor(center, best, leftCell);
				
				if(best == null) {
					bestDistance = Double.POSITIVE_INFINITY;
				} 
				else {
					bestDistance = center.distanceSq(best.getPoint2D());
				}
				
				cellDistance = rightCell.distanceSq(center);
				
				if(cellDistance < bestDistance) {
					best = right.nearestNeighbor(center, best, rightCell);
				}
				
			}
			else {
				best = right.nearestNeighbor(center, best, rightCell);
				
				if(best == null) {
					bestDistance = Double.POSITIVE_INFINITY;
				} 
				else {
					bestDistance = center.distanceSq(best.getPoint2D());
				}
				
				cellDistance = leftCell.distanceSq(center);
				
				if(cellDistance < bestDistance) {
					best = left.nearestNeighbor(center, best, leftCell);
				}
			}
			
			return best;
		}
		
		/**
		 * Helper method for deleting points by traversing through internal nodes
		 * @param pt: pt being deleted
		 * @return the updated internal node after deletion
		 */
		Node delete(Point2D pt) {
			
			if (pt.get(cutDim) < cutVal) {
				left = left.delete(pt);
				
				if (left == null) {
					return right;
				}
			} else if(pt.get(cutDim) > cutVal) {
				right = right.delete(pt);
				
				if (right == null) {
					return left;
				}
			} else {
				left = left.delete(pt);
				
				if (left == null) {
					return right;
				}
				
				right = right.delete(pt);
				
				if (right == null) {
					return left;
				}
				
			}
			
			return this;
		
		}
		
		/**
		 * Helper method for k-NN search in internal nodes
		 * @param center:the point being queried upon
		 * @param k: the number of points in the k-NN search
		 * @param cell:the current cell for the internal node
		 * @param minK: the current result for the k-NN stored in the minK data structure  
		 */
		void kNearestNeighbor(Point2D center, int k, Rectangle2D cell, MinK<Double, LPoint> minK) {
			
			if(cell.distanceSq(center) > minK.getKth()) {
				return;
			}
			
			Rectangle2D leftCell = cell.leftPart(cutDim, cutVal);
			Rectangle2D rightCell = cell.rightPart(cutDim, cutVal);
			
			if(center.get(cutDim) < cutVal) {
				left.kNearestNeighbor(center, k, leftCell, minK);
				right.kNearestNeighbor(center, k, rightCell, minK);
			} else {
				right.kNearestNeighbor(center, k, rightCell, minK);
				left.kNearestNeighbor(center, k, leftCell, minK);
			}
		}
		
	}
	
	/**
	 * Represents an internal node of the extended kd-tree
	 */
	private class ExternalNode extends Node {
		ArrayList<LPoint> points; // the bucket
		
		/**
		 * Constructor
		 */
		public ExternalNode() {
			points = new ArrayList<>(bucketSize);
		}
		
		/**
		 * Helper method for find(Point2D q) that searches for a point in a external node
		 * and returns the associated LPoint if found and null otherwise
		 * @param q: the point that is being searched for within the tree
		 */
		LPoint find(Point2D pt) { 
			
			for(LPoint point: points) {
				if(point.getPoint2D().equals(pt)) {
					return point;
				}
			}
			
			return null;
		}
		
		/**
		 * Helper method for inserting a list of points into an external
		 * node of an XkdTree. 
		 * @param pts: the ArrayList of LPoints being added into the XkdTree
		 * @return: method returns the updated Node once the insertion is completed
		 */
		Node bulkInsert(ArrayList<LPoint> pts) {
			int cutDimension, median;
			double cutValue;
			
			points.addAll(pts);
			
			if(points.size() > bucketSize) {
				Rectangle2D newBox = new Rectangle2D();
				
				for(LPoint p: points) {
					newBox.expand(p.getPoint2D());
				}
				
				if(newBox.getWidth(0) >= newBox.getWidth(1)) {
					cutDimension = 0;
				} 
				else {
					cutDimension = 1;
				}
				
				if(cutDimension == 0) {
					Collections.sort(points, new ByXThenY());
				}
				else {
					Collections.sort(points, new ByYThenX());
				}
				
				median = (points.size())/2;
				
				if(points.size() % 2 == 0) {
					if(cutDimension == 0) {
						cutValue = (points.get(median).getX() 
								   + points.get(median - 1).getX()) / 2;
					} 
					else {
						cutValue = (points.get(median).getY() 
								   + points.get(median - 1).getY()) / 2;
					}
				}
				else {
					if(cutDimension == 0) {
						cutValue = points.get(median).getX();
					} 
					else {
						cutValue = points.get(median).getY();
					}
				}
				
				ArrayList<LPoint> leftList = new ArrayList<>(points.subList(0, median));
				ArrayList<LPoint> rightList = new ArrayList<>(points.subList(median, points.size()));
				
				InternalNode newInternal = new InternalNode(cutDimension, cutValue, 
										   new ExternalNode(), new ExternalNode());
				
				newInternal.left = newInternal.left.bulkInsert(leftList);
				newInternal.right = newInternal.right.bulkInsert(rightList);
				
				return newInternal;
			}
			
			return this;
		}
		
		/**
		 * Helper method for adding external nodes to list 
		 * representation of the xKD tree. 
		 */
		void list() {
			Collections.sort(points, new Lexicographical());
			
			String added = "[ ";
			
			for(LPoint p: points) {
				added += ("{" + p.toString() + "} ");
			}
			
			added += "]";
			
			list.add(added);
		}
		
		/**
		 * Helper method for nearestNeighbor that traverses the points in the
		 * external node to determine if any are closer than the current best,
		 * updating the best variable if so
		 * @param center: the query point of the nearest neighbor search
		 * @param best: the current closest LPoint in the xKDtree to center
		 * @param: cell: the corresponding bounding box of the current node
		 * @return: method returns the LPoint in the xKDtree closest in
		 */ 
		LPoint nearestNeighbor(Point2D center, LPoint best, Rectangle2D cell) {
			
			double bestDistance, tempDistance;
			
			if(best == null) {
				bestDistance = Double.POSITIVE_INFINITY;
			} 
			else {
				bestDistance = center.distanceSq(best.getPoint2D());
			}
			
			for(LPoint p: points) {
				tempDistance = center.distanceSq(p.getPoint2D());
				
				if (tempDistance < bestDistance) {
					best = p;
					bestDistance = tempDistance;
				}
			}
			
			return best;
		}
		
		/**
		 * Helper function for the delete function in external nodes
		 * @param:
		 * @param:
		 * @param
		 */
		Node delete(Point2D pt) {
			
			int counter = 0;
			
			for(LPoint point: points) {
				if(point.getPoint2D().equals(pt)) {
					break;
				}
				
				counter += 1;
			}
			
			if(counter < points.size()) {
				points.remove(counter);
			}
					
			
			if(points.isEmpty() && this != root) { //or numPoints != 1
				
				return null;
			}
			
			return this;
		}
		
		
		/**
		 * Helper function for k-NN in external nodes by adding all the points to the minK data
		 * structure
		 * @param center: point being queried upon
		 * @param cell: the cell of the current region of the external node
		 * @param minK: the data structure which stores the current result for the k-NN
		 */
		void kNearestNeighbor(Point2D center, int k, Rectangle2D cell, MinK<Double, LPoint> minK) {
			
			for(LPoint point: points) {
				minK.add(point.getPoint2D().distanceSq(center), point);
			}
		}
		
	}

	
	/**
	 * Initializes the XkdTree by setting the root to an empty ExternalNode and initializing
	 * each instance variable
	 * @param bucketSize: the maximum amount of points an ExternalNode can store
	 * @param bbox: the bounding box for the xkdTree
	 */
	public XkdTree(int bucketSize, Rectangle2D bbox) {
		numPoints = 0;
		this.bucketSize = bucketSize;
		this.bbox = bbox;
		
		
		root = new ExternalNode();
	}
	
	/**
	 * Removes all entries of the xkdTree
	 */
	public void clear() { 
		numPoints = 0;
		root = new ExternalNode();
	}
	
	/**
	 * @return the number of points in the tree
	 */
	public int size() {
		
		return numPoints; 
	}
	
	/**
	 * Determines whether a point, q, occurs within the tree
	 * @param q: the point that is being searched for within the tree
	 * @return: if the point occurs, the associated LPoint is returned, 
	 * otherwise null is returned
	 */
	public LPoint find(Point2D q) { 
		return root.find(q);
	}
	
	/**
	 * Inserts a single point into the xKDTree
	 * @param pt: the point being inserted
	 * @throws Exception: thrown when point is outside the bounding box
	 */
	public void insert(LPoint pt) throws Exception { 
		ArrayList<LPoint> addedPoint = new ArrayList<>();
		addedPoint.add(pt);
		bulkInsert(addedPoint);
	}
	
	/**
	 * Inserts an ArrayList of points into the xKDTree. 
	 * @param pts: the ArrayList<LPoint> of points being inserted
	 * @throws Exception: thrown any point is outside the bounding box
	 */
	public void bulkInsert(ArrayList<LPoint> pts) throws Exception {
		for(LPoint p: pts) {
			if(p.getPoint2D().getX() < bbox.getLow().getX() || p.getPoint2D().getX() > bbox.getHigh().getX()
			|| p.getPoint2D().getY() < bbox.getLow().getY() || p.getPoint2D().getY() > bbox.getHigh().getY()) {
						throw new Exception("Attempt to insert a point outside bounding box");
			}
		}
			
		root = root.bulkInsert(pts);
		numPoints += pts.size();
	}
	
	/**
	 * Builds an ArrayList<String> representation of the XkdTree.
	 * @return: an ArrayList<String> representation of the XkdTree
	 */
	public ArrayList<String> list() {
		list = new ArrayList<>();
		root.list(); 
		return list;
	}
	
	/**
	 * Performs a nearest neighbor search upon a query point by computing
	 * the point closest in the tree to the query point by means of squared Euclidean
	 * distance
	 * @param center: the point being queried upon in the nearest neighbor search
	 * @return: null if the tree is empty, otherwise returns the LPoint closest to
	 * center in the XkdTree
	 */
	public LPoint nearestNeighbor(Point2D center) { 
		if (numPoints == 0) {
			return null;
		} 
		else {
			return root.nearestNeighbor(center, null, bbox); 
		}
		
	}

	/**
	 * Deletes the point from the kd tree
	 * @param pt: the point to be deleted
	 * @Exception: If the point is not in the kd-tree, and exception is thrown
	 * center in the XkdTree
	 */
	public void delete(Point2D pt) throws Exception {
		if(find(pt) == null) {
			throw new Exception("Deletion of nonexistent point");
		} else {
			root = root.delete(pt);
			numPoints--;
		}
	}
	
	/**
	 * Computes the k nearest neighbors of the point passed into the method
	 * @param center: the point being queried upon in the k nearest neighbor search
	 * @param k: the number of points being queried upon
	 * @return: ArrayList of LPoints listing the k-nearest neighbors of center in
	 * sorted order of increasing distance
	 * center in the XkdTree
	 */
	public ArrayList<LPoint> kNearestNeighbor(Point2D center, int k) { 
		if (numPoints == 0) {
			return new ArrayList<>();
		} 
		else {
			MinK<Double, LPoint> minK = new MinK<>(k, Double.MAX_VALUE);
			root.kNearestNeighbor(center, k, bbox, minK); 
			
			return minK.list();
		}
	}
}

