package database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;
import java.sql.Date;

public class MySqlConnector {
	private Connection connection;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    private static final String DATABASE = "chat";
    private static final String USERTABLE = "user";
    private static final String DB_USER = "chat";
    private static final String DB_PW = "KommNetze";
    private boolean result = false;
    
    
	
	public MySqlConnector() {}
	
	private boolean connectToMySql(String host, String database, String username, String passwd) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			StringBuilder connectionCommand = new StringBuilder()
					.append("jdbc:mysql://")
					.append(host)
					.append("/")
					.append(database)
					.append("?user=")
					.append(username)
					.append("&password=")
					.append(passwd);
			this.connection = DriverManager.getConnection(connectionCommand.toString());
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	public boolean checkUserExists(String username) {
		try {
			this.connectToMySql("localhost", DATABASE, DB_USER, DB_PW);
			preparedStatement = connection.prepareStatement("SELECT username from ? where user = ?");
			preparedStatement.setString(1, DATABASE+"."+USERTABLE);
			preparedStatement.setString(2, username);
			resultSet = preparedStatement.executeQuery();
			result = resultSet.next();
		} catch (Exception ex) {
			ex.printStackTrace();
			result =  false;
		} finally {
            close();
		}
        return result;
	}
	
	public boolean checkCorrectLogin(String username, String passwd) {
		try {
			this.connectToMySql("localhost", DATABASE, DB_USER, DB_PW);
			preparedStatement = connection.prepareStatement("SELECT username, password from ? where user = ? and password = ?");
			preparedStatement.setString(1, DATABASE+"."+USERTABLE);
			preparedStatement.setString(2, username);
			preparedStatement.setString(3, passwd);
			resultSet = preparedStatement.executeQuery();
			result = resultSet.next();
		} catch (Exception ex) {
			ex.printStackTrace();
			result = false;
		} finally {
            close();
		}
        return result;
	}
	
	public boolean registerUser(String username, String passwd) {
		try {
			this.connectToMySql("localhost", DATABASE, DB_USER, DB_PW);
			if (!this.checkUserExists(username)) {
				preparedStatement = connection.prepareStatement("insert into ? values (default, ?, ?, ?, ?)");
				preparedStatement.setString(1, DATABASE+"."+USERTABLE);
				preparedStatement.setString(2, username);
				preparedStatement.setString(3, username);
				preparedStatement.setString(4, passwd);
				preparedStatement.setString(5, "");
				preparedStatement.executeUpdate();
				result = true;
			} else
				result = false;
		} catch (Exception ex) {
			ex.printStackTrace();
			result =  false;
		} finally {
            close();
		}
        return result;
	}
	
	private void close() {
		try {
            if (resultSet != null) {
                    resultSet.close();
            }

            if (statement != null) {
                    statement.close();
            }

            if (connection != null) {
                    connection.close();
            }
		} catch (Exception e) {

    }
	}

}
