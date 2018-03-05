/**
 * 
 */
package com.gcit.library.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.gcit.library.helper.Helper;
import com.gcit.library.main.Main;
import com.gcit.library.model.Book;
import com.gcit.library.model.Branch;
import com.gcit.library.model.Librarian;

/**
 * Entire Librarian controller, separated out each functionalities
 * @author jianwu
 *
 */
public class LibrarianController {
	
	private Librarian librarian;
	private Branch branch;
	private Book book;
	
	private Helper helper = new Helper();
	private LinkedList<Branch> branchList = new LinkedList<Branch>();
	private LinkedList<Book> bookList = new LinkedList<Book>();
	private int choice;

	public void LibrarianMainMenu() throws SQLException {
		while(true) {
			choice = 0;
			librarian = new Librarian();
			System.out.println("1) Enter Branch you manage\n" + "2) go back\n");
			while(true) {
				if(choice != 0) {
					break;
				}
				switch(Main.sc.next()) {
					case "1": 
						choice = 1;
						break;
					case "2":
						//if go back, clean up librarian
						librarian = null;
						return;
					default:
						System.out.println("You must enter a number from 1 to 2!");
				}
			}
			if(choice == 1) {
				LibrarianOp1();
			}
		}
	}
	/** when user press 1 in Main Menu
	 * Select statement(Mysql), no need to call cnn.rollback()
	 * @throws SQLException
	 */
	
	public void LibrarianOp1() throws SQLException {
		while(true) {
			branchList = helper.getBranchList();
			branch = helper.getChoices(branchList);
			if(branch == null) {
				return;
			}else {
				LibrarianOp2();
			}
		}
	}
	
	/**
	 * when user have chosen a branch
	 * @throws SQLException
	 */
	public void LibrarianOp2() throws SQLException {
		while (true) {
			
			System.out.println("You are at " + branch.getBranchName() + " branch \n"
					+ "1) Update the details of the Library \n" + "2) Add copies of Book to the Branch\n"
					+ "3) Quit to previous\n");
			choice = 0;
			while (true) {
				if(choice!=0) {
					break;
				}
				switch (Main.sc.next()) {
					case "1":
						choice = 1;
						break;
					case "2":
						choice = 2;
						break;
					case "3":
						return;
					default:
						System.out.println("Pleaase enter a number from 1 to 3");
				}
			}
			if(choice == 1) {
				updateBranch();
			}else {
				manageBooks();
			}
		}
		
	}
	/**
	 * when user chose to edit branch on librarianop2
	 * @throws SQLException
	 */
	public void updateBranch() throws SQLException {
		String s = "";
		
		System.out.println("You have chosen to update the Branch with Branch Id: " + branch.getId()
				+" and Branch Name: "+ branch.getBranchName() +".");
		System.out.println("Enter ‘quit’ at any prompt to cancel operation.");
		Main.sc.nextLine();
		System.out.println("Please enter new branch name or enter N/A for no change:" );
		s = Main.sc.nextLine();
		//System.out.println(s);
		if(s.toLowerCase().compareTo("quit") == 0){
			return;
		}
		if(s.compareTo("N/A") != 0) {
			branch.setBranchName(s);
		}
		System.out.println("Please enter new branch address or enter N/A for no change: ");
		s = Main.sc.nextLine();
		if(s.toLowerCase().compareTo("quit") == 0){
			return;
		}
		if(s.compareTo("N/A") != 0) {
			branch.setAddress(s);
		}
		//get all input from sc, ready to update
		helper.updateBranchTable(branch.getBranchName(), branch.getAddress(),branch.getId());
	}
	
	/**
	 * when user choose to edit book copies from librarianOp2
	 * @throws SQLException
	 */
	public void manageBooks() throws SQLException {
		System.out.println("Pick the Book you want to add copies of, to your branch: ");
		bookList = helper.getBooksForBranch(branch);
		if(bookList == null) {
			System.out.println("Can't find any book from this branch.");
			return;
		}
		//getBooks();
		book = helper.getChoices(bookList);
		if(book == null) {
			return;
		}else {
			updateBook();
		}
	}
	
	/**
	 * when user choose a specific book from manageBooks
	 * @param book
	 * @param index
	 * @throws SQLException
	 */
	public void updateBook() throws SQLException {
		System.out.println("Existing number of copies for " + book.getTitle() + ": " +
							book.getNumOfCopies());
		System.out.println("Enter a new number of copies: ");
		choice = 0;
		while(true) {
			if(choice > 0) {
				break;
			}
			if(Main.sc.hasNextInt()) {
				choice = Main.sc.nextInt();
			}else {
				System.out.println("Please enter a positive number: ");
				Main.sc.next();
			}
		}
		helper.updateBooks(choice, book);
	}
	public Librarian getLibrarian() {
		return librarian;
	}
	public LinkedList<Branch> getBranchList() {
		return branchList;
	}
	public LinkedList<Book> getBookList() {
		return bookList;
	}
}
