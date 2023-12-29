package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.MultipleConditionTerms;
import rs.ac.bg.etf.pp1.ast.SingleConditionTerm;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;

public class CounterVisitor extends VisitorAdaptor {

	protected int count;
	
	public int getCount() {
		return count;
	}
	
	public static class CondTermCounter extends CounterVisitor {
		public void visit(SingleConditionTerm term) {
			count++;
		}
		
		public void visit(MultipleConditionTerms terms) {
			count++;
		}
	}
}
