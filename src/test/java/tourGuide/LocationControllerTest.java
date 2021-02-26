package tourGuide;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.controller.LocationController;
import tourGuide.service.LocationsService;
import tourGuide.service.UserService;
import tourGuide.user.User;


@RunWith(SpringRunner.class)
@WebMvcTest(controllers = LocationController.class)
public class LocationControllerTest {
		@Autowired
		MockMvc mockMvc;
		
		@MockBean
		LocationsService locationsService;
		
		@MockBean
		UserService userService;
	
		@Test
		public void getGetLocationShouldReturnOk() throws Exception {
			User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
			Location location = new Location(1.2,1.2);
			VisitedLocation visitedLocation = new VisitedLocation(UUID.randomUUID(), location, null);
			given(userService.getUser("jon")).willReturn(user);
	    	given(locationsService.getUserLocation(userService.getUser("jon"))).willReturn(visitedLocation);
			mockMvc.perform(get("/getLocation").param("userName", "jon")).andExpect(status().isOk());
		}
		
		@Test
		public void getAllUsersLocationsShouldReturnOk() throws Exception {
			List<User> users = new ArrayList<User>();
			User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
			Location location = new Location(1.2,1.2);
			VisitedLocation visitedLocation = new VisitedLocation(UUID.randomUUID(), location, null);
			user.addToVisitedLocations(visitedLocation);
			users.add(user);
			JSONObject json = new JSONObject();
			
	  	   	given(userService.getAllUsers()).willReturn(users);	
	  	   	given(locationsService.getAllUsersLocation(userService.getAllUsers())).willReturn(json);	
			
	    	mockMvc.perform(get("/getAllCurrentLocations")).andExpect(status().isOk());
	 	}

}
