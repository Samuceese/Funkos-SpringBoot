package org.example.storage.controller;

import org.example.storage.controller.StorageController;
import org.example.storage.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(StorageController.class)
public class StorageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageService storageService;

    @Test
    public void testServeFile() throws Exception {
        // Simulación de un archivo como recurso
        String filename = "test.txt";
        String fileContent = "Contenido del archivo";
        Resource resource = new ByteArrayResource(fileContent.getBytes());

        // Configurar el comportamiento del servicio simulado
        when(storageService.loadAsResource(filename)).thenReturn(resource);

        // Realizar la solicitud GET al controlador
        MockHttpServletResponse response = mockMvc.perform(get("/storage/" + filename)
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // Verificar la respuesta
        assertEquals(200, response.getStatus());
        assertEquals(fileContent, response.getContentAsString());
    }
}
