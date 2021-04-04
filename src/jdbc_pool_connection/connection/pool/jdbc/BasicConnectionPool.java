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
		//Criando um array de conexões com o tamanho pre-definido
		List<Connection> pool  = new ArrayList<>(INITIAL_POOL_SIZE);
		for(int i = 0; i < INITIAL_POOL_SIZE; i++) {
			//Adicionando cada conexão criada no array
			pool.add(createConnection(url, user, password));
		}
		
		//retornando uma instância de BasicConnectionPool pronta, chamando o contrutor que irá abastecer as váriaveis de instância da class; 
		return new BasicConnectionPool(url, user, password, pool);
				
	}
	
	
	
	@Override
	public Connection getConnection() throws SQLException {
		// TODO Auto-generated method stub
		//Verificando se têm conexões disponíveis no array de conexões disponíveis.
		//Depois verificando se no array de conexões indisponíveis (conexões em uso) têm lá todas as conexões em uso no momento.
		//Caso não tenham lá todas vamos criar uma nova conexão e adicionar no array de conexões disponíveis para uso posterior.
		//Lançamos uma excepção (dizendo que não temos conexões disponíveis) se não tivermos conexões e todas as conexões estiverem em uso no momento.
		if(this.connectionPool.isEmpty()) {
			if(this.usedConnection.size() < INITIAL_POOL_SIZE) {
				Connection connection = createConnection(this.url, this.user, this.password);
				connectionPool.add(connection);
			}else {
				
			}
		}
		
		//Removendo a última conexão disponível no array de conxões disponíveis, depois adicionando no array de conexões indisponíveis.
		Connection connection = this.connectionPool.remove(this.connectionPool.size()-1);
		this.usedConnection.add(connection);
		
		//Se a conexão não for válida crea uma nova conexão, conexão considerada inválida se estiver fechada ou se estiver em uso e ainda assim estiver na pool de conexões disponíveis.
		if(!connection.isValid(MAX_TIMEOUT)) { 
			connection = createConnection(this.url, this.user, this.password);
		}
		
		return connection;
	}
	
	public void shutdown() throws SQLException {
		//Usando stream para percorer um array e usando um method reference para chamar o método releaseConnection para todas conexões.
		this.usedConnection.forEach(this::releaseConnection);
		
		//Fechando todas as conexões.
		for(Connection connection: this.connectionPool) {
			connection.close();
		}
		
		//Limpando o array.
		this.connectionPool.clear();
	}

	@Override
	public boolean releaseConnection(Connection connection) {
		// TODO Auto-generated method stub
		
		//Adicionando a conexão lançada no array de conexões disponíveis e removendo do array de conexões indisponíveis.
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
