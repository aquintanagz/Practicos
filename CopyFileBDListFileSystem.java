package application;

	
	import java.nio.file.*;
	import java.sql.*;

	public class CopyFileBDListFileSystem {

		
	    public static void main(String[] args) {
	        // Parámetros de conexión a PostgreSQL
	        String url = "jdbc:postgresql://192.168.10.130:5432/efilecm-quintana";	        
	        String user = "efilecm-quintana";
	        String password = "quiacS452FR0#60";

	        // Consulta para obtener rutas desde la tabla zpe_rutas
	        String query = "SELECT id, nombre, origen, destino FROM public.zpe_rutas where status = 0";

	        try (Connection conn = DriverManager.getConnection(url, user, password);
	             Statement stmt = conn.createStatement();
	             ResultSet rs = stmt.executeQuery(query)) {

	            while (rs.next()) {
	                int id = rs.getInt("id");
	                String nombre = rs.getString("nombre");
	                String origen = rs.getString("origen");
	                String destino = rs.getString("destino");

	                try {
	                	 Path pathOrigen = Paths.get(origen);
	                     Path pathDestino = Paths.get(destino);
	                     
	                	 // Verificar que el archivo de origen exista
	                    if (!Files.exists(pathOrigen)) {
	                        System.err.println("❌ Archivo de origen no existe: " + pathOrigen);
	                        return;
	                    }

	                    // Crear directorio destino si no existe
	                    Files.createDirectories(pathDestino.getParent());

	                    // Copiar archivo
	                    Files.copy(
	                        Paths.get(origen),
	                        Paths.get(destino),
	                        StandardCopyOption.REPLACE_EXISTING
	                    );
	                    System.out.println("✔ Archivo [" + nombre + "] copiado de " + origen + " a " + destino);
	                } catch (Exception e) {
	                    System.err.println("❌ Error al copiar archivo ID=" + id + ": " + e.getMessage());
	                }
	            }

	        } catch (SQLException e) {
	            System.err.println("❌ Error al conectar o consultar base de datos: " + e.getMessage());
	        }
	    }
	}
