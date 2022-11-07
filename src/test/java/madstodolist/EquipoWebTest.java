package madstodolist;

import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@AutoConfigureMockMvc
public class EquipoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private EquipoService equipoService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void servicioListadoEquipos() throws Exception {

        // GIVEN
        // Un usuario con correo e ID
        Usuario usuario = new Usuario("user@ua");
        usuario.setNombre("Usuario Ejemplo");
        usuario.setId(1L);

        // Un equipo
        Equipo equipo = new Equipo("Equipo A");

        List<Equipo> listaEquipos = new ArrayList<Equipo>();
        listaEquipos.add(equipo);

        // Mockeamos el método usuarioLogeado para que nos devuelva un valor
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        // Mockeamos el servicio de obtención de todos los equipos para que nos devuelva
        when(equipoService.findAllOrderedByName()).thenReturn(listaEquipos);

        // Mockeamos el servicio de búsqueda por Id para que nos devuelva el
        // usuario que acabamos de crear
        when(usuarioService.findById(usuario.getId())).thenReturn(usuario);

        // WHEN, THEN
        // Realizamos una petición GET: /equipos nos redirecciona a la
        // página de listado de equipos
        this.mockMvc.perform(get("/equipos"))
                .andExpect((content().string(allOf(
                        containsString("Lista de Equipos"),
                        containsString("Usuario Ejemplo"),
                        containsString("Equipo A")))));
    }

    @Test
    public void servicioListadoUsuarioEquipos() throws Exception {

        // GIVEN
        // Un usuario con correo e ID
        Usuario usuario = new Usuario("user@ua");
        usuario.setNombre("Usuario Ejemplo");
        usuario.setId(1L);
        List<Usuario> listaUsuarios = new ArrayList<Usuario>();
        listaUsuarios.add(usuario);

        // Un equipo
        Equipo equipo = new Equipo("Equipo A");
        equipo.setId(1L);
        equipo.addUsuario(usuario);

        // Mockeamos el método usuarioLogeado para que nos devuelva un valor
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        // Mockeamos el servicio de obtención de todos los equipos para que nos devuelva
        when(equipoService.usuariosEquipo(equipo.getId())).thenReturn(listaUsuarios);

        // Mockeamos el servicio de búsqueda por Id para que nos devuelva el
        // usuario que acabamos de crear
        when(usuarioService.findById(usuario.getId())).thenReturn(usuario);

        // Mockeamos el servicio de obtención de un equipo
        when(equipoService.recuperarEquipo(1L)).thenReturn(equipo);

        // WHEN, THEN
        // Realizamos una petición GET: /equipos/{id} nos redirecciona a la
        // página de listado de usuarios del equipo con id:{id}
        this.mockMvc.perform(get("/equipos/1"))
                .andExpect((content().string(allOf(
                        containsString("Lista de miembros del Equipo"),
                        containsString("user@ua"),
                        containsString("Equipo A")))));
    }
}
