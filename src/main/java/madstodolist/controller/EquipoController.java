package madstodolist.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
}
