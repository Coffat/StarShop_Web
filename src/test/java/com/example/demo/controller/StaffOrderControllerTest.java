package com.example.demo.controller;

import com.example.demo.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {StaffOrderController.class})
class StaffOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("deny anonymous access to staff orders page")
    void anonymousDenied() throws Exception {
        mockMvc.perform(get("/staff/orders"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "staff@example.com", roles = {"STAFF"})
    @DisplayName("allow STAFF to access staff orders page")
    void staffCanAccess() throws Exception {
        mockMvc.perform(get("/staff/orders"))
                .andExpect(status().isOk());
    }
}


