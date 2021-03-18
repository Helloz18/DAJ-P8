package tourGuide.service;

import java.util.List;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import tourGuide.model.User;

@Service
public class UserService {
	
	GpsUtil gpsUtil = new GpsUtil();
	RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
	TestService testService = new TestService(gpsUtil, rewardsService);

	
	public User getUser(String userName) {
		return testService.getUser(userName);
	}
	
	public List<User> getAllUsers() {
		return testService.getAllUsers();
	}
	
	public void addUser(User user) {
		testService.addUser(user);
	}
	
}
