package tourGuide.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;
import tourGuide.service.RewardsService;
import tourGuide.service.UserService;

@RestController
public class RewardsController {

	@Autowired
	RewardsService rewardsService;
	
	@Autowired
	UserService userService;
	
	  /**
     * 
     * @param userName
     * @return a json object with a list of the user's rewards
     */
    @RequestMapping("/getRewards") 
    public String getRewards(@RequestParam String userName) {
    	rewardsService.calculateRewards(userService.getUser(userName));
    	return JsonStream.serialize(rewardsService.getUserRewards(userService.getUser(userName)));
    }
    
   
  
}
