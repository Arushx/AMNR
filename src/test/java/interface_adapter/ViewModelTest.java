package interface_adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ViewModelTest {

  @Test
  void storesViewNameAndInitialState() {
    ViewModel<String> viewModel = new ViewModel<>("demo", "start");

    assertEquals("demo", viewModel.getViewName());
    assertEquals("start", viewModel.getState());
  }

  @Test
  void setStateDoesNotNotifyUntilFired() {
    ViewModel<String> viewModel = new ViewModel<>("demo", "start");
    List<PropertyChangeEvent> events = new ArrayList<>();
    viewModel.addPropertyChangeListener(events::add);

    viewModel.setState("next");

    assertTrue(events.isEmpty());
  }

  @Test
  void firePropertyChangeSendsCurrentState() {
    ViewModel<String> viewModel = new ViewModel<>("demo", "start");
    List<PropertyChangeEvent> events = new ArrayList<>();
    viewModel.addPropertyChangeListener(events::add);

    viewModel.setState("next");
    viewModel.firePropertyChange();

    assertEquals(1, events.size());
    assertEquals("state", events.get(0).getPropertyName());
    assertEquals("next", events.get(0).getNewValue());
  }

  @Test
  void firesEvenWhenStateIsUnchanged() {
    ViewModel<String> viewModel = new ViewModel<>("demo", "same");
    List<PropertyChangeEvent> events = new ArrayList<>();
    viewModel.addPropertyChangeListener(events::add);

    viewModel.firePropertyChange();
    viewModel.firePropertyChange();

    assertEquals(2, events.size());
  }

  @Test
  void viewManagerStartsWithNoActiveView() {
    assertEquals("", new ViewManagerModel().getState());
  }
}
