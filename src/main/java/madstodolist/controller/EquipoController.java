package madstodolist.controller;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.validation.BindingResult;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.EquipoNotFoundException;
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

    private void comprobarUsuarioAdminYLogeado(Long idUsuario) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        Usuario admin = usuarioService.findAdmin();

        if (admin == null || idUsuarioLogeado == null || admin.getId() != idUsuarioLogeado)
            throw new UsuarioNoLogeadoException();

    }

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

    @GetMapping("/equipos/nuevo")
    public String formNuevoEquipo(Model model) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();

        if (idUsuarioLogeado == null)
            throw new UsuarioNoLogeadoException();

        EquipoData equipoData = new EquipoData();

        Usuario usuario = usuarioService.findById(idUsuarioLogeado);
        model.addAttribute("usuario", usuario);
        model.addAttribute("equipoData", equipoData);
        return "formNuevoEquipo";
    }

    @PostMapping("/equipos/nuevo")
    public String nuevoEquipo(Model model, @Valid EquipoData equipoData,
            BindingResult result) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();

        if (result.hasErrors()) {
            return "formNuevoEquipo";
        }

        if (idUsuarioLogeado == null)
            throw new UsuarioNoLogeadoException();

        equipoService.crearEquipo(equipoData.getNombre());
        return "redirect:/equipos";
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

    @DeleteMapping("/equipos/{idEquipo}/usuarios/{idUsuario}")
    @ResponseBody
    public String eliminarUsuarioEquipo(@PathVariable(value = "idEquipo") Long equipo_id,
            @PathVariable(value = "idUsuario") Long usuario_id,
            Model model, HttpSession session) {

        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();

        if (idUsuarioLogeado == null)
            throw new UsuarioNoLogeadoException();

        equipoService.removeUsuarioEquipo(usuario_id, equipo_id);
        return "";
    }

    @GetMapping("/equipos/{id}/editar")
    public String formEditarNombreEquipo(@PathVariable(value = "id") Long id_equipo,
            @ModelAttribute EquipoData equipoData, Model model) {

        comprobarUsuarioAdminYLogeado(managerUserSession.usuarioLogeado());

        Equipo equipo = equipoService.recuperarEquipo(id_equipo);

        if (equipo == null)
            throw new EquipoNotFoundException();

        Usuario admin = usuarioService.findAdmin();

        model.addAttribute("equipo", equipo);
        model.addAttribute("usuario", admin);
        equipoData.setNombre(equipo.getNombre());
        return "formEditarEquipo";
    }
}
