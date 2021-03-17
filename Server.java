package SMTP;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {

  public static void main(String args[]) throws Exception {

    /* Set the port number. */
    int port = 25;

    /* Establish the listen socket. */
    ServerSocket mailSocket = new ServerSocket(port);

    /* Process SMTP client requests in an infinite loop. */
    while (true) {
      Socket socket = mailSocket.accept();
      System.out.println("Client connected");
      SMTPConnection connection = new SMTPConnection(socket);

      Thread thread = new Thread(connection);

      thread.start();
    }
  }
}
