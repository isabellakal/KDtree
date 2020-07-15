import java.util.ArrayList;
import java.util.Iterator;

//import KDTree.KDNode;
public class KDTree implements Iterable<Datum>{ 

	KDNode 		rootNode;
	int    		k; 
	int			numLeaves;
	
	// constructor

	public KDTree(ArrayList<Datum> datalist) throws Exception {

		Datum[]  dataListArray  = new Datum[ datalist.size() ]; 

		if (datalist.size() == 0) {
			throw new Exception("Trying to create a KD tree with no data");
		}
		else
			this.k = datalist.get(0).x.length;

		int ct=0;
		for (Datum d :  datalist) {
			dataListArray[ct] = datalist.get(ct);
			ct++;
		}
		
	//   Construct a KDNode that is the root node of the KDTree.

		rootNode = new KDNode(dataListArray);
	}
	
	//   KDTree methods
	
	public Datum nearestPoint(Datum queryPoint) {
		return rootNode.nearestPointInNode(queryPoint);
	}
	

	public int height() {
		return this.rootNode.height();	
	}

	public int countNodes() {
		return this.rootNode.countNodes();	
	}
	
	public int size() {
		return this.numLeaves;	
	}

	//-------------------  helper methods for KDTree   ------------------------------

	public static long distSquared(Datum d1, Datum d2) {

		long result = 0;
		for (int dim = 0; dim < d1.x.length; dim++) {
			result +=  (d1.x[dim] - d2.x[dim])*((long) (d1.x[dim] - d2.x[dim]));
		}
		// if the Datum coordinate values are large then we can easily exceed the limit of 'int'.
		return result;
	}

	public double meanDepth(){
		int[] sumdepths_numLeaves =  this.rootNode.sumDepths_numLeaves();
		return 1.0 * sumdepths_numLeaves[0] / sumdepths_numLeaves[1];
	}
	
	class KDNode { 

		boolean leaf;
		Datum leafDatum;           //  only stores Datum if this is a leaf
		
		//  the next two variables are only defined if node is not a leaf

		int splitDim;      // the dimension we will split on
		int splitValue;    // datum is in low if value in splitDim <= splitValue, and high if value in splitDim > splitValue  

		KDNode lowChild, highChild;   //  the low and high child of a particular node (null if leaf)
		  //  You may think of them as "left" and "right" instead of "low" and "high", respectively

		KDNode(Datum[] datalist) throws Exception{

			/*
			 *  This method takes in an array of Datum and returns 
			 *  the calling KDNode object as the root of a sub-tree containing  
			 *  the above fields.
			 */

			//	
			int numOfCoord = datalist.length;
			//Datum[] highChildTree = new Datum[numOfCoord];
			//Datum[] lowChildTree = new Datum[numOfCoord];
			int minKeep=0;
			int maxKeep=0;
			int range=0;
			//boolean duplicates = false;

			//BASE CLAUSE: when datum array contains only one point (size 1)
			if(numOfCoord==1) {
				leaf = true;
				leafDatum = datalist[0];
				numLeaves++;

				//set splitDim or splitValue = 0?
				highChild = null;
				lowChild = null;
			}else {

				//iterate through dimensions after iterating through points (inner for loop)
				for (int i=0;i<k;i++){

					//initially set min and max to first datum's first dimension value of datumlist
					int min = datalist[0].x[i];
					int max = datalist[0].x[i];
					
					int rangeTemp;
					
					//iterate through the points while keeping same dimension
					for(int j=0;j<numOfCoord;j++) {

						if(datalist[j].x[i] < min) {
							min = datalist[j].x[i];
						}//perhaps take out else 
						if(datalist[j].x[i] > max) {
							max = datalist[j].x[i];
						}


					}
					
					rangeTemp = max - min;
					if(rangeTemp> range) {
						range = rangeTemp;
						minKeep = min;
						maxKeep = max;
						splitDim = i;
						//splitValue = Math.floorDiv((maxKeep+minKeep), 2);
					}

				}
				
				splitValue = Math.floorDiv((maxKeep+minKeep), 2);
				
				
				if(minKeep==maxKeep) {
					//duplicates = true;
					Datum[] dupDataList = new Datum[1];
					dupDataList[0] = datalist[0];
					
					leaf = true;
					leafDatum = datalist[0];
					numLeaves++;
					highChild = null;
					lowChild = null;
					return;
				}
				 
				
				ArrayList<Datum> lowChildArray = new ArrayList<Datum>();
				ArrayList<Datum> highChildArray = new ArrayList<Datum>();
				
				for(int i=0;i<numOfCoord;i++) {
					if (datalist[i].x[splitDim] <= splitValue) {
						lowChildArray.add(datalist[i]);
					}
					if (datalist[i].x[splitDim] > splitValue) {
						highChildArray.add(datalist[i]);
					}
				}
				
				//copy low and high child array lists into arrays
			
				Datum[] highChildTree = new Datum[highChildArray.size()];
				Datum[] lowChildTree = new Datum[lowChildArray.size()];
				
				for(int i=0;i<highChildTree.length;i++) {
					highChildTree[i] = highChildArray.get(i);
				}
				for(int i=0;i<lowChildTree.length;i++) {
					lowChildTree[i] = lowChildArray.get(i);
				}


				//call recursively on lowChild and highChild until you reach base case 
				this.lowChild = new KDNode(lowChildTree);
				this.highChild = new KDNode(highChildTree);

			}
			//   

		}
		
		//HELPER METHOD 
				public Datum checkSides(Datum nearestPoint,Datum otherSideNearestPoint,Datum queryPoint){
					if(distSquared(nearestPoint,queryPoint)<distSquared(otherSideNearestPoint,queryPoint))
					{
						return  nearestPoint;
					}
					return otherSideNearestPoint;
				}

		public Datum nearestPointInNode(Datum queryPoint) {
			Datum nearestPoint, nearestPoint_otherSide;

			//   ADD YOUR CODE BELOW HERE (HELPER METHOD (checkSides) above 

			
			Datum pointReturn=null;

			if(this.leaf == false){


				if(this.lowChild!=null && queryPoint.x[this.splitDim] <= this.splitValue)
				{
					nearestPoint=this.lowChild.nearestPointInNode(queryPoint);
					if(Math.pow(queryPoint.x[this.splitDim]-this.splitValue,2)> distSquared(nearestPoint,queryPoint))
					{
						pointReturn = nearestPoint;
					}else{
						nearestPoint_otherSide = this.highChild.nearestPointInNode(queryPoint);
						pointReturn = checkSides(nearestPoint,nearestPoint_otherSide,queryPoint);
					}
					return pointReturn;

				}else{
					if(this.highChild!=null)
					{
						nearestPoint=this.highChild.nearestPointInNode(queryPoint);
						if(Math.pow(queryPoint.x[this.splitDim]-this.splitValue,2)>(distSquared(nearestPoint,queryPoint)))
						{
							pointReturn=nearestPoint;
						}else {
							nearestPoint_otherSide = this.lowChild.nearestPointInNode(queryPoint);
							pointReturn = checkSides(nearestPoint,nearestPoint_otherSide,queryPoint);
						}
						return  pointReturn;
					}
				}
			}



			if(this.leaf == true) {
				return this.leafDatum;
			}
			return pointReturn;

		}

		//   



		// -----------------  KDNode helper methods (useful for debugging) -------------------

		public int height() {
			if (this.leaf) 	
				return 0;
			else {
				return 1 + Math.max( this.lowChild.height(), this.highChild.height());
			}
		}

		public int countNodes() {
			if (this.leaf)
				return 1;
			else
				return 1 + this.lowChild.countNodes() + this.highChild.countNodes();
		}

		/*  
		 * Returns a 2D array of ints.  The first element is the sum of the depths of leaves
		 * of the subtree rooted at this KDNode.   The second element is the number of leaves
		 * this subtree.    Hence,  I call the variables  sumDepth_size_*  where sumDepth refers
		 * to element 0 and size refers to element 1.
		 */
				
		public int[] sumDepths_numLeaves(){
			int[] sumDepths_numLeaves_low, sumDepths_numLeaves_high;
			int[] return_sumDepths_numLeaves = new int[2];
			
			/*     
			 *  The sum of the depths of the leaves is the sum of the depth of the leaves of the subtrees, 
			 *  plus the number of leaves (size) since each leaf defines a path and the depth of each leaf 
			 *  is one greater than the depth of each leaf in the subtree.
			 */
			
			if (this.leaf) {  // base case
				return_sumDepths_numLeaves[0] = 0;
				return_sumDepths_numLeaves[1] = 1;
			}
			else {
				sumDepths_numLeaves_low  = this.lowChild.sumDepths_numLeaves();
				sumDepths_numLeaves_high = this.highChild.sumDepths_numLeaves();
				return_sumDepths_numLeaves[0] = sumDepths_numLeaves_low[0] + sumDepths_numLeaves_high[0] + sumDepths_numLeaves_low[1] + sumDepths_numLeaves_high[1];
				return_sumDepths_numLeaves[1] = sumDepths_numLeaves_low[1] + sumDepths_numLeaves_high[1];
			}	
			return return_sumDepths_numLeaves;
		}
		
	}

	public Iterator<Datum> iterator() {
		return new KDTreeIterator();
	}

	private class KDTreeIterator implements Iterator<Datum> {

		//  

		private ArrayList<Datum> datumList;
		int counter;

		public void inOrderTrav (KDNode node) {
			if (node.leaf==true) {
				datumList.add(node.leafDatum);
			}else {
				inOrderTrav(node.lowChild);
				inOrderTrav(node.highChild);
			}
		}

		KDTreeIterator(){
			datumList = new ArrayList<Datum>();
			counter = -1;
			inOrderTrav(rootNode);
		}

		public boolean hasNext() {
			return counter+1 < datumList.size();
		}

		public Datum next() {
			counter++;
			return datumList.get(counter);
		}

		//   ADD YOUR CODE ABOVE HERE

	}

}

