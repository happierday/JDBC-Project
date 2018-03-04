/**
 * 
 */
package com.gict.library.controller;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;

import com.gcit.library.main.Main;
import com.gcit.library.model.Book;
import com.gcit.library.model.Borrower;
import com.gcit.library.model.Branch;
import com.gcit.library.model.Loan;

/**
 * @author jianwu
 *
 */
public class BorrowerController {
	private String url = Main.getUrl();
	private String driver = Main.getDriver();
	private String username = Main.getUsername();
	private String pwd = Main.getPwd();
	private boolean backToMain = false;
	private int choice = 0;
	private Borrower borrower;
	private Branch branch;
	private Book book;
	private Loan loan;
	private LinkedList<Branch> branchList;
	private LinkedList<Book> bookList = new LinkedList<Book>();
	private LinkedList<Loan> loanList = new LinkedList<Loan>();
	private LibrarianController librarianController;
	
	public void BorrowerMainMenu() throws SQLException {
		while (true) {
			choice = 0;
			System.out.println("Enter the your Card Number: ");
			while (true) {
				if (choice > 0) {
					break;
				}
				if (Main.sc.hasNextInt()) {
					choice = Main.sc.nextInt();
				} else {
					System.out.println("Please enter a number!");
					Main.sc.next();
				}
			}
			validateCardNo();
			if(backToMain) {
				break;
			}
		}
	}
	
	public void validateCardNo() throws SQLException {
		Connection cnn = null;
		ResultSet rs = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			PreparedStatement pstate = cnn.prepareStatement("select * from tbl_borrower where cardNo = ?");
			pstate.setInt(1, choice);
			rs = pstate.executeQuery();	
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			if(cnn != null) {
				if(!rs.next()) {
					System.out.println("Invalid card number");
					cnn.close();
					System.out.println("DB connection closed");
					return;
				}else {
					borrower = new Borrower();
					librarianController = new LibrarianController();
					borrower.setBorrowerId(rs.getInt(1));
					borrower.setName(rs.getString(2));
					borrower.setAddress(rs.getString(3));
					//System.out.println(borrower.toString());
					cnn.close();
					System.out.println("DB connection closed");
					borrowerOp();
				}
			}
		}
	}
	
	public void borrowerOp() throws SQLException {
		while(true) {
			choice = 0;
			System.out.println("1) Check out a book\n" + 
					"2) Return a Book\n" + 
					"3) Quit to Previous\n ");
			while(true) {
				if(choice != 0) {
					break;
				}
				switch(Main.sc.next()) {
				case "1":
					choice = 1;
					break;
				case "2":
					choice = 2;
					break;
				case "3":
					backToMain = true;
					return;
				default:
					System.out.println("Please enter a number between 1 to 3!");
				}
			}
			if(choice == 1) {
				borrowerOp1();
			}else {
				borrowerOp2();
			}
		}
	}

	public void borrowerOp1() throws SQLException {
		librarianController.getLibraryBranch();
		branchList = librarianController.getBranchList();
		choice = 0;
		int size = branchList.size();
		while(true) {
			if(choice > 0) {
				break;
			}
			if(Main.sc.hasNextInt()) {
				choice = Main.sc.nextInt();
				if(choice > 0 && choice < size+1) {
					branch = branchList.get(choice-1);
					break;
				}else if(choice == size+1){
					//if choose to go back, clean up list and librarian
					branchList.clear();
					return;
				}else {
					choice = 0;
					System.out.println("Please enter a number between 1 and " + (size+1)+ "!");
				}
			}else {
				System.out.println("Please enter a number!");
				Main.sc.next();
			}
		}
		checkOutBooks();
	}
	
	/**
	 * check out books, print out the corresponding books from the particular branch
	 * borrower chose
	 * @throws SQLException
	 */
	public void checkOutBooks() throws SQLException {
		Connection cnn = null;
		bookList.clear();
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "select \n" + 
					"	tbl_book_copies.bookId,\n" + 
					"    tbl_book_copies.branchId,\n" +
					"    tbl_book.title,\n" + 
					"    tbl_book_copies.noOfCopies\n" + 
					"from tbl_book_copies\n" + 
					"join tbl_book on tbl_book.bookId = tbl_book_copies.bookId\n" + 
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
				book.setTitle(rs.getString(3));
				book.setNumOfCopies(rs.getInt(4));
				System.out.println(index + ") " + book.getTitle() + ", copies left: " + book.getNumOfCopies());
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
		int n;
		int size = bookList.size();
		System.out.println("Which book you want to check out?");
		choice = 0;
		while(true) {
			if(choice > 0) {
				break;
			}
			if(Main.sc.hasNextInt()) {
				choice = Main.sc.nextInt();
				if(choice > 0 && choice < size+1) {
					book = bookList.get(choice-1);
					break;
				}else if(choice == size+1){
					//if choose to go back, clean up list and librarian
					bookList.clear();
					return;
				}else {
					choice = 0;
					System.out.println("Please enter a number between 1 and " + (size+1)+ "!");
				}
			}else {
				System.out.println("Please enter a number!");
				Main.sc.next();
			}
		}
		if(alreadyBorrowwed()) {
			System.out.println("You can't borrow same book from same branch again!");
			return;
		}else {
			checkOutUpdate();
		}
	}
	/**
	 * need to check whether borrower has check out same book from same branch
	 * @return
	 * @throws SQLException
	 */
	public boolean alreadyBorrowwed() throws SQLException {
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "select * from tbl_book_loans where bookId = ? and branchId = ? and cardNo = ?";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setInt(1, book.getBookId());
			pstate.setInt(2, branch.getId());
			pstate.setInt(3, borrower.getBorrowerId());
			ResultSet rs = pstate.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
			}
		} finally {
			if(cnn != null) {
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
		return false;
	}
	
	/**
	 * update the tbl_book_loans table, will need to update copy table as well
	 * @throws SQLException
	 */
	public void checkOutUpdate() throws SQLException {
		if(alreadyBorrowwed()) {
			System.out.println("You cannot borrow same book from same branch!");
			return;
		}else {
			LocalDate ld,dueDate;
			ld = LocalDateTime.now().toLocalDate();
			dueDate = ld.plusWeeks(1);
			Connection cnn = null;
			System.out.println("Due Date is " + dueDate);
			try {
				Class.forName(driver);
				cnn = DriverManager.getConnection(url,username,pwd);
				cnn.setAutoCommit(false);
				String sql = "insert into tbl_book_loans "+
				             "(bookId,branchId,cardNo,dateOut,dueDate)"+
						     " value(?,?,?,?,?)";
				PreparedStatement pstate = cnn.prepareStatement(sql);
				pstate.setInt(1, book.getBookId());
				pstate.setInt(2, book.getBranchId());
				pstate.setInt(3, borrower.getBorrowerId());
				pstate.setDate(4, Date.valueOf(ld));
				pstate.setDate(5, Date.valueOf(dueDate));
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
					System.out.println("New loan inserted!");
					cnn.close();
					System.out.println("DB connection closed");
				}
				updateTable(-1);
			}
		}
	}
	/**
	 * when check out, need to update number of copies for the book for that
	 * particular branch in the tbl_book_copies table
	 * @throws SQLException
	 */
	public void updateTable(int n) throws SQLException {
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "update tbl_book_copies set noOfCopies = noOfCopies + ? where bookId = ? and branchId = ?";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setInt(1, n);
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
				System.out.println("Book amount updated!");
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
	}
	/**
	 * Alomst same as check out
	 * This is for check in/return book
	 * @throws SQLException
	 */
	public void borrowerOp2() throws SQLException {
		librarianController.getLibraryBranch();
		branchList = librarianController.getBranchList();
		int n;
		while(true) {
			n = Main.sc.nextInt();
			if(n > 0 && n <= branchList.size()+1) {
				if(n == branchList.size()+1) {
					return;
				}else {
					branch = branchList.get(n-1);
					checkInBooks();
				}
				return;
			}else {
				System.out.println("Please enter a number from 1 to " + (branchList.size()+1));
			}
		}
	}
	
	public boolean hasBorrowwed() throws SQLException {
		loanList.clear();
		Connection cnn = null;
		ResultSet rs = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "select loans.bookId,loans.branchId,loans.cardNo, book.title from tbl_book_loans loans\n" + 
					"join tbl_book book on book.bookId = loans.bookId\n" + 
					"where loans.branchId = ? and loans.cardNo = ? and loans.dateIn is null;";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			//System.out.println(branch.getId());
			pstate.setInt(1, branch.getId());
			pstate.setInt(2, borrower.getBorrowerId());
			rs = pstate.executeQuery();
			if(rs.next()) {
				rs.previous();
				Loan loan;
				int index = 0;
				while(rs.next()) {
					index++;
					loan = new Loan();
					loan.setBookId(rs.getInt(1));
					loan.setBranchId(rs.getInt(2));
					loan.setCardNo(rs.getInt(3));
					loan.setBookTitle(rs.getString(4));
					loanList.add(loan);
					System.out.println("You borrowwed these books: ");
					System.out.println(index + ") " + loan.getBookTitle());
				}
				index++;
				System.out.println(index + ") go back");	
				return true;
			}else {
				return false;
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
				return false;
			}
		} finally {
			if(cnn!=null) {
				cnn.close();
				System.out.println("DB closed!");
			}
		}
		return false; 
	}
	
	/**
	 * actino for check in books, need to check whether borrower has
	 * borrow the book from that branch or not
	 * @throws SQLException
	 */
	public void checkInBooks() throws SQLException {
		if(hasBorrowwed()) {
			choice = 0;
			int size = loanList.size();
			while(true) {
				if(choice > 0) {
					break;
				}
				if(Main.sc.hasNextInt()) {
					choice = Main.sc.nextInt();
					if(choice > 0 && choice < size+1) {
						loan = loanList.get(choice-1);
						break;
					}else if(choice == size+1){
						//if choose to go back, clean up list and librarian
						loanList.clear();
						return;
					}else {
						choice = 0;
						System.out.println("Please enter a number between 1 and " + (size+1)+ "!");
					}
				}else {
					System.out.println("Please enter a number!");
					Main.sc.next();
				}
			}
			checkInUpdate();
		}else {
			System.out.println("You didn't borrow any book from this branch!");
		}
	}
	
	/**
	 * update dateIn in the corresponding row
	 * @param loan
	 * @throws SQLException
	 */
	public void checkInUpdate() throws SQLException {
		book = new Book();
		book.setBookId(loan.getBookId());
		book.setBranchId(loan.getBranchId());
		LocalDate ld;
		ld = LocalDateTime.now().toLocalDate();
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "update tbl_book_loans "+
			             "set dateIn = ?" + 
			             "where bookId = ? and branchId = ? and cardNo = ?";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setDate(1, Date.valueOf(ld));
			pstate.setInt(2, loan.getBookId());
			pstate.setInt(3, loan.getBranchId());
			pstate.setInt(4, loan.getCardNo());
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
				System.out.println("Updated!");
				cnn.close();
				System.out.println("DB connection closed");
			}
			updateTable(1);
		}
	}
}
