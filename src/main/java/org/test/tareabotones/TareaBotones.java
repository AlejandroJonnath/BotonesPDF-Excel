package org.test.tareabotones;

/* Preparado por Alejandro Bedoya
 * Este servlet permite descargar archivos (PDF o Excel) desde el servidor.
 * - Usa parámetros de la URL (?type=pdf/excel) para seleccionar el archivo.
 * - Valida tipos de archivo permitidos y maneja errores (404 si no existe, 400 si tipo inválido).
 * - Configura cabeceras HTTP para forzar la descarga en el navegador.
 * - Transfiere el archivo en bloques de 4KB para eficiencia.
 */

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;

@WebServlet("/download")//Esto mapea el servlet a la URL /download
public class TareaBotones extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // OBTENER PARÁMETRO ----------------------------------------------
        String type = req.getParameter("type");  //Lee el parámetro type de la URL
        String fileName;  // Almacenará el nombre del archivo a descargar
        String mimeType; // Almacenará el tipo MIME (ej. application/pdf)

        // VALIDAR TIPO DE ARCHIVO ----------------------------------------
        if ("pdf".equalsIgnoreCase(type)) {// Si el tipo es PDF (no sensible a mayúsculas)
            fileName = "ejemplo.pdf";// Asigna nombre de archivo
            mimeType = "application/pdf";// Tipo MIME para PDFs
        } else if ("excel".equalsIgnoreCase(type)) { // Si el tipo es Excel
            fileName = "ejemplo.xlsx";// Nombre archivo Excel
            mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";//MIME específico para Excel
        } else {
            //Si el tipo es inválido, envía error HTTP 400 (Bad Request)
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tipo de archivo inválido");
            return; //Termina la ejecución aquí
        }

        //BUSCAR ARCHIVO EN EL SERVIDOR ----------------------------------
        // Obtiene la ruta real del directorio /files dentro del proyecto
        String filesDir = getServletContext().getRealPath("/files/");
        File file = new File(filesDir, fileName); // Crea objeto File con la ruta completa

        if (!file.exists()) { //Verifica si el archivo existe físicamente
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Archivo no encontrado");
            return; // Si no existe, envía error 404 y termina
        }

        //CONFIGURAR RESPUESTA HTTP --------------------------------------
        resp.setContentType(mimeType);//Indica el tipo de contenido (MIME)
        //Fuerza la descarga (attachment) en lugar de abrir en el navegador:
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setContentLengthLong(file.length()); // Tamaño del archivo en bytes

        //TRANSFERIR ARCHIVO ---------------------------------------------
        try (
                //Buffers optimizan lectura/escritura usando bloques de 4KB (generalmente es para archivos pesados)
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(file)); // Lee el archivo
                BufferedOutputStream out = new BufferedOutputStream(resp.getOutputStream()) // Escribe a la respuesta
        ) {
            byte[] buffer = new byte[4096]; // Buffer de 4KB (tamaño estándar)
            int bytesRead; // Cantidad de bytes leídos en cada iteración

            // Lee el archivo en bloques hasta que no quede nada (-1)
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead); // Escribe el bloque leído
            }
        }//El try-with-resources cierra automáticamente los streams (¡no hay fugas de recursos!)
    }
}

/*

CONCLUSIONES:

 * - Los servlets usan doGet() para manejar solicitudes GET.
 * - Los MIME Types son cruciales para que el navegador sepa cómo manejar el archivo.
 * - Siempre validar entradas del usuario (ej. el parámetro 'type').
 * - Usar buffers mejora el rendimiento vs leer/escribir byte por byte.
 * - try-with-resources (Java 7+) evita tener que cerrar manualmente los streams.

 */