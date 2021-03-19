package tourGuide.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import tourGuide.service.UserService;

@RestController
public class MainController {
	
	@Autowired
	UserService userService;

	  @RequestMapping("/")
	    public String index() {
	    	
	        return "Greetings from TourGuide!";
	    }
	  
	  
	  @RequestMapping("/user")
	    public String getUser(@RequestParam String userName) {  	
	        return userService.getUser(userName).toString();
	    }
	  
}
