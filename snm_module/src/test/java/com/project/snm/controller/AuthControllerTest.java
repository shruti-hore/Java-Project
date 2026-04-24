package com.project.snm.controller;

import com.project.snm.dto.RegisterRequest;
import com.project.snm.exception.ConflictException;
import com.project.snm.exception.NotFoundException;
import com.project.snm.model.mysql.UserRecord;
import com.project.snm.security.JwtService;
import com.project.snm.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class AuthControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(userService, jwtService)).build();
    }

    @Test
    void testRegisterSuccess() throws Exception {
        UserRecord user = new UserRecord();
        user.setUserId("user-123");
        
        when(userService.registerUser(any())).thenReturn(user);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user-123"));
    }

    @Test
    void testRegisterDuplicate() throws Exception {
        when(userService.registerUser(any())).thenThrow(new ConflictException("Conflict"));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    void testLoginChallengeSuccess() throws Exception {
        UserRecord user = new UserRecord();
        user.setSaltBase64("salt");
        user.setVaultBlobBase64("vault");

        when(userService.findByEmailHmac("hmac")).thenReturn(user);

        mockMvc.perform(post("/auth/login/challenge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"emailHmac\": \"hmac\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saltBase64").value("salt"))
                .andExpect(jsonPath("$.vaultBlobBase64").value("vault"));
    }

    @Test
    void testLoginChallengeNotFound() throws Exception {
        when(userService.findByEmailHmac("hmac")).thenThrow(new NotFoundException("Not Found"));

        mockMvc.perform(post("/auth/login/challenge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"emailHmac\": \"hmac\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testLoginVerifySuccess() throws Exception {
        UserRecord user = new UserRecord();
        user.setUserId("user-123");
        user.setBcryptHash(BCrypt.hashpw("hash", BCrypt.gensalt()));
        user.setPublicKeyBase64("pubkey");

        when(userService.findByEmailHmac("hmac")).thenReturn(user);
        when(jwtService.issueToken("user-123")).thenReturn("fake-jwt");

        mockMvc.perform(post("/auth/login/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"emailHmac\": \"hmac\", \"bcryptHash\": \"hash\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("fake-jwt"))
                .andExpect(jsonPath("$.publicKeyBase64").value("pubkey"));
    }

    @Test
    void testLoginVerifyWrongPassword() throws Exception {
        UserRecord user = new UserRecord();
        user.setBcryptHash(BCrypt.hashpw("other-hash", BCrypt.gensalt()));

        when(userService.findByEmailHmac("hmac")).thenReturn(user);

        mockMvc.perform(post("/auth/login/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"emailHmac\": \"hmac\", \"bcryptHash\": \"hash\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLookupSuccess() throws Exception {
        UserRecord user = new UserRecord();
        user.setPublicKeyBase64("pubkey");

        when(userService.findByEmailHmac("hmac")).thenReturn(user);

        mockMvc.perform(post("/auth/lookup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"emailHmac\": \"hmac\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicKeyBase64").value("pubkey"));
    }
}
