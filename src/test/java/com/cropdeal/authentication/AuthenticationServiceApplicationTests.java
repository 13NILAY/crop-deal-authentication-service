package com.cropdeal.authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cropdeal.authentication.model.User;
import com.cropdeal.authentication.repository.UserRepository;
import com.cropdeal.authentication.service.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationServiceApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtService jwtService;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	@Rollback
	void adminUsersEndpointsRequireAdminAndDoNotExposePassword() throws Exception {
		User admin = saveUser("ADMIN");
		User farmer = saveUser("FARMER");
		User dealer = saveUser("DEALER");

		mockMvc.perform(get("/admin/users"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/admin/users").header("Authorization", bearer(farmer)))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/admin/users").header("Authorization", bearer(dealer)))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/admin/users").param("search", admin.getEmail()).param("role", "admin")
					.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].email").value(admin.getEmail()))
				.andExpect(jsonPath("$.content[0].password").doesNotExist());
	}

	@Test
	@Transactional
	@Rollback
	void adminCanDeactivateUsersButNotSelfAndInactiveUsersCannotLogIn() throws Exception {
		User admin = saveUser("ADMIN");
		User dealer = saveUser("DEALER");
		String adminToken = bearer(admin);

		mockMvc.perform(patch("/admin/users/{id}/status", admin.getId())
					.contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}")
					.header("Authorization", adminToken))
				.andExpect(status().isForbidden());

		mockMvc.perform(patch("/admin/users/{id}/status", dealer.getId())
					.contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}")
					.header("Authorization", adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.active").value(false));

		mockMvc.perform(get("/admin/users/{id}", dealer.getId())
					.header("Authorization", bearer(dealer)))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"" + dealer.getEmail() + "\",\"password\":\"Secret123\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@Transactional
	@Rollback
	void adminUserLookupReturnsNotFoundForMissingId() throws Exception {
		User admin = saveUser("ADMIN");
		mockMvc.perform(get("/admin/users/{id}", Long.MAX_VALUE)
					.header("Authorization", bearer(admin)))
				.andExpect(status().isNotFound());
	}

		    @Test
		    @Transactional
		    @Rollback
		    void adminActionsAreRecordedWithAuthenticatedActor() throws Exception {
			User admin = saveUser("ADMIN");
			User dealer = saveUser("DEALER");
			mockMvc.perform(patch("/admin/users/{id}/status", dealer.getId())
					.contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}")
					.header("Authorization", bearer(admin)))
				.andExpect(status().isOk());

			mockMvc.perform(get("/admin/activity").param("action", "ADMIN_USER_SUSPENDED")
					.param("entityType", "USER").param("adminId", admin.getEmail())
					.header("Authorization", bearer(admin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].adminId").value(admin.getEmail()))
				.andExpect(jsonPath("$.content[0].entityId").value(dealer.getId().toString()));
		    }

	private User saveUser(String role) {
		String email = role.toLowerCase() + "-" + UUID.randomUUID() + "@example.com";
		return userRepository.saveAndFlush(new User(email, passwordEncoder.encode("Secret123"), role));
	}

	private String bearer(User user) {
		return "Bearer " + jwtService.generateToken(user.getEmail(), user.getRole());
	}

}
