package tourGuide;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import tourGuide.controller.TripDealsController;
import tourGuide.model.User;
import tourGuide.service.TripPricerService;
import tourGuide.service.UserService;
import tripPricer.Provider;


@RunWith(SpringRunner.class)
@WebMvcTest(controllers = TripDealsController.class)
public class TripDealsControllerTest {
	@Autowired
	MockMvc mockMvc;

	@MockBean
	TripPricerService tripPricerService;

	@MockBean
	UserService userService;

	@Test
	public void getTripDealsShouldReturnOk() throws Exception {
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		given(userService.getUser("jon")).willReturn(user);
		List<Provider> providers = new ArrayList<Provider>();
		given(tripPricerService.getTripDeals(userService.getUser("jon"))).willReturn(providers);
		mockMvc.perform(get("/getTripDeals").param("userName", "jon")).andExpect(status().isOk());

	}


}
