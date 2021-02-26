package tourGuide;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import tourGuide.controller.MainController;


@RunWith(SpringRunner.class)
@WebMvcTest(controllers = MainController.class)
public class MainControllerTest {
	
		@Autowired
		MockMvc mockMvc;
		
		@Test
		public void getMainPageShouldReturnOk() throws Exception {
			mockMvc.perform(get("/")).andExpect(status().isOk());
		}
	
}
