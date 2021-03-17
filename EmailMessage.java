package SMTP;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Encapsulates a message.
 */
public class EmailMessage {

  private String from;
  private List<String> to;
  private String body;

  public EmailMessage(String from, List<String> to, String body) {
    this.from = from;
    this.to = to;
    this.body = body;
  }

  public String getFrom() {
    return from;
  }

  public List<String> getTo() {
    return to;
  }

  public String getBody() {
    return body;
  }

  public String toAddress() {
    return to.stream().collect(Collectors.joining(","));
  }
}
