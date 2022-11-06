package madstodolist;

import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;

import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import java.util.List;

@SpringBootTest
@Sql(scripts = "/clean-db.sql", executionPhase = AFTER_TEST_METHOD)
public class EquipoServiceTest {

    @Autowired
    EquipoService equipoService;

    @Autowired
    UsuarioService usuarioService;

    @Test
    public void crearRecuperarEquipo() {
        Equipo equipo = equipoService.crearEquipo("Proyecto 1");
        Equipo equipoBd = equipoService.recuperarEquipo(equipo.getId());
        assertThat(equipoBd).isNotNull();
        assertThat(equipoBd.getNombre()).isEqualTo("Proyecto 1");
    }

    @Test
    public void listadoEquiposOrdenAlfabetico() {
        // GIVEN
        // Dos equipos en la base de datos
        equipoService.crearEquipo("Proyecto BBB");
        equipoService.crearEquipo("Proyecto AAA");

        // WHEN
        // Recuperamos los equipos
        List<Equipo> equipos = equipoService.findAllOrderedByName();

        // THEN
        // Los equipos están ordenados por nombre
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto AAA");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto BBB");        
    }    

    @Test
    public void accesoUsuariosGeneraExcepcion() {
        // Given
        // Un equipo en la base de datos
        Equipo equipo = equipoService.crearEquipo("Proyecto 1");

        // WHEN
        // Se recupera el equipo
        Equipo equipoBd = equipoService.recuperarEquipo(equipo.getId());

        // THEN
        // Se produce una excepción al intentar acceder a sus usuarios
        assertThatThrownBy(() -> {
            equipoBd.getUsuarios().size();
        }).isInstanceOf(LazyInitializationException.class);
    }

    @Test
    public void actualizarRecuperarUsuarioEquipo() {
        // GIVEN
        // Un equipo creado en la base de datos y un usuario registrado
        Equipo equipo = equipoService.crearEquipo("Proyecto 1");
        Usuario usuario = new Usuario("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        // WHEN
        // Añadimos el usuario al equipo y lo recuperamos
        equipoService.addUsuarioEquipo(usuario.getId(), equipo.getId());
        List<Usuario> usuarios = equipoService.usuariosEquipo(equipo.getId());

        // THEN
        // El usuario se ha recuperado correctamente
        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getEmail()).isEqualTo("user@ua");
    }

}
