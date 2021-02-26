package tourGuide.controller;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.service.LocationsService;
import tourGuide.service.UserService;
import tourGuide.user.User;

@RestController
public class LocationController {

	@Autowired
	UserService userService;
	
	@Autowired
	LocationsService locationsService;
	
	 /**
     * get the last visitedLocation of a user if there is one, or the current visited location if it's his first
     * @param userName
     * @return a string with the latitude and longitude of the user
     */
    @RequestMapping("/getLocation") 
    public String getLocation(@RequestParam String userName) {
    	VisitedLocation visitedLocation = locationsService.getUserLocation(userService.getUser(userName));
		return JsonStream.serialize(visitedLocation.location);
    }
    
    /**
     * 
     * @return
     * @throws JSONException 
     */
    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() throws JSONException {
    	// TODO: Get a list of every user's most recent location as JSON
    	//- Note: does not use gpsUtil to query for their current location, 
    	//        but rather gathers the user's current location from their stored location history.
    	//
    	// Return object should be the just a JSON mapping of userId to Locations similar to:
    	//     {
    	//        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371} 
    	//        ...
    	//     }
    	List<User> users = new ArrayList<User>();
    	users = userService.getAllUsers();  	
    	return JsonStream.serialize(locationsService.getAllUsersLocation(users));
    }
    
}
