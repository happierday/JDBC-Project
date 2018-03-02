/**
 * 
 */
package com.gict.library.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.gcit.library.main.Main;
import com.gcit.library.model.Branch;
import com.gcit.library.model.Librarian;

/**
 * @author jianwu
 *
 */
public class LibrarianController {
	
	private Librarian librarian;
	private LinkedList<Branch> list = new LinkedList<Branch>();
	private int choice;
	private String url = Main.getUrl();
	private String driver = Main.getDriver();
	private String username = Main.getUsername();
	private String pwd = Main.getPwd();
	@SuppressWarnings("finally")
	public void LibrarianMainMenu() {
		librarian = new Librarian();
		System.out.println("1) Enter Branch you manage\n" + "2) go back\n");
		//choice = Main.sc.nextInt();
		while(true) {
			switch(Main.sc.nextInt()) {
				case 1: 
					try {
						LibrarianOp1();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case 2:
					//if go back, clean up librarian
					librarian = null;
					Main.mainMenu();
					break;
				default:
					System.out.println("You must enter a number from 1 to 2!");
			}
		}
	}
	/**
	 * Select statement(Mysql), no need to call cnn.rollback()
	 * @throws SQLException
	 */
	private void LibrarianOp1() throws SQLException {
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			PreparedStatement pstate = cnn.prepareStatement("select * from tbl_library_branch");
			ResultSet rs = pstate.executeQuery();
			Branch branch;
			while(rs.next()) {
				branch = new Branch();
				branch.setId(rs.getInt(1));
				branch.setBranchName(rs.getString(2));
				branch.setAddress(rs.getString(3));
				System.out.println(branch.toString());
				list.add(branch);
			}
			System.out.println((list.size()+1) + ") go back");
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(cnn != null) {
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
//		for(int i = 0; i< list.size();i++) {
//			System.out.println(list.get(i).getId());
//		}
		int size = list.size();
		
		while(true) {
			choice = Main.sc.nextInt();
			if(choice > 0 && choice <= size) {
				librarian.setBranchIndex(choice);
				LibrarianOp2();
				break;
			}else if(choice == size+1){
				//if choose to go back, clean up list and librarian
				librarian = null;
				list.clear();
				LibrarianMainMenu();
				break;
			}else {
				System.out.println("Please enter a number between 1 and " + (size+1)+ "!");
			}
		}
	}
	
	private void LibrarianOp2() {
		System.out.println("1) Update the details of the Library \n" + 
				"2) Add copies of Book to the Branch");
		while(true) {
			choice = Main.sc.nextInt();
			switch(choice) {
				case 1:
					try {
						updateBranch();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case 2:
					manageBooks();
					break;
				default:
					System.out.println("Pleaase enter a number from 1 to 2");
			}
		}
		
	}
	
	private void updateBranch() throws SQLException {
		//System.out.println(librarian.getBranchIndex());
		Branch tempBranch = list.get(librarian.getBranchIndex()-1);
		String s = "";
		
		System.out.println("You have chosen to update the Branch with Branch Id: " + tempBranch.getId()
				+" and Branch Name: "+ tempBranch.getBranchName() +".");
		System.out.println("Enter ‘quit’ at any prompt to cancel operation.");
		Main.sc.nextLine();
		System.out.println("Please enter new branch name or enter N/A for no change:" );
		s = Main.sc.nextLine();
		//System.out.println(s);
		if(s.toLowerCase().compareTo("quit") == 0){
			LibrarianOp2();
			return;
		}
		if(s.compareTo("N/A") != 0) {
			tempBranch.setBranchName(s);
		}
		System.out.println("Please enter new branch address or enter N/A for no change: ");
		s = Main.sc.nextLine();
		if(s.toLowerCase().compareTo("quit") == 0){
			LibrarianOp2();
			return;
		}
		if(s.compareTo("N/A") != 0) {
			tempBranch.setAddress(s);
		}
		System.out.println(tempBranch.toString());
		//get all input from sc, ready to update
		Connection cnn = null;
			try {
				Class.forName(driver);
				cnn = DriverManager.getConnection(url,username,pwd);
				cnn.setAutoCommit(false);
				String sql = "update tbl_library_branch set branchName = ?, branchAddress=? where branchId = ?";
				PreparedStatement pstate = cnn.prepareStatement(sql);
				pstate.setString(1, tempBranch.getBranchName());
				pstate.setString(2,tempBranch.getAddress());
				pstate.setInt(3, tempBranch.getId());
				pstate.execute();
			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(cnn != null) {
					cnn.rollback();
					System.out.println("DB Roll Back due to error! Back to previous Menu!");
					LibrarianOp2();
					return;
				}
			} finally {
				if(cnn != null) {
					cnn.commit();
					cnn.close();
					System.out.println("DB connection closed");
					LibrarianOp2();
					return;
				}
			}
	}
	
	public void manageBooks() {
		
	}
}
