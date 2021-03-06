package com.galvanize.autos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AutosController.class)
class AutosControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    AutoService autoService;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void getAutosReturnsListOfAutos() throws Exception {
        List<Auto> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            testList.add(new Auto("red", "Honda", "Civic", 2000 + i, "XX89DM"));
        }
        when(autoService.getAllAutos()).thenReturn(new AutosList(testList));
        mockMvc.perform(get("/api/autos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autosList", hasSize(5)));
    }

    @Test
    public void getAutosReturnsNoContent() throws Exception {
        when(autoService.getAllAutos()).thenReturn(new AutosList());

        mockMvc.perform(get("/api/autos"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void getAutosWithParamsReturnListOfAutos() throws Exception {
        List<Auto> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            testList.add(new Auto("red", "Honda", "Civic", 2000 + i, "XX89DM"));
        }
        when(autoService.getAllAutos(anyString(),anyString())).thenReturn(new AutosList(testList));
        mockMvc.perform(get("/api/autos?color=red&make=Honda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autosList", hasSize(5)));
    }

    @Test
    public void getAutosWithColorParamReturnsListOfAutos() throws Exception {
        List<Auto> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            testList.add(new Auto("red", "Honda", "Civic", 2000 + i, "XX89DM"));
        }
        when(autoService.getAllAutosByColor(anyString())).thenReturn(new AutosList(testList));
        mockMvc.perform(get("/api/autos?color=red"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autosList", hasSize(5)));
    }

    @Test
    public void getAutosWithMakeParamReturnsListOfAutos() throws Exception {
        List<Auto> testList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            testList.add(new Auto("red", "Honda", "Civic", 2000 + i, "XX89DM"));
        }
        when(autoService.getAllAutosByMake(anyString())).thenReturn(new AutosList(testList));
        mockMvc.perform(get("/api/autos?make=Honda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autosList", hasSize(5)));
    }

    @Test
    public void postAutoReturnsPostDataAuto() throws Exception {
        Auto testAuto = new Auto("red", "Honda", "Civic", 2000, "XX89DM");
        when(autoService.addAuto(any(Auto.class))).thenReturn(testAuto);

        mockMvc.perform(post("/api/autos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(testAuto)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("year").value(2000));
    }

    @Test
    public void postAutoReturns400ForBadRequest() throws Exception {
        Auto testAuto = new Auto("red", "Honda", "Civic", 2000, "XX89DM");
        when(autoService.addAuto(any(Auto.class))).thenThrow(InvalidAutoException.class);
        mockMvc.perform(post("/api/autos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(testAuto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getAutoWithVin_returnsCorrectAuto() throws Exception {
        Auto testAuto = new Auto("red", "Honda", "Civic", 2000, "XX89DM");
        when(autoService.getAuto(anyString())).thenReturn(testAuto);
        mockMvc.perform(get("/api/autos/XX89DM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("vin").value(testAuto.getVin()));
    }

    @Test
    public void getAutoWithVin_returns204WhenNoAutoFound() throws Exception {
        when(autoService.getAuto(anyString())).thenReturn(null);
        mockMvc.perform(get("/api/autos/XX91DM"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateAuto_returnsUpdatedAuto() throws Exception {
        Auto testAuto = new Auto("red", "Honda", "Civic", 2000, "XX89DM");
        UpdateAuto testUpdate = new UpdateAuto("blue", "David");
        testAuto.setColor(testUpdate.getColor());
        testAuto.setOwner(testUpdate.getOwner());
        when(autoService.updateAuto(anyString(), anyString(), anyString())).thenReturn(testAuto);
        mockMvc.perform(patch("/api/autos/XX89DM")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(testUpdate)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("color").value("blue"))
                        .andExpect(jsonPath("owner").value("David"));

    }

    @Test
    public void updateAutoWithVin_returns204WhenNoAutoFound() throws Exception {
        UpdateAuto testUpdate = new UpdateAuto("blue", "David");
        when(autoService.updateAuto(anyString(), anyString(), anyString())).thenReturn(null);
        mockMvc.perform(patch("/api/autos/XX91DM")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(testUpdate)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateAutoWithVin_returns400ForInvalidRequest() throws Exception {
        UpdateAuto testUpdate = new UpdateAuto("blue", "David");
        when(autoService.updateAuto(anyString(), anyString(), anyString())).thenThrow(InvalidUpdateAutoException.class);
        mockMvc.perform(patch("/api/autos/XX89DM")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(testUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteAutoWhenAutoExists_returns202() throws Exception {
        mockMvc.perform(delete("/api/autos/XX89DM"))
                .andExpect(status().isAccepted());
        verify(autoService).deleteAuto(anyString());
    }

    @Test
    public void deleteAuto_returnsNoContentIfAutoDoesNotExist() throws Exception {
        doThrow(new AutoNotFoundException()).when(autoService).deleteAuto(anyString());
        mockMvc.perform(delete("/api/autos/XX89DM"))
                .andExpect(status().isNoContent());
    }
}