import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

//Represents a node in a Fibonacci heap 
class FibonacciNode {
	int degree;
	FibonacciNode child, left, right, parent;
	public int count;
	String hashtag;

	Boolean childCut; // default = false

	public FibonacciNode(String hashtag, int count) {
		this.count = count;
		this.hashtag = hashtag;
	}

	void addChild(FibonacciNode node) { /*
										 * add child node 'node' to this node.
										 * Used in removeMax, where one node can
										 * be a child of another node
										 */
		if (this.degree == 0) {
			this.child = node;
			node.right = node;
			node.left = node;
		} else {
			FibonacciNode child1 = this.child;
			node.left = child1.left;
			node.right = child1;
			child1.left.right = node;
			child1.left = node;

		}
		this.degree = this.degree + 1;
		node.parent = this;
		node.childCut = false; /* childCut is set to false */

	}
}

// Represents a Fibonacci heap

public class FibonacciHeap {

	private FibonacciNode maxNode;

	// add a node to the heap
	public FibonacciNode add(FibonacciNode node) {
		this.topLevel(node); /* add this root node to the heap */
		return node;
	}

	// add node to the top level

	public void topLevel(FibonacciNode node) {
		if (maxNode == null) {
			maxNode = node;
			maxNode.right = maxNode;
			maxNode.left = maxNode;
		} else {// Add node as the sibling of max
			node.left = maxNode.left;
			node.right = maxNode;
			maxNode.left.right = node;
			maxNode.left = node;
			if (node.count > maxNode.count)
				maxNode = node; // change max if it's necessary
		}
	}

	// increaseKey would increase the count of a node if the node is already in
	// the heap
	public void increaseKey(FibonacciNode node, int value) {
		if (node == null) {
			return;
		}
		FibonacciNode parent = node.parent;
		if (node.parent == null) {
			node.count += value;
			if (node != maxNode && node.count > maxNode.count)
				maxNode = node;
		} else {
			// when increased value is less than parent's count, nothing else
			// needed to be done.

			if ((value != -1) && (node.count + value <= node.parent.count)) {
				node.count += value;
			} else {
				if (node.parent.degree == 1) {
					// if parent only has one child, set the child pointer to
					// null
					node.parent.child = null;
				} else if (node.parent.degree > 1) {
					// Remove node from its sibling list
					node.left.right = node.right;
					node.right.left = node.left;
					// We have to have the parent point to the right child if
					// this child
					// is the one pointed to by the parent
					if (node.parent.child == node)
						node.parent.child = node.right;
				} else {
					return;
				}
				node.parent.degree--;
				node.left = null;
				node.right = null;
				node.parent = null;
				node.childCut = false;

				if (value != -1)
					node.count += value;
				topLevel(node); // add node to the top of the heap
				cascadingCut(parent); // performs a cascadingCut operation
			}
		}
	}

	/*
	 * Performs a cascading cut operation. Cuts this from its parent and then
	 * does the same for its parent, and so on up the tree.if the node
	 * encountered has Boolean value of childCut true.
	 * 
	 */
	private void cascadingCut(FibonacciNode node) {
		if (node == null || node.parent == null)// modification
			return;// modification
		if (node.childCut == false && node.parent != null)
			node.childCut = true;
		else if (node.childCut == true && node.parent != null) {
			increaseKey(node, -1);
		}

	}

	// removeMax: remove the node with max count in Fibonacci Heap */
	public FibonacciNode removeMax() {
		if (maxNode == null) {
			return null;
		}
		// All root nodes are stored in the queue
		Queue<FibonacciNode> q = new LinkedList<FibonacciNode>();
		FibonacciNode max_node = maxNode;
		FibonacciNode child = max_node.child;
		for (int i = 0; i < max_node.degree; i++) {

			FibonacciNode tra = child.right;
			child.childCut = false;
			child.parent = null;
			child.left = null;
			child.right = null;
			q.add(child);

			child = tra;
		}

		/* Add all the siblings of max node to the queue */
		FibonacciNode temp = maxNode.right;

		while ((temp != null) && (temp != maxNode)) {
			q.add(temp);
			temp = temp.right;
		}

		maxNode = null;
		merge(q); // Pair wise combine all the root nodes in the queue
		return max_node;
	}

	// Pairwise combine all root nodes with same degree in the queue
	private void merge(Queue<FibonacciNode> q) {
		HashMap<Integer, FibonacciNode> map = new HashMap<Integer, FibonacciNode>();
		int maxDegree = 0;
		while (!q.isEmpty()) {
			FibonacciNode node1 = q.poll();
			if (node1 != null) {
				while (map.containsKey(node1.degree)) {
					// If the hashmap already contains nodes with same degree,
					// combine the two
					FibonacciNode node2 = map.get(node1.degree);
					map.remove(node1.degree);
					if (node1.count >= node2.count) {
						node1.addChild(node2);
					} else {
						node2.addChild(node1);
						node1 = node2;
					}
				}
				map.put(node1.degree, node1);
				// put the node in the map after merging
				if (node1.degree > maxDegree)
					maxDegree = node1.degree;
			}
		}

		for (int i = 0; i <= maxDegree; i++) {
			if (map.containsKey(i)) {
				topLevel(map.get(i)); /* add root node to the heap */
				map.remove(i);
			}
		}
	}
}
