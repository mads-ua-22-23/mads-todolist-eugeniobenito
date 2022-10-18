package madstodolist.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.model.Usuario;
import madstodolist.service.UsuarioService;

@Controller
public class UsuarioController {

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

    @GetMapping("/registrados")
    public String listadoUsuarios(Model model, HttpSession session) {
        
        comprobarUsuarioAdminYLogeado(managerUserSession.usuarioLogeado());

        model.addAttribute("usuarios", usuarioService.allUsuarios());
        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String detallesUsuario(@PathVariable(value = "id") Long idUsiario,Model model, HttpSession session) {
        
        comprobarUsuarioAdminYLogeado(managerUserSession.usuarioLogeado());

        Usuario usuario = usuarioService.findById(idUsiario);
        model.addAttribute("user", usuario);
        return "detalleUsuario";
    }
    
}
