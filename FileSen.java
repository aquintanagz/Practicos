package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.sen.constantes.Globales;
import com.sen.dao.DBConfig;
import com.sen.dao.DBServices;
import com.sen.utilerias.FileSystemComponent;

public class FileSen {

	
	  
	  public static void main(String[] args) {

		
	  String ruta = "Z:\\DISCO_DURO";

      FileSen fileSen = new  FileSen(); 
      fileSen.guardarListAllFiles( ruta);
	  }
	
	 public  void guardarListAllFiles(String ruta) {

		  Path rootPath = Paths.get(ruta); 
		  
	      List<File> allFiles = new ArrayList<>(); 
	      try {
	    	  DBConfig dbc = new DBConfig();
	          DBServices bd = new DBServices(dbc);
	          listAllFiles_(rootPath, allFiles,bd);
	    	  System.out.println("Termino");
		} catch (IOException e) {
			e.printStackTrace();
		} 

	      System.out.println("Found files:"); 
	      allFiles.forEach(System.out::println); 
	  }
	 
	 public  void listAllFiles_(Path currentPath, List<File> allFiles,DBServices bd) 
		      throws IOException  
		    { 
		        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath))  
		        { 
		            for (Path entry : stream) { 
		                if (Files.isDirectory(entry)) { 
		                    listAllFiles_(entry, allFiles,bd); 
		                } else { 
		                	File file = entry.toFile();
		                	 long bytes = file.length();
		                	 long kilobytes = (bytes / 1024);
		                	 String nombre = file.getName();
		                	 String gpath = file.getPath();
		                	guardarFicheroArchivoVerificado_(nombre, gpath,kilobytes);
		                   // allFiles.add(file); 
		                } 
		            } 
		        } 
		    } 
	 
	  public boolean guardarFicheroArchivoVerificado_(String  nombreArchivo,String ruta, long size) {
		    boolean update = false;
		    Connection conn = null;
		    PreparedStatement ps = null;
		    try {
		      Properties properties = new Properties();
		      InputStream inputStream =
		          new FileInputStream(
		              Globales.DIR_CONFIGURACION + File.separator + Globales.ARCHIVO_PROPIEDADES_SERVIDOR);
		      properties.load(inputStream);

		      conn = DBConfig.getDataSource().getConnection();
		     
		      String stcUpdateStr = "UPDATE "
	              + "z_rutas_x"
	              + " SET ruta='"
	              + ruta
	              + "' WHERE "
	              + " ruta='"
	              + ruta +  "'";
		      ps =
		          conn.prepareStatement(stcUpdateStr
		              );
		      if (ps.executeUpdate() > 0) {
		        update = true;
		      } else {
		       // throw new SQLException("No se pudo actualizar la columna path de la tabla 'fichero_ver'");
		      
		      ps = null;

		      String insertControl =
		          "INSERT INTO public.z_rutas_x("
		              + "	 nombre, ruta,size_kb) "
		              + "	VALUES ( ?, ?, ?);";
		      ps = conn.prepareStatement(insertControl);
		      ps.setString(1, nombreArchivo);
		      ps.setString(2,ruta);
		      ps.setLong(3, size);
		      
		      
		      ps.execute();
		      
		      System.out.println("Se guardo en en el registro en z_rutas_x");
		      
		      } 
		    } catch (SQLException ex) {
		      //LOG.error("Ha ocurrido un error al escribir datos en la BD.", ex);
		    } catch (IOException ex) {
		      //LOG.error("Ha ocurrido un error al leer archivo properties.", ex);
		    } finally {
		      cierraRecursos(conn, ps, null, null);
		    }
		    return update;
		  }

 		
	  private static void cierraRecursos(
		      Connection con, PreparedStatement ps, CallableStatement cs, ResultSet rs) {
		    try {

		      if (ps != null) {
		        ps.close();
		      }
		      if (cs != null) {
		        cs.close();
		      }
		      if (rs != null) {
		        rs.close();
		      }
		      if (con != null) {
		        con.close();
		      }
		    } catch (Exception e) {
		      System.out.println("-------------------------\n");
		      System.out.println("ErrorCode.TAG_CERRAR_RECURSOS" + "Error al cerrar la conexión a la BD: " + e);
		    }
		  }
	  
	  /* 
	   CREATE TABLE public.z_rutas_x (
	id_z_rutas_x int4 NOT NULL DEFAULT nextval('z_rutas_x_seq'::regclass),
	nombre varchar(500) NULL,
	ruta text NOT NULL,
	size_kb float4 NULL,
	tipo varchar(40) NULL,
	orden int4 NULL,
	orden2 int4 NULL,
	seg varchar(40) NULL,
	date_captura timestamptz NULL DEFAULT now(),
	time_captura timestamptz NULL DEFAULT now(),
	z_ruta_desc int4 NULL,
	descripcion varchar(200) NULL
);

CREATE SEQUENCE public.z_rutas_x_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1;
	   * */
		  

}
