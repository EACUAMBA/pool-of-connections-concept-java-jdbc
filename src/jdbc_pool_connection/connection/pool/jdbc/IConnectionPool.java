package jdbc_pool_connection.connection.pool.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/*
*Jan 24, 2021
*Edilson A. Cuamba
*
*/

public interface IConnectionPool {
	Connection getConnection() throws SQLException;
	boolean releaseConnection(Connection connection);
	void shutdown() throws SQLException;
	String getUrl();
	String getUser();
	String getPassword();
}
