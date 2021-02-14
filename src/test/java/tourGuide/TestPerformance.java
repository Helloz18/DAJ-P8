package tourGuide;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.jsoniter.output.JsonStream;

import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestPerformance {

	/*
	 * A note on performance improvements:
	 *     
	 *     The number of users generated for the high volume tests can be easily adjusted via this method:
	 *     
	 *     		InternalTestHelper.setInternalUserNumber(100000);
	 *     
	 *     
	 *     These tests can be modified to suit new solutions, just as long as the performance metrics
	 *     at the end of the tests remains consistent. 
	 * 
	 *     These are performance metrics that we are trying to hit:
	 *     
	 *     highVolumeTrackLocation: 100,000 users within 15 minutes:
	 *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
	 *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */
	
	//déplacement de ces déclarations en dehors de chaque test
	GpsUtil gpsUtil = new GpsUtil();
	RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
	TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
	
	private Logger logger = LoggerFactory.getLogger(Tracker.class);
	
	/**
	 * méthode ajoutée pour paramétrer la locale par défaut dans tous les tests de la classe
	 */
	@Before
	public void init() {
		Locale.setDefault(Locale.US);
		logger.debug("number of users set "+InternalTestHelper.getInternalUserNumber());
	}
	
	@Test
	public void highVolumeTrackLocation() throws InterruptedException, ExecutionException {
		// GIVEN
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();		

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		////v2 - utilisation d'un pool de thread : amélioration des résultats
		int nombre = 1000;
		ExecutorService executorService = Executors.newFixedThreadPool(nombre);
		List<Future<VisitedLocation>> futures = new ArrayList<>();
		
		// WHEN
		for(User user : allUsers) {
			futures.add(executorService.submit( () -> tourGuideService.trackUserLocation(user) ));
		}
		for ( Future<VisitedLocation> future : futures ) {
			VisitedLocation visitedLocation = future.get();
			System.out.println(visitedLocation);
		}
		executorService.shutdown(); 
		
		/////v1 - appel direct à la méthode
		//tourGuideService.trackUserLocation(user);

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		// THEN
		assertEquals(InternalTestHelper.getInternalUserNumber(), futures.size());
		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	
	//@Ignore
	//TODO à corriger
	@Test
	public void highVolumeGetRewards() throws InterruptedException, ExecutionException {
				
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		// GIVEN
		//V1
	    Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));
	     
		// V2
		int nombre = 1000;
		ExecutorService executorService = Executors.newFixedThreadPool(nombre);
		List<Future> futures = new ArrayList<>();
	
		// WHEN
		for(User u : allUsers) {
			futures.add(executorService.submit( (
					) -> rewardsService.calculateRewards(u) ));
			}
			for(Future future : futures) {
				future.get();
			}	
			executorService.shutdown();
		
		//V1
	    //allUsers.forEach(u -> rewardsService.calculateRewards(u));
	    
		for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();
		
		// THEN

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	
	@Test
	public void highVolumeGetFiveNearAttractions() throws InterruptedException, ExecutionException, JSONException {
	
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		//GIVEN
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		int nombre = 1000;
		ExecutorService executorService = Executors.newFixedThreadPool(nombre);
		List<Future<JSONObject>> futures = new ArrayList<>();
		
		//WHEN
		for(User u : allUsers) {
		futures.add(executorService.submit( (
				) -> tourGuideService.getFiveNearAttractionsWithDistanceAndRewardsFromCurrentUserLocation(u) ));
		}
		for(Future<JSONObject> future : futures) {
			JSONObject result = future.get();
			assertEquals(5, result.getJSONArray("attraction").length());
		}	
		executorService.shutdown();
		
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();
		
		//THEN
		System.out.println("highVolumeGetFive: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	

	@Test
	public void highVolumeGetAllUsersLocation() throws JSONException, InterruptedException, ExecutionException {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		//GIVEN
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		JSONObject jsonAllUsersLocations = new JSONObject();
		
		//WHEN
		int nombre = 1000;
		ExecutorService executorService = Executors.newFixedThreadPool(nombre);
		List<Future<JSONObject>> futures = new ArrayList<>();
			
		//WHEN
		for(User user : allUsers) {
		futures.add(executorService.submit( (
				) -> jsonAllUsersLocations.put(
						user.getUserId().toString(), JsonStream.serialize(tourGuideService.trackUserLocation(user).location))));
		}
		List<JSONObject> list = new ArrayList<>();
		for(Future<JSONObject> future : futures) {
			JSONObject result = future.get();
			list.add(result);
		}	
		executorService.shutdown();

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();
	
		//THEN
		assertEquals(InternalTestHelper.getInternalUserNumber(),list.size() );
		
		System.out.println("highVolumeGetAllUsers: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	
	}
}
