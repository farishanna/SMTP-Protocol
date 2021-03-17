package SMTP;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Spawn a new SMTP connection for the connected client.
 */
public class SMTPConnection implements Runnable {

  /* The socket to the client */
  private Socket socket;
  /* We need the name of the local machine and remote machine. */
  private String localHost;
  private PrintWriter toClient;
  private Scanner fromClient;

  public SMTPConnection(Socket socket) throws Exception {
    this.socket = socket;
  }

  public void run() {
    try {
      init();
      EmailMessage message = processMessage();
      saveMessage(message);
      close();
    } catch (Exception e) {
      System.err.println(e.toString());
    }
  }

  /**
   * Initialize variables.
   */
  private void init() throws IOException {
    toClient = new PrintWriter(socket.getOutputStream(), true);
    fromClient = new Scanner(socket.getInputStream());
    localHost = InetAddress.getLocalHost().getHostName();
  }

  /**
   * Accepts an email message
   */
  private EmailMessage processMessage() throws IOException {
    sendToClient(220, localHost);
    String clientHost = readMessageLine("HELLO");
    sendToClient(250, String.format("Hello %s, pleased to meet you", clientHost));
    String mailFrom = removeArrow(readMessageLine("MAIL FROM:"));
    sendToClient(250, "ok");
    String rcptTo = removeArrow(readMessageLine("RCPT TO:"));
    sendToClient(250, "ok");
    readMessageLine("DATA"); // expect data now
    sendToClient(354, "End data with <CR><LF>.<CR><LF>");
    String body = readData();
    sendToClient(250, "ok Message accepted for delivery");
    readMessageLine("QUIT");
    sendToClient(221, localHost + " closing connection");
    return new EmailMessage(
        mailFrom,
        Arrays.asList(rcptTo),
        body
    );
  }

  /**
   * Read one message line with an expected prefix or error
   *
   * @return Message without the prefix
   */
  private String readMessageLine(String expectedPrefix) throws IOException {
    String line = fromClient.nextLine();
    System.out.println("C: " + line);
    if (!line.startsWith(expectedPrefix)) {
      close();
      throw new RuntimeException("Invalid message");
    }
    return line.substring(expectedPrefix.length()).trim();
  }

  /**
   * Read email body into a string
   *
   * @return email body
   */
  private String readData() throws IOException {
    StringBuilder data = new StringBuilder();

    String line = fromClient.nextLine();
    System.out.println("C: " + line);
    while (!line.trim().equals(".")) {
      data.append(line);
      data.append("\n");
      line = fromClient.nextLine();
      System.out.println("C: " + line);
    }

    return data.toString();
  }

  /**
   * Remove starting and end arrow from the string
   */
  private String removeArrow(String s) {
    s = s.trim();
    if (!s.startsWith("<")) {
      throw new RuntimeException("invalid format: " + s);
    }
    if (!s.endsWith(">")) {
      throw new RuntimeException("invalid format: " + s);
    }
    return s.substring(1, s.length() - 1);
  }

  /**
   * Send a response to the client.
   *
   * @param responseCode code
   * @param message string message to send
   */
  private void sendToClient(int responseCode, String message) {
    if (socket.isClosed()) {
      throw new RuntimeException("socket closed, cannot send message");
    }
    toClient.println(responseCode + " " + message);
  }

  private void saveMessage(EmailMessage message) throws IOException {
    String rcpts = message.getTo().get(0);

    for (String rcpt :
        rcpts.split(",")) {
      PrintWriter file = new PrintWriter(new FileWriter(rcpt), true);
      file.println("MAIL FROM: <" + message.getFrom() + ">");
      file.println("RCPT TO <" + rcpts + ">");
      file.println(message.getBody());
      file.close();
    }
  }

  /**
   * Close streams and connections
   */
  private void close() throws IOException {
    if (toClient != null) {
      toClient.close();
    }
    if (fromClient != null) {
      fromClient.close();
    }
    if (socket != null) {
      socket.close();
    }
  }
}
