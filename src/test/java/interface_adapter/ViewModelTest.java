package interface_adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ViewModelTest {

  private ViewManagerModel viewModel;
  private List<PropertyChangeEvent> events;

  @BeforeEach
  void setUp() {
    viewModel = new ViewManagerModel();
    events = new ArrayList<>();
    viewModel.addPropertyChangeListener(events::add);
  }

  @Test
  void storesViewName() {
    assertEquals("view manager", viewModel.getViewName());
  }

  @Test
  void settingFieldsDoesNotNotifyUntilFired() {
    viewModel.setActiveView("next");

    assertTrue(events.isEmpty());
  }

  @Test
  void firePropertyChangeSendsTheViewModel() {
    viewModel.setActiveView("next");
    viewModel.firePropertyChange();

    assertEquals(1, events.size());
    assertEquals(ViewModel.STATE_PROPERTY, events.get(0).getPropertyName());
    assertSame(viewModel, events.get(0).getNewValue());
  }

  @Test
  void firesEvenWhenNothingChanged() {
    viewModel.firePropertyChange();
    viewModel.firePropertyChange();

    assertEquals(2, events.size());
  }

  @Test
  void customPropertyNameIsPassedThrough() {
    viewModel.firePropertyChange("reset");

    assertEquals("reset", events.get(0).getPropertyName());
  }

  @Test
  void viewManagerStartsWithNoActiveView() {
    assertEquals("", viewModel.getActiveView());
  }
}
