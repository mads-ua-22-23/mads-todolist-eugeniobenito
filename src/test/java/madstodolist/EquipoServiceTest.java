package madstodolist;

import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.UsuarioService;

import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Assertions;
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

    @Test
    public void comprobarRelacionUsuarioEquipos() {
        // GIVEN
        // Un equipo creado en la base de datos y un usuario registrado
        Equipo equipo = equipoService.crearEquipo("Proyecto 1");
        Usuario usuario = new Usuario("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        // WHEN
        // Añadimos el usuario al equipo y lo recuperamos
        equipoService.addUsuarioEquipo(usuario.getId(), equipo.getId());
        Usuario usuarioBD = usuarioService.findById(usuario.getId());

        // THEN
        // Se recuperan también los equipos del usuario,
        // porque la relación entre usuarios y equipos es EAGER
        assertThat(usuarioBD.getEquipos()).hasSize(1);
    }

    @Test
    public void comprobarEliminarRelacionUsuarioEquipos() {
        // GIVEN
        // Un equipo creado en la base de datos y un usuario
        // registrado miembro del mismo
        Equipo equipo = equipoService.crearEquipo("Proyecto 1");
        Usuario usuario = new Usuario("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        equipoService.addUsuarioEquipo(usuario.getId(), equipo.getId());

        // WHEN
        // Eliminamos al usuario del equipo
        equipoService.removeUsuarioEquipo(usuario.getId(), equipo.getId());

        // THEN
        // ERecuperamos al usuario y al equipo y este no pertenece al mismo
        Equipo equipoBD = equipoService.recuperarEquipo(equipo.getId());
        Usuario usuarioBD = usuarioService.findById(usuario.getId());
        
        List<Usuario> usuarios_equipo = equipoService.usuariosEquipo(equipoBD.getId());

        assertThat(usuarios_equipo).isEmpty();
        assertThat(usuarioBD.getEquipos()).isEmpty();
    }

    @Test
    public void servicioCrearEquipoExcepcionNombreVacio() {
        // WHEN, THEN   
        // Creamos un equipo con el nombre vacío se lanza una excepción
        // de tipo EquipoServiceException
        
        Assertions.assertThrows(EquipoServiceException.class, () -> {
            equipoService.crearEquipo("");
        });
    }

    @Test
    public void servicioRecuperarEquipoNoExistente() {
        // WHEN, THEN   
        // Intentamos recuperar un  equipo no existente en la base de datos
        // lanza excepción de tipo EquipoServiceException
        
        Assertions.assertThrows(EquipoServiceException.class, () -> {
            equipoService.recuperarEquipo(1L);
        });
    }

    @Test
    public void servicioAñadirUsuarioEquipoNoExistente() {
        // WHEN, THEN   
        // Intentamos añadir a un  equipo no existente en la base de datos
        // un usuario no existente
        // lanza excepción de tipo EquipoServiceException
        
        Assertions.assertThrows(EquipoServiceException.class, () -> {
            equipoService.addUsuarioEquipo(1L, 1L);
        });
    }

    @Test
    public void servicioEliminarUsuarioEquipoNoExistente() {
        // WHEN, THEN   
        // Intentamos añadir a un  equipo no existente en la base de datos
        // un usuario no existente
        // lanza excepción de tipo EquipoServiceException
        
        Assertions.assertThrows(EquipoServiceException.class, () -> {
            equipoService.removeUsuarioEquipo(1L, 1L);
        });
    }

    @Test
    public void servicioAñadirUsuarioYaExistenteEnEquipo() {
        /// Un equipo creado en la base de datos y un usuario registrado
        // miembro del equipo
        Equipo equipo = equipoService.crearEquipo("Proyecto 1");
        Usuario usuario = new Usuario("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        equipoService.addUsuarioEquipo(usuario.getId(), equipo.getId());

        // WHEN, THEN
        // Intentamos añadir al usuario miembro se lanza excepción de
        // tipo EquipoServiceException
        Usuario usuarioBD = usuarioService.findById(usuario.getId());
                
        Assertions.assertThrows(EquipoServiceException.class, () -> {
            equipoService.addUsuarioEquipo(usuarioBD.getId(), equipo.getId());
        });
    }

    @Test
    public void servicioEliminarUsuarioNoExistenteEnEquipo() {
        /// Un equipo creado en la base de datos y un usuario registrado
        // NO miembro del equipo
        Equipo equipo = equipoService.crearEquipo("Proyecto 1");
        Usuario usuario = new Usuario("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        // WHEN, THEN
        // Intentamos eliminar al usuario NO miembro se lanza excepción de
        // tipo EquipoServiceException
        Usuario usuarioBD = usuarioService.findById(usuario.getId());
                
        Assertions.assertThrows(EquipoServiceException.class, () -> {
            equipoService.removeUsuarioEquipo(usuarioBD.getId(), equipo.getId());
        });
    }

    @Test
    public void servicioListarUsuarioEquipoNoExistente() {
        // WHEN, THEN   
        // Intentamos listar a los usuarios de un equipo no existente en la base de datos
        // lanza excepción de tipo EquipoServiceException
        
        Assertions.assertThrows(EquipoServiceException.class, () -> {
            equipoService.usuariosEquipo(1L);
        });
    }

    @Test
    public void testModificarNombreEquipo() {
        // GIVEN 
        // Un equipo guardado en la base de datos
        Equipo equipo = equipoService.crearEquipo("Proyecto 1");
        Equipo equipoBd = equipoService.recuperarEquipo(equipo.getId());        
        assertThat(equipoBd.getNombre()).isEqualTo("Proyecto 1");

        // WHEN
        // Cambiamos su nombre
        equipoService.modificaNombreEquipo(equipoBd.getId(), "Proyecto MADS");

        // THEN
        // Lo recuperamos de la base de datos y el nombre se ha modificado
        assertThat(equipoBd.getNombre()).isEqualTo("Proyecto 1");
    }
}
