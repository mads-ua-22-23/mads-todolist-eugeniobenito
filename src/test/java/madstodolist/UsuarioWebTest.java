package madstodolist;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.model.Usuario;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
//
// A diferencia de los tests web de tarea, donde usábamos los datos
// de prueba de la base de datos, aquí vamos a practicar otro enfoque:
// moquear el usuarioService.
public class UsuarioWebTest {

        @Autowired
        private MockMvc mockMvc;

        // Moqueamos el usuarioService.
        // En los tests deberemos proporcionar el valor devuelto por las llamadas
        // a los métodos de usuarioService que se van a ejecutar cuando se realicen
        // las peticiones a los endpoint.
        @MockBean
        private UsuarioService usuarioService;

        // Moqueamos el managerUserSession para poder moquear el usuario logeado
        @MockBean
        private ManagerUserSession managerUserSession;

        @Test
        public void servicioLoginUsuarioOK() throws Exception {
                // GIVEN
                // Moqueamos la llamada a usuarioService.login para que
                // devuelva un LOGIN_OK y la llamada a usuarioServicie.findByEmail
                // para que devuelva un usuario determinado.

                Usuario anaGarcia = new Usuario("ana.garcia@gmail.com");
                anaGarcia.setId(1L);

                when(usuarioService.login("ana.garcia@gmail.com", "12345678"))
                                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
                when(usuarioService.findByEmail("ana.garcia@gmail.com"))
                                .thenReturn(anaGarcia);

                // WHEN, THEN
                // Realizamos una petición POST al login pasando los datos
                // esperados en el mock, la petición devolverá una redirección a la
                // URL con las tareas del usuario

                this.mockMvc.perform(post("/login")
                                .param("eMail", "ana.garcia@gmail.com")
                                .param("password", "12345678"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/usuarios/1/tareas"));
        }

    @Test
    public void servicioLoginUsuarioNotFound() throws Exception {
        // GIVEN
        // Moqueamos el método usuarioService.login para que devuelva
        // USER_NOT_FOUND
        when(usuarioService.login("pepito.perez@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.USER_NOT_FOUND);

        // WHEN, THEN
        // Realizamos una petición POST con los datos del usuario mockeado y
        // se debe devolver una página que contenga el mensaja "No existe usuario"
        this.mockMvc.perform(post("/login")
                        .param("eMail","pepito.perez@gmail.com")
                        .param("password","12345678"))
                .andExpect(content().string(containsString("No existe usuario")));
    }

    @Test
    public void servicioLoginUsuarioErrorPassword() throws Exception {
        // GIVEN
        // Moqueamos el método usuarioService.login para que devuelva
        // ERROR_PASSWORD
        when(usuarioService.login("ana.garcia@gmail.com", "000"))
                .thenReturn(UsuarioService.LoginStatus.ERROR_PASSWORD);

        // WHEN, THEN
        // Realizamos una petición POST con los datos del usuario mockeado y
        // se debe devolver una página que contenga el mensaja "Contraseña incorrecta"
        this.mockMvc.perform(post("/login")
                        .param("eMail","ana.garcia@gmail.com")
                        .param("password","000"))
                .andExpect(content().string(containsString("Contraseña incorrecta")));
    }

        @Test
        public void servicioListarUsuarios() throws Exception {
                // GIVEN
                // Dos usuarios con correo, ID
                Usuario usuario = new Usuario("user@ua");
                usuario.setId(1L);

                Usuario usuario_2 = new Usuario("user_2@ua");
                usuario.setId(2L);

                List<Usuario> listaUsuarios = new ArrayList<Usuario>();
                listaUsuarios.add(usuario);
                listaUsuarios.add(usuario_2);

                // Y un usuario administrador
                Usuario admin = new Usuario("admin@ua");
                admin.setNombre("Admin");
                admin.setId(3L);
                admin.setIsAdmin(true);

                // WHEN
                // Mockeamos el servicio de obtención de todos los usuarios para que nos devuelva
                // la lista de usuarios
                when(usuarioService.allUsuarios()).thenReturn(listaUsuarios);

                // Mockeamos el servicio de obtención del administrador para 
                // que nos devuelva al adminitrador que acabamos de crea
                when(usuarioService.findAdmin()).thenReturn(admin);

                // Mockeamos el método usuarioLogeado para que nos devuelva al administrador
                when(managerUserSession.usuarioLogeado()).thenReturn(admin.getId());

                // WHEN, THEN
                // Realizamos una petición get con la lista de usuarios
                this.mockMvc.perform(get("/registrados"))
                                .andExpect((content().string(allOf(
                                                containsString("Admin"),
                                                containsString("Tareas"),
                                                containsString("Lista de usuarios"),
                                                containsString("Id"),
                                                containsString("Email"),
                                                containsString("user@ua"),
                                                containsString("user_2@ua"),
                                                containsString("Detalles"),
                                                containsString("Total usuarios:")))));
        }

        @Test
        public void servicioDescripciónUsuario() throws Exception {

                // GIVEN
                // Un usuario con correo y Id
                Usuario usuario = new Usuario("user@ua");
                usuario.setNombre("Usuario Ejemplo");
                usuario.setId(1L);

                // Y un usuario administrador
                Usuario admin = new Usuario("admin@ua");
                admin.setNombre("Admin");
                admin.setId(2L);
                admin.setIsAdmin(true);

                // WHEN
                // Mockeamos el servicio de búsqueda por Id para que nos devuelva el
                // usuario que acabamos de crear
                when(usuarioService.findById(usuario.getId())).thenReturn(usuario);

                // Mockeamos el servicio de obtención del administrador para 
                // que nos devuelva al usuario que acabamos de crea
                when(usuarioService.findAdmin()).thenReturn(admin);

                // Mockeamos el método usuarioLogeado para que nos devuelva al administrador
                when(managerUserSession.usuarioLogeado()).thenReturn(admin.getId());

                // THEN
                // Se realiza la petición GET a la descipción del usuario,
                // el HTML devuelto contiene la información de nuestro usuario
                this.mockMvc.perform(get("/registrados/1"))
                                .andExpect((content().string(allOf(
                                                containsString("Admin"),
                                                containsString("Tareas"),
                                                containsString("Descripción de user@ua"),
                                                containsString("Id"),
                                                containsString("Nombre"),
                                                containsString("Email"),
                                                containsString("Fecha de nacimiento"),
                                                containsString("Usuario Ejemplo"),
                                                containsString("user@ua")))));
        }

        @Test
        public void servicioRegistrarUsuarioAdminNoExiste() throws Exception {
                this.mockMvc.perform(get("/registro"))
                .andExpect((content().string(allOf(
                        containsString("Quiero ser"),
                        containsString("Administrador")))));
        }

        @Test
        public void servicioRegistrarUsuarioAdminExiste() throws Exception {

                // GIVEN
                // Un usuario con correo, Id y administrado
                Usuario usuario = new Usuario("user@ua");
                usuario.setNombre("Usuario Ejemplo");
                usuario.setId(1L);
                usuario.setIsAdmin(true);

                // WHEN
                // Mockeamos el servicio de obtención del administrador para 
                // que nos devuelva al usuario que acabamos de crea
                when(usuarioService.findAdmin()).thenReturn(usuario);

                // THEN
                // Se realiza la petición GET a la página de registro y comprobamos la ausencia
                // del checkbox
                this.mockMvc.perform(get("/registro"))
                        .andExpect(content().string(
                                allOf(not(containsString("Quiero ser")),
                                     (not(containsString("Administrador"))))));
        }
}
