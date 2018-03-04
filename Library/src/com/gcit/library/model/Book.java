/**
 * 
 */
package com.gcit.library.model;

/**
 * @author jianwu
 *
 */
public class Book {
	private int bookId;
	private int branchId;
	private int authorId;
	private String title;
	private int numOfCopies;
	private int pubId;
	public int getBookId() {
		return bookId;
	}
	public void setBookId(int bookId) {
		this.bookId = bookId;
	}
	public int getBranchId() {
		return branchId;
	}
	public void setBranchId(int branchId) {
		this.branchId = branchId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getAuthor() {
		return authorId;
	}
	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}
	public int getNumOfCopies() {
		return numOfCopies;
	}
	public void setNumOfCopies(int numOfCopies) {
		this.numOfCopies = numOfCopies;
	}
	public int getPublisher() {
		return pubId;
	}
	public void setPubId(int pubId) {
		this.pubId = pubId;
	}
}
