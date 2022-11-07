package madstodolist;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import madstodolist.model.Equipo;
import madstodolist.model.EquipoRepository;
import madstodolist.model.Usuario;
import madstodolist.model.UsuarioRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import java.util.List;

@SpringBootTest
@Sql(scripts = "/clean-db.sql", executionPhase = AFTER_TEST_METHOD)
public class EquipoTest {

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    //
    // Tests modelo Equipo en memoria, sin la conexión con la BD
    //

    @Test
    public void crearEquipo() {
        Equipo equipo = new Equipo("Proyecto P1");
        assertThat(equipo.getNombre()).isEqualTo("Proyecto P1");
    }

    @Test
    public void comprobarIgualdadEquipos() {
        // GIVEN
        // Creamos tres equipos sin id, sólo con el nombre
        Equipo equipo1 = new Equipo("Proyecto P1");
        Equipo equipo2 = new Equipo("Proyecto P2");
        Equipo equipo3 = new Equipo("Proyecto P2");

        // THEN
        // Comprobamos igualdad basada en el atributo nombre y que el
        // hashCode es el mismo para dos equipos con igual nombre
        assertThat(equipo1).isNotEqualTo(equipo2);
        assertThat(equipo2).isEqualTo(equipo3);
        assertThat(equipo2.hashCode()).isEqualTo(equipo3.hashCode());

        // WHEN
        // Añadimos identificadores y comprobamos igualdad por identificadores
        equipo1.setId(1L);
        equipo2.setId(1L);
        equipo3.setId(2L);

        // THEN
        // Comprobamos igualdad basada en el atributo nombre
        assertThat(equipo1).isEqualTo(equipo2);
        assertThat(equipo2).isNotEqualTo(equipo3);
    }

    @Test
    public void cambiarNombreEquipo() {
        Equipo equipo = new Equipo("Proyecto P1");
        equipo.setNombre("Proyecto MADS");
        assertThat(equipo.getNombre()).isEqualTo("Proyecto MADS");
    }

    //
    // Tests EquipoRepository.
    //

    @Test
    @Transactional
    public void grabarYBuscarEquipo() {
        // GIVEN
        // Un equipo nuevo
        Equipo equipo = new Equipo("Proyecto P1");

        // WHEN
        // Salvamos el equipo en la base de datos
        equipoRepository.save(equipo);

        // THEN
        // Su identificador se ha actualizado y lo podemos
        // usar para recuperarlo de la base de datos
        Long equipoId = equipo.getId();
        assertThat(equipoId).isNotNull();
        Equipo equipoDB = equipoRepository.findById(equipoId).orElse(null);
        assertThat(equipoDB).isNotNull();
        assertThat(equipoDB.getNombre()).isEqualTo("Proyecto P1");
    }

    @Test
    @Transactional
    public void comprobarRelacionBaseDatos() {
        // GIVEN
        // Un equipo y un usuario en la BD
        Equipo equipo = new Equipo("Proyecto 1");
        equipoRepository.save(equipo);

        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        // WHEN
        // Añadimos el usuario al equipo

        equipo.addUsuario(usuario);

        // THEN
        // La relación entre usuario y equipo pqueda actualizada en BD

        Equipo equipoBD = equipoRepository.findById(equipo.getId()).orElse(null);
        Usuario usuarioBD = usuarioRepository.findById(usuario.getId()).orElse(null);

        assertThat(equipo.getUsuarios()).hasSize(1);
        assertThat(equipo.getUsuarios()).contains(usuario);
        assertThat(usuario.getEquipos()).hasSize(1);
        assertThat(usuario.getEquipos()).contains(equipo);
    }

    @Test
    @Transactional
    public void comprobarFindAll() {
        // GIVEN
        // Dos equipos en la base de datos
        equipoRepository.save(new Equipo("Proyecto 2"));
        equipoRepository.save(new Equipo("Proyecto 3"));

        // WHEN
        List<Equipo> equipos = equipoRepository.findAll();

        // THEN
        assertThat(equipos).hasSize(2);
    }

    @Test
    @Transactional
    public void comprobarRelacionEliminarUsuarioBD() {
        // GIVEN
        // Un usuario perteneciente a un equipo
        Usuario usuario = new Usuario("Usuario Ejemplo");
        usuarioRepository.save(usuario);

        Equipo equipo = new Equipo("Equipo A");
        equipoRepository.save(equipo);

        equipo.addUsuario(usuario);

        // WHEN
        // El usuario pertenece al grupo
        Equipo equipoBD = equipoRepository.findById(equipo.getId()).orElse(null);
        Usuario usuarioBD = usuarioRepository.findById(usuario.getId()).orElse(null);

        assertThat(equipoBD.getUsuarios()).hasSize(1);
        assertThat(equipoBD.getUsuarios()).contains(usuarioBD);
        assertThat(usuarioBD.getEquipos()).hasSize(1);
        assertThat(usuarioBD.getEquipos()).contains(equipoBD);

        // Lo eliminamos del equipo
        equipo.removeUsuario(usuario);

        // THEN
        // El usuario no se encuentra en el equipo
        equipoBD = equipoRepository.findById(equipo.getId()).orElse(null);
        usuarioBD = usuarioRepository.findById(usuario.getId()).orElse(null);

        assertThat(equipo.getUsuarios()).hasSize(0);
        assertThat(equipo.getUsuarios()).isEmpty();
        assertThat(usuario.getEquipos()).hasSize(0);
        assertThat(usuario.getEquipos()).isEmpty();
    }

    @Test
    @Transactional
    public void actualizarNombreEquipo() {
        // GIVEN
        // Un equipo nuevo guardado en la base de datos
        Equipo equipo = new Equipo("Proyecto P1");
        equipoRepository.save(equipo);

        // WHEN
        // Lo obtenemos de la base de datos y cambiamos su nombre
        Equipo equipoBD = equipoRepository.findById(equipo.getId()).orElse(null);
        equipoBD.setNombre("Proyecto MADS");
        equipoRepository.save(equipoBD);

        // THEN
        // Si lo volvemos a recuperar, el nombre ha cambiado
        equipoBD = equipoRepository.findById(equipo.getId()).orElse(null);
        assertThat(equipoBD.getNombre()).isEqualTo("Proyecto MADS");
    }
        
}
