package tourGuide;


import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import tourGuide.controller.AttractionController;
import tourGuide.model.User;
import tourGuide.service.AttractionsService;
import tourGuide.service.UserService;


@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AttractionController.class)
public class AttractionsControllerTest {
		@Autowired
		MockMvc mockMvc;
		
		@MockBean
		AttractionsService attractionsService;
		
		@MockBean
		UserService userService;
		
		@Test
		public void getFiveNearByAttractionsShouldReturnOk() throws Exception {
			User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
			JSONObject json = new JSONObject();
			given(userService.getUser("jon")).willReturn(user);
	    	given(attractionsService.getFiveNearAttractionsWithDistanceAndRewardsFromCurrentUserLocation(
	    			userService.getUser("jon"))).willReturn(json);
	    	mockMvc.perform(get("/getNearbyAttractions").param("userName", "jon")).andExpect(status().isOk());
		}

}
