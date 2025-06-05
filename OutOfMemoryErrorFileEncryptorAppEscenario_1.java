package application;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.Key;
import java.sql.*;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;

import com.sen.constantes.Globales;

public class OutOfMemoryErrorFileEncryptorAppEscenario_1 {

   /*Para correr este script a manera de ejemplo considerar tener un archivo pesado*/
	/* */
    public static void main(String[] args) {
    	
     			String ruta ="C:\\Lapras\\PUERTAS\\PROYECTOS_06_ESCENARIO_1\\ARCHIVO_PESADO\\DGRMSG_DA_2023_SERV-DGRMSG-2024-01-025_01.pdf" ;
     			
                File inputFile = new File(ruta);
                if (inputFile.exists()) {
                    // Obtener el directorio del archivo original
                    String parentDir = inputFile.getParent();

                    // Crear el nuevo directorio destino (agregar "_")
                    String newDir = parentDir.replace("ARCHIVO_PESADO", "ARCHIVO_PESADO_ENC");

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

                  
                } else {
                    System.err.println("Archivo no encontrado: " + ruta);
                }
            
        
    }
    
    /*Por este metodo marca error de memoria en archivos muy grandes , porque todo lo guarda en la RAM*/
    public static void encryptFile_(File inputFile, File outputFile) {
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
    
   
    public static void encryptFile(File inputFile, File outputFile) {
        try {
            Key key = generarKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            try (FileInputStream fis = new FileInputStream(inputFile);
                 FileOutputStream fos = new FileOutputStream(outputFile);
                 CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

                byte[] buffer = new byte[8192]; // 8 KB por bloque
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    cos.write(buffer, 0, bytesRead);
                }

            }
        } catch (Exception e) {
            System.err.println("Error al encriptar archivo: " + e.getMessage());
        }
    }
    
    public static void decryptFile(File inputFile, File outputFile) {
        try {
            Key key = generarKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            try (FileInputStream fis = new FileInputStream(inputFile);
                 CipherInputStream cis = new CipherInputStream(fis, cipher);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = cis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }

            }
        } catch (Exception e) {
            System.err.println("Error al desencriptar archivo: " + e.getMessage());
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
