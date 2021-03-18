package tourGuide;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;


import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
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

	}
	
	@Test
	public void userGetRewards() {	
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsUtil.getAttractions().get(0);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		UserReward userReward = new UserReward(
				new VisitedLocation(user.getUserId(), new Location(1.2, 2.3), null),
				new Attraction("nameOfAttraction", "cityOfAttraction", "stateOfAttraction",1.2, 2.3),
					(int) (Math.random() * ( 1000 - 100 )));
		user.addUserReward(userReward);
		List<UserReward> userRewards = rewardsService.getUserRewards(user);
		testService.tracker.stopTracking();
		assertTrue(userRewards.size() > 0);
	}
	
	@Test
	public void isWithinAttractionProximity() {
		Attraction attraction = gpsUtil.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}
	
	
	//changement de nom : test d'originie nearAllAttractions, changé en calculateRewards
	// modification du service, à la place du forEach, une boucle for seulement
	// méthode très longue
	@Test
	public void testCalculateRewards() {
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

		rewardsService.calculateRewards(testService.getAllUsers().get(0));
		List<UserReward> userRewards = rewardsService.getUserRewards(testService.getAllUsers().get(0));
		testService.tracker.stopTracking();
		assertTrue(userRewards.size() == 1);
	}
	
}
