/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Admin
 */
public class DBConnect {

	private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=Volunteer_System_New;encrypt=false";
	private static final String USER = "sa";
	private static final String PASSWORD = "123456";

	static {
		try {
			// Load driver 1 lần duy nhất khi class được nạp
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(" Không tìm thấy SQL Server JDBC Driver!", e);
		}
	}

	// Mỗi lần gọi sẽ tạo một connection mới
	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}
}
