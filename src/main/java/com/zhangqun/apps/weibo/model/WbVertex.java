package com.zhangqun.apps.weibo.model;

public class WbVertex {
	private String label;
	private double weight;
	
	public WbVertex(String label) {
		this(label, 1);
	}
	
	public WbVertex(String label, double weight) {
		this.label = label;
		this.weight = weight;
	}

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "(" + label + ")";
	}
	
	
}
