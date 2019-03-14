///////////////////////////////////////////////////////////////////////////////
//                   
// Main Class File:  HW3.java
// File:             DecisionTreeImpl.java
// Semester:         CS 540 Spring 2018
//
// Author:           Meiliu Wu, mwu233@wisc.edu
// Lecturer's Name:  Chuck Dyer
//
///////////////////////////////////////////////////////////////////////////////

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.lang.Math;

/**
 * Fill in the implementation details of the class DecisionTree using this file.
 * Any methods or secondary classes that you want are fine but we will only
 * interact with those methods in the DecisionTree framework.
 * 
 * I add code for the 1 member and 4 methods specified below.
 * Self-handle the problem when train data set input file has no instance or no attributes 
 * (that is, List<Instance> instances or List<String> attributes is null).
 * 
 * See DecisionTree for a description of default methods.
 * 
 * <p>Bugs: if the train data set input file does not contain labels, it will have 
 * java.lang.StringIndexOutOfBoundsException: String index out of range: 2
	at java.lang.String.substring(Unknown Source)
	at HW3.createDataSet(HW3.java:88)
 * 
 * @author Meiliu Wu
 */
public class DecisionTreeImpl extends DecisionTree {
	private DecTreeNode root;
	// ordered list of class labels
	private List<String> labels;
	// ordered list of attributes
	private List<String> attributes;
	// map to ordered discrete values taken by attributes
	private Map<String, List<String>> attributeValues;
	// map for getting the index
	private HashMap<String, Integer> label_inv;
	private HashMap<String, Integer> attr_inv;

	/**
	 * Answers static questions about decision trees.
	 */
	DecisionTreeImpl() {
		// no code necessary this is void purposefully
	}

	/**
	 * Build a decision tree given only a training set.
	 * 
	 * @param train:
	 *            the training set
	 */
	DecisionTreeImpl(DataSet train) {

		this.labels = train.labels;
		this.attributes = train.attributes;
		this.attributeValues = train.attributeValues;
		// TODO: Homework requirement, learn the decision tree here
		// Get the list of instances via train.instances
		// You should write a recursive helper function to build the tree
		//
		// this.labels contains the possible labels for an instance
		// this.attributes contains the whole set of attribute names
		// train.instances contains the list of instances
		
		// call the DecisionTreeLearning method as recursive helper function
		root = DecisionTreeLearning(train.instances,this.attributes,train.instances);
	}
	
	/**
	 * Build a decision tree given current node instances, left attributes to be used, parentNode's instances.
	 * 
	 * @param train:
	 *            the training set
	 */
	public DecTreeNode DecisionTreeLearning(List<Instance> instances, List<String> attributes, List<Instance> parentInstances) {
		
		// if List<Instance> instances is null, there is no label
		if (instances == null) {
			return new DecTreeNode(null,null,null,true);
		}
		
		int attrSize = attributes.size();
		int insSize = instances.size();
		
		// if examples is empty then return PLURALITY-VALUE(parent examples)
		if (insSize == 0) {
			// leaf node: attribute is set null, parentAttributeValue will be set after this tree is built, terminal reaches (true)
			return new DecTreeNode(majorityLabel(parentInstances),null,null,true);
		} 
		// if all examples have the same classification then return the classification
		else if (sameLabel(instances)) {
			// leaf node: attribute is set null, parentAttributeValue will be set after this tree is built, terminal reaches (true)
			return new DecTreeNode(instances.get(0).label,null,null,true);
		} 
		// if attributes is empty then return PLURALITY-VALUE(examples)
		else if (attrSize == 0) {
			// leaf node: attribute is set null, parentAttributeValue will be set after this tree is built, terminal reaches (true)
			return new DecTreeNode(majorityLabel(instances),null,null,true);
		} 
		else {
			// to get the best attribute with maximum info gain: best_attribute(examples, attributes)
			String bestAttribute_sofar = attributes.get(0);
			double maxInfoGain_sofar = InfoGain(instances, bestAttribute_sofar);
			
			// Iterate all usable attributes for this current node to get the bestAttribute that has maxInfoGain
			for (int i = 1; i < attrSize; i++) {
				String attr_name = attributes.get(i);
				double infoGain = InfoGain(instances, attr_name);
				if (infoGain > maxInfoGain_sofar) {
					maxInfoGain_sofar = infoGain;
					bestAttribute_sofar = attr_name;
				}
			}
			
			// tree = create-node with bestAttribute; majority label for internal nodes
			DecTreeNode treeRoot = new DecTreeNode(majorityLabel(instances), bestAttribute_sofar, null, false);
			
			// for bestAttribute, get the # of attribute values
			int attriValSize = attributeValues.get(bestAttribute_sofar).size();
			
			// for bestAttribute, get the index of it in the tree
			int attrIndex = getAttributeIndex(bestAttribute_sofar);
			
			// remove bestAttribute from the list that has left attributes to be used
			List<String> attrSub = new ArrayList<String>(attributes);
			attrSub.remove(bestAttribute_sofar);
			
			// for each value v of bestAttribute do
			for (int i = 0; i < attriValSize; i++) {
				String attriVal = attributeValues.get(bestAttribute_sofar).get(i);
				
				// create a List storing subset instances with this particular bestAttribute value
				List<Instance> subsetInstances = new ArrayList<Instance>();
				for (Instance ins : instances) {
					if (ins.attributes.get(attrIndex).equals(attriVal)) {
						subsetInstances.add(ins);
					}
				}
				
				// subtree = buildtree(subset instances, attributes left - {bestAttribute}, parent's instances)
				DecTreeNode subtreeRoot = DecisionTreeLearning(subsetInstances,attrSub,instances);
				// the subtree root node's parentAttributeValue is set to be this particular bestAttribute value
				subtreeRoot.parentAttributeValue = attriVal;
				
				// add arc from tree to subtree
				treeRoot.addChild(subtreeRoot);	
			}
			// return tree
			return treeRoot;
		}
	}

	boolean sameLabel(List<Instance> instances) {
		// Suggested helper function
		// returns if all the instances have the same label
		// labels are in instances.get(i).label
		// TODO

		int instancesSize = instances.size();

		// use the first instance to check the others
		String checkLabel = instances.get(0).label;
		for (int i = 1; i < instancesSize; i++) {
			String insLabel = instances.get(i).label;
			if (!checkLabel.equals(insLabel)) {
				return false;
			}
		}
		return true;
	}

	String majorityLabel(List<Instance> instances) {
		// Suggested helper function
		// returns the majority label of a list of examples
		// TODO
		
		int instancesSize = instances.size();
		int labelSzie = labels.size();
		int[] labels_count = new int[labelSzie];
		
		if (instancesSize == 0) {
			return "No instance";
		}
		// count # of each label
		for (int i = 0; i < instancesSize; i++) {
			int labelIndex = getLabelIndex(instances.get(i).label);
			labels_count[labelIndex]++;
		}
		
		// get the index of the label with max count
		int maxLabelCountSofar = labels_count[0];
		int index_maxLabelCount = 0;
		for (int j = 1; j < labelSzie; j++) {
			// If ties occur when determining the majority class,
			// choose the one with the smallest index in List<String> labels.
			if (maxLabelCountSofar < labels_count[j]) {
				maxLabelCountSofar = labels_count[j];
				index_maxLabelCount = j;
			}
		}
		return labels.get(index_maxLabelCount);

	}

	double entropy(List<Instance> instances) {
		// Suggested helper function
		// returns the Entropy of a list of examples
		// TODO
		
		int instancesSize = instances.size();
		int labelSzie = labels.size();
		int[] labels_count = new int[labelSzie];
		
		// count # of each label
		for (int i = 0; i < instancesSize; i++) {
			int labelIndex = getLabelIndex(instances.get(i).label);
			labels_count[labelIndex]++;
		}
		
		double entropy = 0.0;
		for (int k = 0; k < labelSzie; k++) {
			// p (each label)
			double label_k_prob = 1.0 * labels_count[k] / instancesSize;
			// if p (each label) = 0, skip this label in the sum calculation
			if (label_k_prob == 0.0) {
				continue;
			}
			double log2_label_k_prob = Math.log(label_k_prob) / Math.log(2);
			entropy -= label_k_prob * log2_label_k_prob;
		}

		return entropy;
	}

	double conditionalEntropy(List<Instance> instances, String attr) {
		// Suggested helper function
		// returns the conditional entropy of a list of examples, given the attribute
		// attr
		// TODO
		
		int labelSize = labels.size();
		int instancesSize = instances.size();

		int attrIndex = getAttributeIndex(attr);
		int attrValSize = this.attributeValues.get(attr).size();
		
		// count # of each attrVal of this attr
		int[] attrValCount = new int[attrValSize];
		// count # of each label for each attrVal
		int[][] labelsCount_attrValCount = new int[attrValSize][labelSize];
		
		for (int i = 0; i < instancesSize; i++) {
			Instance ins = instances.get(i);
			String ins_attr_value = ins.attributes.get(attrIndex);
			int attrValIndex = getAttributeValueIndex(attr, ins_attr_value);
			int labelIndex = getLabelIndex(instances.get(i).label);

			attrValCount[attrValIndex]++;
			labelsCount_attrValCount[attrValIndex][labelIndex]++;

		}
		
		double condiEntropy = 0.0;
		for (int i = 0; i < attrValSize; i++) {
			// the p (this attrVal in this attr)
			double attrVal_prob = 1.0 * attrValCount[i] / instancesSize;
			// if p (this attrVal in this attr) = 0, skip this attrVal in the sum calculation
			if (attrVal_prob == 0.0) {
				continue;
			}
			double H_YGivenAttrVal = 0.0;
			for (int l = 0; l < labelSize; l++) {
				// the p (this label in this attrVal)
				double label_attrVal_prob = 1.0 * labelsCount_attrValCount[i][l] / attrValCount[i];
				// if p (this label in this attrVal) = 0, skip this label in this attrVal in the sum calculation
				if (label_attrVal_prob == 0.0) {
					continue;
				}
				double log2_label_attrVal_prob = Math.log(label_attrVal_prob) / Math.log(2);
				H_YGivenAttrVal -= label_attrVal_prob * log2_label_attrVal_prob;
			}
			condiEntropy += attrVal_prob * H_YGivenAttrVal;
		}

		return condiEntropy;
	}

	double InfoGain(List<Instance> instances, String attr) {
		// Suggested helper function
		// returns the info gain of a list of examples, given the attribute attr
		
		return entropy(instances) - conditionalEntropy(instances, attr);
	}

	@Override
	public String classify(Instance instance) {
		// TODO: Homework requirement
		// The tree is already built, when this function is called
		// this.root will contain the learnt decision tree.
		// write a recusive helper function, to return the predicted label of instance
		
		if (this.root.label == null || this.attributes == null) {
			return "Input file is invalid. No Decision Tree is built.";
		}
		// call the classifyHelper method
		String predictedLabel = classifyHelper(instance.attributes, this.root);
		
		// return the predicted label of instance
		return predictedLabel;
	}
	
	public String classifyHelper(List<String> insAttributeValues, DecTreeNode currNode) {
		
		if(currNode.terminal == true) {
			return currNode.label;
		}
		else {
			// the index of current node's attribute
			int attrIndex = getAttributeIndex(currNode.attribute);
			// get the instance's attrVal
			String attrValue = insAttributeValues.get(attrIndex);
			// get this attrVal's index
			int attrValueIndex = getAttributeValueIndex(currNode.attribute, attrValue);
			// the children index for this node is same as this attrVal index of this node's attribute
			return classifyHelper(insAttributeValues, currNode.children.get(attrValueIndex));
		}
	}

	@Override
	public void rootInfoGain(DataSet train) {
		this.labels = train.labels;
		this.attributes = train.attributes;
		this.attributeValues = train.attributeValues;
		// TODO: Homework requirement
		// Print the Info Gain for using each attribute at the root node
		// The decision tree may not exist when this function is called.
		// But you just need to calculate the info gain with each attribute,
		// on the entire training set.
		
		List<Instance> instances = train.instances;
		// if input file is invalid:
		if (this.attributes == null || instances == null) {
			System.out.println("The input file is invalid.");
			return;
		}
		
		int attrSize = this.attributes.size();
		double[] attrs_infoGain = new double[attrSize];
		
		// for all attributes
		for (int i = 0; i < attrSize; i++) {
			attrs_infoGain[i] = InfoGain(instances, this.attributes.get(i));
			// print out
			System.out.print(this.attributes.get(i) + " ");
			System.out.format("%.5f\n", attrs_infoGain[i]);
		}
		
	}

	@Override
	public void printAccuracy(DataSet test) {
		// TODO: Homework requirement
		// Print the accuracy on the test set.
		// The tree is already built, when this function is called
		// You need to call function classify, and compare the predicted labels.
		// List of instances: test.instances
		// getting the real label: test.instances.get(i).label

		// count # of true prediction / total # of instances in test set
		
		int testSize = test.instances.size();
		int correctCount = 0;
		for (int i = 0; i < testSize; i++) {
			Instance ins = test.instances.get(i);
			// if this.root.label == null is true, then there is no instance in train set at the very beginning. No Decision Tree is built.
			if (classify(ins).equals("Input file is invalid. No Decision Tree is built.")) {
				System.out.println("Input file is invalid. No Decision Tree is built.");
				return;
			}
			else if (classify(ins).equals(ins.label)) {
				correctCount++;
			}
		}
		
		double testAcc = 1.0 * correctCount / testSize;
		System.out.format("%.5f\n", testAcc);
	}

	@Override
	/**
	 * Print the decision tree in the specified format Do not modify
	 */
	public void print() {

		printTreeNode(root, null, 0);
	}

	/**
	 * Prints the subtree of the node with each line prefixed by 4 * k spaces. Do
	 * not modify
	 */
	public void printTreeNode(DecTreeNode p, DecTreeNode parent, int k) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < k; i++) {
			sb.append("    ");
		}
		String value;
		if (parent == null) {
			value = "ROOT";
		} else {
			int attributeValueIndex = this.getAttributeValueIndex(parent.attribute, p.parentAttributeValue);
			value = attributeValues.get(parent.attribute).get(attributeValueIndex);
		}
		sb.append(value);
		if (p.terminal) {
			sb.append(" (" + p.label + ")");
			System.out.println(sb.toString());
		} else {
			sb.append(" {" + p.attribute + "?}");
			System.out.println(sb.toString());
			for (DecTreeNode child : p.children) {
				printTreeNode(child, p, k + 1);
			}
		}
	}

	/**
	 * Helper function to get the index of the label in labels list
	 */
	private int getLabelIndex(String label) {
		if (label_inv == null) {
			this.label_inv = new HashMap<String, Integer>();
			for (int i = 0; i < labels.size(); i++) {
				label_inv.put(labels.get(i), i);
			}
		}
		return label_inv.get(label);
	}

	/**
	 * Helper function to get the index of the attribute in attributes list
	 */
	private int getAttributeIndex(String attr) {
		if (attr_inv == null) {
			this.attr_inv = new HashMap<String, Integer>();
			for (int i = 0; i < attributes.size(); i++) {
				attr_inv.put(attributes.get(i), i);
			}
		}
		return attr_inv.get(attr);
	}

	/**
	 * Helper function to get the index of the attributeValue in the list for the
	 * attribute key in the attributeValues map
	 */
	private int getAttributeValueIndex(String attr, String value) {
		for (int i = 0; i < attributeValues.get(attr).size(); i++) {
			if (value.equals(attributeValues.get(attr).get(i))) {
				return i;
			}
		}
		return -1;
	}
}
