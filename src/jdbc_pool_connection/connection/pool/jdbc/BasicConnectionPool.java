package jdbc_pool_connection.connection.pool.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
*Jan 24, 2021
*Edilson A. Cuamba
*
*/

public class BasicConnectionPool implements IConnectionPool {

	private String url;
	private String user;
	private String password;
	private List<Connection> connectionPool;
	private List<Connection> usedConnection = new ArrayList<>();
	private static int INITIAL_POOL_SIZE = 10, MAX_TIMEOUT = 120;
	
	private  BasicConnectionPool(String url, String user, String password, List<Connection> pool) {
		// TODO Auto-generated constructor stub
		this.url = url;
		this.password = password;
		this.user = user;
		this.connectionPool = pool;
	}



	public static BasicConnectionPool create(String url, String user, String password)throws SQLException{
		//Criando um array de conex�es com o tamanho pre-definido
		List<Connection> pool  = new ArrayList<>(INITIAL_POOL_SIZE);
		for(int i = 0; i < INITIAL_POOL_SIZE; i++) {
			//Adicionando cada conex�o criada no array
			pool.add(createConnection(url, user, password));
		}
		
		//retornando uma inst�ncia de BasicConnectionPool pronta, chamando o contrutor que ir� abastecer as v�riaveis de inst�ncia da class; 
		return new BasicConnectionPool(url, user, password, pool);
				
	}
	
	
	
	@Override
	public Connection getConnection() throws SQLException {
		// TODO Auto-generated method stub
		//Verificando se t�m conex�es dispon�veis no array de conex�es dispon�veis.
		//Depois verificando se no array de conex�es indispon�veis (conex�es em uso) t�m l� todas as conex�es em uso no momento.
		//Caso n�o tenham l� todas vamos criar uma nova conex�o e adicionar no array de conex�es dispon�veis para uso posterior.
		//Lan�amos uma excep��o (dizendo que n�o temos conex�es dispon�veis) se n�o tivermos conex�es e todas as conex�es estiverem em uso no momento.
		if(this.connectionPool.isEmpty()) {
			if(this.usedConnection.size() < INITIAL_POOL_SIZE) {
				Connection connection = createConnection(this.url, this.user, this.password);
				connectionPool.add(connection);
			}else {
				
			}
		}
		
		//Removendo a �ltima conex�o dispon�vel no array de conx�es dispon�veis, depois adicionando no array de conex�es indispon�veis.
		Connection connection = this.connectionPool.remove(this.connectionPool.size()-1);
		this.usedConnection.add(connection);
		
		//Se a conex�o n�o for v�lida crea uma nova conex�o, conex�o considerada inv�lida se estiver fechada ou se estiver em uso e ainda assim estiver na pool de conex�es dispon�veis.
		if(!connection.isValid(MAX_TIMEOUT)) { 
			connection = createConnection(this.url, this.user, this.password);
		}
		
		return connection;
	}
	
	public void shutdown() throws SQLException {
		//Usando stream para percorer um array e usando um method reference para chamar o m�todo releaseConnection para todas conex�es.
		this.usedConnection.forEach(this::releaseConnection);
		
		//Fechando todas as conex�es.
		for(Connection connection: this.connectionPool) {
			connection.close();
		}
		
		//Limpando o array.
		this.connectionPool.clear();
	}

	@Override
	public boolean releaseConnection(Connection connection) {
		// TODO Auto-generated method stub
		
		//Adicionando a conex�o lan�ada no array de conex�es dispon�veis e removendo do array de conex�es indispon�veis.
		connectionPool.add(connection);
		return usedConnection.remove(connection);
	}
	
	private static Connection createConnection(String url, String user, String password)throws SQLException{
		return DriverManager.getConnection(url, user, password);
	}
	
	public int getSize() {
		return connectionPool.size() + usedConnection.size();
	}

	@Override
	public String getUrl() {
		// TODO Auto-generated method stub
		return this.url;
	}

	@Override
	public String getUser() {
		// TODO Auto-generated method stub
		return this.user;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return this.password;
	}
	
	public static void setMAX_TIMEOUT(int max_timeout) {
		MAX_TIMEOUT = max_timeout;
	}
	
	public static void setINITIAL_POOL_SIZE(int initial_pool_size) {
		INITIAL_POOL_SIZE = initial_pool_size;
	}
	
	public static void main(String args[]) throws SQLException {
		
		BasicConnectionPool b = BasicConnectionPool.create("jdbc:sqlite:C:/apps/sqlite/databases/thunder_vendas.db", "root", "");
		
		System.out.println(b.getConnection().prepareStatement("Select * from action").executeQuery().getString(2));;
	}
	 
}
