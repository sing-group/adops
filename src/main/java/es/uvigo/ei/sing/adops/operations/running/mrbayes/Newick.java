/*-
 * #%L
 * ADOPS
 * %%
 * Copyright (C) 2012 - 2017 David Reboiro-Jato, Miguel Reboiro-Jato, Jorge Vieira, Florentino Fdez-Riverola, Cristina P. Vieira, Nuno A. Fonseca
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package es.uvigo.ei.sing.adops.operations.running.mrbayes;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Newick implements Cloneable {
	private String name;
	private double distance;
	private Newick parent;
	
	private final List<Newick> children;
	
	public Newick() {
		this("", Double.NaN);
	}
	
	public Newick(double distance) {
		this("", distance);
	}
	
	public Newick(String name) {
		this(name, Double.NaN);
	}
	
	public Newick(String name, double distance) {
		this(null, name, distance);
	}
	
	public Newick(Newick parent) {
		this(parent, "", Double.NaN);
	}
	
	public Newick(Newick parent, double distance) {
		this(parent, "", distance);
	}
	
	public Newick(Newick parent, String name) {
		this(parent, name, Double.NaN);
	}
	
	public Newick(Newick parent, String name, double distance) {
		if (distance < 0d)
			throw new IllegalArgumentException("distance must be greater than or equal to 0");
		this.distance = distance;
		this.name = name;
		this.parent = parent;
		this.children = new LinkedList<Newick>();
	}
	
	public void normalize() {
		this.normalize(this.sumDistances());
	}
	
	public void normalize(boolean inverse) {
		this.normalize(this.sumDistances(), inverse);
	}
	
	private void normalize(double normalization) {
		this.normalize(normalization, false);
	}
	
	private void normalize(double normalization, boolean inverse) {
		if (!Double.isNaN(normalization) && normalization > 0d) {
			if (!Double.isNaN(this.distance)) {
				this.distance /= normalization;
				
				if (inverse)
					this.distance = 1d - this.distance;
			}
			
			
			for (Newick child:this.children) {
				child.normalize(normalization);
			}
		}		
	}
	
	//TODO: change the name?
	public void relativize() {
		this.relativize(this.maxDistance());
	}
	
	private void relativize(double maxDistance) {
		if (!Double.isNaN(maxDistance) && maxDistance > 0d) {
			if (!Double.isNaN(this.distance))
				this.distance /= maxDistance;
			
			for (Newick child:this.children) {
				child.relativize(maxDistance);
			}
		}		
	}
	
	private double maxDistance() {
		double maxDistance = this.distance;
		
		for (Newick child:this.children) {
			double childMaxDistance = child.maxDistance();
			if (!Double.isNaN(childMaxDistance)) {
				if (Double.isNaN(maxDistance) || childMaxDistance > maxDistance)
					maxDistance = childMaxDistance;				
			}
		}
		
		return maxDistance;		
	}
	
	private double sumDistances() {
		double sumDistance = (Double.isNaN(this.distance))?0d:this.distance;
		
		for (Newick child:this.children) {
			sumDistance += child.sumDistances();
		}
		
		return sumDistance;
	}
	
	public void setParent(Newick parent) {
		this.parent = parent;
	}
	
	public Newick getParent() {
		return parent;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDistance(double value) {
		this.distance = value;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public boolean addChild(Newick node) {
		return this.children.add(node);
	}
	
	public boolean removeChild(Newick node) {
		return this.children.remove(node);
	}
	
	public List<Newick> getChildren() {
		return children;
	}
	
	public List<Newick> toPlainList() {
		return this.toPlainList(new LinkedList<Newick>());
	}
	
	public List<Newick> toPlainList(List<Newick> list) {
		list.add(this);
		
		for (Newick child:this.children) {
			child.toPlainList(list);
		}
		
		return list;
	}
	
	public List<Newick> toSortedList() {
		return this.toSortedList(true);
	}
	
	public List<Newick> toSortedList(boolean includeDistanceless) {
		final List<Newick> plainList = this.toPlainList();
		
		if (!includeDistanceless) {
			final List<Newick> distancelessList = new LinkedList<Newick>();
			
			for (Newick node:plainList) {
				if (Double.isNaN(node.getDistance())) {
					distancelessList.add(node);
				}
			}
			
			plainList.removeAll(distancelessList);
		}
		
		Collections.sort(plainList, new Comparator<Newick>() {
			public int compare(Newick o1, Newick o2) {
				return Double.compare(o1.getDistance(), o2.getDistance());
			}
		});
		
		return plainList;
	}
	
	@Override
	public String toString() {
		String toString = "";
		if (!this.children.isEmpty()) {
			StringBuffer sb = new StringBuffer();
			for (Newick child:this.children) {
				sb.append(child.toString()).append(',');
			}
			sb.setCharAt(sb.length()-1, ')'); // Replace last , with a )
			
			toString = '(' + sb.toString();
		}
		
		toString += this.name;
		
		if (!Double.isNaN(this.distance))
			toString += ":" + this.distance;
		
		if (this.parent == null)
			toString += ";";
		
		return toString;
	}
	
	@Override
	public Newick clone() {
		final Newick clon = new Newick(this.getName(), this.getDistance());
		
		for (Newick child:this.children) {
			final Newick childClon = child.clone();
			childClon.setParent(clon);
			clon.addChild(childClon);
		}
		
		return clon;
	}
	
	
	public static Newick parse(String newick) throws ParseException {
		if (!newick.startsWith("(")) {
			throw new ParseException("newick", 0);
		} else if (!newick.endsWith(";")) {
			throw new ParseException("newick", newick.length()-1);
		} else {
			final int indexOfParenthesis = newick.lastIndexOf(')');
			final String childrenNewick = newick.substring(0, indexOfParenthesis+1);
			
			if (indexOfParenthesis == newick.length()-2) {
				return Newick.parseNewick("", childrenNewick);
			} else {
				return Newick.parseNewick(
					newick.substring(indexOfParenthesis, newick.length()-1), 
					childrenNewick
				);
			}
		}
	}
	
//	private static Newick parseNewick(String node) {
//		return Newick.parseNewick(node, (Newick) null);
//	}
	
	private static Newick parseNode(String node, Newick parent) {
		if (node.length() == 0) {
			return new Newick(parent);			
		} else if (node.startsWith(":")) {
			return new Newick(parent, Double.parseDouble(node.substring(1)));
//		} else if (node.endsWith(":")) {
//			return new Newick(parent, node.substring(0, node.length() - 1));				
		} else if (node.contains(":")) {
			final String[] values = node.split(":");
			return new Newick(parent, values[0], Double.parseDouble(values[1]));
		} else {
			return new Newick(parent, node);
		}
	}
	
	private static Newick parseNewick(String node, String children) {
		return Newick.parseNode(node, children, null);
	}
	
	private static Newick parseNode(String node, String children, Newick parent) {
		final Newick newNode = Newick.parseNode(node, parent);
		
		if (children.startsWith("(") && children.endsWith(")")) {
			int pCount = 0;
			char[] childrenNewick = children.substring(1, children.length()-1).toCharArray();
			
			String childNewick = "";
			for (int i = 0; i < childrenNewick.length; i++) {
				char character = childrenNewick[i]; 
				if (character == '(') {
					childNewick += character;
					pCount++;
				} else if (character == ')') {
					childNewick += character;
					pCount--;					
				} else if (character == ',' && pCount == 0) {
					String[] splitNode = Newick.splitNode(childNewick);
					if (splitNode.length == 1) {
						newNode.addChild(Newick.parseNode(splitNode[0], newNode));
					} else {
						newNode.addChild(Newick.parseNode(splitNode[0], splitNode[1], newNode));
					}
					
					childNewick = "";
				} else {
					childNewick += character;
				}
			}
			
			if (childNewick.length() >  0) {
				String[] splitNode = Newick.splitNode(childNewick);
				if (splitNode.length == 1) {
					newNode.addChild(Newick.parseNode(splitNode[0], newNode));
				} else {
					newNode.addChild(Newick.parseNode(splitNode[0], splitNode[1], newNode));
				}
			}
		}
		
		return newNode;
	}
	
	private static String[] splitNode(String newick) {
		if (newick.startsWith("(")) {
			if (newick.endsWith(")")) {
				return new String[] { newick, "" };
			} else {
				int cutIndex = newick.lastIndexOf(")") + 1;
				return new String[] {
					newick.substring(cutIndex),
					newick.substring(0, cutIndex)
				};
			}
		} else {
			return new String[] { newick };
		}
	}
	
//	public final static class Node {
//		private final double value;
//		private final String name;
//		private final Node parent;
//		
//		private final List<Node> children;
//		
////		public Node() {
////			this("", Double.NaN);
////		}
////		
////		public Node(double value) {
////			this("", value);
////		}
////
////		public Node(String name) {
////			this(name, Double.NaN);
////		}
////		
////		public Node(String name, double value) {
////			this(null, name, value);
////		}
//		
//		public Node(Node parent) {
//			this(parent, "", Double.NaN);
//		}
//		
//		public Node(Node parent, double value) {
//			this(parent, "", value);
//		}
//		
//		public Node(Node parent, String name) {
//			this(parent, name, Double.NaN);
//		}
//		
//		public Node(Node parent, String name, double value) {
//			this.value = value;
//			this.name = name;
//			this.parent = parent;
//			this.children = new LinkedList<Node>();
//		}
//		
//		public Node getParent() {
//			return parent;
//		}
//		
//		public String getName() {
//			return name;
//		}
//		
//		public double getValue() {
//			return value;
//		}
//		
//		public void addChild(Node node) {
//			this.children.add(node);
//		}
//		
//		@Override
//		public String toString() {
//			String toString = "";
//			if (!this.children.isEmpty()) {
//				StringBuffer sb = new StringBuffer();
//				for (Node child:this.children) {
//					sb.append(child.toString()).append(',');
//				}
//				sb.setCharAt(sb.length()-1, ')'); // Replace last , with a )
//				
//				toString = '(' + sb.toString();
//			}
//			
//			toString +=  this.name;
//			
//			if (!Double.isNaN(this.value))
//				toString += ":" + this.value;
//			
//			if (this.parent == null)
//				toString += ";";
//			
//			return toString;
//		}
//	}
	
	public static void main(String[] args) {
		try {
			System.out.println(Newick.parse("(,,(,));"));
			System.out.println(Newick.parse("(A,B,(C,D));"));
			System.out.println(Newick.parse("(A,B,(C,D)E)F;"));
			System.out.println(Newick.parse("(:0.1,:0.2,(:0.3,:0.4):0.5);"));
			System.out.println(Newick.parse("(:0.1,:0.2,(:0.3,:0.4):0.5):0.0;"));
			System.out.println(Newick.parse("(A:0.1,B:0.2,(C:0.3,D:0.4):0.5);"));
			System.out.println(Newick.parse("(A:0.1,B:0.2,(C:0.3,D:0.4)E:0.5)F;"));
			System.out.println(Newick.parse("((B:0.2,(C:0.3,D:0.4)E:0.5)F:0.1)A;"));
			System.out.println(Newick.parse("(((0.0:0.5,0.0:0.5):1.08114,0.0:1.58114):2.16052,((1.0:0.70711,1.0:0.70711):0.15892,1.0:0.86603):2.87563);"));
			System.out.println(Newick.parse("(1:4.306816736322153e-02,2:2.058697096338600e-02,(3:1.734088958589415e-02,((4:2.562712122218737e-02,5:9.790132941891863e-02)0.813:2.109938136975416e-01,6:4.465185560571604e-01)1.000:1.452732275121912e+00)0.711:2.549622811123574e-02);"));
			
			List<Double> doubles = Arrays.asList(new Double[] { 0.1, 0.5, Double.NaN, 12.3 });
			Collections.sort(doubles);
			System.out.println(doubles);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
