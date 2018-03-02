/**
 * 
 */
package com.gcit.library.model;

/**
 * @author jianwu
 *
 */
public class Branch {
	private int id;
	private String branchName;
	private String address;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getBranchName() {
		return branchName;
	}
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	@Override
	public String toString() {
		return id + ") " + branchName + ", " + address;
	}
}
