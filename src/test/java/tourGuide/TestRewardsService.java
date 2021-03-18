package tourGuide;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;


import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.service.LocationsService;
import tourGuide.service.RewardsService;
import tourGuide.service.TestService;

public class TestRewardsService {
	
	
	GpsUtil gpsUtil = new GpsUtil();
	RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
	TestService testService = new TestService(gpsUtil, rewardsService);
	LocationsService locationsService = new LocationsService();

	@Before
	public void init() {
		Locale.setDefault(Locale.US);
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
		
	}
	
	@Test
	public void nearAttrationTest() {
		User user = testService.getAllUsers().get(0);
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		Attraction attraction = gpsUtil.getAttractions().get(0);
		assertTrue(rewardsService.nearAttraction(visitedLocation, attraction));
	}
	

	
	//changement de nom : test d'originie nearAllAttractions, changé en calculateRewards
	// modification du service, à la place du forEach, une boucle for seulement
	// méthode très longue
	@Test
	public void testCalculateRewards() throws JSONException, IOException {
		User user = testService.getAllUsers().get(0);
		rewardsService.calculateRewards(user);
		List<UserReward> userRewards = rewardsService.getUserRewards(user);
		testService.tracker.stopTracking();
		assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
	}
	
//	@Test
//	public void userGetRewards() throws JSONException, IOException {	
//		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
//		
//		User user = testService.getAllUsers().get(0);
//
//	List<UserReward> userRewards = rewardsService.getUserRewards(user);
//	locationsService.trackUserLocation(user);
////		System.out.println(userRewards.size());
////		
////		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
////		user.addToVisitedLocations(visitedLocation);
////		rewardsService.calculateRewards(user);
//		
//		testService.tracker.stopTracking();
//		assertTrue(userRewards.size() > 0);
//	}
	
//	@Test
//	public void isWithinAttractionProximity() {
//		Attraction attraction = gpsUtil.getAttractions().get(0);
//		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
//	}
	
	
}
