package tuesday.utils;

public class ResouceReader {

  private static final ResouceReader INSTANCE = new ResouceReader();
  private ResouceReader() { }
  public static ResouceReader getInstance() { return INSTANCE; }

  public java.io.InputStream getResourceAsStream(String resource) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
  }

}
