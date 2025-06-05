package application;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.Key;
import java.sql.*;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;

import com.sen.constantes.Globales;

public class FileEncryptorApp {

    private static final String DB_URL = "jdbc:postgresql://192.168.10.130:5432/efilecm-quintana";
    private static final String DB_USER = "efilecm-quintana";
    private static final String DB_PASSWORD = "quintaS3sadan4d0#1rs9";

    public static void main(String[] args) {
    	
    	 Properties properties = new Properties();
         InputStream inputStream;
		try {
			inputStream = new FileInputStream(
                 Globales.DIR_CONFIGURACION + File.separator + Globales.ARCHIVO_PROPIEDADES_SERVIDOR);
			
			 properties.load(inputStream);

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		

	  	  String path = properties.getProperty("webService.sen.pathDestino") ;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            List<String> rutas = obtenerRutasDesdeBD_(conn);

            for (String ruta : rutas) {
            	/*ruta = path +ruta;*/
                File inputFile = new File(ruta);
                if (inputFile.exists()) {
                    // Obtener el directorio del archivo original
                    String parentDir = inputFile.getParent();

                    // Crear el nuevo directorio destino (agregar "_")
                    String newDir = parentDir.replace("EXPEDIENTES_ZPR", "EXPEDIENTES_ZPR_ENC");

                    // Asegurarse de que la nueva carpeta exista
                    File newDirFile = new File(newDir);
                    if (!newDirFile.exists()) {
                        newDirFile.mkdirs(); // Crear la carpeta si no existe
                    }

                    // El nombre del archivo encriptado
                    File outputFile = new File(newDir, inputFile.getName() );

                    // Llamar al método de encriptación
                    encryptFile(inputFile, outputFile);
                    System.out.println("Archivo encriptado: " + outputFile.getAbsolutePath());

                    // Opcional: actualizar BD
                    marcarComoProcesado_(conn, ruta);
                } else {
                    System.err.println("Archivo no encontrado: " + ruta);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en la base de datos: " + e.getMessage());
        }
    }
    
    public static void encryptFile(File inputFile, File outputFile) {
        try {
            byte[] contentFile = FileUtils.readFileToByteArray(inputFile);
            doCrypto(Cipher.ENCRYPT_MODE, contentFile, outputFile);
        } catch (Exception e) {
        	  System.err.println("Error en la base de datos: " + e.getMessage());

           }
    }

    private static void doCrypto(int cipherMode, byte[] contentFile, File outputFile) {
        try {
            Key key = generarKey();
            Cipher c = Cipher.getInstance("AES");
            c.init(cipherMode, key);
            byte[] outputBytes = c.doFinal(contentFile);
            FileUtils.writeByteArrayToFile(outputFile, outputBytes);
        } catch (Exception e) {
        	 System.err.println("a ocurrido un error al encriptar archivo.: " + e.getMessage());
 }
    }
    
    private void doCryptoA(int cipherMode, byte[] contentFile, File outputFile) {
  	  byte[] outputBytes = null;
  	  try {
        Key key = generarKey();
        Cipher c = Cipher.getInstance("AES");
        c.init(cipherMode, key);
        outputBytes = c.doFinal(contentFile);
        FileUtils.writeByteArrayToFile(outputFile, outputBytes);
        
      } catch (Exception e) {
    	  System.err.println("ha ocurrido un error al encriptar archivo.");
      }
    }

    private static Key generarKey() {
        byte[] keyValue =
                new byte[] {'Y', 'o', 'u', 'B', 'e', 's', 't', 'F', 'r', 'i', 'e', 'n', 'd', 'K', 'e', 'y'};
        return new SecretKeySpec(keyValue, "AES");
    }

    private static List<String> obtenerRutasDesdeBD(Connection conn) throws SQLException {
        List<String> rutas = new ArrayList<>();
        String query = "SELECT ruta_archivo FROM archivos WHERE procesado = false";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                rutas.add(rs.getString("ruta_archivo"));
            }
        }
        return rutas;
    }

    private static List<String> obtenerRutasDesdeBD_(Connection conn) throws SQLException {
        List<String> rutas = new ArrayList<>();
        String query = "SELECT destino FROM zpe_rutas where status = 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                rutas.add(rs.getString("destino"));
            }
        }
        return rutas;
    }

    
    
    private static void marcarComoProcesado(Connection conn, String ruta) throws SQLException {
        String update = "UPDATE archivos SET procesado = true WHERE ruta_archivo = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(update)) {
            pstmt.setString(1, ruta);
            pstmt.executeUpdate();
        }
    }
    
    private static void marcarComoProcesado_(Connection conn, String ruta) throws SQLException {
        String update = "UPDATE zpe_rutas SET status = 1 WHERE destino = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(update)) {
            pstmt.setString(1, ruta);
            pstmt.executeUpdate();
        }
    }
}
