package madstodolist;

import madstodolist.model.Usuario;
import madstodolist.service.UsuarioService;
import madstodolist.service.UsuarioServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@SpringBootTest
@Sql(scripts = "/clean-db.sql", executionPhase = AFTER_TEST_METHOD)
public class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    // Método para inicializar los datos de prueba en la BD
    // Devuelve el identificador del usuario de la BD
    Long addUsuarioBD() {
        Usuario usuario = new Usuario("user@ua");
        usuario.setNombre("Usuario Ejemplo");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        return usuario.getId();
    }

    // Método para incializar varios usuarios de prueba en BD
    public void addDosUsuariosBD() {
        Usuario usuario_1 = new Usuario("user1@ua");
        usuario_1.setPassword("123");
        Usuario usuario_2 = new Usuario("user2@ua");
        usuario_2.setPassword("123");
        usuario_1 = usuarioService.registrar(usuario_1);
        usuario_2 = usuarioService.registrar(usuario_2);        
    }

    @Test
    public void servicioLoginUsuario() {
        // GIVEN
        // Un usuario en la BD

        addUsuarioBD();

        // WHEN
        // intentamos logear un usuario y contraseña correctos
        UsuarioService.LoginStatus loginStatus1 = usuarioService.login("user@ua", "123");

        // intentamos logear un usuario correcto, con una contraseña incorrecta
        UsuarioService.LoginStatus loginStatus2 = usuarioService.login("user@ua", "000");

        // intentamos logear un usuario que no existe,
        UsuarioService.LoginStatus loginStatus3 = usuarioService.login("pepito.perez@gmail.com", "12345678");

        // THEN

        // el valor devuelto por el primer login es LOGIN_OK,
        assertThat(loginStatus1).isEqualTo(UsuarioService.LoginStatus.LOGIN_OK);

        // el valor devuelto por el segundo login es ERROR_PASSWORD,
        assertThat(loginStatus2).isEqualTo(UsuarioService.LoginStatus.ERROR_PASSWORD);

        // y el valor devuelto por el tercer login es USER_NOT_FOUND.
        assertThat(loginStatus3).isEqualTo(UsuarioService.LoginStatus.USER_NOT_FOUND);
    }

    @Test
    public void servicioRegistroUsuario() {
        // GIVEN
        // Creado un usuario nuevo, con una contraseña

        Usuario usuario = new Usuario("usuario.prueba2@gmail.com");
        usuario.setPassword("12345678");

        // WHEN
        // registramos el usuario,

        usuarioService.registrar(usuario);

        // THEN
        // el usuario se añade correctamente al sistema.

        Usuario usuarioBaseDatos = usuarioService.findByEmail("usuario.prueba2@gmail.com");
        assertThat(usuarioBaseDatos).isNotNull();
        assertThat(usuarioBaseDatos.getPassword()).isEqualTo(usuario.getPassword());
    }

    @Test
    public void servicioRegistroUsuarioExcepcionConNullPassword() {
        // GIVEN
        // Un usuario creado sin contraseña,

        Usuario usuario =  new Usuario("usuario.prueba@gmail.com");

        // WHEN, THEN
        // intentamos registrarlo, se produce una excepción de tipo UsuarioServiceException
        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(usuario);
        });
    }


    @Test
    public void servicioRegistroUsuarioExcepcionConEmailRepetido() {
        // GIVEN
        // Un usuario en la BD

        addUsuarioBD();

        // WHEN
        // Creamos un usuario con un e-mail ya existente en la base de datos,
        Usuario usuario =  new Usuario("user@ua");
        usuario.setPassword("12345678");

        // THEN
        // si lo registramos, se produce una excepción de tipo UsuarioServiceException
        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(usuario);
        });
    }

    @Test
    public void servicioRegistroUsuarioDevuelveUsuarioConId() {
        // GIVEN
        // Dado un usuario con contraseña nuevo y sin identificador,

        Usuario usuario = new Usuario("usuario.prueba@gmail.com");
        usuario.setPassword("12345678");

        // WHEN
        // lo registramos en el sistema,

        usuarioService.registrar(usuario);

        // THEN
        // se actualiza el identificador del usuario

        assertThat(usuario.getId()).isNotNull();

        // con el identificador que se ha guardado en la BD.

        Usuario usuarioBD = usuarioService.findById(usuario.getId());
        assertThat(usuarioBD).isEqualTo(usuario);
    }

    @Test
    public void servicioConsultaUsuarioDevuelveUsuario() {
        // GIVEN
        // Un usuario en la BD

        Long usuarioId = addUsuarioBD();

        // WHEN
        // recuperamos un usuario usando su e-mail,

        Usuario usuario = usuarioService.findByEmail("user@ua");

        // THEN
        // el usuario obtenido es el correcto.

        assertThat(usuario.getId()).isEqualTo(usuarioId);
        assertThat(usuario.getEmail()).isEqualTo("user@ua");
        assertThat(usuario.getNombre()).isEqualTo("Usuario Ejemplo");
    }

    @Test
    public void servicioConsultaListaUsuarios() {
        // GIVEN
        // Cargamos 2 usuarios en la base de datos
        addDosUsuariosBD();

        // WHEN
        // Recuperamos a dos todos los usuarios
        Iterable<Usuario> usuarios = usuarioService.allUsuarios();

        // THEN
        // Los usuarios obtenidos se corresponden con los registrados
        for(Usuario user : usuarios) {
            Usuario usuario = usuarioService.findByEmail(user.getEmail());
            assertThat(usuario).isEqualTo(user);
        }

        // La lista contiene exactamente los usuarios registrados
        assertThat(usuarios).hasSize(2);
    }

    @Test
    public void servicioRegistroUsuarioAdmin() {
        
        // GIVEN
        // Creado un usuario nuevo, con una contraseña y como administrador
        Usuario usuario = new Usuario("user@ua");
        usuario.setPassword("12345678");
        usuario.setIsAdmin(true);

        // WHEN
        // registramos el usuario
        usuarioService.registrar(usuario);

        // THEN
        // el usuario se añade correctamente al sistema
        Usuario usuarioByEmail = usuarioService.findByEmail("user@ua");
        Usuario usuarioByAdmin = usuarioService.findAdmin();

        assertThat(usuarioByAdmin).isNotNull();
        assertThat(usuarioByEmail.getEmail()).isEqualTo(usuarioByAdmin.getEmail());
    }

    @Test
    public void servicioRegistroUsuarioAdminExcepcionAdminYaExistente() {
        
        // GIVEN
        // Un usuario administrador creado y registrado
        Usuario usuario = new Usuario("user@ua");
        usuario.setPassword("12345678");
        usuario.setIsAdmin(true);
        usuarioService.registrar(usuario);

        // WHEN
        // Intentamos registrar otro usuario administrador
        Usuario fake_admin = new Usuario("fake_admin@ua");
        fake_admin.setPassword("12345678");
        fake_admin.setIsAdmin(true);

        // THEN
        // Si intentamos registrarlo, se produce una excepción de tipo UsuarioServiceException
        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(fake_admin);
        });
    }    

    @Test
    public void servicioSolitarAdminInexistenteDevuelveNull() {
        
        // GIVEN
        // Un usuario no administrador creado y registrado
        Usuario usuario = new Usuario("user@ua");
        usuario.setPassword("12345678");
        usuarioService.registrar(usuario);

        // WHEN, THEN      
        // Intentamos obtener al administrador recibimos null
        assertThat(usuarioService.findAdmin()).isNull();
    }    
}
