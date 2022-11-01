package madstodolist;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.model.Usuario;
import madstodolist.service.UsuarioService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class NavbarWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void getAboutUsuarioNoLogeado() throws Exception {
        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(containsString("Login")))
                .andExpect(content().string(containsString("Registro")));
    }

    @Test
    public void getNavbarVistasUsuarioLogeado() throws Exception {

        Usuario usuario = new Usuario("user@ua.com");
        usuario.setNombre("Usuario Ejemplo");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        Long usuarioId = usuario.getId();

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String[] urls = { "/usuarios/" + usuarioId.toString() + "/tareas",
                          "/usuarios/" + usuarioId.toString() + "/tareas/nueva",
                          "/about",
        };

        for(String url : urls) {
            this.mockMvc.perform(get(url))
            .andExpect((content().string(allOf(
                    containsString("ToList"),
                    containsString("Tareas"),
                    containsString("Cuenta"),
                    containsString("Usuario Ejemplo")))));        
        }
    }

}