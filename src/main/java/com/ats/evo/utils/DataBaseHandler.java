/*
 *
 *  * Copyright © 2014 - 2021 Leipzig University (Database Research Group)
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, version 3.
 *  *
 *  * This program is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ats.evo.utils;

import com.ats.evo.Globals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseHandler {

	private static DataBaseHandler singleton = null;
	private Connection databaseConnection = null;
	
	public static DataBaseHandler getInstance() {
		if (singleton == null) {
			synchronized(DataBaseHandler.class) {
				if (singleton==null) {
					singleton = new DataBaseHandler();
				}
			}
		}
		return singleton;
	}

	private DataBaseHandler() {}
	
	public Connection getDatabaseConnection() throws SQLException {
		if (databaseConnection==null) {
			try {
				this.createDatabaseConnection();
				return this.databaseConnection;
			} catch (SQLException e) {
				printSQLException(e);
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			if (this.databaseConnection.isClosed()) {
				try {
					this.createDatabaseConnection();
					return this.databaseConnection;
				} catch (SQLException e) {
					printSQLException(e);
					return null;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			return this.databaseConnection;
		}
	}
		
	public void createDatabaseConnection() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class.forName("org.h2.Driver").newInstance();
		this.databaseConnection = DriverManager.getConnection(Globals.DB_URL);
	}
	
	public void closeDatabaseConnection() throws SQLException {
		databaseConnection.close();
		System.out.println(databaseConnection.isClosed());
		this.databaseConnection=null;
	}
	
	public int executeDml(String dmlQuery) throws SQLException {
		
		Connection dbCon = null;
		Statement stmt = null;
		int affectedRows=0;
		
		dbCon = this.getDatabaseConnection();
		stmt = dbCon.createStatement();
		affectedRows = stmt.executeUpdate(dmlQuery);
		
		return(affectedRows);
	}
	
	public int[] executeDml(String[] dmlQueries) throws SQLException {
		
		Connection dbCon = null;
		Statement stmt = null;
		int[] affectedRows = new int[]{};
		
		dbCon = this.getDatabaseConnection();
		stmt = dbCon.createStatement();
		for (String dmlQuery : dmlQueries) {
			stmt.addBatch(dmlQuery);
		}
		affectedRows = stmt.executeBatch();
		stmt.close();
		dbCon.close();
		return(affectedRows);
	}
	
	public ResultSet executeSelect(String sqlQuery) throws SQLException {
	
		Connection dbCon = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		dbCon = this.getDatabaseConnection();
		stmt = dbCon.createStatement();
		rs = stmt.executeQuery(sqlQuery);


		return rs;
	}
	
	public void closeStatement(Statement stmt) throws SQLException {
		Connection dbCon = null; 
		
	
		dbCon = stmt.getConnection();
		
		stmt.close(); 
		dbCon.close();
	} 
	
	public void closeStatement(ResultSet rs) throws SQLException {
		Connection dbCon = null;
		Statement stmt = null;
		
		stmt = rs.getStatement();
		dbCon = stmt.getConnection();
		rs.close();
		if (stmt!=null) {
			stmt.close();
		}
		if (dbCon!=null && !dbCon.isClosed()) {
			dbCon.close();
		}
	}
	
	public PreparedStatement prepareStatement(String sqlQuery) throws SQLException {
		Connection dbCon = null;
		PreparedStatement pStmt = null;
		
		dbCon = this.getDatabaseConnection();
		pStmt = dbCon.prepareStatement(sqlQuery);

		return pStmt;
	}
	
	 /**
     * Prints details of an SQLException chain to <code>System.err</code>.
     * Details included are SQL State, Error code, Exception message.
     *
     * @param e the SQLException from which to print details.
     */
    public static void printSQLException(SQLException e)
    {
        // Unwraps the entire exception chain to unveil the real cause of the
        // Exception.
        while (e != null)
        {
            System.err.println("\n----- SQLException -----");
            System.err.println("  SQL State:  " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  Message:    " + e.getMessage());
            // for stack traces, refer to derby.log or uncomment this:
            //e.printStackTrace(System.err);
            e = e.getNextException();
        }
    }
}
