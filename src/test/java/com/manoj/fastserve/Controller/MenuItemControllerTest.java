package com.manoj.fastserve.Controller;


import com.manoj.fastserve.Config.TestCacheConfig;
import com.manoj.fastserve.Entity.MenuItem;
import com.manoj.fastserve.Service.MenuItemService;


import com.manoj.fastserve.Util.JwtUtil;
import org.junit.jupiter.api.Test;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;


import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;


import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.util.List;


import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Import(TestCacheConfig.class)
@WebMvcTest(MenuItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class MenuItemControllerTest {



    @Autowired
    private MockMvc mockMvc;



    @MockitoBean
    private MenuItemService menuItemService;

    @MockitoBean
    private JwtUtil jwtUtil;


    @Test
    void searchMenu_shouldReturnResults() throws Exception {


        MenuItem item = new MenuItem();

        item.setName("Burger");
        item.setPrice(120.0);



        Page<MenuItem> page =
                new PageImpl<>(
                        List.of(item),
                        PageRequest.of(0,10),
                        1
                );



        when(menuItemService.searchMenu(
                eq("Burger"),
                isNull(),
                isNull(),
                isNull(),
                any()
        ))
                .thenReturn(page);




        mockMvc.perform(
                        get("/menu/search")
                                .param("name","Burger")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name")
                        .value("Burger"))
                .andExpect(jsonPath("$.content[0].price")
                        .value(120.0));

    }





    @Test
    void searchMenu_withFilters_shouldPassParameters() throws Exception {



        Page<MenuItem> page =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(0,10),
                        0
                );



        when(menuItemService.searchMenu(
                eq("Pizza"),
                eq(100.0),
                eq(500.0),
                eq(1L),
                any()
        ))
                .thenReturn(page);




        mockMvc.perform(
                        get("/menu/search")
                                .param("name","Pizza")
                                .param("minPrice","100")
                                .param("maxPrice","500")
                                .param("restaurantId","1")
                )
                .andExpect(status().isOk());



        verify(menuItemService)
                .searchMenu(
                        eq("Pizza"),
                        eq(100.0),
                        eq(500.0),
                        eq(1L),
                        any()
                );

    }




    @Test
    void searchMenu_withoutFilters_shouldReturnEmptyPage() throws Exception {



        Page<MenuItem> page =
                new PageImpl<>(
                        List.of()
                );



        when(menuItemService.searchMenu(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any()
        ))
                .thenReturn(page);



        mockMvc.perform(
                        get("/menu/search")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content")
                        .isEmpty());

    }


}