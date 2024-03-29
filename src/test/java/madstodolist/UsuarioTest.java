package madstodolist;

import madstodolist.model.Usuario;
import madstodolist.model.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@SpringBootTest
@Sql(scripts = "/clean-db.sql", executionPhase = AFTER_TEST_METHOD)
public class UsuarioTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    //
    // Tests modelo Usuario en memoria, sin la conexión con la BD
    //

    @Test
    public void crearUsuario() throws Exception {

        // GIVEN
        // Creado un nuevo usuario,
        Usuario usuario = new Usuario("juan.gutierrez@gmail.com");

        // WHEN
        // actualizamos sus propiedades usando los setters,

        usuario.setNombre("Juan Gutiérrez");
        usuario.setPassword("12345678");
        usuario.setIsAdmin(true);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        usuario.setFechaNacimiento(sdf.parse("1997-02-20"));

        // THEN
        // los valores actualizados quedan guardados en el usuario y se
        // pueden recuperar con los getters.

        assertThat(usuario.getEmail()).isEqualTo("juan.gutierrez@gmail.com");
        assertThat(usuario.getNombre()).isEqualTo("Juan Gutiérrez");
        assertThat(usuario.getPassword()).isEqualTo("12345678");
        assertThat(usuario.getFechaNacimiento()).isEqualTo(sdf.parse("1997-02-20"));
        assertTrue(usuario.getIsAdmin());
    }

    @Test
    public void comprobarIgualdadUsuariosSinId() {
        // GIVEN
        // Creados tres usuarios sin identificador, y dos de ellas con
        // el mismo e-mail

        Usuario usuario1 = new Usuario("juan.gutierrez@gmail.com");
        Usuario usuario2 = new Usuario("juan.gutierrez@gmail.com");
        Usuario usuario3 = new Usuario("ana.gutierrez@gmail.com");

        // THEN
        // son iguales (Equal) los que tienen el mismo e-mail.

        assertThat(usuario1).isEqualTo(usuario2);
        assertThat(usuario1).isNotEqualTo(usuario3);
    }

    @Test
    public void comprobarIgualdadUsuariosConId() {
        // GIVEN
        // Creadas tres usuarios con distintos e-mails y dos de ellos
        // con el mismo identificador,

        Usuario usuario1 = new Usuario("juan.gutierrez@gmail.com");
        Usuario usuario2 = new Usuario("pedro.gutierrez@gmail.com");
        Usuario usuario3 = new Usuario("ana.gutierrez@gmail.com");

        usuario1.setId(1L);
        usuario2.setId(2L);
        usuario3.setId(1L);

        // THEN
        // son iguales (Equal) los usuarios que tienen el mismo identificador.

        assertThat(usuario1).isEqualTo(usuario3);
        assertThat(usuario1).isNotEqualTo(usuario2);
    }

    //
    // Tests UsuarioRepository.
    // El código que trabaja con repositorios debe
    // estar en un entorno transactional, para que todas las peticiones
    // estén en la misma conexión a la base de datos, las entidades estén
    // conectadas y sea posible acceder a colecciones LAZY.
    //

    @Test
    @Transactional
    public void crearUsuarioBaseDatos() throws ParseException {
        // GIVEN
        // Un usuario nuevo creado sin identificador

        Usuario usuario = new Usuario("juan.gutierrez@gmail.com");
        usuario.setNombre("Juan Gutiérrez");
        usuario.setPassword("12345678");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        usuario.setFechaNacimiento(sdf.parse("1997-02-20"));

        // WHEN
        // se guarda en la base de datos

        usuarioRepository.save(usuario);

        // THEN
        // se actualiza el identificador del usuario,

        assertThat(usuario.getId()).isNotNull();

        // y con ese identificador se recupera de la base de datos el usuario con
        // los valores correctos de las propiedades.

        Usuario usuarioBD = usuarioRepository.findById(usuario.getId()).orElse(null);
        assertThat(usuarioBD.getEmail()).isEqualTo("juan.gutierrez@gmail.com");
        assertThat(usuarioBD.getNombre()).isEqualTo("Juan Gutiérrez");
        assertThat(usuarioBD.getPassword()).isEqualTo("12345678");
        assertThat(usuarioBD.getFechaNacimiento()).isEqualTo(sdf.parse("1997-02-20"));
    }

    @Test
    @Transactional
    public void buscarUsuarioEnBaseDatos() {
        // GIVEN
        // Un usuario en la BD
        Usuario usuario = new Usuario("user@ua");
        usuario.setNombre("Usuario Ejemplo");
        usuarioRepository.save(usuario);
        Long usuarioId = usuario.getId();

        // WHEN
        // se recupera de la base de datos un usuario por su identificador,

        Usuario usuarioBD = usuarioRepository.findById(usuarioId).orElse(null);

        // THEN
        // se obtiene el usuario correcto y se recuperan sus propiedades.

        assertThat(usuarioBD).isNotNull();
        assertThat(usuarioBD.getId()).isEqualTo(usuarioId);
        assertThat(usuarioBD.getNombre()).isEqualTo("Usuario Ejemplo");
    }

    @Test
    @Transactional
    public void buscarUsuarioPorEmail() {
        // GIVEN
        // Un usuario en la BD
        Usuario usuario = new Usuario("user@ua");
        usuario.setNombre("Usuario Ejemplo");
        usuarioRepository.save(usuario);
        Long usuarioId = usuario.getId();

        // WHEN
        // buscamos al usuario por su correo electrónico,

        Usuario usuarioBD = usuarioRepository.findByEmail("user@ua").orElse(null);

        // THEN
        // se obtiene el usuario correcto.

        assertThat(usuarioBD.getNombre()).isEqualTo("Usuario Ejemplo");
    }

    @Test
    @Transactional
    public void listaUsuarios() {
        // GIVEN
        // Se registran 2 usuarios en la base de datos
        Usuario usuario_1 = new Usuario("user1@ua");
        Usuario usuario_2 = new Usuario("user2@ua");
        usuarioRepository.save(usuario_1);
        usuarioRepository.save(usuario_2);

        // WHEN
        // Recuperamos la lista con todos los usuarios
        Iterable<Usuario> usuarios = usuarioRepository.findAll();

        // THEN
        // Se recupera una lista con dos elementos
        assertThat(usuarios).hasSize(2);

        // Si registramos un usuario más, la lista aumenta
        Usuario usuario_3 = new Usuario("user3@ua");
        usuarioRepository.save(usuario_3);
        usuarios = usuarioRepository.findAll();
        assertThat(usuarios).hasSize(3);

        // Si eliminamos a todos los usuarios, la lista
        // estará vacía
        usuarioRepository.deleteAll();
        usuarios = usuarioRepository.findAll();
        assertThat(usuarios).hasSize(0);
    }

    @Test
    @Transactional
    public void crearUsuarioAdministradorBaseDatos() {

        // GIVEN
        // Un usuario administrador creado
        Usuario usuario = new Usuario("user@ua");
        usuario.setNombre("Usuario Ejemplo");
        usuario.setIsAdmin(true);

        // WHEN
        // se guarda en la base de datos
        usuarioRepository.save(usuario);

        // THEN
        // Se obtiene una lista con el usuario administrador creado
        Iterable<Usuario> admin = usuarioRepository.findByIsAdminTrue();
        List<Usuario> result = new ArrayList<Usuario>();
        admin.forEach(result::add);

        // Comprobamos que el usuario administrador se ha recuperado correctamente
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(usuario);
        assertThat(result.get(0).getEmail()).isEqualTo("user@ua");
        assertThat(result.get(0).getNombre()).isEqualTo("Usuario Ejemplo");
        assertTrue(result.get(0).getIsAdmin());
    }
}
