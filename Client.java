package SMTP;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Client that sends message to the server.
 */
public class Client {

  private String serverHost;
  private int port = 25; // smtp port
  private Socket socket;

  private Scanner fromServer;
  private PrintWriter toServer;

  public Client(String serverHost) throws IOException {
    this.serverHost = serverHost;
    socket = new Socket(serverHost, port);

    fromServer = new Scanner(socket.getInputStream());
    toServer = new PrintWriter(socket.getOutputStream(), true);
  }


  public void sendMessage(EmailMessage message) throws IOException {
    System.out.println("Sending message...");
    responseCodeOrError(220, "Invalid Server / not an smtp server");
    toServer.println("HELLO " + InetAddress.getLocalHost().getHostName());
    responseCodeOrError(250, "Invalid server");
    toServer.println("MAIL FROM: <" + message.getFrom() + ">");
    responseCodeOrError(250, "Invalid FROM address");
    toServer.println("RCPT TO: <" + message.toAddress() + ">");
    responseCodeOrError(250, "Invalid Recipient");
    toServer.println("DATA");
    responseCodeOrError(354, "Data entry not accepted");
    toServer.println(message.getBody());
    toServer.println(".");
    responseCodeOrError(250, "Sending data failed");
    quit();
    System.out.println("Message sent successfully");
  }

  private int responseCode() throws IOException {
    String serverLine = fromServer.nextLine();
    System.out.println("S: " + serverLine);
    return Integer.parseInt(serverLine.substring(0, 3));
  }

  private void responseCodeOrError(int responseCode, String errorMessage) throws IOException {
    if (responseCode() != responseCode) {
      quit();
      throw new RuntimeException(errorMessage);
    }
  }

  private void quit() throws IOException {
    if (toServer != null) {
      toServer.println("QUIT");
      responseCodeOrError(221, "server did not close connection");
    }
    close();
  }

  public void close() throws IOException {
    if (toServer != null) {
      toServer.close();
    }
    if (fromServer != null) {
      fromServer.close();
    }
    if (socket != null) {
      socket.close();
    }
  }

  public static void main(String[] args) {
    EmailMessage emailMessage = new EmailMessage(
        "student@reading.ac.uk",
        Arrays.asList("student-loan@gov.uk"),
        "Hello loan team,\n"
            + "If I don't study, will I lose my loan?"
    );

    try {
      Client client = new Client("localhost");
      client.sendMessage(emailMessage);
      client.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
