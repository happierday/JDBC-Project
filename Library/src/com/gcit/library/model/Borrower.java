/**
 * 
 */
package com.gcit.library.model;

/**
 * @author jianwu
 *
 */
public class Borrower {
	private int borrowerId;
	private String name;
	private String address;
	public int getBorrowerId() {
		return borrowerId;
	}
	public void setBorrowerId(int borrowerId) {
		this.borrowerId = borrowerId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	@Override
	public String toString() {
		return "Borrower [borrowerId=" + borrowerId + ", name=" + name + ", address=" + address + "]";
	}
}
