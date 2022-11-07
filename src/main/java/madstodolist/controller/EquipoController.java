package madstodolist.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;

@Controller
public class EquipoController {

    @Autowired
    EquipoService equipoService;

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    ManagerUserSession managerUserSession;

    @GetMapping("/equipos")
    public String listadoEquipos(Model model, HttpSession session) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();

        if (idUsuarioLogeado == null)
            throw new UsuarioNoLogeadoException();

        List<Equipo> equipos = equipoService.findAllOrderedByName();
        Usuario usuario = usuarioService.findById(idUsuarioLogeado);

        model.addAttribute("usuario", usuario);
        model.addAttribute("equipos", equipos);
        return "listaEquipos";
    }

    @GetMapping("/equipos/{id}")
    public String listadoUsuariosEquipos(@PathVariable(value = "id") Long equipo_id, Model model, HttpSession session) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();

        if (idUsuarioLogeado == null)
            throw new UsuarioNoLogeadoException();

        Usuario usuario = usuarioService.findById(idUsuarioLogeado);
        Equipo equipo = equipoService.recuperarEquipo(equipo_id);
        List<Usuario> usuarios = equipoService.usuariosEquipo(equipo_id);
        model.addAttribute("usuario", usuario);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("equipo", equipo);
        return "listaUsuariosEquipos";
    }

    @PostMapping("/equipos/{idEquipo}/usuarios/{idUsuario}")
    @ResponseBody
    public String nuevoUsuarioEquipo(@PathVariable(value = "idEquipo") Long equipo_id,
            @PathVariable(value = "idUsuario") Long usuario_id,
            Model model, HttpSession session) {

        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();

        if (idUsuarioLogeado == null)
            throw new UsuarioNoLogeadoException();

        equipoService.addUsuarioEquipo(usuario_id, equipo_id);
        return "";
    }
}
