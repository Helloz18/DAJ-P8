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
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.controller.TourGuideController;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;


@RunWith(SpringRunner.class)
@WebMvcTest(controllers = TourGuideController.class)
public class TestTourGuideController {

	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	TourGuideService tourGuideService;
	
	@Test
	public void getMainPageShouldReturnOk() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().isOk());
	}
	
	@Test
	public void getGetLocationShouldReturnOk() throws Exception {
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Location location = new Location(1.2,1.2);
		VisitedLocation visitedLocation = new VisitedLocation(UUID.randomUUID(), location, null);
		given(tourGuideService.getUser("jon")).willReturn(user);
    	given(tourGuideService.getUserLocation(tourGuideService.getUser("jon"))).willReturn(visitedLocation);
		mockMvc.perform(get("/getLocation").param("userName", "jon")).andExpect(status().isOk());
	}
	
	@Test
	public void getFiveNearByAttractionsShouldReturnOk() throws Exception {
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		JSONObject json = new JSONObject();
		given(tourGuideService.getUser("jon")).willReturn(user);
    	given(tourGuideService.getFiveNearAttractionsWithDistanceAndRewardsFromCurrentUserLocation(
    			tourGuideService.getUser("jon"))).willReturn(json);
    	mockMvc.perform(get("/getNearbyAttractions").param("userName", "jon")).andExpect(status().isOk());
	}
	
	@Test
	public void getRewardsShouldReturnOk() throws Exception {
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		given(tourGuideService.getUser("jon")).willReturn(user);
		List<UserReward> value = new ArrayList<UserReward>();
    	given(tourGuideService.getUserRewards(tourGuideService.getUser("jon"))).willReturn(value);
    	mockMvc.perform(get("/getRewards").param("userName", "jon")).andExpect(status().isOk());
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
		
  	   	given(tourGuideService.getAllUsers()).willReturn(users);	
  	   	given(tourGuideService.getAllUsersLocation(tourGuideService.getAllUsers())).willReturn(json);	
		
    	mockMvc.perform(get("/getAllCurrentLocations")).andExpect(status().isOk());
 	}
	
	@Test
	public void getTripDealsShouldReturnOk() throws Exception {
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		given(tourGuideService.getUser("jon")).willReturn(user);
	  	List<Provider> providers = new ArrayList<Provider>();
		given(tourGuideService.getTripDeals(tourGuideService.getUser("jon"))).willReturn(providers);
		mockMvc.perform(get("/getTripDeals").param("userName", "jon")).andExpect(status().isOk());
  
	}
}
