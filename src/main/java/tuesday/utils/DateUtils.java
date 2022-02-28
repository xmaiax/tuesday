package tuesday.utils;

public class DateUtils {

  private final java.text.SimpleDateFormat sdf =
    new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

  private static final DateUtils INSTANCE = new DateUtils();
  private DateUtils() { }
  public static DateUtils getInstance() { return INSTANCE; }

  public String formatDate(java.util.Date date) {
    return this.sdf.format(date);
  }

}
