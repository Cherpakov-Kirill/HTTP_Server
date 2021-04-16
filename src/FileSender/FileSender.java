package FileSender;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileSender {
    private final File file;
    private final PrintStream outputStream;
    private final boolean keepConnectionAlive;

    public FileSender(boolean keepAlive, File sendFile, PrintStream out) {
        file = sendFile;
        outputStream = out;
        keepConnectionAlive = keepAlive;
    }

    private String getFileExtension() {
        String fileName = file.getName();
        // если в имени файла есть точка и она не является первым символом в названии файла
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            // то вырезаем все знаки после последней точки в названии файла, то есть ХХХХХ.txt -> txt
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }

    private String printContentType() {
        String contentType = "";
        switch (getFileExtension()) {
            case "txt" -> {
                return "Content-Type: text/plain";
            }
            case "jpeg" -> {
                return "Content-Type: image/jpeg";
            }
            case "html" -> {
                return "Content-Type: text/html";
            }
            default -> {
                return "WRONG!";
            }
        }
    }

    private void printDate() {
        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss z");
        System.out.println("Date: " + formatter.format(currentDate));
        outputStream.println("Date: " + formatter.format(currentDate));
    }

    public void sendFileToSocket() {
        byte[] byteArray = new byte[(int) file.length() + 1];
        long s = file.length();
        try {
            FileInputStream fis = new FileInputStream(file.getPath());
            String content = printContentType();
            System.out.println("Server's answer:");
            if (content.equals("WRONG!")) {
                outputStream.println("HTTP/1.1 405 Method Not Allowed");
                System.err.println("405 Error");
                return;
            }
            outputStream.println("HTTP/1.1 200 OK");
            System.out.println("HTTP/1.1 200 OK");
            outputStream.println("Server: CherpakovKirill");
            System.out.println("Server: CherpakovKirill");
            printDate();
            long lastMod = file.lastModified();
            Date modifiedDate = new Date(lastMod);
            SimpleDateFormat formatter = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss z");
            outputStream.println("Last-Modified: " + formatter.format(modifiedDate));
            System.out.println("Last-Modified: " + formatter.format(modifiedDate));

            if (keepConnectionAlive) {
                outputStream.println("Connection: keep-alive");
                System.out.println("Connection: keep-alive");
            } else {
                outputStream.println("Connection: close");
                System.out.println("Connection: close");
            }
            outputStream.println("Content-Length: " + file.length());
            System.out.println("Content-Length: " + file.length());
            outputStream.println(content);
            System.out.println(content);

            outputStream.println();
            while (s > 0) {
                int len = fis.read(byteArray);
                outputStream.write(byteArray, 0, len);
                s -= len;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Server's answer:");
            System.err.println("File not found!");
            outputStream.println("HTTP/1.1 404 Not Found");
            System.err.println("404 Error");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
