package tourGuide;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;


import tourGuide.controller.RewardsController;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.service.RewardsService;
import tourGuide.service.UserService;


@RunWith(SpringRunner.class)
@WebMvcTest(controllers = RewardsController.class)
public class RewardsControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	RewardsService rewardsService;

	@MockBean
	UserService userService;

	@Test
	public void getRewardsShouldReturnOk() throws Exception {
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		given(userService.getUser("jon")).willReturn(user);
		List<UserReward> value = new ArrayList<UserReward>();
		given(rewardsService.getUserRewards(userService.getUser("jon"))).willReturn(value);
		mockMvc.perform(get("/getRewards").param("userName", "jon")).andExpect(status().isOk());
	}

}
