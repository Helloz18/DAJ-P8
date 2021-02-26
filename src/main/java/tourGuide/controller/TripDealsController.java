package tourGuide.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import tourGuide.service.TripPricerService;
import tourGuide.service.UserService;
import tripPricer.Provider;

@RestController
public class TripDealsController {
	
	TripPricerService tripPricerService = new TripPricerService();
	
	@Autowired
	UserService userService;
	
	/**
     * the user's rewards are sumed to suggest a list of trip deals available with this sum. The suggestion take in
     * consideration the user's preferences (number of adult, children...)
     * @param userName
     * @return
     */
    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
    	List<Provider> providers = tripPricerService.getTripDeals(userService.getUser(userName));
    	return JsonStream.serialize(providers);
    }
    
    
   

}
