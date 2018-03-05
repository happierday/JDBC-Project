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
	
	private String url = Main.getUrl();
	private String driver = Main.getDriver();
	private String username = Main.getUsername();
	private String pwd = Main.getPwd();
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
	 * get library branches.
	 * @throws SQLException
	 */
	//TODO move this to helper class
//	public void getLibraryBranch() throws SQLException {
//		Connection cnn = null;
//		branchList.clear();
//		try {
//			Class.forName(driver);
//			cnn = DriverManager.getConnection(url,username,pwd);
//			PreparedStatement pstate = cnn.prepareStatement("select * from tbl_library_branch");
//			ResultSet rs = pstate.executeQuery();
//			Branch branch;
//			while(rs.next()) {
//				branch = new Branch();
//				branch.setId(rs.getInt(1));
//				branch.setBranchName(rs.getString(2));
//				branch.setAddress(rs.getString(3));
//				System.out.println(branch.toString());
//				branchList.add(branch);
//			}
//			System.out.println((branchList.size()+1) + ") go back");
//		} catch (ClassNotFoundException | SQLException e) {
//			e.printStackTrace();
//		} finally {
//			if(cnn != null) {
//				cnn.close();
//				System.out.println("DB connection closed");
//			}
//		}
//	}
	/**
	 * when user have chosen a branch
	 * @throws SQLException
	 */
	public void LibrarianOp2() throws SQLException {
		while (true) {
			
			System.out.println("You are at " + branch.getBranchName() + "branch \n"
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
		System.out.println(branch.toString());
		//get all input from sc, ready to update
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "update tbl_library_branch set branchName = ?, branchAddress=? where branchId = ?";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setString(1, branch.getBranchName());
			pstate.setString(2,branch.getAddress());
			pstate.setInt(3, branch.getId());
			pstate.execute();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				System.out.println("Branch Updated!");
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
	}
	
	/**
	 * get books from a particular branch user chose
	 * @throws SQLException
	 */
	public void getBooks() throws SQLException {
		Connection cnn = null;
		bookList.clear();
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "select \n" + 
					"	tbl_book_copies.bookId,\n" + 
					"    tbl_book_copies.branchId,\n" + 
					"    tbl_book.pubId,\n" + 
					"    tbl_book_authors.authorId,\n" + 
					"    tbl_book.title,\n" + 
					"    tbl_book_copies.noOfCopies\n" + 
					"from tbl_book_copies\n" + 
					"join tbl_book on tbl_book.bookId = tbl_book_copies.bookId\n" + 
					"join tbl_book_authors on tbl_book.bookId = tbl_book_authors.bookId\n" + 
					"where tbl_book_copies.branchId = ?;";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setInt(1, branch.getId());
			ResultSet rs = pstate.executeQuery();
			Book book;
			int index = 0;
			while(rs.next()) {
				index ++;
				book = new Book();
				book.setBookId(rs.getInt(1));
				book.setBranchId(rs.getInt(2));
				book.setPubId(rs.getInt(3));
				book.setAuthorId(rs.getInt(4));
				book.setTitle(rs.getString(5));
				book.setNumOfCopies(rs.getInt(6));
				System.out.println(index + ") " + book.getTitle() + ", now has " + book.getNumOfCopies() + " copies! ");
				bookList.add(book);
			}
			System.out.println((bookList.size()+1) + ") go back");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
				return;
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
	}
	/**
	 * when user choose to edit book copies from librarianOp2
	 * @throws SQLException
	 */
	public void manageBooks() throws SQLException {
		System.out.println("Pick the Book you want to add copies of, to your branch: ");
		getBooks();
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
		Connection cnn = null;
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
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "update tbl_book_copies set noOfCopies = ?\n" + 
					"where bookId = ? and branchId = ?;";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setInt(1, choice);
			pstate.setInt(2, book.getBookId());
			pstate.setInt(3, book.getBranchId());
			pstate.execute();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
				return;
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				System.out.println("Book copied updated!");
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
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
