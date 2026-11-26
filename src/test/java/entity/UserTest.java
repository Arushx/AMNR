package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void storesUsernameAndPassword() {
    User user = new User("andrew", "secret");

    assertEquals("andrew", user.getUsername());
    assertEquals("secret", user.getPassword());
  }

  @Test
  void rejectsNullUsername() {
    assertThrows(IllegalArgumentException.class, () -> new User(null, "secret"));
  }

  @Test
  void rejectsBlankUsername() {
    assertThrows(IllegalArgumentException.class, () -> new User("   ", "secret"));
  }

  @Test
  void rejectsUsernameWithSpaces() {
    assertThrows(IllegalArgumentException.class, () -> new User("andrew tu", "secret"));
  }

  @Test
  void rejectsNullPassword() {
    assertThrows(IllegalArgumentException.class, () -> new User("andrew", null));
  }

  @Test
  void rejectsBlankPassword() {
    assertThrows(IllegalArgumentException.class, () -> new User("andrew", ""));
  }
}
