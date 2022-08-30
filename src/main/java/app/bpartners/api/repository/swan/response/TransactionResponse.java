package app.bpartners.api.repository.swan.response;

import app.bpartners.api.repository.swan.model.Transaction;
import java.util.List;

public class TransactionResponse {
  public Data data;

  public static class Data {
    public Account accounts;
  }

  public static class Account {
    public List<Edge> edges;
  }

  public static class Edge {
    public Node node;
  }

  public static class Node {
    public Transactions transactions;
  }

  public static class Transactions {
    public List<Transaction> edges;
  }
}
