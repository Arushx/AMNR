package entity;

/**
 * An account that can log in to the app.
 *
 * <p>Instances are immutable. The password is stored as plain text.
 */
public class User {

  private final String username;
  private final String password;

  /**
   * Creates a user.
   *
   * @param username the name used to log in; must not be blank or contain whitespace
   * @param password the password used to log in; must not be blank
   * @throws IllegalArgumentException if the username or password is invalid
   */
  public User(String username, String password) {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("username must not be blank");
    }
    if (username.chars().anyMatch(Character::isWhitespace)) {
      throw new IllegalArgumentException("username must not contain spaces");
    }
    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("password must not be blank");
    }
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
