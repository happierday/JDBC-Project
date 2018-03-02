/**
 * 
 */
package com.gcit.library.model;

/**
 * @author jianwu
 *
 */
public class Librarian {
	//branch position in the list
	private int branchIndex;
	public int getBranchIndex() {
		return branchIndex;
	}
	public void setBranchIndex(int branchIndex) {
		this.branchIndex = branchIndex;
	}
	@Override
	public String toString() {
		return "Librarian [id=" + branchIndex + ". ";
	}
}
