package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.concepts.Obj;

public class Helper {
	
	public Obj getIndexNode() {
		return indexNode;
	}

	public void setIndexNode(Obj indexNode) {
		this.indexNode = indexNode;
	}

	public Obj getLeftIndexNode() {
		return leftIndexNode;
	}

	public void setLeftIndexNode(Obj leftIndexNode) {
		this.leftIndexNode = leftIndexNode;
	}

	private static Helper instance = null;
	
	private Obj indexNode = null;
	private Obj leftIndexNode = null;
	
	private Helper() {
		
	}
	
	public static Helper getInstance() {
		if (instance == null)
			instance = new Helper();
		
		return instance;
	}
}
