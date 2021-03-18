package tourGuide.service;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.jsoniter.output.JsonStream;

import gpsUtil.GpsUtil;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.model.User;

@Service
public class LocationsService {
	
	GpsUtil gpsUtil = new GpsUtil();
	RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
	
	
	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);
		return visitedLocation;
	}
	
	public Location getLocation(User user) {
		return user.getLastVisitedLocation().location;
	}
	
	/**
	 * 
	 * @param user
	 * @return
	 */
	public VisitedLocation trackUserLocation(User user) {

		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}
	

	/**
	 * This method is used by product Owner to see all the user locations.
	 * @param users
	 * @return
	 * @throws JSONException
	 */
	public JSONObject getAllUsersLocation(List<User> users) throws JSONException {
		//V3
		// ne pas utiliser trackUserLocation car calculate rewards prend trop de temps
		// utiliser uniquement getLocation()
			
		JSONObject jsonAllUsersLocations = new JSONObject();
		for(User user : users) {
			jsonAllUsersLocations.put(user.getUserId().toString(), JsonStream.serialize(getLocation(user)));
		}
		//Locale.setDefault(Locale.US);
		////V2 - ok
//		for(User user : users) {
//		jsonAllUsersLocations.put(user.getUserId().toString(), JsonStream.serialize(trackUserLocation(user).location));
//		}
		
		return jsonAllUsersLocations;
	}

}
