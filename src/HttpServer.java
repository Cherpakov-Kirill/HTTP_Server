import FileSender.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HttpServer {
    ///Start page: http://localhost:8080/
    private static boolean checkMimeTypes(BufferedReader input) {
        String accept = "";
        try {
            accept = input.readLine();
            while (!accept.startsWith("Accept")) {
                accept = input.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        accept = accept.substring(8);
        int lenAccept = accept.length();
        while (lenAccept > 0) {
            int stopInx;
            int semicolon = accept.indexOf(";");
            int commaInx = accept.indexOf(",");
            if (commaInx != -1) {
                if (semicolon != -1) {
                    stopInx = Math.min(semicolon, commaInx);
                }
                else stopInx = commaInx;

            } else {
                if(semicolon!=-1) stopInx = semicolon;
                else stopInx = accept.length();
            }

            String checkType = accept.substring(0, stopInx);
            if(!checkType.contains("*")){
                if (checkType.equals("text/plain") || checkType.equals("image/jpeg") || checkType.equals("text/html"))
                    return true;
            }
            else{
                if(checkType.equals("*/*")) return true;
                if(checkType.equals("image/*")) return true;
                if(checkType.equals("text/*")) return true;
            }

            if(commaInx == -1 ) return false;
            accept = accept.substring(commaInx + 1);
            if(accept.isEmpty()) return false;
            lenAccept = accept.length();
        }
        return false;
    }

    private static boolean workingWithRequests(Socket socket) {
        boolean keepAliveConnection = false;
        try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintStream outputStream = new PrintStream(socket.getOutputStream())) {

            while (!input.ready()) ;

            input.mark(2000);

            if (input.readLine().isEmpty()) {
                throw new IOException("Connection is fault!");
            }
            input.reset();

            System.out.println();
            System.out.println("Client's request:");
            while (input.ready()) {
                String str = input.readLine();
                if (str.equals("Connection: keep-alive")) {
                    keepAliveConnection = true;
                }
                System.out.println(str);
            }

            input.reset();

            String header = input.readLine();

            if(!checkMimeTypes(input)){
                System.out.println("Server's answer:");
                System.err.println("Client's MIME-types not supported");
                outputStream.println("HTTP/1.1 405 Method Not Allowed");
                System.err.println("405 Error");
            }

            if (header.equals("GET / HTTP/1.1")) {
                File file = new File(new File("").getAbsolutePath(), "mainPage.html");
                FileSender sender = new FileSender(keepAliveConnection, file, outputStream);
                sender.sendFileToSocket();
            } else {
                String fullPath = header.substring(4, header.lastIndexOf(" "));
                String dir = fullPath.substring(fullPath.indexOf("/"), fullPath.lastIndexOf("/"));
                String fileName = fullPath.substring(fullPath.lastIndexOf("/") + 1);
                File file = new File(new File("").getAbsolutePath() + dir, fileName);
                System.out.println(file.getAbsolutePath());
                System.out.println(file.getName());
                FileSender sender = new FileSender(keepAliveConnection, file, outputStream);
                sender.sendFileToSocket();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keepAliveConnection;
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server started!");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected!");
                boolean keepAliveConnection = workingWithRequests(socket);
                while (keepAliveConnection) {
                    keepAliveConnection = workingWithRequests(socket);
                }
                System.out.println("Client disconnected!");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}