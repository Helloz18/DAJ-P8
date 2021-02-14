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
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

public class TestRewardsService {
	
	//déplacement des déclarations, et suppression de InternalTestHelper
	
	GpsUtil gpsUtil = new GpsUtil();
	RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
	TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);


	@Before
	public void init() {
		Locale.setDefault(Locale.US);
	}
	
	@Test
	public void userGetRewards() {	
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsUtil.getAttractions().get(0);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		tourGuideService.trackUserLocation(user); // appelle calculate rewards
		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.tracker.stopTracking();
		assertTrue(userRewards.size() == 1);
	}
	
	@Test
	public void isWithinAttractionProximity() {
		Attraction attraction = gpsUtil.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}
	
	
	//changement de nom : test d'originie nearAllAttractions, changé en calculateRewards
	//@Ignore // Needs fixed - can throw ConcurrentModificationException
	// modification du service, à la place du forEach, une boucle for seulement
	// méthode très longue
	@Test
	public void testCalculateRewards() {
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
		
		// V2 - paramétrer un utilisateur
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsUtil.getAttractions().get(0);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		rewardsService.calculateRewards(user);
		List<UserReward> userRewards = tourGuideService.getUserRewards(user);
		tourGuideService.tracker.stopTracking();

		// V1	
//		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
//		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
//		tourGuideService.tracker.stopTracking();

		assertTrue(userRewards.size() == 1);
	}
	
}